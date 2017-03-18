package models.skatepark;

import be.objectify.deadbolt.java.views.html.pattern;
import com.avaje.ebean.Expr;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by cdelargy on 12/20/14.
 */
@Entity
public class Event extends Model {

    public static final String ACTIVE_EVENTS_CACHE_NAME = "activeEvents";

    @Id
    public String id;

    public String name;

    public boolean publicVisibility;

    public boolean reservePark;

    @Column(columnDefinition = "text")
    public String notes;

    @Column(columnDefinition = "text")
    public String privateNotes;

    @Formats.DateTime(pattern = "yyyy-MM-dd h:mm a")
    public Date startTime;

    @Formats.DateTime(pattern = "yyyy-MM-dd h:mm a")
    public Date endTime;

    public boolean registrable;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date registrationEndDate;

    public Integer maxRegistrations;

    @OneToMany(cascade = CascadeType.ALL)
    public List<Registration> registrations;

    public Double cost;

    public boolean archived;

    public static final Finder<String, Event> find = new Finder(String.class, Event.class);

    public List<Event> findConflicts() {
        List<Event> results = new ArrayList<>();
        if(this.reservePark) {
            results = this.find.where()
                    .and(Expr.gt("endTime", this.startTime), Expr.lt("startTime", this.endTime))
                    .ne("id", this.id).eq("archived", false).findList();
        }
        return results;
    }
    public boolean isPastRegistrationEndDate() {
        return registrationEndDate.before(new Date());
    }


    public static List<Event> getActivePublicEvents() {
        List<Event> events = (List<Event>) play.cache.Cache.get(ACTIVE_EVENTS_CACHE_NAME);
        if (null == events) {
            Date now = new Date();
            events = Event.find.orderBy("startTime").where().gt("endTime", now)
                    .where().eq("archived", false).where().eq("public_visibility", true).findList();
            play.cache.Cache.set(ACTIVE_EVENTS_CACHE_NAME, events);
        }
        return events;
    }
    
    @Override
    public void save() {
        play.cache.Cache.remove(ACTIVE_EVENTS_CACHE_NAME);
        play.cache.Cache.set(this.id.toString(), this);
        super.save();
    }

    @Override
    public void update() {
        play.cache.Cache.remove(ACTIVE_EVENTS_CACHE_NAME);
        play.cache.Cache.set(this.id.toString(), this);
        super.update();
    }
}
