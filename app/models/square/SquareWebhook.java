package models.square;

/**
 * Created by cdelargy on 1/15/15.
 */
public class SquareWebhook {

    public enum EventType {
        PAYMENT_UPDATED
    }

    public String entity_id;

    public EventType event_type;

    public String merchant_id;
}