package models.site;

import play.data.format.Formats;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by cdelargy on 10/16/14.
 */
@Entity
public class NewsItem extends Model {

    @Id
    public String id;

    public boolean expires;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date expireDate;

    @Temporal(TemporalType.DATE)
    @Formats.DateTime(pattern="MM/dd/yyyy")
    public Date createDate;

    public String title;

    @Column(columnDefinition = "text")
    public String content;

    @Column(columnDefinition = "text")
    public String extendedContent;

    public boolean sticky;

    public boolean frontPage;

    public static final Finder<Long, NewsItem> find = new Finder<>(Long.class, NewsItem.class);

}
