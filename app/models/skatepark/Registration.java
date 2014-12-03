package models.skatepark;

import com.avaje.ebean.annotation.EnumValue;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by cdelargy on 11/30/14.
 */
@Entity
public class Registration extends Model {

    public enum PaymentType {
        @EnumValue("CASH")
        CASH,
        @EnumValue("SQUARE")
        SQUARE,
        @EnumValue("ONLINE")
        ONLINE
    }

    @Id
    public Long id;

    @ManyToOne
    public Camp camp;

    public boolean paid;

    public PaymentType paymentType;

    public String participantName;

    @Column(columnDefinition = "text")
    public String notes;

    public Date timestamp;

    public static final Finder<Long, Registration> find = new Finder<Long, Registration>(Long.class, Registration.class);
}
