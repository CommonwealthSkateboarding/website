package models.site;

import models.security.User;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

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
}
