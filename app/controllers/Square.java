package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.square.Order;
import models.square.Payment;
import models.square.PaymentItemization;
import models.square.SquareWebhook;
import play.Logger;
import play.api.libs.json.Json;
import play.libs.ws.*;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        JsonNode json = response.asJson();
        ObjectMapper mapper = new ObjectMapper();
        Iterator<JsonNode> iter = json.elements();
        ArrayList<Order> orders = new ArrayList<Order>();
        while (iter.hasNext()) {
            Order order = null;
            try {
                order = mapper.readValue(iter.next().asText(), Order.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            orders.add(order);
        }
        return orders;
    }

    private static Payment getPayment(String paymentId) {
        WSRequestHolder holder = WS.url(PAYMENT_URL + paymentId).setHeader(AUTHORIZATION_HEADER, BEARER_TOKEN);
        WSResponse response = holder.get().get(10000);
        //System.out.println(response.getBody());
        Payment payment = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            payment = mapper.readValue(response.getBody(), Payment.class);
        } catch (IOException e) {
            Logger.error("Bad conversion of square payment", e);
        }
        return payment;
    }

    public static Result receiveWebhook() {
        SquareWebhook hook = null;
        Logger.info("Receiving webhook: " + request().body().asJson().asText());
        try {
            ObjectMapper mapper = new ObjectMapper();
            hook = mapper.readValue(request().body().asJson().asText(), SquareWebhook.class);
        } catch (IOException e) {
            Logger.error("Bad conversion of square webhook invocation", e);
        }
        Payment payment = getPayment(hook.entity_id);
        Slack.emitPaymentDetails(payment);
        return ok();
    }

}
