package models.skatepark;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    @Column(columnDefinition = "text")
    public String notes;

    @OneToMany(cascade = CascadeType.ALL)
    public List<UnlimitedPass> unlimitedPasses;

    public int sessionPasses;

    public Double credit;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Visit> visits;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date createDate;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastVisited;

    public static final Finder<Long, Membership> find = new Finder<Long, Membership>(
            Long.class, Membership.class);

    public boolean checkedInToday() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        if (null != this.lastVisited) {
            return (this.lastVisited.after(today.getTime()));
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
}
