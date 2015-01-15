package controllers;

import models.security.AuditRecord;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackMessage;
import play.Play;

/**
 * Created by cdelargy on 1/15/15.
 */
public class Slack {

    // "id_1/id_2/token"
    private static String SLACKBOT_KEYS = Play.application().configuration().getString("slackbot.keys");
    private static final String SLACKBOT_CHANNEL = "#website";

    private static SlackApi api = new SlackApi("https://hooks.slack.com/services/" + SLACKBOT_KEYS);

    private static void dispatch(SlackMessage msg) {
        if (null != SLACKBOT_KEYS) {
            api.call(msg);
        }
    }

    public static void emitAuditLog(AuditRecord log) {
        // Set user's name when available
        dispatch(new SlackMessage(SLACKBOT_CHANNEL, ((null == log.user)?null:log.user.name), (log.delta + " [<" +
                Play.application().configuration().getString("application.baseUrl") + routes.Admin.logIndex(0)) +
                "| Log>]"));
    }
}
