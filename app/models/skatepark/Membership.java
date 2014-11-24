package models.skatepark;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
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

    @OneToMany(cascade = CascadeType.ALL)
    public List<Visit> visits;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date createDate;

    @Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
    public Date lastVisited;

    public static final Finder<Long, Membership> find = new Finder<Long, Membership>(
            Long.class, Membership.class);
}
