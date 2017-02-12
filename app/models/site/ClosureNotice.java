package models.site;

import models.security.User;
import play.cache.Cache;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by cdelargy on 5/24/15.
 */
@Entity
@Table(name = "closure")
public class ClosureNotice extends Model {

    public static String CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME = "currentActiveClosures";

    @Id
    public Long id;

    public Date created;

    public String message;

    @ManyToOne
    public User createdBy;

    public boolean enabled;

    public boolean archived;

    public static final Finder<Long, ClosureNotice> find = new Finder<>(Long.class, ClosureNotice.class);

    @Override
    public void save() {
        play.cache.Cache.remove(ClosureNotice.CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME);
        super.save();
    }

    @Override
    public void update() {
        play.cache.Cache.remove(ClosureNotice.CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME);
        super.update();
    }

    public static List<ClosureNotice> getCachedActiveClosures() {
        List<ClosureNotice> closures = (List<ClosureNotice>) Cache.get(ClosureNotice.CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME);
        if (null == closures) {
            closures = ClosureNotice.find.where().eq("enabled", true).findList();
            Cache.set(ClosureNotice.CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME, closures);
        }
        return closures;
    }
}
