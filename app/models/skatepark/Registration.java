package models.skatepark;

import play.db.ebean.Model;

import javax.persistence.Id;

/**
 * Created by cdelargy on 11/30/14.
 */
public class Registration extends Model {

    @Id
    public Long id;

    public String scheduleDescription;

    public static final Finder<Long, Registration> find = new Finder<Long, Registration>(Long.class, Registration.class);
}
