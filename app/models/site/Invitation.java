package models.site;

import models.security.User;
import models.skatepark.Membership;
import play.db.ebean.Model;

import javax.persistence.Entity;
import java.util.Date;

/**
 * Created by cdelargy on 3/13/15.
 */
@Entity
public class Invitation extends Model {

    public Membership membership;

    public String code;

    public String email;

    public Date sent;

    public User claimedBy;

}
