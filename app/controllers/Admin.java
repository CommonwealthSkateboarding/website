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
import views.html.admin.news.addNews;
import views.html.admin.news.editNews;
import views.html.admin.news.newsIndex;
import views.html.admin.membership.memberIndex;

import java.util.Date;
import java.util.List;

@Restrict({@Group("ADMIN")})
@Security.Authenticated(Secured.class)
public class Admin extends Controller {

    public static final int PER_PAGE = 25;

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
        List<Membership> list = new Model.Finder(Long.class, Membership.class).setMaxRows(PER_PAGE+1)
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

    public static Result findMemberPage() {
        //TODO impl
        return ok(addMember.render(getLocalUser(session())));
    }


    public static Result editMemberPage(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
        if (null == news) {
            return redirect(routes.Admin.newsIndex(0)); // not found
        }
        return ok(editNews.render(news, getLocalUser(session())));
    }

    public static Result addMember() {
        Membership membership = Form.form(Membership.class).bindFromRequest().get();
        membership.createDate = new Date();
        membership.save();

        return redirect(routes.Admin.memberIndex(0));
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

    public static Result test1(){
        Membership m = new Membership();
        m.name = "Bob Shmob";
        m.save();

        Visit v = new Visit();
        v.addVisit(m, getLocalUser(session()));

        UnlimitedPass u = new UnlimitedPass();
        u.addNewUnlimitedPass(m, getLocalUser(session()), new Date(), 3);

        return ok(m.id + " " + v.id + " " + u.id);

    }

}
