package controllers;

import models.skatepark.Registration;
import play.Play;
import play.mvc.Controller;
import views.html.email.inlineCampRegistrationEmail;
import views.html.email.inlineCampReminderEmail;
import views.html.email.inlineEventRegistrationEmail;
import play.api.libs.mailer.MailerClient;

import javax.inject.Inject;

/**
 * Created by cdelargy on 1/9/15.
 */
public class Email extends Controller {

    @Inject
    static MailerClient mailerClient;

    private static String COMMONWEALTH_RETURN_EMAIL = Play.application().configuration().getString("email.fromAddress");

    public static void sendCampRegistrationConfirmation(String recipientAddress, Registration registration) {
        play.libs.mailer.Email mail = new play.libs.mailer.Email();
        mail.setSubject("Commonwealth Skatepark Camp Registration Confirmation");
        mail.addTo(recipientAddress);
        mail.setFrom(COMMONWEALTH_RETURN_EMAIL);
        String body = inlineCampRegistrationEmail.render(registration).body();
        mail.setBodyHtml(body);
        mailerClient.send(mail);
    }
    public static void sendEventRegistrationConfirmation(String recipientAddress, Registration registration) {
        play.libs.mailer.Email mail = new play.libs.mailer.Email();
        mail.setSubject("Commonwealth Skatepark Event Registration Confirmation");
        mail.addTo(recipientAddress);
        mail.setFrom(COMMONWEALTH_RETURN_EMAIL);
        String body = inlineEventRegistrationEmail.render(registration).body();
        mail.setBodyHtml(body);
        mailerClient.send(mail);
    }

    public static void sendCampReminderEmail(String recipientAddress, Registration registration) {
        play.libs.mailer.Email mail = new play.libs.mailer.Email();
        mail.setSubject("Commonwealth Skatepark Upcoming Camp Reminder");
        mail.addTo(recipientAddress);
        mail.setFrom(COMMONWEALTH_RETURN_EMAIL);
        String body = inlineCampReminderEmail.render(registration).body();
        mail.setBodyHtml(body);
        mailerClient.send(mail);
    }
}
