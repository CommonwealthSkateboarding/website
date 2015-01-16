package models.square;

/**
 * Created by cdelargy on 12/25/14.
 */
public class OrderHistoryEntry {

    public enum ActionType {
        //The buyer initiated the order.
        ORDER_PLACED,
        //The order was rejected by Square.
        DECLINED,
        //The order's associated payment was processed.
        PAYMENT_RECEIVED,
        //The merchant canceled the order.
        CANCELED,
        //The merchant shipped or otherwise fulfilled the order.
        COMPLETED,
        //The merchant refunded the order.
        REFUNDED,
        EXPIRED
    }

    public ActionType action;

    public String created_at;
}
