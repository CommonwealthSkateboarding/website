package controllers;

import models.security.AuditRecord;
import models.skatepark.Visit;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import org.apache.commons.lang3.time.DateUtils;
import play.Play;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by cdelargy on 1/15/15.
 */
public class Slack {

    public static final String BASE_URL = Play.application().configuration().getString("application.baseUrl");

    // "id_1/id_2/token"
    private static String SLACKBOT_KEYS = Play.application().configuration().getString("slackbot.keys");

    private static final String SLACKBOT_CHANNEL = "#audit";

    private static SlackApi api = new SlackApi("https://hooks.slack.com/services/" + SLACKBOT_KEYS);

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    private static void dispatch(SlackMessage msg) {
        if (null != SLACKBOT_KEYS) {
            api.call(msg);
        }
    }

    public static void emitAuditLog(AuditRecord log) {
        // Set user's name when available
        dispatch(new SlackMessage(SLACKBOT_CHANNEL, ((null == log.user)?null:log.user.name), (log.delta + " [<" +
                BASE_URL + routes.Admin.logIndex(0)) + "| Log>]"));
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

        dispatch(new SlackMessage(SLACKBOT_CHANNEL, null, (sb.toString() + " [<" + BASE_URL +
                routes.Admin.dashboard()) + "| Dashboard>]"));
    }
}