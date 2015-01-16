package models.square;

/**
 * Created by cdelargy on 12/26/14.
 */
public class Notification {

    public enum EventType {
        PAYMENT_UPDATED
    }

    public String merchant_id;
    public EventType event_type;
    public String entity_id;
}
