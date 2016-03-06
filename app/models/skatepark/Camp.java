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

    public static final String ACTIVE_CAMPS_CACHE_NAME = "activeCamps";

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
    public Date earlyRegistrationEndDate;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date registrationEndDate;

    public Integer maxRegistrations;

    @Column(columnDefinition = "text")
    public String description;

    public Double cost;

    public Double earlyRegistrationDiscount;

    public String scheduleDescription;

    public String instructors;

    public boolean archived;

    public static final Finder<String, Camp> find = new Finder<>(String.class, Camp.class);

    public boolean isPastRegistrationEndDate() {
        return registrationEndDate.before(new Date());
    }

    public boolean isPastEarlyRegistrationEndDate() {
        return (null == earlyRegistrationEndDate || earlyRegistrationEndDate.before(new Date()));
    }

    // Return cost including any early discount
    public Double getCurrentCost() {
        if (
                (null != earlyRegistrationEndDate) &&
                (null != earlyRegistrationDiscount) &&
                !earlyRegistrationEndDate.before(new Date())
        ) {
            return (cost - earlyRegistrationDiscount);
        } else {
            return cost;
        }
    }

    @Override
    public void save() {
        play.cache.Cache.remove(ACTIVE_CAMPS_CACHE_NAME);
        play.cache.Cache.set(this.id.toString(), this);
        super.save();
    }

    @Override
    public void update() {
        play.cache.Cache.remove(ACTIVE_CAMPS_CACHE_NAME);
        play.cache.Cache.set(this.id.toString(), this);
        super.update();
    }

    private void clearCachedCamps() {
    }

    public static List<Camp> getActiveCamps() {
        List<Camp> camps = (List<Camp>) play.cache.Cache.get(ACTIVE_CAMPS_CACHE_NAME);
        if(null == camps) {
            //Date today = new Date();
            //today = DateUtils.ceiling(today, Calendar.DATE);
            //today = DateUtils.addDays(today, -1);
            //camps = Camp.find.where().ge("registrationEndDate", today).eq("archived", false).orderBy("startDate").findList();
            camps = Camp.find.where().eq("archived", false).orderBy("startDate").findList();
            play.cache.Cache.set(ACTIVE_CAMPS_CACHE_NAME, camps);
        }
        return camps;
    }
}
