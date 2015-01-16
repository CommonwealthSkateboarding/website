package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.square.Order;
import models.square.SquareWebhook;
import play.Logger;
import play.api.libs.json.Json;
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
    // Headers:
        // Authorization: Bearer YOUR_ACCESS_TOKEN
        // Accept: application/json
        // qdq0cRgmvgFbSJmcrIsKJQ

    private static String BASE_URL = "https://connect.squareup.com/v1/me/";
    private static String INVENTORY_URL = BASE_URL + "inventory";

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

        System.out.println(json);
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

    public static Result receiveWebhook() {
        JsonNode json = request().body().asJson();
        if(json == null) {
            return badRequest("Expecting Json data");
        } else {
            SquareWebhook hook = null;
            try {
                ObjectMapper mapper = new ObjectMapper();
                hook = mapper.readValue(json.asText(), SquareWebhook.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Logger.info("entity id: " + hook.entity_id);
        }

    }

}
