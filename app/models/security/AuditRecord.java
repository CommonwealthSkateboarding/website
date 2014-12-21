package models.security;

import models.site.NewsItem;
import models.skatepark.*;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by cdelargy on 11/28/14.
 */
@Entity
public class AuditRecord extends Model {

    @Id
    public Long id;

    @ManyToOne
    public User user;

    @ManyToOne
    public Membership membership;

    @ManyToOne
    public NewsItem newsItem;

    @ManyToOne
    public UnlimitedPass unlimitedPass;

    @ManyToOne
    public Visit visit;

    @ManyToOne
    public Camp camp;

    @ManyToOne
    public Event event;

    public Date timestamp;

    public String delta;

    public static final Finder<Long, AuditRecord> find = new Finder<Long, AuditRecord>(
            Long.class, AuditRecord.class);
}
