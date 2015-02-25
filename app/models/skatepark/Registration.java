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
        @EnumValue("STRIPE")
        STRIPE,
        @EnumValue("OTHER")
        OTHER
    }

    public enum RegistrationType {
        @EnumValue("CAMP")
        CAMP,
        @EnumValue("EVENT")
        EVENT
    }

    @Id
    public Long id;

    @ManyToOne
    public Camp camp;

    @ManyToOne
    public Event event;

    public boolean paid;

    public Double totalPaid;

    public PaymentType paymentType;

    public RegistrationType registrationType;

    public String participantName;

    public String registrantEmail;

    @Column(columnDefinition = "text")
    public String notes;

    public Date timestamp;

    public String confirmationId;

    public static final Finder<Long, Registration> find = new Finder<Long, Registration>(Long.class, Registration.class);
}
