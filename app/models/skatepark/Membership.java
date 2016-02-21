package models.skatepark;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by cdelargy on 11/23/14.
 */
@Entity
public class Membership extends Model {

    @Id
    public Long id;

    public String name;

    public String parentName;

    public String address;

    public String city;

    public String state;

    public String zipcode;

    public String country;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date birthDate;

    public String telephone;

    public String email;

    public String emergencyContactName;

    public String emergencyContactNumber;

    public String emergencyContactNameB;

    public String emergencyContactNumberB;

    public String emergencyContactNameC;

    public String emergencyContactNumberC;

    @Column(columnDefinition = "text")
    public String notes;

    @OneToMany(cascade = CascadeType.ALL)
    public List<UnlimitedPass> unlimitedPasses;

    public int sessionPasses;
    public int allDayPasses;

    public Double credit;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    public List<Visit> visits;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date createDate;

    @JsonIgnore
    @OneToOne
    public Visit lastVisit;

    public boolean duplicate;

    public static final Finder<Long, Membership> find = new Finder<Long, Membership>(
            Long.class, Membership.class);

    public boolean isCheckedInToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        if ((null != this.lastVisit) && (null != this.lastVisit.time)) {
            return (this.lastVisit.time.after(today.getTime()));
        } else {
            return false;
        }
    }

    public void deposit(Double amount) {
        if (null == this.credit) {
            this.credit = amount;
        } else {
            this.credit = (this.credit + amount);
        }
        this.save();
    }

    public void spend(Double amount) {
        if (null == this.credit) {
            this.credit = (0 - amount);
        } else {
            this.credit = (this.credit - amount);
        }
        this.save();
    }

    public UnlimitedPass getMostValidUnlimitedPass() {
        for (UnlimitedPass pass : unlimitedPasses) {
            if (pass.isValid()) {
                return pass;
            }
        }
        return null;
    }

    /**
     * Should help rendering by hiding passes which are old and not valid. Will include the latest expired pass if
     * there are no unexpired passes
     * @return list of unlimited passes for display
     */

    @JsonIgnore
    public List<UnlimitedPass> getUnlimitedPassesForDisplay() {
        Date now = new Date();
        UnlimitedPass lastExpired = null;
        List<UnlimitedPass> passes = new ArrayList<UnlimitedPass>();
        passes.addAll(unlimitedPasses);
        for (UnlimitedPass pass : unlimitedPasses) {
            if (pass.expires.before(now)) {
                passes.remove(pass);
                if (null == lastExpired || lastExpired.expires.before(pass.expires)) {
                    lastExpired = pass;
                }
            }
        }

        Collections.sort(passes, new Comparator<UnlimitedPass>() {
            public int compare(UnlimitedPass p1, UnlimitedPass p2) {
                if (p1.expires == null || p2.expires == null)
                    return 0;
                return p1.expires.compareTo(p2.expires);
            }
        });

        if (passes.isEmpty() && null != lastExpired) {
            passes.add(lastExpired);
        }

        return passes;
    }
}
