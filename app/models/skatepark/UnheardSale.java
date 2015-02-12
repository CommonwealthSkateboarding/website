package models.skatepark;

import models.security.User;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Created by cdelargy on 2/9/15.
 */
@Entity
public class UnheardSale extends Model {

    @Id
    public Long id;
    public String brand;
    public String description;
    public Double retailPrice;
    public boolean teamRiderSale;
    public boolean invoiced;
    @ManyToOne
    public User soldBy;
    public Date created;

    public static final Finder<Long, UnheardSale> find = new Finder<Long, UnheardSale>(Long.class, UnheardSale.class);
}
