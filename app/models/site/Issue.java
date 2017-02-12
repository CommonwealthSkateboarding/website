package models.site;

import models.security.User;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by cdelargy on 1/31/15.
 */
@Entity
public class Issue extends Model {

    @Id
    public Long id;

    public Date created;
    public Date resolved;

    public String title;

    @Column(columnDefinition = "text")
    public String description;

    @ManyToOne
    public User createdBy;

    @ManyToOne
    public User owner;

    public static final Finder<Long, Issue> find = new Finder<>(Long.class, Issue.class);
}
