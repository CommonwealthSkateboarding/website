package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.feth.play.module.pa.PlayAuthenticate;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import models.NewsItem;
import models.User;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.admin.addNews;
import views.html.admin.editNews;
import views.html.admin.adminIndex;

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

    public static Result adminIndex(Long page) {
        boolean hasNextPage = false;
        List<NewsItem> news = getNewsItems(page);
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            news.remove(PER_PAGE);
            hasNextPage = true;
        }
        return ok(adminIndex.render(news, page, hasNextPage, getLocalUser(session())));
    }

    private static List<NewsItem> getNewsItems(Long page) {
        Date now = new Date();
        return new Model.Finder(Long.class, NewsItem.class)
                .setMaxRows(PER_PAGE).setFirstRow(page.intValue() * PER_PAGE).orderBy(Application.STICKY_REVERSE_DATE_ORDER).findList();
    }

    public static Result addNewsPage() {
        return ok(addNews.render(getLocalUser(session())));
    }

    public static Result editNewsPage(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
        if (null == news) {
            return redirect(routes.Admin.adminIndex(0)); // not found
        }
        return ok(editNews.render(news, getLocalUser(session())));
    }

    public static Result addNewsItem() {
        NewsItem newsItem = Form.form(NewsItem.class).bindFromRequest().get();
        newsItem.createDate = new Date();
        newsItem.save();

        return redirect(routes.Admin.adminIndex(0));
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

        return redirect(routes.Admin.adminIndex(0));
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
