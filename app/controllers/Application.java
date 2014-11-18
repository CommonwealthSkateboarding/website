package controllers;

import com.avaje.ebean.Expr;
import models.NewsItem;
import play.db.ebean.Model;
import play.mvc.*;

import views.html.*;

import java.util.Date;
import java.util.List;

public class Application extends Controller {

    public static final int PER_PAGE = 5;
    public static final String STICKY_REVERSE_DATE_ORDER = "sticky DESC, createDate DESC";
    public static final String FLASH_MESSAGE_KEY = "message";
    public static final String FLASH_ERROR_KEY = "error";

    public static Result oAuthDenied(final String providerKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        flash(FLASH_ERROR_KEY, "You need to accept the OAuth connection in order to use this website!");
        return redirect(routes.Application.index(0));
    }

    public static Result login() {
        return ok(login.render());
    }

    public static Result index(Long page) {
        boolean hasNextPage = false;
        List<NewsItem> news = getNewsItems(page);
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            news.remove(PER_PAGE);
            hasNextPage = true;
        }
        return ok(index.render(news, page, hasNextPage));
    }

    private static List<NewsItem> getNewsItems(Long page) {
        Date now = new Date();
        return new Model.Finder(Long.class, NewsItem.class)
                .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now))
                .setMaxRows(PER_PAGE+1).setFirstRow(page.intValue()*PER_PAGE).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
    }

    public static Result showNews(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
        Date now = new Date();
        if (null == news || (news.expires && news.expireDate.before(now))) {
            return redirect(routes.Application.index(0)); // not found, deleted, expired
        }
        return ok(newsItem.render(news));
    }

    public static Result shop(){
        return ok(shop.render());
    }
    public static Result camp(){
        return ok(camp.render());
    }
    public static Result events(){
        return ok(events.render());
    }
    public static Result about(){
        return ok(about.render());
    }
}
