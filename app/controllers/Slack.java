package controllers;

import models.security.AuditRecord;
import models.skatepark.Visit;
import models.square.Payment;
import models.square.PaymentItemization;
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

import static utils.Formatter.prettyDollars;

/**
 * Created by cdelargy on 1/15/15.
 */
public class Slack {

    public static final String BASE_URL = Play.application().configuration().getString("application.baseUrl");

    // "id_1/id_2/token"
    private static String SLACKBOT_KEYS = Play.application().configuration().getString("slackbot.keys");

    private static final String SLACKBOT_AUDIT_CHANNEL = "#audit";
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

    public static void emitAuditLog(AuditRecord log) {
        // Set user's name when available
        dispatch(new SlackMessage(SLACKBOT_AUDIT_CHANNEL, ((null == log.user)?null:log.user.name), (log.delta + " [<" +
                BASE_URL + routes.Admin.logIndex(0)) + "|Log>]"));
    }

    public static void emitDailyAttendanceReport() {
        Date today = DateUtils.addDays(new Date(), -1);
        today = DateUtils.ceiling(today, Calendar.DATE);
        List<Visit> visits = Visit.find.where().eq("refunded", false).where().gt("time", today).findList();
        HashMap<Visit.VisitType, Integer> breakdown = new HashMap<>();
        for(Visit visit : visits) {
            if (breakdown.containsKey(visit.visitType)) {
                breakdown.put(visit.visitType, (breakdown.get(visit.visitType) + 1));
            } else {
                breakdown.put(visit.visitType, 1);
            }
        }

        StringBuffer sb = new StringBuffer();
        int totalAttendance = 0;
        sb.append("Attendance report for " + dateFormat.format(today) + ": \n");
        if (breakdown.isEmpty()) {
            sb.append("No visits today!");
        } else {
            for (Visit.VisitType type : Visit.VisitType.values()) {
                if (null != breakdown.get(type)) {
                    sb.append(type.name() + ": " + breakdown.get(type) + "\n");
                    totalAttendance = totalAttendance + breakdown.get(type);
                }
            }
            sb.append("Total: " + totalAttendance);
        }

        dispatch(new SlackMessage(SLACKBOT_GENERAL_CHANNEL, null, (sb.toString() + " [<" + BASE_URL +
                routes.Admin.dashboard()) + "|Dashboard>]"));
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
            if (interval > FIFTEEN_MINUTES_IN_MILLISECONDS) {
                sb.append("_Created " + TimeUtil.millisToLongDHMS(interval) + " ago_ :troll:\n");
            }
        } catch (ParseException e) {
            Logger.error("Square sending unparse-able dates?", e);
        }
        sb.append("<" + payment.receipt_url + "|Square order " + payment.id + "> (" +
                prettyDollars(payment.total_collected_money.amount / 100.0) + " " + paymentMethods.toString() + ") ");
        for (PaymentItemization item : Arrays.asList(payment.itemizations)) {
            sb.append("\n" + item.quantity.intValue() + "x " + item.name + " (" +
                    prettyDollars(item.total_money.amount/100) + ")");
        }
        dispatch(new SlackMessage(SLACKBOT_FINANCE_CHANNEL, null, sb.toString()));

        return null;
    }
}
