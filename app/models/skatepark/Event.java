package models.skatepark;

import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by cdelargy on 12/20/14.
 */
@Entity
public class Event extends Model {

    @Id
    public Long id;

    public String name;

    public boolean publicVisibility;

    public boolean reservePark;

    @Column(columnDefinition = "text")
    public String notes;

    @Formats.DateTime(pattern = "yyyy-MM-dd h:mm a")
    public Date startTime;

    @Formats.DateTime(pattern = "yyyy-MM-dd h:mm a")
    public Date endTime;

    public static final Finder<Long, Event> find = new Finder(Long.class, Event.class);
}
