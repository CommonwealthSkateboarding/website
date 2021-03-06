package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.stripe.model.Charge;
import models.security.AuditRecord;
import models.security.SecurityRole;
import models.security.User;
import models.site.ClosureNotice;
import models.site.Issue;
import models.site.NewsItem;
import models.skatepark.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.time.DateUtils;
import play.Logger;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.PdfUtil;
import utils.TimeUtil;
import views.html.admin.camp.*;
import views.html.admin.closure.closureIndex;
import views.html.admin.event.*;
import views.html.admin.index;
import views.html.admin.issues.issueIndex;
import views.html.admin.logIndex;
import views.html.admin.membership.*;
import views.html.admin.news.addNews;
import views.html.admin.news.editNews;
import views.html.admin.news.newsIndex;
import views.html.admin.register.bitcoinSale;
import views.html.admin.userIndex;
import views.html.email.inlineCampReminderEmail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Restrict({@Group("ADMIN")})
@Security.Authenticated(Secured.class)
public class Admin extends Controller {

    public static final int PER_PAGE = 25;
    public static final int MAX_RECENT_AUDIT_DISPLAY = 50;
    public static final String RECENT_VISIT_ORDER = "lastVisit.time DESC, name";
    public static final String RECENT_EVENT_ORDER = "timestamp DESC";
    public static final String USER_LAST_LOGIN_ORDER = "lastLogin DESC";

    public static User getLocalUser(final Http.Session session) {
        final User localUser = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session));
        return localUser;
    }

    public static Result dashboard() {
        Date lastTwoWeeks = DateUtils.addDays(new Date(), -14);
        lastTwoWeeks = DateUtils.ceiling(lastTwoWeeks, Calendar.DATE);
        List<Visit> visits = Visit.find.where().eq("refunded", false).where().gt("time", lastTwoWeeks).findList();

        Date thisMorning = DateUtils.truncate(new Date(), Calendar.DATE);
        Date nextWeek = DateUtils.ceiling(DateUtils.addDays(new Date(), 7), Calendar.DATE);

        List<Event> events = Event.find.where().gt("startTime", thisMorning).where().lt("endTime", nextWeek)
                .where().eq("archived", false).orderBy("startTime").findList();

        return ok(index.render(visits, events, getLocalUser(session())));
    }

    @Restrict({@Group("BLOG")})
    public static Result newsIndex(Long page) {
        boolean hasNextPage = false;
        List<NewsItem> news = new Model.Finder(String.class, NewsItem.class)
                .setMaxRows(PER_PAGE + 1).setFirstRow(page.intValue() * PER_PAGE).
                        orderBy(NewsItem.STICKY_REVERSE_DATE_ORDER).findList();

        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            news.remove(PER_PAGE);
            hasNextPage = true;
        }
        return ok(newsIndex.render(news, page, hasNextPage, getLocalUser(session())));
    }

    @Restrict({@Group("BLOG")})
    public static Result addNewsPage() {
        return ok(addNews.render(getLocalUser(session())));
    }

    @Restrict({@Group("BLOG")})
    public static Result editNewsPage(String id) {
        NewsItem news = (NewsItem) new Model.Finder(String.class, NewsItem.class).byId(id);
        if (null == news) {
            return redirect(routes.Admin.newsIndex(0)); // not found
        }
        return ok(editNews.render(news, getLocalUser(session())));
    }

    @Restrict({@Group("BLOG")})
    public static Result addNewsItem() {
        NewsItem newsItem = Form.form(NewsItem.class).bindFromRequest().get();

        String titleDigest = newsItem.title.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ", "_");
        String proposedId = titleDigest.substring(0,(titleDigest.length()>64)?64:titleDigest.length());
        NewsItem news = (NewsItem) new Model.Finder(String.class, NewsItem.class).byId(proposedId);
        //If one already exists, append a 4 digit number
        if (null != news) {
            proposedId = titleDigest.substring(0,(titleDigest.length()>60)?60:titleDigest.length()) +
                    "_" + org.apache.commons.lang3.RandomStringUtils.randomNumeric(4);
        }

        newsItem.createDate = new Date();
        newsItem.id = proposedId;
        newsItem.save();

        audit("Added a news item '" + newsItem.title + "'", null, newsItem);
        return redirect(routes.Admin.newsIndex(0));
    }

    @Restrict({@Group("BLOG")})
    public static Result updateNewsItem(String id) {
        NewsItem news = (NewsItem) new Model.Finder(String.class, NewsItem.class).byId(id);
        if (null == news) {
            return notFound("Bad post id");
        };

        NewsItem newNews = Form.form(NewsItem.class).bindFromRequest().get();

        news.content = newNews.content;
        news.expireDate = newNews.expireDate;
        news.expires = newNews.expires;
        news.extendedContent = newNews.extendedContent;
        news.title = newNews.title;
        news.sticky = newNews.sticky;
        news.frontPage = newNews.frontPage;

        news.save();

        audit("Updated a news item '" + news.title + "'", null, news);

        return redirect(routes.Admin.newsIndex(0));
    }

    public static Result memberIndex(Long page) {
        boolean hasNextPage = false;
        List<Membership> list = Membership.find.where().eq("duplicate", false)
                .orderBy(RECENT_VISIT_ORDER).setMaxRows(PER_PAGE + 1)
                .setFirstRow(page.intValue() * PER_PAGE).findList();
        if (list.size() == (PER_PAGE + 1)) { // if there is another page after
            list.remove(PER_PAGE);
            hasNextPage = true;
        }

        return ok(memberIndex.render(list, page, hasNextPage, getLocalUser(session())));
    }

    public static Result negativeBalancePage() {
        List<Membership> list = Membership.find.orderBy("credit ASC").where().lt("credit", 0).findList();

        return ok(negativeBalance.render(list, getLocalUser(session())));
    }

    public static Result unlimitedPassHoldersPage() {
        Date now = new Date();
        Date threeMonthsAgo = DateUtils.addMonths(new Date(), -3);
        threeMonthsAgo = DateUtils.ceiling(threeMonthsAgo, Calendar.DATE);
        List<UnlimitedPass> currentList = UnlimitedPass.find.where().gt("expires", now)
                .where().lt("starts", now).orderBy("expires").findList();

        List<UnlimitedPass> expiredList = UnlimitedPass.find.where().gt("expires", threeMonthsAgo)
                .where().lt("expires", now).orderBy("expires").findList();

        Map<Membership, UnlimitedPass> current = new HashMap<>();
        Map<Membership, UnlimitedPass> expired = new HashMap<>();

        for (UnlimitedPass pass : currentList) {
            // Override only with later expiry membership
            if (current.containsKey(pass.membership) && current.get(pass.membership).expires.before(pass.expires)) {
                current.put(pass.membership, pass);
            } else if (!current.containsKey(pass.membership)) {
                current.put(pass.membership, pass);
            }
        }
        for (UnlimitedPass pass : expiredList) {
            if (!expired.containsKey(pass.membership) && !current.containsKey(pass.membership)) {
                expired.put(pass.membership, pass);
            } else if (expired.containsKey(pass.membership) && expired.get(pass.membership).expires.before(pass.expires)) {
                expired.put(pass.membership, pass);
            }
        }

        return ok(unlimitedPassHolders.render(current, expired, getLocalUser(session())));
    }

    public static Result addMemberPage(String name) {
        Membership member = new Membership();
        member.name = name;
        return ok(addMember.render(member, false, null, getLocalUser(session())));
    }

    public static Result findMember() {
        Long page = new Long(0);
        String searchName = (String) Form.form().bindFromRequest().get().getData().get("name");
        List<Membership> results = Membership.find.where().eq("duplicate", false)
                .like("name", "%" + searchName + "%").orderBy(RECENT_VISIT_ORDER).findList();
        return ok(memberIndex.render(results, page, false, getLocalUser(session())));
    }

    public static Result addUnlimitedPass(Long id) {
        String expectedPattern = "MM/dd/yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);
        try {
            Date startDate = formatter.parse((String) Form.form().bindFromRequest().data().get("startDate"));
            int duration = Integer.parseInt((String) Form.form().bindFromRequest().data().get("length"));
            Membership member = Membership.find.byId(id);
            member.lastActive = new Date();
            UnlimitedPass pass = UnlimitedPass.addNewUnlimitedPass(member, getLocalUser(session()), startDate, duration);
            audit("Added an unlimited pass for " + pass.membership.name, pass.membership, pass);
        } catch (ParseException e) {
            return internalServerError(); //todo better handling?
        }
        return redirect(routes.Admin.viewMemberPage(id));
    }

    public static Result editUnlimitedPass(Long id) {
        Long memberId;
        String expectedPattern = "MM/dd/yyyy";
        SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);
        try {
            Date startDate = formatter.parse((String) Form.form().bindFromRequest().data().get("startDate"));
            Date expireDate = formatter.parse((String) Form.form().bindFromRequest().data().get("expireDate"));
            UnlimitedPass pass = UnlimitedPass.find.byId(id);
            pass.starts = startDate;
            pass.expires = expireDate;
            pass.save();
            memberId = pass.membership.id; //for return redirect
            audit("Edited an unlimited pass for " + pass.membership.name, pass.membership, pass);

        } catch (ParseException e) {
            return internalServerError(); //todo better handling?
        }
        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result removeUnlimitedPass(Long id) {
        UnlimitedPass pass = UnlimitedPass.find.byId(id);
        pass.delete();
        audit("Deleted an unlimited pass for " + pass.membership.name, pass.membership, pass);
        return redirect(routes.Admin.viewMemberPage(pass.membership.id));
    }

    public static Result addPromoPass(Long id, int passes, String reason) {
        Membership member = Membership.find.byId(id);
        member.promoPasses = (member.promoPasses + passes);
        member.lastActive = new Date();
        member.save();
        audit("Added " + passes + " promotional " + ((passes == 1)?"pass":"passes") + " for " + member.name + " (" + reason + ")", member, null);
        return redirect(routes.Admin.viewMemberPage(id));
    }

    public static Result subtractPromoPass(Long id) {
        Membership member = Membership.find.byId(id);
        if (member.promoPasses < 1) {
            return unauthorized("Member does not have an available promo pass");
        } else {
            member.promoPasses = (member.promoPasses - 1);
            member.save();
            audit("Deducted a promo pass from " + member.name, member, null);
            return redirect(routes.Admin.viewMemberPage(id));
        }
    }

    public static Result addSessionPass(Long id, int passes) {
        Membership member = Membership.find.byId(id);
        member.sessionPasses = (member.sessionPasses + passes);
        member.lastActive = new Date();
        member.save();
        audit("Added " + passes + " session " + ((passes == 1)?"pass":"passes") + " for " + member.name, member, null);
        return redirect(routes.Admin.viewMemberPage(id));
    }

    public static Result subtractSessionPass(Long id) {
        Membership member = Membership.find.byId(id);
        if (member.sessionPasses < 1) {
            return unauthorized("Member does not have an available session pass");
        } else {
            member.sessionPasses = (member.sessionPasses - 1);
            member.save();
            audit("Deducted a session pass from " + member.name, member, null);
            return redirect(routes.Admin.viewMemberPage(id));
        }
    }

    public static Result addAllDayPass(Long id) {
        Membership member = Membership.find.byId(id);
        member.allDayPasses = (member.allDayPasses + 1);
        member.lastActive = new Date();
        member.save();
        audit("Added an all day pass for " + member.name, member, null);
        return redirect(routes.Admin.viewMemberPage(id));
    }

    public static Result subtractAllDayPass(Long id) {
        Membership member = Membership.find.byId(id);
        if (member.allDayPasses < 1) {
            return unauthorized("Member does not have an available all day pass");
        } else {
            member.allDayPasses = (member.allDayPasses - 1);
            member.save();
            audit("Deducted an all day pass from " + member.name, member, null);
            return redirect(routes.Admin.viewMemberPage(id));
        }
    }

    public static Result viewMemberPage(Long id) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(id);
        if (null == member) {
            return redirect(routes.Admin.memberIndex(0)); // not found
        }

        Date lastMonth = DateUtils.addMonths(new Date(), -1);
        lastMonth = DateUtils.ceiling(lastMonth, Calendar.DATE);
        List<AuditRecord> logs = AuditRecord.find.where().eq("membership_id", id).where().orderBy("timestamp DESC")
                .setMaxRows(MAX_RECENT_AUDIT_DISPLAY).findList();
        return ok(viewMember.render(member, logs, getLocalUser(session())));
    }

    public static Result editMemberPage(Long id) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(id);
        if (null == member) {
            return redirect(routes.Admin.memberIndex(0)); // not found
        }
        return ok(editMember.render(member, getLocalUser(session())));
    }

    public static Result updateMembership(Long id) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(id);
        if (null == member) {
            return notFound("Bad member id");
        };

        Membership newMember = Form.form(Membership.class).bindFromRequest().get();

        member.address = newMember.address;
        member.birthDate = newMember.birthDate;
        member.city = newMember.city;
        member.country = newMember.country;
        member.email = newMember.email;
        member.emergencyContactName = newMember.emergencyContactName;
        member.emergencyContactNumber = newMember.emergencyContactNumber;
        member.emergencyContactNameB = newMember.emergencyContactNameB;
        member.emergencyContactNumberB = newMember.emergencyContactNumberB;
        member.emergencyContactNameC = newMember.emergencyContactNameC;
        member.emergencyContactNumberC = newMember.emergencyContactNumberC;
        member.name = newMember.name;
        member.notes = newMember.notes;
        member.parentName = newMember.parentName;
        member.state = newMember.state;
        member.telephone = newMember.telephone;
        member.zipcode = newMember.zipcode;

        member.save();

        audit("Updated " + member.name + " in the membership database", member, null);

        return redirect(routes.Admin.viewMemberPage(member.id));
    }
    public static Result markDuplicate(Long id, Boolean state) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(id);
        if (null == member) {
            return notFound("Bad member id");
        };

        member.duplicate = state;

        member.save();

        audit((state?"Marked ":"Unmarked ") + member.name + " as a duplicate in the membership database", member, null);

        return redirect(routes.Admin.viewMemberPage(member.id));
    }

    public static Result addMember(boolean ignoreDuplicate) {
        Membership membership = Form.form(Membership.class).bindFromRequest().get();
        membership.createDate = new Date();
        membership.lastActive = new Date();

        //remove trailing and extra whitespace, add appropriate capitalization
        membership.name = WordUtils.capitalize(StringUtils.stripToEmpty(membership.name.replaceAll("\\s+", " ")));

        List<Membership> existingMembersWithName = Membership.find.where().eq("duplicate", false)
                .like("name", "%" + membership.name + "%").orderBy(RECENT_VISIT_ORDER).findList();

        if (ignoreDuplicate || existingMembersWithName.isEmpty()) {
            membership.save();

            audit("Added " + membership.name + " to the membership database", membership, null);

            return redirect(routes.Admin.viewMemberPage(membership.id));
        } else {
            // display warning re: duplicate name case
            Logger.info("Intercepted duplicate member name '" + membership.name +"', requesting confirmation to add");
            return ok(addMember.render(membership, true, existingMembersWithName, getLocalUser(session())));
        }

    }

    public static Result sessionVisit(Long memberId, boolean soldOnSpot) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (!soldOnSpot && member.sessionPasses == 0) {
            return badRequest("Member doesn't have enough session passes");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), Visit.VisitType.SESSION);
        if (!soldOnSpot) {
            member.sessionPasses = (member.sessionPasses - 1);
        }
        member.lastVisit = visit;
        member.lastActive = new Date();
        member.save();

        audit("Checked in " + member.name + " with a" + (soldOnSpot?"":" saved") + " session pass", member, visit);

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result promoVisit(Long memberId) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (member.promoPasses == 0) {
            return badRequest("Member doesn't have enough promo passes");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), Visit.VisitType.PROMO);
        member.promoPasses = (member.promoPasses - 1);
        member.lastVisit = visit;
        member.lastActive = new Date();
        member.save();

        audit("Checked in " + member.name + " with a promo pass", member, visit);

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result allDayVisit(Long memberId, boolean soldOnSpot) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (!soldOnSpot && member.allDayPasses == 0) {
            return badRequest("Member doesn't have enough all day passes");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), Visit.VisitType.ALL_DAY);
        if (!soldOnSpot) {
            member.allDayPasses = (member.allDayPasses - 1);
        }
        member.lastVisit = visit;
        member.lastActive = new Date();
        member.save();

        audit("Checked in " + member.name + " with a" + (soldOnSpot?"n":" saved") + " all day pass", member, visit);

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result memberPassVisit(Long memberId, Long unlimitedPassId) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        UnlimitedPass pass = (UnlimitedPass) new Model.Finder(Long.class, UnlimitedPass.class).byId(unlimitedPassId);
        if (null == pass) {
            return notFound("Bad pass id");
        };
        if (!pass.isValid()) {
            return badRequest("This pass is not valid for use");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), Visit.VisitType.UNLIMITED);
        member.lastVisit = visit;
        member.lastActive = new Date();
        member.save();

        audit("Checked in " + member.name + " with an unlimited pass", member, visit);

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result undoVisit(Long id) {
        Visit visit = Visit.find.byId(id);
        if (null == visit) {
            return notFound("Bad visit id");
        };
        if (visit.visitType == Visit.VisitType.SESSION) {
            visit.membership.sessionPasses = (visit.membership.sessionPasses + 1);
            audit("Undid a session pass visit from " + visit.membership.name + " and refunded a session pass", visit.membership, null);
        } else if (visit.visitType == Visit.VisitType.PROMO) {
            visit.membership.promoPasses = (visit.membership.promoPasses + 1);
            audit("Undid a promotional pass visit from " + visit.membership.name + " and refunded a promotional pass", visit.membership, null);
        } else if (visit.visitType == Visit.VisitType.ALL_DAY) {
            visit.membership.allDayPasses = (visit.membership.allDayPasses + 1);
            audit("Undid an all day pass visit from " + visit.membership.name + " and refunded an all day pass", visit.membership, null);
        } else if (visit.visitType == Visit.VisitType.UNLIMITED) {
            audit("Undid an unlimited pass visit from " + visit.membership.name, visit.membership, null);
        }
        if (null != visit.membership.lastVisit && visit.membership.lastVisit.equals(visit)) {
            // Reset the last visited date in the membership if applicable
            visit.membership.lastVisit = visit.previousVisit;
        }
        visit.refunded = true;
        visit.save();
        visit.membership.save();

        return redirect(routes.Admin.viewMemberPage(visit.membership.id));
    }

    public static Result extendSessionVisit(Long id) {
        Visit visit = Visit.find.byId(id);
        if (null == visit) {
            return notFound("Bad visit id");
        };
        if (visit.visitType != Visit.VisitType.SESSION) {
            return internalServerError("Can't extend a non-session visit");
        } else {
            visit.expires = null;
            visit.visitType = Visit.VisitType.ALL_DAY;
            audit("Extended a visit from session to all day for " + visit.membership.name, visit.membership, visit);
            visit.save();
        }

        return redirect(routes.Admin.viewMemberPage(visit.membership.id));
    }

    public static Result addCredit(Long memberId) {
        Double amount = Double.parseDouble((String) Form.form().bindFromRequest().data().get("amount"));
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        member.deposit(amount);
        audit("Added " + utils.Formatter.prettyDollarsAndCents(amount) + " credit to " + member.name + "'s membership", member, null);
        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result subtractCredit(Long memberId) {
        Double amount = Double.parseDouble((String) Form.form().bindFromRequest().data().get("amount"));
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        member.spend(amount);
        audit("Subtracted " + utils.Formatter.prettyDollarsAndCents(amount) + " credit from " + member.name + "'s membership", member, null);
        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    //TODO: Extract to own interface?
    public static void audit(String description, Membership membership, Object payload) {
        AuditRecord log = new AuditRecord();
        log.delta = description;
        try {
            log.user = getLocalUser(session());
        } catch (RuntimeException e) {
            // catches case where no user HTTP context exists when audit fires
        }
        log.timestamp = new Date();
        log.membership = membership;
        if (null == payload) {
            // chill
        } else if (payload.getClass().equals(NewsItem.class)) {
            log.newsItem = (NewsItem) payload;
        } else if (payload.getClass().equals(UnlimitedPass.class)) {
            log.unlimitedPass = (UnlimitedPass) payload;
        } else if (payload.getClass().equals(Visit.class)) {
            log.visit = (Visit) payload;
        } else if (payload.getClass().equals(Camp.class)) {
            log.camp = (Camp) payload;
        } else if (payload.getClass().equals(Event.class)) {
            log.event = (Event) payload;
        } else if (payload.getClass().equals(ClosureNotice.class)) {
            log.closure = (ClosureNotice) payload;
        }
        //TODO: Consider adding link for admin issues
        log.save();
        Slack.emitAuditLog(log);
    }

    public static Result logIndex(Long page) {
        boolean hasNextPage = false;
        List<AuditRecord> list = AuditRecord.find.orderBy(RECENT_EVENT_ORDER).setMaxRows(PER_PAGE+1)
                .setFirstRow(page.intValue() * PER_PAGE).findList();
        if (list.size() == (PER_PAGE + 1)) { // if there is another page after
            list.remove(PER_PAGE);
            hasNextPage = true;
        }

        return ok(logIndex.render(list, page, hasNextPage, getLocalUser(session())));
    }

    @Restrict({@Group("USER_ADMIN")})
    public static Result userIndex() {
        List<User> users = User.find.orderBy(USER_LAST_LOGIN_ORDER).findList();
        List<SecurityRole> roles = SecurityRole.find.findList();
        return ok(userIndex.render(users, roles, getLocalUser(session())));
    }

    @Restrict({@Group("USER_ADMIN")})
    public static Result setUserRole(Long userId, Long roleId, boolean state) {
        User user = User.find.byId(userId);
        SecurityRole role = SecurityRole.find.byId(roleId);
        if (state) {
            user.roles.add(role);
            audit("Added role " + role.roleName + " for " + user.name, null, null);
        } else {
            user.roles.remove(role);
            audit("Removed role " + role.roleName + " for " + user.name, null, null);
        }
        user.save();

        return redirect(routes.Admin.userIndex());
    }

    @Restrict({@Group("CAMP")})
    public static Result campIndex() {
        List<Camp> camps = Camp.find.where().eq("archived", false).orderBy("startDate").findList();
        return ok(campIndex.render(camps, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result viewCampPage(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Admin.campIndex()); // not found
        }
        List<AuditRecord> logs = AuditRecord.find.where().eq("camp_id", id).orderBy("timestamp DESC").findList();
        return ok(viewCamp.render(camp, logs, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result viewCampPDF(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Admin.campIndex()); // not found
        }
        return ok(PdfUtil.getCampPDF(camp).toByteArray()).as("application/pdf");
    }

    @Restrict({@Group("CAMP")})
    public static Result addCampPage() {
        return ok(addCamp.render(getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result addCamp() {
        Camp camp = Form.form(Camp.class).bindFromRequest().get();
        String titleDigest = camp.title.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ", "_");
        String proposedId = titleDigest.substring(0,(titleDigest.length()>56)?56:titleDigest.length()) +
                "_" + TimeUtil.getMonthYearString(camp.startDate);
        Camp camp2 = (Camp) new Model.Finder(String.class, Camp.class).byId(proposedId);
        //If one already exists, append a 4 digit number
        if (null != camp2) {
            proposedId = titleDigest.substring(0,(titleDigest.length()>52)?52:titleDigest.length()) + "_" +
                TimeUtil.getMonthYearString(camp.startDate) + org.apache.commons.lang3.RandomStringUtils.randomNumeric(4);
        }
        camp.id = proposedId;
        camp.createDate = new Date();
        camp.save();

        audit("Added " + camp.title + " to the camp database", null, camp);

        return redirect(routes.Admin.viewCampPage(camp.id));
    }

    @Restrict({@Group("CAMP")})
    public static Result editCampPage(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Admin.campIndex()); // not found
        }
        return ok(editCamp.render(camp, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result updateCamp(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return notFound("Bad camp id");
        };

        Camp newCamp = Form.form(Camp.class).bindFromRequest().get();

        camp.cost = newCamp.cost;
        camp.description = newCamp.description;
        camp.endDate = newCamp.endDate;
        camp.maxRegistrations = newCamp.maxRegistrations;
        camp.registrationEndDate = newCamp.registrationEndDate;
        camp.earlyRegistrationEndDate = newCamp.earlyRegistrationEndDate;
        camp.earlyRegistrationDiscount = newCamp.earlyRegistrationDiscount;
        camp.scheduleDescription = newCamp.scheduleDescription;
        camp.startDate = newCamp.startDate;
        camp.title = newCamp.title;
        camp.instructors = newCamp.instructors;
        camp.privateNotes = newCamp.privateNotes;

        camp.save();

        audit("Updated " + camp.title + " in the camp database", null, camp);

        return redirect(routes.Admin.viewCampPage(camp.id));
    }

    @Restrict({@Group("CAMP")})
    public static Result campRegistrationPage(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return notFound("Bad camp id");
        }
        ;
        return ok(campRegistration.render(camp, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result addCampRegistration(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return notFound("Bad camp id");
        };

        Registration registration = Form.form(Registration.class).bindFromRequest().get();

        registration.timestamp = new Date();
        registration.camp = camp;
        registration.registrationType = Registration.RegistrationType.CAMP;
        registration.confirmationId = org.apache.commons.lang3.RandomStringUtils.random(6, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");
        if(registration.totalPaid == null) {
            registration.totalPaid = 0.00;
        }

        registration.save();

        audit("Added registration for " + registration.participantName + " to " + camp.title, null, camp);

        return redirect(routes.Admin.viewCampPage(id));
    }

    @Restrict({@Group("CAMP")})
    public static Result editCampRegistrationPage(Long id) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        if (null == reg) {
            return notFound("Bad registration id");
        };
        return ok(editCampRegistration.render(reg, getLocalUser(session())));

    }

    @Restrict({@Group("CAMP")})
    public static Result editCampRegistration(Long id) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        if (null == reg) {
            return notFound("Bad registration id");
        };

        Registration newReg = Form.form(Registration.class).bindFromRequest().get();

        reg.notes = newReg.notes;
        reg.paid = newReg.paid;
        reg.participantName = newReg.participantName;
        reg.emergencyContactName = newReg.emergencyContactName;
        reg.emergencyTelephone = newReg.emergencyTelephone;
        reg.alternateEmergencyContactName = newReg.alternateEmergencyContactName;
        reg.alternateEmergencyTelephone = newReg.alternateEmergencyTelephone;
        reg.paymentType = newReg.paymentType;
        reg.registrantEmail = newReg.registrantEmail;
        reg.totalPaid = newReg.totalPaid;
        reg.save();

        audit("Edited registration for " + reg.participantName + " to " + reg.camp.title, null, reg.camp);

        return redirect(routes.Admin.viewCampPage(reg.camp.id));
    }

    @Restrict({@Group("CAMP")})
    public static Result moveCampRegistrationPage(Long id) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        List<Camp> camps = Camp.find.where().eq("archived", false).orderBy("startDate").findList();
        if (null == reg) {
            return notFound("Bad registration id");
        };
        return ok(moveCampRegistration.render(reg, camps, getLocalUser(session())));

    }

    @Restrict({@Group("CAMP")})
    public static Result moveCampRegistration(Long regId, String campId) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(regId);
        if (null == reg) {
            return notFound("Bad registration id");
        };

        Camp newCamp = (Camp) new Model.Finder(Long.class, Camp.class).byId(campId);
        if (null == newCamp) {
            return notFound("Bad camp id");
        };

        reg.camp = newCamp;
        reg.save();

        audit("Moved registration for " + reg.participantName + " to " + reg.camp.title, null, reg.camp);

        return redirect(routes.Admin.viewCampPage(reg.camp.id));
    }

    @Restrict({@Group("CAMP")})
    public static Result removeCampRegistration(Long id, Boolean confirm) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        if (null == reg) {
            return notFound("Bad registration id");
        }

        if (!confirm) {
            return ok(removeCampRegistration.render(reg, getLocalUser(session())));
        } else {
            reg.delete();
            audit("Deleted registration of " + reg.participantName + " for " + reg.camp.title, null, reg.camp);
            return redirect(routes.Admin.viewCampPage(reg.camp.id));
        }
    }

    @Restrict({@Group("CAMP")})
    public static Result sendCampReminderEmail(Long id, Boolean confirm) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        if (null == reg) {
            return notFound("Bad registration id");
        };

        if (!confirm) {
            return ok(inlineCampReminderEmail.render(reg));
        } else {
            Email.sendCampReminderEmail(reg.registrantEmail, reg);

            audit("Sent camp reminder to " + reg.registrantEmail + " re: " + reg.camp.title, null, reg.camp);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
            Date now = new Date();
            reg.notes = reg.notes + "<br>At " + dateFormat.format(now) + " sent reminder of camp to " + reg.registrantEmail;
            reg.update();

            return redirect(routes.Admin.viewCampPage(reg.camp.id));
        }
    }


    @Restrict({@Group("CAMP")})
    public static Result archiveCamp(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Admin.campIndex()); // not found
        }
        camp.archived = true;
        camp.save();

        audit("Archived camp " + camp.title, null, camp);

        return redirect(routes.Admin.campIndex());
    }

    @Restrict({@Group("EVENTS")})
    public static Result eventIndex() {
        Date now = new Date();
        List<Event> upcomingEvents = Event.find.orderBy("startTime").where().gt("endTime", now).where().eq("archived", false).findList();
        List<Event> pastEvents = Event.find.orderBy("startTime DESC").where().lt("endTime", now).where().eq("archived", false).findList();
        return ok(eventIndex.render(upcomingEvents, pastEvents, getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result addEventPage() {
        return ok(addEvent.render(getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result addEvent() {
        Event event = Form.form(Event.class).bindFromRequest().get();

        String titleDigest = event.name.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ", "_");
        String proposedId = titleDigest.substring(0,(titleDigest.length()>56)?56:titleDigest.length()) +
                "_" + TimeUtil.getMonthYearString(event.startTime);
        Event event2 = (Event) new Model.Finder(String.class, Event.class).byId(proposedId);
        //If one already exists, append a 4 digit number
        if (null != event2) {
            proposedId = titleDigest.substring(0,(titleDigest.length()>51)?51:titleDigest.length()) + "_" +
                TimeUtil.getMonthYearString(event.startTime) + org.apache.commons.lang3.RandomStringUtils.randomNumeric(4);
        }
        event.id = proposedId;
        event.save();

        audit("Added " + event.name + " to the event database", null, event);
        return redirect(routes.Admin.viewEventPage(event.id));
    }

    @Restrict({@Group("EVENTS")})
    public static Result viewEventPage(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return redirect(routes.Admin.eventIndex()); // not found
        }
        List<AuditRecord> logs = AuditRecord.find.where().eq("event_id", id).orderBy("timestamp DESC").findList();
        return ok(viewEvent.render(event, logs, getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result editEventPage(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return redirect(routes.Admin.eventIndex()); // not found
        }
        return ok(editEvent.render(event, getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result editEvent(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return notFound("Bad event id");
        }
        Event newEvent = Form.form(Event.class).bindFromRequest().get();

        event.archived = newEvent.archived;
        event.endTime = newEvent.endTime;
        event.name = newEvent.name;
        event.notes = newEvent.notes;
        event.publicVisibility = newEvent.publicVisibility;
        event.reservePark = newEvent.reservePark;
        event.startTime = newEvent.startTime;
        event.privateNotes = newEvent.privateNotes;
        event.cost = newEvent.cost;
        event.maxRegistrations = newEvent.maxRegistrations;
        event.registrable = newEvent.registrable;
        event.registrationEndDate = newEvent.registrationEndDate;
        event.save();

        audit("Edited event " + event.name, null, event);

        return redirect(routes.Admin.viewEventPage(event.id));
    }

    @Restrict({@Group("EVENTS")})
    public static Result archiveEvent(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return redirect(routes.Admin.eventIndex()); // not found
        }
        event.archived = true;
        event.save();

        audit("Archived event " + event.name, null, event);

        return redirect(routes.Admin.eventIndex());
    }

    @Restrict({@Group("EVENTS")})
    public static Result eventRegistrationPage(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return notFound("Bad event id");
        }
        ;
        return ok(eventRegistration.render(event, getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result addEventRegistration(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return notFound("Bad event id");
        };

        Registration registration = Form.form(Registration.class).bindFromRequest().get();

        registration.timestamp = new Date();
        registration.event = event;
        registration.registrationType = Registration.RegistrationType.EVENT;
        registration.confirmationId = org.apache.commons.lang3.RandomStringUtils.random(6, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");
        if(registration.totalPaid == null) {
            registration.totalPaid = 0.00;
        }

        registration.save();

        audit("Added registration for " + registration.participantName + " to " + event.name, null, event);

        return redirect(routes.Admin.viewEventPage(id));
    }

    @Restrict({@Group("EVENTS")})
    public static Result editEventRegistrationPage(Long id) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        if (null == reg) {
            return notFound("Bad registration id");
        };
        return ok(editEventRegistration.render(reg, getLocalUser(session())));

    }

    @Restrict({@Group("EVENTS")})
    public static Result editEventRegistration(Long id) {
        Registration reg = (Registration) new Model.Finder(Long.class, Registration.class).byId(id);
        if (null == reg) {
            return notFound("Bad registration id");
        };

        Registration newReg = Form.form(Registration.class).bindFromRequest().get();

        reg.notes = newReg.notes;
        reg.paid = newReg.paid;
        reg.participantName = newReg.participantName;
        reg.paymentType = newReg.paymentType;
        reg.registrantEmail = newReg.registrantEmail;
        reg.totalPaid = newReg.totalPaid;
        reg.save();

        audit("Edited registration for " + reg.participantName + " to " + reg.event.name, null, reg.event);

        return redirect(routes.Admin.viewEventPage(reg.event.id));
    }

    public static Result issueIndex() {
        Date now = new Date();
        List<Issue> issues = Issue.find.orderBy("created").where().isNull("resolved").findList();
        return ok(issueIndex.render(issues, getLocalUser(session())));
    }

    public static Result addIssue() {
        Issue issue = Form.form(Issue.class).bindFromRequest().get();
        issue.created = new Date();
        issue.createdBy = getLocalUser(session());
        issue.save();

        audit("Added new open issue: " + issue.title, null, issue);

        Slack.notifyOfNewIssue(issue);

        return redirect(routes.Admin.issueIndex());
    }

    public static Result resolveIssue(Long id) {
        Issue issue = Issue.find.byId(id);
        if (null == issue) {
            return redirect(routes.Admin.issueIndex()); // not found
        }
        issue.resolved = new Date();
        issue.owner = getLocalUser(session());
        issue.save();

        Slack.notifyOfClosedIssue(issue);
        audit("Resolved issue " + issue.title, null, issue);

        return redirect(routes.Admin.issueIndex());
    }

    public static Result takeIssue(Long id) {
        Issue issue = Issue.find.byId(id);
        if (null == issue) {
            return redirect(routes.Admin.issueIndex()); // not found
        }
        issue.owner = getLocalUser(session());
        issue.save();

        audit("Took ownership of issue " + issue.title, null, issue);

        return redirect(routes.Admin.issueIndex());
    }

    public static Result bitcoinSaleIndex() {
        List<BitcoinSale> sales = BitcoinSale.find.orderBy("created DESC").setMaxRows(PER_PAGE).findList();
        return ok(bitcoinSale.render(sales, getLocalUser(session())));
    }

    public static Result addBitcoinSale() {
        BitcoinSale sale = Form.form(BitcoinSale.class).bindFromRequest().get();
        sale.created = new Date();
        sale.soldBy = getLocalUser(session());
        sale.save();
        try {
            Charge charge = Stripe.chargeStripe(sale.amount, sale.stripeToken, "Shop sale: " + sale.description);
            audit("Processed bitcoin sale for " + sale.description, null, null);
            Slack.emitBitcoinPayment(charge);
            return redirect(routes.Admin.bitcoinSaleIndex());
        } catch (Exception e) {
            Logger.error("Stripe error", e);
            return internalServerError(e.toString());
        }

    }

    public static Result closureIndex() {
        Date now = new Date();
        List<ClosureNotice> closures = ClosureNotice.find.orderBy("created").where().eq("archived", false).findList();
        return ok(closureIndex.render(closures, getLocalUser(session())));
    }

    public static Result addClosure() {
        ClosureNotice closure = Form.form(ClosureNotice.class).bindFromRequest().get();
        closure.created = new Date();
        closure.createdBy = getLocalUser(session());
        closure.save();

        audit("Added new closure notice: " + closure.message, null, closure);

        return redirect(routes.Admin.closureIndex());
    }

    public static Result archiveClosure(Long id) {
        ClosureNotice closure = ClosureNotice.find.byId(id);
        if (null == closure) {
            return redirect(routes.Admin.closureIndex()); // not found
        }
        closure.enabled = false;
        closure.archived = true;
        closure.save();

        audit("Archived closure \"" + closure.message + "\"", null, closure);

        return redirect(routes.Admin.closureIndex());
    }

    public static Result toggleClosure(Long id, boolean state) {
        ClosureNotice closure = ClosureNotice.find.byId(id);
        if (null == closure) {
            return redirect(routes.Admin.closureIndex()); // not found
        }
        closure.enabled = state;
        closure.save();

        audit((closure.enabled?"Enabled":"Disabled") + " closure \"" + closure.message + "\"" , null, closure);

        return redirect(routes.Admin.closureIndex());
    }

    public static void runSlackClosureReport() {
        List<ClosureNotice> closures = ClosureNotice.find.where().eq("enabled", true).findList();
        if (null != closures && closures.size() > 0) {
            Logger.info("Currently " + closures.size() + " active closures, emitting to slack");
            Slack.emitClosuresReport(closures);
        }
    }

}