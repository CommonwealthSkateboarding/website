package models.skatepark;

import com.avaje.ebean.annotation.EnumValue;
import models.security.User;
import utils.VisitCounter;
import org.apache.commons.lang3.time.DateUtils;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by cdelargy on 11/23/14.
 */
@Entity
public class Visit extends Model {

    public static final int SESSION_PASS_LENGTH_IN_HOURS = 2;

    public enum VisitType {
        @EnumValue("PROMO")
        PROMO,
        @EnumValue("SESSION")
        SESSION,
        @EnumValue("ALL_DAY")
        ALL_DAY,
        @EnumValue("UNLIMITED")
        UNLIMITED
    }

    @Id
    public Long id;

    @ManyToOne
    public Membership membership;

    @ManyToOne
    public User verifiedBy;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date time;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date expires;

    @OneToOne
    public Visit previousVisit;

    public VisitType visitType;

    public boolean refunded;

    public static final Finder<Long, Visit> find = new Finder<Long, Visit>(
            Long.class, Visit.class);

    /**
     * Adds new visit for a member
     */
    public static Visit addVisit(final Membership membership, final User verifiedBy, VisitType type) {

        final Visit ret = new Visit();

        ret.membership = membership;
        ret.time = new Date();
        ret.previousVisit = membership.lastVisit;
        ret.verifiedBy = verifiedBy;
        ret.visitType = type;
        if (type == VisitType.SESSION) {
            ret.expires = DateUtils.addHours(new Date(), SESSION_PASS_LENGTH_IN_HOURS);
        }

        ret.save();

        VisitCounter.newVisit();

        return ret;
    }

    public boolean isValidNow() {
        if (this.refunded) {
            return false;
        } else if (this.visitType == VisitType.SESSION && this.expires.after(new Date())) {
            return true;
        } else if (this.visitType != VisitType.SESSION && DateUtils.isSameDay(new Date(), this.time)) {
            return true;
        }
        return false;
    }

    public static int countSince2015() {
        return find.where().gt("time", "2015-01-01").findRowCount();
    }
}
