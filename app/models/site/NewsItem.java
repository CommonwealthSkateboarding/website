package models.site;

import com.avaje.ebean.Expr;
import net.sf.ehcache.Ehcache;
import play.Play;
import play.api.cache.EhCachePlugin;
import play.cache.*;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import javax.persistence.Cache;
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
        clearCachedPages();
        super.save();
    }

    @Override
    public void update() {
        clearCachedPages();
        super.update();
    }

    private void clearCachedPages() {
        List keys = Play.application().plugin(EhCachePlugin.class).cache().getKeys();
        for(Object key : keys)
        {
            if(key.toString().contains(NEWS_PAGE_CACHE_PREFIX)) {
                play.cache.Cache.remove(key.toString());
            }
        }
    }

    public static List<NewsItem> getCachedPagedNews(boolean frontPage, Long page, int perPage) {
        String cacheKey = NEWS_PAGE_CACHE_PREFIX + (frontPage?"FP":"BL") + perPage + "pp" + page;
        List<NewsItem> news = (List<NewsItem>) play.cache.Cache.get(cacheKey);
        if (null == news) {
            Date now = new Date();
            if (frontPage) {
                news = NewsItem.find
                        .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now)).where().eq("frontPage", true)
                        .setMaxRows(perPage + 1).setFirstRow(page.intValue() * perPage).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
            } else {
                news = NewsItem.find
                        .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now)).setMaxRows(perPage + 1)
                        .setFirstRow(page.intValue() * perPage).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
            }
            play.cache.Cache.set(cacheKey, news);
        }
        return news;
    }
}
