package models.skatepark;

import org.apache.commons.lang3.time.DateUtils;
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
        Date tomorrow = DateUtils.addDays(new Date(), 1);
        return registrationEndDate.before(tomorrow); //inclusive of end date
    }

    public boolean isPastEarlyRegistrationEndDate() {
        Date tomorrow = DateUtils.addDays(new Date(), 1);
        return (null == earlyRegistrationEndDate || earlyRegistrationEndDate.before(tomorrow)); //inclusive of end date)
    }

    // Return cost including any early discount
    public Double getCurrentCost() {
        Date tomorrow = DateUtils.addDays(new Date(), 1);
        if (
                (null != earlyRegistrationEndDate) &&
                (null != earlyRegistrationDiscount) &&
                !earlyRegistrationEndDate.before(tomorrow) //inclusive of end date
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
