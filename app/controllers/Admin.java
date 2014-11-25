package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
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
        } catch (ParseException e) {
            return internalServerError(); //todo better handling?
        }
        return redirect(routes.Admin.viewMemberPage(id));
    }

    public static Result viewMemberPage(Long id) {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(id);
        if (null == member) {
            return redirect(routes.Admin.memberIndex(0)); // not found
        }
        return ok(viewMember.render(member, getLocalUser(session())));
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

        return redirect(routes.Admin.memberIndex(0));
    }

    public static Result addMember() {
        Membership membership = Form.form(Membership.class).bindFromRequest().get();
        membership.createDate = new Date();
        membership.save();

        return redirect(routes.Admin.memberIndex(0));
    }

    public static Result memberSessionVisit(Long memberId)
    {
        Membership member = (Membership) new Model.Finder(Long.class, Membership.class).byId(memberId);
        if (null == member) {
            return notFound("Bad member id");
        };
        if (member.sessionPasses == 0) {
            return badRequest("Member doesn't have enough session passes");
        }

        Visit.addVisit(member, getLocalUser(session()), false);
        member.sessionPasses = (member.sessionPasses - 1);
        member.lastVisited = new Date();
        member.save();

        return redirect(routes.Admin.viewMemberPage(memberId));
    }

    public static Result memberPassVisit(Long memberId, Long unlimitedPassId)
    {
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

        Visit.addVisit(member, getLocalUser(session()), true);
        member.lastVisited = new Date();
        member.save();

        return redirect(routes.Admin.viewMemberPage(memberId));
    }
    /**
     * Stub implementation for future emailin'
     */
    public static void sendEmail(String recipient, String sender, String subject, String message){
        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
        mail.setSubject(subject);
        mail.setRecipient(recipient);
        mail.setFrom(sender);
        String body = views.html.email.simple.render(message).body();
        mail.sendHtml(body);
    }

}
