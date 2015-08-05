package models.skatepark;

import models.security.User;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Created by cdelargy on 3/29/15.
 */
@Entity
public class BitcoinSale extends Model {

    @Id
    public Long id;
    public String description;
    public Double amount;
    @Transient
    public String stripeToken;
    @ManyToOne
    public User soldBy;
    public Date created;

    public static final Finder<Long, BitcoinSale> find = new Finder<Long, BitcoinSale>(Long.class, BitcoinSale.class);
}
