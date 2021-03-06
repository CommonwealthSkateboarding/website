package controllers;

import com.stripe.exception.CardException;
import com.stripe.model.Charge;
import models.security.AuditRecord;
import models.site.ClosureNotice;
import models.site.NewsItem;
import utils.VisitCounter;
import models.skatepark.*;
import play.Logger;
import play.cache.Cached;
import play.data.Form;
import play.db.ebean.Model;
import play.mvc.*;

import views.html.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class Application extends Controller {

    public static final int PER_PAGE = 5;
    public static final String USER_ROLE = "USER";
    public static final Double CAMP_DEPOSIT = 100.0; //Dollars
    public static final Double EVENT_DEPOSIT = 50.0; //Dollars

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
        List<ClosureNotice> closures = ClosureNotice.getCachedActiveClosures();

        boolean hasNextPage = false;

        List<NewsItem> news = NewsItem.getCachedPagedNews(true, page, PER_PAGE);
        
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            hasNextPage = true;
        }
        return ok(index.render((hasNextPage?news.subList(0,PER_PAGE-1):news), closures, page, hasNextPage));
    }

    public static Result showNews(String id) {
        NewsItem news = NewsItem.getCachedNews(id);
        Date now = new Date();
        if (null == news || (news.expires && news.expireDate.before(now))) {
            return redirect(routes.Application.index(0)); // not found, deleted, expired
        }
        return ok(newsItem.render(news));
    }

    public static Result blog(Long page) {
        boolean hasNextPage = false;

        List<NewsItem> news = NewsItem.getCachedPagedNews(false, page, PER_PAGE);
        if (news.size() == (PER_PAGE + 1)) { // if there is another page after
            hasNextPage = true;
        }
        return ok(blog.render((hasNextPage?news.subList(0,PER_PAGE-1):news), page, hasNextPage));
    }

    @Cached(key = "shop")
    public static Result shop(){
        return ok(shop.render());
    }

    public static Result camp(){
        List<Camp> camps = Camp.getActiveCamps();
        return ok(camp.render(camps));
    }

    public static Result campDetail(String id) {
        Camp camp = Camp.find.byId(id);
        if (null == camp) {
            return redirect(routes.Application.camp()); // not found
        }
        return ok(campDetail.render(camp, null));
    }

    @Cached(key = "sessions")
    public static Result sessions(){
        return ok(sessions.render());
    }

    public static Result events() {
        return ok(events.render(Event.getActivePublicEvents()));
    }

    public static Result eventDetail(String id) {
        Event event = Event.find.byId(id);
        if (null == event || !event.publicVisibility) {
            return redirect(routes.Application.events()); // not found
        }
        return ok(eventDetail.render(event, null));
    }

    @Cached(key = "about")
    public static Result about(){
        return ok(about.render());
    }

    @Cached(key = "store")
    public static Result store(){
        return ok(store.render());
    }

    @Cached(key = "contact")
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
            Charge charge = Stripe.chargeStripe(info.fullyPaid?camp.getCurrentCost():CAMP_DEPOSIT, info.stripeToken, "Registration for " + camp.title);

            Registration reg = new Registration();

            reg.registrationType = Registration.RegistrationType.CAMP;
            reg.camp = camp;
            reg.paid = info.fullyPaid.booleanValue();
            reg.totalPaid = (info.fullyPaid ? camp.getCurrentCost() : CAMP_DEPOSIT);
            reg.registrantEmail = info.email;
            reg.participantName = info.name;
            reg.emergencyContactName = info.emergencyContactName;
            reg.emergencyTelephone = info.emergencyTelephone;
            reg.alternateEmergencyContactName = info.alternateEmergencyContactName;
            reg.alternateEmergencyTelephone = info.alternateEmergencyTelephone;
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
        return ok(registrationPage.render(registration, null));
    }

    public static Result registrationPayBalance(String id) {

        Registration registration = Registration.find.where().eq("confirmationId", id).findUnique();

        RegistrationInfo info = Form.form(RegistrationInfo.class).bindFromRequest().get();
        Double amount = registration.getRemainingDue();

        try {
            Charge charge = Stripe.chargeStripe(amount, info.stripeToken, "Remainder of payment due");

            registration.paid = true;
            registration.totalPaid = registration.totalPaid + amount;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
            Date now = new Date();
            registration.notes = registration.notes + "<br><br>At " + dateFormat.format(now) + " paid (" + utils.Formatter.prettyDollarsAndCents(amount) + ") on the web and generated a stripe chargeId of: " + charge.getId();
            registration.update();

            audit("Processed rest of payment for " + (registration.registrationType.equals(Registration.RegistrationType.CAMP)?registration.camp.title:registration.event.name) + " registration " + registration.confirmationId + " from web for " + registration.participantName, (registration.registrationType.equals(Registration.RegistrationType.CAMP)?registration.camp:registration.event));

            Slack.emitRegistrationBalancePayment(registration, amount);

            return redirect(routes.Application.registrationPage(registration.confirmationId));
        } catch (CardException e) {
            return ok(registrationPage.render(registration, info));
        } catch (Exception e) {
            Logger.error("Stripe error", e);
            return internalServerError();
        }
    }

    public static Result visitCounter() {
        return ok(visitCounter.render());
    }

    public static WebSocket<String> visitCounterWebsocket(){
        return new WebSocket<String>(){
            public void onReady(WebSocket.In<String> in, WebSocket.Out<String> out){
                VisitCounter.start(in, out);
            }
        };
    }
}
