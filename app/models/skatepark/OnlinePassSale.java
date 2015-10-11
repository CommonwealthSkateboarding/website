package models.skatepark;

import models.security.User;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by cdelargy on 9/27/15.
 */
@Entity
public class OnlinePassSale extends Model {

    public static final Double PRICE_TWO_HOUR_PASS = 7.0;
    public static final Double PRICE_TEN_PACK_PASS = 55.0;
    public static final Double PRICE_UNLIMITED_PASS_PER_MONTH = 48.0;

    @Id
    public Long id;
    public int twoHourPasses;
    public int tenPackPasses;
    public int unlimitedPassMonths;
    public int giftCreditDollars;

    public Double amount() {
        return (twoHourPasses * PRICE_TWO_HOUR_PASS) +
                (tenPackPasses * PRICE_TEN_PACK_PASS) +
                (unlimitedPassMonths * PRICE_UNLIMITED_PASS_PER_MONTH) +
                (giftCreditDollars);
    }

    @Transient
    public Long membershipId;

    @Transient
    public Boolean self;

    @Transient
    public String stripeToken;

    public String recipient;

    public Date created;
    public Boolean redeemed;
    @ManyToOne
    public User purchasedBy;
    public String alternateEmailAddress;
    @ManyToOne
    public Membership appliedTo;

    public static final Finder<Long, OnlinePassSale> find = new Finder<Long, OnlinePassSale>(Long.class, OnlinePassSale.class);
}
