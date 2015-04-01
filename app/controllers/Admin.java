package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.stripe.model.Charge;
import models.security.AuditRecord;
import models.security.SecurityRole;
import models.security.User;
import models.site.Issue;
import models.site.NewsItem;
import models.skatepark.*;
import org.apache.commons.lang3.time.DateUtils;
import play.Logger;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.TimeUtil;
import views.html.admin.camp.*;
import views.html.admin.event.*;
import views.html.admin.index;
import views.html.admin.issues.issueIndex;
import views.html.admin.logIndex;
import views.html.admin.membership.*;
import views.html.admin.news.addNews;
import views.html.admin.news.editNews;
import views.html.admin.news.newsIndex;
import views.html.admin.register.unheardSaleIndex;
import views.html.admin.register.bitcoinSale;
import views.html.admin.userIndex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Restrict({@Group("ADMIN")})
@Security.Authenticated(Secured.class)
public class Admin extends Controller {

    public static final int PER_PAGE = 25;
    public static final String RECENT_VISIT_ORDER = "lastVisit.time DESC";
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
                        orderBy(Application.STICKY_REVERSE_DATE_ORDER).findList();

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

        String titleDigest = newsItem.title.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ","_");
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
        List<Membership> list = Membership.find.orderBy(RECENT_VISIT_ORDER).setMaxRows(PER_PAGE+1)
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
        return ok(addMember.render(name, getLocalUser(session())));
    }

    public static Result findMember() {
        Long page = new Long(0);
        String searchName = (String) Form.form().bindFromRequest().get().getData().get("name");
        List<Membership> results = Membership.find.where()
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

    public static Result addSessionPass(Long id, int passes) {
        Membership member = Membership.find.byId(id);
        member.sessionPasses = (member.sessionPasses + passes);
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
        List<AuditRecord> logs = AuditRecord.find.where().eq("membership_id", id).where().gt("timestamp", lastMonth)
                .orderBy("timestamp DESC").findList();
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

    public static Result addMember() {
        Membership membership = Form.form(Membership.class).bindFromRequest().get();
        membership.createDate = new Date();
        membership.save();

        audit("Added " + membership.name + " to the membership database", membership, null);

        return redirect(routes.Admin.viewMemberPage(membership.id));
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
        member.save();

        audit("Checked in " + member.name + " with a" + (soldOnSpot?"":" saved") + " session pass", member, visit);

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

    public static void audit(String description, Membership membership, Object payload) {
        AuditRecord log = new AuditRecord();
        log.delta = description;
        log.user = getLocalUser(session());
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
        return ok(viewCamp.render(camp, getLocalUser(session())));
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
        camp.scheduleDescription = newCamp.scheduleDescription;
        camp.startDate = newCamp.startDate;
        camp.title = newCamp.title;
        camp.instructors = newCamp.instructors;

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
        reg.paymentType = newReg.paymentType;
        reg.registrantEmail = newReg.registrantEmail;
        reg.totalPaid = newReg.totalPaid;
        reg.save();

        audit("Edited registration for " + reg.participantName + " to " + reg.camp.title, null, reg.camp);

        return redirect(routes.Admin.viewCampPage(reg.camp.id));
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

        String titleDigest = event.name.toLowerCase().replaceAll("[^A-Za-z0-9 ]", "").replaceAll(" ","_");
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
        return ok(viewEvent.render(event, getLocalUser(session())));
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

    public static Result unheardSaleIndex() {
        boolean showAll = Boolean.parseBoolean(request().getQueryString("showAll"));

        List<UnheardSale> sales;
        if (showAll) {
            sales = UnheardSale.find.orderBy("created").where().findList();
        } else {
            sales = UnheardSale.find.orderBy("created").where().eq("invoiced", false).findList();
        }
        return ok(unheardSaleIndex.render(sales, getLocalUser(session())));
    }

    public static Result addUnheardSale() {
        UnheardSale sale = Form.form(UnheardSale.class).bindFromRequest().get();
        sale.created = new Date();
        sale.soldBy = getLocalUser(session());
        sale.save();

        audit("Added new unheard sale: " + sale.brand + " " + sale.description, null, sale);

        return redirect(routes.Admin.unheardSaleIndex());
    }

    public static Result deleteUnheardSale(Long id) {
        UnheardSale sale = UnheardSale.find.byId(id);
        if (null == sale) {
            return redirect(routes.Admin.unheardSaleIndex()); // not found
        }
        sale.delete();

        audit("Deleted unheard sale (id: " + sale.id + ")", null, null);

        return redirect(routes.Admin.unheardSaleIndex());
    }

    public static Result invoiceUnheardSale(Long id) {
        UnheardSale sale = UnheardSale.find.byId(id);
        if (null == sale) {
            return redirect(routes.Admin.unheardSaleIndex()); // not found
        }
        sale.invoiced = true;
        sale.save();

        audit("Invoiced unheard sale (id: " + sale.id + ")", null, null);

        return redirect(routes.Admin.unheardSaleIndex());
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
}