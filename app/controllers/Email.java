package controllers;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import models.skatepark.Registration;
import play.Play;
import play.mvc.Controller;
import views.html.email.campRegistrationEmail;

/**
 * Created by cdelargy on 1/9/15.
 */
public class Email extends Controller {

    private static String COMMONWEALTH_RETURN_EMAIL = Play.application().configuration().getString("email.fromAddress");

    public static void sendRegistrationConfirmation(String recipientAddress, Registration registration) {
        MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
        mail.setSubject("Commonwealth Skatepark Camp Registration Confirmation");
        mail.setRecipient(recipientAddress);
        mail.setFrom(COMMONWEALTH_RETURN_EMAIL);
        String body = campRegistrationEmail.render(registration).body();
        mail.sendHtml(body);
    }
}