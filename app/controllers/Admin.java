package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import models.security.AuditRecord;
import models.security.SecurityRole;
import models.security.User;
import models.site.NewsItem;
import models.skatepark.*;
import models.square.Order;
import org.apache.commons.lang3.time.DateUtils;
import play.data.Form;
import play.db.ebean.Model;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.camp.*;
import views.html.admin.event.addEvent;
import views.html.admin.event.editEvent;
import views.html.admin.event.eventIndex;
import views.html.admin.event.viewEvent;
import views.html.admin.index;
import views.html.admin.logIndex;
import views.html.admin.membership.*;
import views.html.admin.news.addNews;
import views.html.admin.news.editNews;
import views.html.admin.news.newsIndex;
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
        List<Order> orders = Square.getOrders();
        for(Order order : orders) {
            System.out.println(order);
        }
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
        Date now = new Date();
        List<NewsItem> news = new Model.Finder(Long.class, NewsItem.class)
                .setMaxRows(PER_PAGE+1).setFirstRow(page.intValue() * PER_PAGE).
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
    public static Result editNewsPage(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
        if (null == news) {
            return redirect(routes.Admin.newsIndex(0)); // not found
        }
        return ok(editNews.render(news, getLocalUser(session())));
    }

    @Restrict({@Group("BLOG")})
    public static Result addNewsItem() {
        NewsItem newsItem = Form.form(NewsItem.class).bindFromRequest().get();
        newsItem.createDate = new Date();
        newsItem.save();

        audit("Added a news item '" + newsItem.title + "'", null, newsItem);
        return redirect(routes.Admin.newsIndex(0));
    }

    @Restrict({@Group("BLOG")})
    public static Result updateNewsItem(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
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

    public static Result addMemberPage() {
        return ok(addMember.render(getLocalUser(session())));
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

    public static Result sessionVisit(Long memberId) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (member.sessionPasses == 0) {
            return badRequest("Member doesn't have enough session passes");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), Visit.VisitType.SESSION);
        member.sessionPasses = (member.sessionPasses - 1);
        member.lastVisit = visit;
        member.save();

        audit("Checked in " + member.name + " with a session pass", member, visit);

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result allDayVisit(Long memberId) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (member.allDayPasses == 0) {
            return badRequest("Member doesn't have enough all day passes");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), Visit.VisitType.ALL_DAY);
        member.allDayPasses = (member.allDayPasses - 1);
        member.lastVisit = visit;
        member.save();

        audit("Checked in " + member.name + " with an all day pass", member, visit);

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
        audit("Added " + utils.Formatter.prettyDollars(amount) + " credit to " + member.name + "'s membership", member, null);
        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result subtractCredit(Long memberId) {
        Double amount = Double.parseDouble((String) Form.form().bindFromRequest().data().get("amount"));
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        member.spend(amount);
        audit("Subtracted " + utils.Formatter.prettyDollars(amount) + " credit from " + member.name + "'s membership", member, null);
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
        List<Camp> camps = Camp.find.findList();
        return ok(campIndex.render(camps, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result viewCampPage(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
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
        camp.createDate = new Date();
        camp.save();

        audit("Added " + camp.title + " to the camp database", null, camp);

        return redirect(routes.Admin.viewCampPage(camp.id));
    }

    @Restrict({@Group("CAMP")})
    public static Result editCampPage(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Admin.campIndex()); // not found
        }
        return ok(editCamp.render(camp, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result updateCamp(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
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

        camp.save();

        audit("Updated " + camp.title + " in the camp database", null, camp);

        return redirect(routes.Admin.viewCampPage(camp.id));
    }

    @Restrict({@Group("CAMP")})
    public static Result campRegistrationPage(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
        if (null == camp) {
            return notFound("Bad camp id");
        };
        return ok(campRegistration.render(camp, getLocalUser(session())));
    }

    @Restrict({@Group("CAMP")})
    public static Result addCampRegistration(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
        if (null == camp) {
            return notFound("Bad camp id");
        };

        Registration registration = Form.form(Registration.class).bindFromRequest().get();

        registration.timestamp = new Date();
        registration.camp = camp;

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
        reg.save();

        audit("Edited registration for " + reg.participantName + " to " + reg.camp.title, null, reg.camp);

        return redirect(routes.Admin.viewCampPage(reg.camp.id));
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
        event.save();

        audit("Added " + event.name + " to the event database", null, event);
        return redirect(routes.Admin.viewEventPage(event.id));
    }

    @Restrict({@Group("EVENTS")})
    public static Result viewEventPage(Long id) {
        Event event = Event.find.byId(id);
        if (null == event) {
            return redirect(routes.Admin.eventIndex()); // not found
        }
        return ok(viewEvent.render(event, getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result editEventPage(Long id) {
        Event event = Event.find.byId(id);
        if (null == event) {
            return redirect(routes.Admin.eventIndex()); // not found
        }
        return ok(editEvent.render(event, getLocalUser(session())));
    }

    @Restrict({@Group("EVENTS")})
    public static Result editEvent(Long id) {
        Event event = Event.find.byId(id);
        if (null == event) {
            return notFound("Bad event id");
        };

        Event newEvent = Form.form(Event.class).bindFromRequest().get();

        event.archived = newEvent.archived;
        event.endTime = newEvent.endTime;
        event.name = newEvent.name;
        event.notes = newEvent.notes;
        event.publicVisibility = newEvent.publicVisibility;
        event.reservePark = newEvent.reservePark;
        event.startTime = newEvent.startTime;
        event.privateNotes = newEvent.privateNotes;
        event.save();

        audit("Edited event " + event.name, null, event);

        return redirect(routes.Admin.viewEventPage(event.id));
    }

    @Restrict({@Group("EVENTS")})
    public static Result archiveEvent(Long id) {
        Event event = Event.find.byId(id);
        if (null == event) {
            return redirect(routes.Admin.eventIndex()); // not found
        }
        event.archived = true;
        event.save();

        audit("Archived event " + event.name, null, event);

        return redirect(routes.Admin.eventIndex());
    }

}
