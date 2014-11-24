package models.skatepark;

import models.security.User;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by cdelargy on 11/23/14.
 */
@Entity
public class UnlimitedPass extends Model {

    @Id
    public Long id;

    @ManyToOne
    public Membership membership;

    @ManyToOne
    public User addedBy;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date created;

    public int uses;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date lastUsed;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date starts;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date expires;

    public static final Model.Finder<Long, UnlimitedPass> find = new Model.Finder<Long, UnlimitedPass>(
            Long.class, UnlimitedPass.class);

    /**
     * Adds new unlimited pass to a membership
     */
    public static UnlimitedPass addNewUnlimitedPass(final Membership membership, final User addedBy, Date startDate,
        int durationInMonths) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.MONTH, durationInMonths);
        Date endDate = cal.getTime();

        final UnlimitedPass ret = new UnlimitedPass();
        ret.addedBy = addedBy;
        ret.created = new Date();
        ret.starts = startDate;
        ret.expires = endDate;
        ret.membership = membership;
        ret.uses = 0;

        membership.unlimitedPasses.add(ret);
        membership.save();

        return ret;
    }

}
