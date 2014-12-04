package controllers;

import com.avaje.ebean.Expr;
import models.site.NewsItem;
import models.skatepark.Camp;
import org.apache.commons.lang3.time.DateUtils;
import play.db.ebean.Model;
import play.mvc.*;

import views.html.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Application extends Controller {

    public static final int PER_PAGE = 5;
    public static final String STICKY_REVERSE_DATE_ORDER = "sticky DESC, createDate DESC";
    public static final String USER_ROLE = "USER";

    public static Result oAuthDenied(final String providerKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        return redirect(routes.Application.login());
    }

    public static Result login() {
        return redirect(com.feth.play.module.pa.controllers.routes.Authenticate.authenticate("google"));
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
        Date tomorrow = new Date();
        tomorrow = DateUtils.ceiling(tomorrow, Calendar.DATE);
        List<Camp> camps = Camp.find.where().gt("registrationEndDate", tomorrow).findList();
        return ok(camp.render(camps));
    }

    public static Result campDetail(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Application.camp()); // not found
        }
        return ok(campDetail.render(camp));
    }
    public static Result events(){
        return ok(events.render());
    }
    public static Result about(){
        return ok(about.render());
    }
}
