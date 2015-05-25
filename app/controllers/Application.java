package controllers;

import com.avaje.ebean.Expr;
import com.stripe.exception.CardException;
import com.stripe.model.Charge;
import models.security.AuditRecord;
import models.site.ClosureNotice;
import models.site.NewsItem;
import models.skatepark.*;
import org.apache.commons.lang3.time.DateUtils;
import play.Logger;
import play.cache.Cache;
import play.cache.Cached;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;

import views.html.*;

import java.util.*;

public class Application extends Controller {

    public static final int PER_PAGE = 5;
    public static final String STICKY_REVERSE_DATE_ORDER = "sticky DESC, createDate DESC";
    public static final String USER_ROLE = "USER";
    public static final Double CAMP_DEPOSIT = 100.0; //Dollars
    public static final Double EVENT_DEPOSIT = 50.0; //Dollars
    public static final int CACHE_TIME_IN_SECONDS = 2 * 60; // 2 minutes

    public static Result removeTrailingSlash(String path) {
        return movedPermanently("/" + path);
    }

    public static Result oAuthDenied(final String providerKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        return redirect(routes.Application.login());
    }

    public static Result login() {
        return redirect(com.feth.play.module.pa.controllers.routes.Authenticate.authenticate("google"));
    }

    public static Result index(Long page) {

        List<ClosureNotice> closures = (List<ClosureNotice>) Cache.get(ClosureNotice.CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME);
        if (null == closures) {
            Logger.info("empty cache for closures");
            closures = ClosureNotice.find.where().eq("enabled", true).findList();
            Cache.set(ClosureNotice.CURRENTLY_ACTIVE_CLOSURES_CACHE_NAME, closures);
        }

        String cacheKey = "newsPage" + page;
        boolean hasNextPage = false;
        List<NewsItem> news = (List<NewsItem>) Cache.get(cacheKey);
        if (null == news) {
            news = getNewsItems(page);
            Cache.set(cacheKey, news, CACHE_TIME_IN_SECONDS);
        }
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            news.remove(PER_PAGE);
            hasNextPage = true;
        }
        return ok(index.render(news, closures, page, hasNextPage));
    }

    private static List<NewsItem> getNewsItems(Long page) {
        Date now = new Date();
        return new Model.Finder(Long.class, NewsItem.class)
                .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now)).where().eq("frontPage", true)
                .setMaxRows(PER_PAGE+1).setFirstRow(page.intValue()*PER_PAGE).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
    }

    public static Result showNews(String id) {
        NewsItem news = (NewsItem) new Model.Finder(String.class, NewsItem.class).byId(id);
        Date now = new Date();
        if (null == news || (news.expires && news.expireDate.before(now))) {
            return redirect(routes.Application.index(0)); // not found, deleted, expired
        }
        return ok(newsItem.render(news));
    }

    public static Result blog(Long page) {
        boolean hasNextPage = false;
        Date now = new Date();
        List<NewsItem> news = NewsItem.find.where().or(Expr.eq("expires", false), Expr.gt("expireDate", now))
                .setMaxRows(PER_PAGE+1).setFirstRow(page.intValue()*PER_PAGE).orderBy("createDate DESC").findList();
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            news.remove(PER_PAGE);
            hasNextPage = true;
        }
        return ok(blog.render(news, page, hasNextPage));
    }

    @Cached(duration = CACHE_TIME_IN_SECONDS, key = "shop")
    public static Result shop(){
        return ok(shop.render());
    }

    @Cached(duration = CACHE_TIME_IN_SECONDS, key = "camp")
    public static Result camp(){
        Date today = new Date();
        today = DateUtils.ceiling(today, Calendar.DATE);
        today = DateUtils.addDays(today, -1);
        List<Camp> camps = Camp.find.where().ge("registrationEndDate", today).eq("archived", false).orderBy("startDate").findList();
        return ok(camp.render(camps));
    }

    public static Result campDetail(String id) {
        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Application.camp()); // not found
        }
        return ok(campDetail.render(camp, null));
    }

    @Cached(duration = CACHE_TIME_IN_SECONDS, key = "sessions")
    public static Result sessions(){
        return ok(sessions.render());
    }

    @Cached(duration = CACHE_TIME_IN_SECONDS, key = "events")
    public static Result events() {
        Date now = new Date();
        List<Event> publicEvents = Event.find.orderBy("startTime").where().gt("endTime", now)
                .where().eq("archived", false).where().eq("public_visibility", true).findList();
        return ok(events.render(publicEvents));
    }

    public static Result eventDetail(String id) {
        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            return redirect(routes.Application.events()); // not found
        }
        return ok(eventDetail.render(event, null));
    }

    @Cached(duration = CACHE_TIME_IN_SECONDS, key = "about")
    public static Result about(){
        return ok(about.render());
    }

    @Cached(duration = CACHE_TIME_IN_SECONDS, key = "contact")
    public static Result contact(){
        return ok(contact.render());
    }

    public static Result registerForCampWithStripe(String id) {

        Camp camp = (Camp) new Model.Finder(String.class, Camp.class).byId(id);
        if (null == camp) {
            //TODO: Log this extremely hard to produce error condition
            return redirect(routes.Application.camp()); // not found
        }

        RegistrationInfo info = Form.form(RegistrationInfo.class).bindFromRequest().get();

        try {
            Charge charge = Stripe.chargeStripe(info.fullyPaid?camp.cost:CAMP_DEPOSIT, info.stripeToken, "Registration for " + camp.title);

            Registration reg = new Registration();

            reg.registrationType = Registration.RegistrationType.CAMP;
            reg.camp = camp;
            reg.paid = info.fullyPaid.booleanValue();
            reg.totalPaid = (info.fullyPaid ? camp.cost : CAMP_DEPOSIT);
            reg.registrantEmail = info.email;
            reg.participantName = info.name;
            reg.paymentType = Registration.PaymentType.STRIPE;
            reg.timestamp = new Date();
            reg.notes = "Paid (" + utils.Formatter.prettyDollarsAndCents(reg.totalPaid) + ") on the web by " + info.billingName + "(" + (info.telephone.isEmpty() ? "" : "tel: " + info.telephone + ", ") + "email: " + info.email + ") and generated a stripe chargeId of: " + charge.getId();
            reg.confirmationId = org.apache.commons.lang3.RandomStringUtils.random(6, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");
            reg.save();

            audit("Added registration for camp " + camp.title + " from web for " + info.name, camp);

            Email.sendCampRegistrationConfirmation(info.email, reg);
            Slack.emitCampRegistrationPayment(reg);

            return redirect(routes.Application.registrationPage(reg.confirmationId));
        } catch (CardException e) {
            return ok(campDetail.render(camp, info));
        } catch (Exception e) {
            Logger.error("Stripe error", e);
            return internalServerError();
        }
    }

    public static Result registerForEventWithStripe(String id) {

        Event event = (Event) new Model.Finder(String.class, Event.class).byId(id);
        if (null == event) {
            //TODO: Log this extremely hard to produce error condition
            return redirect(routes.Application.events()); // not found
        }

        RegistrationInfo info = Form.form(RegistrationInfo.class).bindFromRequest().get();

        try {
            Charge charge = Stripe.chargeStripe(info.fullyPaid ? event.cost : EVENT_DEPOSIT, info.stripeToken, "Registration for " + event.name);

            Registration reg = new Registration();

            reg.registrationType = Registration.RegistrationType.EVENT;
            reg.event = event;
            reg.paid = info.fullyPaid.booleanValue();
            reg.totalPaid = (info.fullyPaid ? event.cost : EVENT_DEPOSIT);
            reg.registrantEmail = info.email;
            reg.participantName = info.name;
            reg.paymentType = Registration.PaymentType.STRIPE;
            reg.timestamp = new Date();
            reg.notes = "Paid (" + utils.Formatter.prettyDollarsAndCents(reg.totalPaid) + ") on the web by " + info.billingName + "(" + (info.telephone.isEmpty() ? "" : "tel: " + info.telephone + ", ") + "email: " + info.email + ") and generated a stripe chargeId of: " + charge.getId();
            reg.confirmationId = org.apache.commons.lang3.RandomStringUtils.random(6, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");
            reg.save();

            audit("Added registration for event " + event.name + " from web for " + info.name, event);

            Email.sendEventRegistrationConfirmation(info.email, reg);
            Slack.emitEventRegistrationPayment(reg);

            return redirect(routes.Application.registrationPage(reg.confirmationId));
        } catch (CardException e) {
            return ok(eventDetail.render(event, info));
        } catch (Exception e) {
            Logger.error("Stripe error", e);
            return internalServerError();
        }
    }

    public static void audit(String description, Object payload) {
        AuditRecord log = new AuditRecord();
        log.delta = description;
        log.timestamp = new Date();
        if (null == payload) {
            // chill
        } else if (payload.getClass().equals(Camp.class)) {
            log.camp = (Camp) payload;
        } else if (payload.getClass().equals(Event.class)) {
            log.event = (Event) payload;
        }
        log.save();
        Slack.emitAuditLog(log);
    }

    public static Result registrationPage(String id) {
        Registration registration = Registration.find.where().eq("confirmationId", id).findUnique();
        return ok(registrationPage.render(registration));
    }
}
