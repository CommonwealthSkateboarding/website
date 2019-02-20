package controllers;

import com.stripe.model.Charge;
import models.security.AuditRecord;
import models.security.User;
import models.site.ClosureNotice;
import models.site.Issue;
import models.skatepark.Registration;
import models.skatepark.Visit;
import models.square.Payment;
import models.square.PaymentItemization;
import models.square.Refund;
import models.square.Tender;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.commons.lang3.time.DateUtils;
import play.Logger;
import play.Play;
import utils.TimeUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static controllers.Admin.getLocalUser;
import static play.mvc.Http.Context.Implicit.session;
import static utils.Formatter.prettyDollarsAndCents;

/**
 * Created by cdelargy on 1/15/15.
 */
public class Slack {

    public static final String BASE_URL = Play.application().configuration().getString("application.baseUrl");

    // "id_1/id_2/token"
    private static String SLACKBOT_KEYS = Play.application().configuration().getString("slackbot.keys");

    private static final String SLACKBOT_AUDIT_CHANNEL = "#audit";
    private static final String SLACKBOT_BUSINESS_CHANNEL = "#business";
    private static final String SLACKBOT_FINANCE_CHANNEL = "#finance";
    private static final String SLACKBOT_GENERAL_CHANNEL = "#general";
    private static final long FIFTEEN_MINUTES_IN_MILLISECONDS = 1000*60*15;

    private static SlackApi api = new SlackApi("https://hooks.slack.com/services/" + SLACKBOT_KEYS);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    private static SimpleDateFormat squareDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private static void dispatch(SlackMessage msg) {
        if (null != SLACKBOT_KEYS) {
            api.call(msg);
        } else {
            Logger.info(msg.prepare().toString());
        }
    }

    public static void emitLogin(User knownUser) {
        dispatch(new SlackMessage(SLACKBOT_AUDIT_CHANNEL, ((null == knownUser)?null:knownUser.name), "Logged in"));
    }

    public static void emitAuditLog(AuditRecord log) {
        // Set user's name when available
        dispatch(new SlackMessage(SLACKBOT_AUDIT_CHANNEL, ((null == log.user)?null:log.user.name), log.delta));
    }

    public static Void emitPaymentDetails(Payment payment) {
        StringBuilder sb = new StringBuilder();
        StringBuilder paymentMethods = new StringBuilder();
        for (Tender t : Arrays.asList(payment.tender)) {
            if(t.type == Tender.Type.CREDIT_CARD) {
                paymentMethods.append(":credit_card:");
            } else if (t.type == Tender.Type.CASH) {
                paymentMethods.append(":dollar:");
            }
        }
        try {
            Date created = squareDF.parse(payment.created_at);
            Date now = new Date();
            Long interval = (now.getTime() - created.getTime());

            // if payment happened more than 15 minutes ago...
            if (interval > FIFTEEN_MINUTES_IN_MILLISECONDS && (null == payment.refunds || payment.refunds.length == 0)) {
                sb.append("_Created " + TimeUtil.millisToLongDHMS(interval) + " ago_ :troll:\n");
            }
        } catch (ParseException e) {
            Logger.error("Square sending unparse-able dates?", e);
        }
        sb.append("<" + payment.receipt_url + "|Square order " + payment.id + "> (" +
                prettyDollarsAndCents(payment.total_collected_money.amount / 100.0) + " " + paymentMethods.toString() + ") ");
        for (PaymentItemization item : Arrays.asList(payment.itemizations)) {
            sb.append("\n" + item.quantity.intValue() + "x " + item.name + (item.item_variation_name != null?" (" + item.item_variation_name + ")":"") +
                    (item.name.contains("Custom Amount")?" ಠ_ಠ":"") +
                    " " + prettyDollarsAndCents(item.total_money.amount/100));
        }
        if (null != payment.refunds && payment.refunds.length > 0) {
            for (Refund refund : Arrays.asList(payment.refunds)) {
                sb.append("\n" + "Refunded " + prettyDollarsAndCents(refund.refunded_money.amount / 100));
            }
        }
        dispatch(new SlackMessage(SLACKBOT_FINANCE_CHANNEL, null, sb.toString()));

        return null;
    }

    public static void emitDailyPaymentReport(List<Payment> payments) {
        Double totalSales = 0.00;
        for(Payment p : payments) {
            totalSales += p.total_collected_money.amount / 100.0;
        }
        dispatch(new SlackMessage(SLACKBOT_BUSINESS_CHANNEL, null, "Daily payment report! :tada:\nIn the last 24 hours, " + payments.size() + " payments were collected for a total revenue of " + prettyDollarsAndCents(totalSales)));
    }

    public static void emitClosuresReport(List<ClosureNotice> closures) {
        dispatch(new SlackMessage(SLACKBOT_BUSINESS_CHANNEL, null, "Active closure report! :tada:"));
        for(ClosureNotice closure : closures) {
            dispatch(new SlackMessage(SLACKBOT_BUSINESS_CHANNEL, null, (closure.message + "\nCreated " + TimeUtil.getMonthDayYearString(closure.created) + " by " + closure.createdBy.name + " [<" + BASE_URL + routes.Admin.closureIndex()) + "|Edit>]"));
        }
    }

    public static void notifyOfNewIssue(Issue issue) {
        dispatch(new SlackMessage(SLACKBOT_GENERAL_CHANNEL, issue.createdBy.name, ("New issue created: " + issue.title +
                " [<" + BASE_URL + routes.Admin.issueIndex()) + "|View Issues>]"));
    }

    public static void notifyOfClosedIssue(Issue issue) {
        dispatch(new SlackMessage(SLACKBOT_GENERAL_CHANNEL, getLocalUser(session()).name, ("Issue closed: " +
                issue.title + " [<" + BASE_URL + routes.Admin.issueIndex()) + "|View Issues>]"));
    }

    public static void emitCampRegistrationPayment(Registration reg) {
        dispatch(new SlackMessage(SLACKBOT_FINANCE_CHANNEL, reg.participantName, ("Payment of "
                + prettyDollarsAndCents(reg.totalPaid) + " for " + reg.camp.title + " registration of " + reg.participantName +
                " [<" + BASE_URL + routes.Admin.viewCampPage(reg.camp.id)) + "|View Camp>]"));
    }

    public static void emitEventRegistrationPayment(Registration reg) {
        dispatch(new SlackMessage(SLACKBOT_FINANCE_CHANNEL, reg.participantName,
                ("Payment of " + prettyDollarsAndCents(reg.totalPaid) + " for " + reg.event.name + " registration of " +
                reg.participantName + " [<" + BASE_URL + routes.Admin.viewEventPage(reg.event.id)) + "|View Event>]"));
    }

    public static void emitRegistrationBalancePayment(Registration reg, Double amount) {
        dispatch(new SlackMessage(SLACKBOT_FINANCE_CHANNEL, reg.participantName, ("Balance payment of "
                + prettyDollarsAndCents(amount) + " for " + (reg.registrationType.equals(Registration.RegistrationType.CAMP)?reg.camp.title:reg.event.name) + " registration of " + reg.participantName)));
    }

    public static void emitBitcoinPayment(Charge charge) {
        dispatch(new SlackMessage(SLACKBOT_FINANCE_CHANNEL, getLocalUser(session()).name,
                ("Payment of " + prettyDollarsAndCents((charge.getAmount()/100.0)) + " for " + charge.getDescription())));
    }
}
