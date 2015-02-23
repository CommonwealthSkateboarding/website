package controllers;

import com.avaje.ebean.Expr;
import com.stripe.model.Charge;
import models.security.AuditRecord;
import models.site.NewsItem;
import models.skatepark.*;
import org.apache.commons.lang3.time.DateUtils;
import play.Logger;
import play.cache.Cache;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;

import views.html.*;

import java.util.*;

public class Application extends Controller {

    public static final int PER_PAGE = 5;
    public static final String STICKY_REVERSE_DATE_ORDER = "sticky DESC, createDate DESC";
    public static final String USER_ROLE = "USER";

    public static Result oAuthDenied(final String providerKey) {
        com.feth.play.module.pa.controllers.Authenticate.noCache(response());
        return redirect(routes.Application.login());
    }

    public static Result login() {
        return redirect(com.feth.play.module.pa.controllers.routes.Authenticate.authenticate("google"));
    }

    public static Result index(Long page) {
        String cacheKey = "newsPage" + page;
        boolean hasNextPage = false;
        List<NewsItem> news = (List<NewsItem>) Cache.get(cacheKey);
        if (null == news) {
            news = getNewsItems(page);
            Cache.set(cacheKey, news, 60 * 1); // cache for 1 minute
        }
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            news.remove(PER_PAGE);
            hasNextPage = true;
        }
        return ok(index.render(news, page, hasNextPage));
    }

    private static List<NewsItem> getNewsItems(Long page) {
        Date now = new Date();
        return new Model.Finder(Long.class, NewsItem.class)
                .where().or(Expr.eq("expires", false), Expr.gt("expireDate", now)).where().eq("frontPage", true)
                .setMaxRows(PER_PAGE+1).setFirstRow(page.intValue()*PER_PAGE).orderBy(STICKY_REVERSE_DATE_ORDER).findList();
    }

    public static Result showNews(Long id) {
        NewsItem news = (NewsItem) new Model.Finder(Long.class, NewsItem.class).byId(id);
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

    public static Result shop(){
        return ok(shop.render());
    }

    public static Result camp(){
        Date tomorrow = new Date();
        tomorrow = DateUtils.ceiling(tomorrow, Calendar.DATE);
        List<Camp> camps = Camp.find.where().gt("registrationEndDate", tomorrow).findList();
        return ok(camp.render(camps));
    }

    public static Result campDetail(Long id) {
        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
        if (null == camp) {
            return redirect(routes.Application.camp()); // not found
        }
        return ok(campDetail.render(camp));
    }

    public static Result sessions(){
        return ok(sessions.render());
    }

    public static Result events() {
        Date now = new Date();
        List<Event> publicEvents = Event.find.orderBy("startTime").where().gt("endTime", now)
                .where().eq("archived", false).where().eq("public_visibility", true).findList();
        return ok(events.render(publicEvents));
    }

    public static Result eventDetail(Long id) {
        Event event = (Event) new Model.Finder(Long.class, Event.class).byId(id);
        if (null == event) {
            return redirect(routes.Application.events()); // not found
        }
        return ok(eventDetail.render(event));
    }
    
    public static Result about(){
        return ok(about.render());
    }

    public static Result contact(){
        return ok(contact.render());
    }

    public static Result registerForCampWithStripe(Long id) {

        Camp camp = (Camp) new Model.Finder(Long.class, Camp.class).byId(id);
        if (null == camp) {
            //TODO: Log this extremely hard to produce error condition
            return redirect(routes.Application.camp()); // not found
        }

        String name = Form.form().bindFromRequest().data().get("name");
        String email = Form.form().bindFromRequest().data().get("stripeEmail");
        String stripeToken = Form.form().bindFromRequest().data().get("stripeToken");

        Charge charge = Stripe.chargeStripe(camp.cost, stripeToken, "Registration for " + camp.title);

        Registration reg = new Registration();

        reg.registrationType = Registration.RegistrationType.CAMP;
        reg.camp = camp;
        reg.paid = true;
        reg.registrantEmail = email;
        reg.participantName = name;
        reg.paymentType = Registration.PaymentType.STRIPE;
        reg.timestamp = new Date();
        reg.notes = "Paid on the web by " + email + " and generated a stripe chargeId of: " + charge.getId();
        reg.confirmationId = org.apache.commons.lang3.RandomStringUtils.random(6, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");
        reg.save();

        audit("Added registration for camp " + camp.title + " from web for " + name, camp);

        Email.sendCampRegistrationConfirmation(email, reg);

        return redirect(routes.Application.registrationPage());
    }

    public static Result registerForEventWithStripe(Long id) {

        Event event = (Event) new Model.Finder(Long.class, Event.class).byId(id);
        if (null == event) {
            //TODO: Log this extremely hard to produce error condition
            return redirect(routes.Application.events()); // not found
        }

        String name = Form.form().bindFromRequest().data().get("name");
        String email = Form.form().bindFromRequest().data().get("stripeEmail");
        String stripeToken = Form.form().bindFromRequest().data().get("stripeToken");

        Charge charge = Stripe.chargeStripe(event.cost, stripeToken, "Registration for " + event.name);

        Registration reg = new Registration();

        reg.registrationType = Registration.RegistrationType.EVENT;
        reg.event = event;
        reg.paid = true;
        reg.registrantEmail = email;
        reg.participantName = name;
        reg.paymentType = Registration.PaymentType.STRIPE;
        reg.timestamp = new Date();
        reg.notes = "Paid on the web by " + email + " and generated a stripe chargeId of: " + charge.getId();
        reg.confirmationId = org.apache.commons.lang3.RandomStringUtils.random(6, "ABCDEFGHJKMNPQRSTUVWXYZ23456789");
        reg.save();

        audit("Added registration for event " + event.name + " from web for " + name, event);

        Email.sendEventRegistrationConfirmation(email, reg);

        return redirect(routes.Application.registrationPage());
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

    public static Result registrationPage() {
        return ok(registrationPage.render());
    }
}
