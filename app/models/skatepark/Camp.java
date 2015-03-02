package models.skatepark;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by cdelargy on 11/30/14.
 */
@Entity
public class Camp extends Model {

    @Id
    public String id;

    public String title;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Registration> registrations;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date createDate;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date startDate;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date endDate;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date registrationEndDate;

    public Integer maxRegistrations;

    @Column(columnDefinition = "text")
    public String description;

    public Double cost;

    public String scheduleDescription;

    public String instructors;

    public static final Finder<Long, Camp> find = new Finder<Long, Camp>(Long.class, Camp.class);

}
