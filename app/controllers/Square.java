package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.square.Order;
import models.square.Payment;
import models.square.SquareWebhook;
import play.Logger;
import play.Play;
import play.libs.ws.*;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import utils.TimeUtil;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Created by cdelargy on 12/18/14.
 */
public class Square extends Controller {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String SQUARE_TOKEN = "square.token";
    public static final String BEARER_TOKEN = "Bearer " + Play.application().configuration().getString(SQUARE_TOKEN);
    public static final String LAST_WEBHOOK_ENTITY_ID = "lastWebhookEntityId";

    private static String BASE_URL = "https://connect.squareup.com/v1/me/";
    private static String INVENTORY_URL = BASE_URL + "inventory";
    private static String PAYMENT_URL = BASE_URL + "payments";


    private static String ORDERS_URL = BASE_URL + "orders?order=DESC";

    private static boolean enabled = (null != Play.application().configuration().getString(SQUARE_TOKEN));

    //https://connect.squareup.com/v1/me/inventory

    public static Promise<JsonNode> getInventory() {
        WSRequest holder = WS.url(INVENTORY_URL).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        return holder.get().map(response -> response.asJson());
    }

    public static List<Order> getOrders() {
        WSRequest holder = WS.url(ORDERS_URL).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        WSResponse response = holder.get().get(1000);
        Order[] orders = null;
        if (response.getStatus() != OK) {
            Logger.error("Got bad response from square when getting orders: " + response.getBody().toString());
        } else {
            ObjectMapper mapper = new ObjectMapper();
            try {
                orders = mapper.readValue(response.getBody(), Order[].class);
            } catch (IOException e) {
                Logger.error("Unable to iterate through square orders", e);
            }
        }
        return (null == orders)?(new ArrayList<>()):Arrays.asList(orders);
    }

    private static Payment getPayment(String paymentId) {
        WSRequest holder = WS.url(PAYMENT_URL + "/" + paymentId).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        WSResponse response = holder.get().get(10000);
        Payment payment = null;
        if (response.getStatus() != OK) {
            Logger.error("Got bad response from square when getting payment: " + response.getBody().toString());
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                payment = mapper.readValue(response.getBody(), Payment.class);
            } catch (IOException e) {
                Logger.error("Bad conversion of square payment id: " + paymentId, e);
            }
        }
        return payment;
    }

    public static void runSlackPaymentsReport(Date since) {
        Payment[] payments = null;
        if (enabled) {
            WSRequest holder = WS.url(PAYMENT_URL).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN)
                    .setQueryParameter("begin_time", TimeUtil.getISO8601Date(since));
            WSResponse response = holder.get().get(30000);
            if (response.getStatus() != OK) {
                Logger.error("Got bad response from square when getting payments: " + response.getBody().toString());
            } else {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    payments = mapper.readValue(response.getBody(), Payment[].class);
                } catch (IOException e) {
                    Logger.error("Unable to iterate through square payments", e);
                }
            }
        }
        if (null != payments && payments.length > 0) {
            List<Payment> paymentList = Arrays.asList(payments);
            Logger.info("Received " + paymentList.size() + " square payments, emitting to slack");
            Slack.emitDailyPaymentReport(paymentList);
        }
    }

    public static Result receiveWebhook() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            final SquareWebhook hook = mapper.readValue(request().body().asJson().toString(), SquareWebhook.class);
            if (SquareWebhook.EventType.PAYMENT_UPDATED == hook.event_type) {
                String lastWebhookEntityId = (String) play.cache.Cache.get(LAST_WEBHOOK_ENTITY_ID);
                if (hook.entity_id.equals(lastWebhookEntityId)) {
                    Logger.info("Ignoring square webhook with same entityId as the last: " +  hook.entity_id);
                } else {
                    play.cache.Cache.set(LAST_WEBHOOK_ENTITY_ID, hook.entity_id);
                    Promise<Payment> promisedPayment = Promise.promise(() -> getPayment(hook.entity_id));
                    promisedPayment.map(Slack::emitPaymentDetails);
                }
            } else {
                Logger.info("Discarding unsupported square webhook for " + hook.event_type.name());
            }
        } catch (IOException e) {
            Logger.error("Bad conversion of square webhook invocation", e);
        }

        return ok();
    }

}
