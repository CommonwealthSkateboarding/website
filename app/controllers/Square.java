package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.square.Order;
import models.square.Payment;
import models.square.SquareWebhook;
import play.Logger;
import play.libs.ws.*;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cdelargy on 12/18/14.
 */
public class Square extends Controller {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_TOKEN = "Bearer " + play.Play.application().configuration().getString("square.token");

    private static String BASE_URL = "https://connect.squareup.com/v1/me/";
    private static String INVENTORY_URL = BASE_URL + "inventory";
    private static String PAYMENT_URL = BASE_URL + "payments/";


    private static String ORDERS_URL = BASE_URL + "orders?order=DESC";

    //https://connect.squareup.com/v1/me/inventory

    public static Promise<JsonNode> getInventory() {
        WSRequestHolder holder = WS.url(INVENTORY_URL).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        return holder.get().map(response -> response.asJson());
    }

    public static List<Order> getOrders() {
        WSRequestHolder holder = WS.url(ORDERS_URL).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        WSResponse response = holder.get().get(1000);
        ArrayList<Order> orders = new ArrayList<>();
        if (response.getStatus() != OK) {
            Logger.error("Got bad response from square when getting orders: " + response.getBody().toString());
        } else {
            JsonNode json = response.asJson();
            ObjectMapper mapper = new ObjectMapper();
            Iterator<JsonNode> iter = json.elements();

            while (iter.hasNext()) {
                Order order = null;
                try {
                    order = mapper.readValue(iter.next().asText(), Order.class);
                } catch (IOException e) {
                    Logger.error("Unable to iterate through square orders", e);
                }
                orders.add(order);
            }
        }
        return orders;
    }

    private static Payment getPayment(String paymentId) {
        WSRequestHolder holder = WS.url(PAYMENT_URL + paymentId).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        WSResponse response = holder.get().get(10000);
        Payment payment = null;
        if (response.getStatus() != OK) {
            Logger.error("Got bad response from square when getting payment: " + response.getBody().toString());
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                payment = mapper.readValue(response.getBody(), Payment.class);
            } catch (IOException e) {
                Logger.error("Bad conversion of square payment (perhaps token not configured?)", e);
            }
        }
        return payment;
    }

    public static Result receiveWebhook() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            final SquareWebhook hook = mapper.readValue(request().body().asJson().toString(), SquareWebhook.class);
            Promise<Payment> promisedPayment = Promise.promise(() -> getPayment(hook.entity_id));
            promisedPayment.map(Slack::emitPaymentDetails);
        } catch (IOException e) {
            Logger.error("Bad conversion of square webhook invocation", e);
        }

        return ok();
    }

}
