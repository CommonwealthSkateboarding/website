package models.skatepark;

import com.avaje.ebean.Expr;
import play.data.format.Formats;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public boolean archived;

    public static final Finder<Long, Event> find = new Finder(Long.class, Event.class);

    public List<Event> findConflicts() {
        List<Event> results = new ArrayList<>();
        if(this.reservePark) {
            results = this.find.where()
                    .and(Expr.gt("endTime", this.startTime), Expr.lt("startTime", this.endTime))
                    .ne("id", this.id).eq("archived", false).findList();
        }
        return results;
    }
}
