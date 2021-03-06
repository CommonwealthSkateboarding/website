import akka.actor.Cancellable;
import controllers.Admin;
import controllers.Slack;
import controllers.Square;
import models.skatepark.Membership;
import models.square.Payment;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.cache.Cache;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Call;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.duration.Duration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static play.mvc.Controller.session;
import static play.mvc.Results.internalServerError;
import static play.mvc.Results.notFound;


public class Global extends GlobalSettings {

    public static final String LAST_QUERIED_SQUARE = "lastQueriedSquare";

    public F.Promise<Result> onError(Http.RequestHeader request, Throwable t) {
        Logger.info("Request failed", t);
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return F.Promise.<Result>pure(internalServerError(
                views.html.errorPage.render("Sorry, but your request could not be completed", Admin.getLocalUser(session()), t, sw.toString())
        ));
    }

    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
        return F.Promise.<Result>pure(notFound(
                views.html.errorPage.render("The page you requested cannot be found", null, null, null)
        ));
    }

    public void onStart(final Application app) {
        /**
         * Largely adapted from https://github.com/joscha/play-authenticate
         */
        PlayAuthenticate.setResolver(new Resolver() {

            @Override
            public Call login() {
                // Your login page
                return routes.Application.login();
            }

            @Override
            public Call afterAuth() {
                // The user will be redirected to this page after authentication
                // if no original URL was saved
                return routes.Admin.dashboard();
            }

            @Override
            public Call afterLogout() {
                return routes.Application.index(0);
            }

            @Override
            public Call auth(final String provider) {
                // You can provide your own authentication implementation,
                // however the default should be sufficient for most cases
                return com.feth.play.module.pa.controllers.routes.Authenticate.authenticate(provider);
            }

            @Override
            public Call onException(final AuthException e) {
                if (e instanceof AccessDeniedException) {
                    return routes.Application.oAuthDenied(((AccessDeniedException) e).getProviderKey());
                }

                // more custom problem handling here...

                return super.onException(e);
            }

            @Override
            public Call askLink() {
                // TODO: add linking
                return null;
            }

            @Override
            public Call askMerge() {
                // TODO: add merging
                return null;
            }
        });

        Cancellable expiredPassTask = Akka.system().scheduler().schedule(
                //11:30 PM Local Time
                Duration.create(nextExecutionInSeconds(23,30), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                () -> {
                    Logger.info("Scanning for expired passes");
                    List<Membership> list = Membership.find.where().gt("promo_passes", 0).lt("last_active", DateUtils.addDays(new Date(), -60)).findList();

                    Logger.info("Found " + list.size() + " members with expired passes");
                    for (Membership member : list)
                    {
                        member.isPromoPassExpired(); // No need to take action, model will enforce
                        try {
                            Thread.sleep(3000); // Throttle the database updates
                        } catch (InterruptedException e) {}
                    }

                }, Akka.system().dispatcher()
        );

        Cancellable reportTask = Akka.system().scheduler().schedule(
                //8:00 AM Local Time
                Duration.create(nextExecutionInSeconds(8,00), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                () -> {
                    Logger.info("Performing 24 hour sales report");
                    Square.runSlackPaymentsReport(DateUtils.addDays(new Date(), -1));
                }, Akka.system().dispatcher()
        );

        Cancellable openClosuresTask = Akka.system().scheduler().schedule(
                //8:05 AM Local Time
                Duration.create(nextExecutionInSeconds(8,05), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                () -> {
                    Logger.info("Performing 24 hour active closures report");
                    Admin.runSlackClosureReport();
                }, Akka.system().dispatcher()
        );

        Thread cancelTasksOnShutdown = new Thread("shutdownHook") {
            public void run() {
                Logger.info("Cancelling akka tasks for application shutdown");
                reportTask.cancel();
                openClosuresTask.cancel();
                expiredPassTask.cancel();
            }
        };

        Akka.system().registerOnTermination(cancelTasksOnShutdown);

        /**
        Akka.system().scheduler().schedule(
                //2200 hours = 10PM in local timezone
                Duration.create(nextExecutionInSeconds(22, 0), TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                () -> {
                    Logger.info("Executing daily attendance report");
                    Slack.emitDailyAttendanceReport();
                }, Akka.system().dispatcher()
        );
         **/

        /** To be replaced if webhooks for square break again
        Akka.system().scheduler().schedule(Duration.create(10, TimeUnit.SECONDS),
                Duration.create(5, TimeUnit.MINUTES),
                () -> {
                    Date lastQueriedSquare = (Date) Cache.get(LAST_QUERIED_SQUARE);
                    try {
                        if (null != lastQueriedSquare) {
                            Square.runSlackPaymentsReport(lastQueriedSquare);
                        }
                        Cache.set(LAST_QUERIED_SQUARE, new Date());
                    } catch (TimeoutException e) {
                        Logger.error("Square did not respond to our request", e);
                    }
                }, Akka.system().dispatcher()
        );
         **/
    }

    private static int nextExecutionInSeconds(int hour, int minute){
        return Seconds.secondsBetween(
                new DateTime(),
                nextExecution(hour, minute)
        ).getSeconds();
    }

    private static DateTime nextExecution(int hour, int minute){
        DateTime next = new DateTime()
                .withHourOfDay(hour)
                .withMinuteOfHour(minute)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        return (next.isBeforeNow())
                ? next.plusHours(24)
                : next;
    }
}
