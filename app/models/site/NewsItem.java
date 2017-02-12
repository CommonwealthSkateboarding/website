package models.site;

import com.avaje.ebean.Expr;
import controllers.Application;
import play.data.format.Formats;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by cdelargy on 10/16/14.
 */
@Entity
public class NewsItem extends Model {

    public static final String STICKY_REVERSE_DATE_ORDER = "sticky DESC, createDate DESC";
    public static final String NEWS_PAGE_CACHE_PREFIX = "cachedNewsPageEntry";

    @Id
    public String id;

    public boolean expires;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date expireDate;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date createDate;

    public String title;

    @Column(columnDefinition = "text")
    public String content;

    @Column(columnDefinition = "text")
    public String extendedContent;

    public boolean sticky;

    public boolean frontPage;

    public static final Finder<String, NewsItem> find = new Finder<>(String.class, NewsItem.class);

    @Override
    public void save() {
        play.cache.Cache.set("news"+id, this);
        super.save();
    }

    @Override
    public void update() {
        play.cache.Cache.set("news"+id, this);
        super.update();
    }

    public static List<NewsItem> getCachedFrontPageNews() {
        List<NewsItem> news = (List<NewsItem>) play.cache.Cache.get("frontPageNews");
        if (null == news) {
            news = getNewsItems(true, 0, Application.PER_PAGE);
            play.cache.Cache.set("frontPageNews", news);
        }
        return news;
    }

    public static List<NewsItem> getNewsItems(boolean frontPage, int page, int perPage) {
            Date now = new Date();
            List<NewsItem> news;
            if (frontPage) {
                news = NewsItem.find
                        .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now)).where().eq("frontPage", true)
                        .setMaxRows(perPage + 1).setFirstRow(page * perPage).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
            } else {
                news = NewsItem.find
                        .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now)).setMaxRows(perPage + 1)
                        .setFirstRow(page * perPage).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
            }
            return news;
        }

    public static NewsItem getCachedNews(String id) {
        NewsItem news = (NewsItem) play.cache.Cache.get("news"+id);
        if (null == news) {
            news = find.byId(id);
            play.cache.Cache.set("news"+id, news);
        }
        return news;
    }
}
