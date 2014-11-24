package models.skatepark;

import models.security.User;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by cdelargy on 11/23/14.
 */
@Entity
public class Visit extends Model {

    @Id
    public Long id;

    @ManyToOne
    public Membership membership;

    @ManyToOne
    public User verifiedBy;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date time;

    public static final Finder<Long, Visit> find = new Finder<Long, Visit>(
            Long.class, Visit.class);

    /**
     * Adds new visit for a member
     */
    public void addVisit(final Membership membership, final User verifiedBy) {

        final Visit ret = new Visit();

        ret.membership = membership;
        ret.time = new Date();
        ret.verifiedBy = verifiedBy;

        ret.save();
    }

}
