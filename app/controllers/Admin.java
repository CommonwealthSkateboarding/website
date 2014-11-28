package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import models.security.AuditRecord;
import models.security.User;
import models.site.NewsItem;
import models.skatepark.Membership;
import models.skatepark.UnlimitedPass;
import models.skatepark.Visit;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.index;
import views.html.admin.logIndex;
import views.html.admin.membership.addMember;
import views.html.admin.membership.editMember;
import views.html.admin.membership.viewMember;
import views.html.admin.news.addNews;
import views.html.admin.news.editNews;
import views.html.admin.news.newsIndex;
import views.html.admin.membership.memberIndex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Restrict({@Group("ADMIN")})
@Security.Authenticated(Secured.class)
public class Admin extends Controller {

    public static final int PER_PAGE = 25;
    public static final String RECENT_VISIT_ORDER = "lastVisited DESC";
    public static final String RECENT_EVENT_ORDER = "timestamp DESC";

    public static User getLocalUser(final Http.Session session) {
        final User localUser = User.findByAuthUserIdentity(PlayAuthenticate.getUser(session));
        return localUser;
    }

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

    public static Result addNewsPage() {
        return ok(addNews.render(getLocalUser(session())));
    }

    public static Result editNewsPage(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
        if (null == news) {
            return redirect(routes.Admin.newsIndex(0)); // not found
        }
        return ok(editNews.render(news, getLocalUser(session())));
    }

    public static Result addNewsItem() {
        NewsItem newsItem = Form.form(NewsItem.class).bindFromRequest().get();
        newsItem.createDate = new Date();
        newsItem.save();

        audit("Added a news item '" + newsItem.title + "'", null, newsItem);
        return redirect(routes.Admin.newsIndex(0));
    }

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

        news.save();

        audit("Updated a news item '" + news.title + "'", null, news);

        return redirect(routes.Admin.newsIndex(0));
    }

    public static Result memberIndex(Long page) {
        boolean hasNextPage = false;
        List<Membership> list = new Model.Finder(Long.class, Membership.class).orderBy(RECENT_VISIT_ORDER).setMaxRows(PER_PAGE+1)
                .setFirstRow(page.intValue() * PER_PAGE).findList();
        if (list.size() == (PER_PAGE + 1)) { // if there is another page after
            list.remove(PER_PAGE);
            hasNextPage = true;
        }

        return ok(memberIndex.render(list, page, hasNextPage, getLocalUser(session())));
    }

    public static Result index() {
        return ok(index.render(getLocalUser(session())));
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

    public static Result addSessionPass(Long id) {
        Membership member = Membership.find.byId(id);
        member.sessionPasses = (member.sessionPasses + 1);
        member.save();
        audit("Added a session pass for " + member.name, member, null);
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

    public static Result viewMemberPage(Long id) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(id);
        if (null == member) {
            return redirect(routes.Admin.memberIndex(0)); // not found
        }
        List<AuditRecord> logs = AuditRecord.find.where().eq("membership_id", id).orderBy("timestamp DESC").findList();
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

    public static Result memberSessionVisit(Long memberId) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (member.sessionPasses == 0) {
            return badRequest("Member doesn't have enough session passes");
        }

        Visit visit = Visit.addVisit(member, getLocalUser(session()), false);
        member.sessionPasses = (member.sessionPasses - 1);
        member.lastVisited = visit.time;
        member.save();

        audit("Checked in " + member.name + " with a session pass", member, visit);

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

        Visit visit = Visit.addVisit(member, getLocalUser(session()), true);
        member.lastVisited = visit.time;
        member.save();

        audit("Checked in " + member.name + " with an unlimited pass", member, visit);

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result undoVisit(Long id) {
        Visit visit = Visit.find.byId(id);
        if (null == visit) {
            return notFound("Bad visit id");
        };
        if (!visit.unlimitedPassVisit) {
            visit.membership.sessionPasses = (visit.membership.sessionPasses + 1);
            audit("Undid a session pass visit from " + visit.membership.name + " and refunded a session pass", visit.membership, null);
        } else {
            audit("Undid an unlimited pass visit from " + visit.membership.name, visit.membership, null);
        }
        if (visit.membership.lastVisited.equals(visit.time)) {
            // Reset the last visited date in the membership if applicable
            visit.membership.lastVisited = visit.previousVisitDate;
        }
        visit.membership.save();

        //visit.delete();

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
    /**
     * Stub implementation for future emailin'
     */
    public static void sendEmail(String recipient, String sender, String subject, String message) {
        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
        mail.setSubject(subject);
        mail.setRecipient(recipient);
        mail.setFrom(sender);
        String body = views.html.email.simple.render(message).body();
        mail.sendHtml(body);
    }

    private static void audit(String description, Membership membership, Object payload) {
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
        }
        log.save();
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

}
