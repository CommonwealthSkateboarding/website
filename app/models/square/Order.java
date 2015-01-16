package models.square;

import java.util.Arrays;

/**
 * Created by cdelargy on 12/25/14.
 */
public class Order {

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", buyer_email='" + buyer_email + '\'' +
                ", recipient_name='" + recipient_name + '\'' +
                ", recipient_phone_number='" + recipient_phone_number + '\'' +
                ", shipping_address=" + shipping_address +
                ", subtotal_money=" + subtotal_money +
                ", total_shipping_money=" + total_shipping_money +
                ", total_tax_money=" + total_tax_money +
                ", total_price_money=" + total_price_money +
                ", total_discount_money=" + total_discount_money +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", expires_at='" + expires_at + '\'' +
                ", payment_id='" + payment_id + '\'' +
                ", buyer_note='" + buyer_note + '\'' +
                ", completed_note='" + completed_note + '\'' +
                ", refunded_note='" + refunded_note + '\'' +
                ", canceled_note='" + canceled_note + '\'' +
                ", tender=" + tender +
                ", order_history=" + Arrays.toString(order_history) +
                ", promo_code='" + promo_code + '\'' +
                ", btc_receive_address='" + btc_receive_address + '\'' +
                ", btc_price_satoshi=" + btc_price_satoshi +
                '}';
    }

    public enum State {
        //The order has been created and is awaiting validation from Square.
        PENDING,
        //The order has been validated and is ready to be completed by the merchant.
        OPEN,
        //The merchant shipped or otherwise fulfilled the order.
        COMPLETED,
        //The merchant canceled the order or didn't act on it within a required time period.
        CANCELED,
        //The merchant refunded a previously COMPLETED order.
        REFUNDED,
        //The order cannot be processed because it was rejected by Square, or because it has been too long since the order was created
        REJECTED
    }

    //The order's unique identifier.
    public String id;

    //The order's current state, such as OPEN or COMPLETED.
    public State state;

    //The email address of the order's buyer.
    public String buyer_email;

    //The name of the order's buyer.
    public String recipient_name;

    //The phone number to use for the order's delivery.
    public String recipient_phone_number;

    //The address to ship the order to.
    public GlobalAddress shipping_address;

    //The amount of all items purchased in the order, before taxes and shipping.
    public Money subtotal_money;

    //The shipping cost for the order.
    public Money total_shipping_money;

    //The total of all taxes applied to the order.
    public Money total_tax_money;

    //The total cost of the order.
    public Money total_price_money;

    //The total of all discounts applied to the order.
    public Money total_discount_money;

    //The time when the order was created, in ISO 8601 format.
    public String created_at;

    //The time when the order was last modified, in ISO 8601 format.
    public String updated_at;

    //The time when the order expires if no action is taken, in ISO 8601 format.
    public String expires_at;

    //The unique identifier of the payment associated with the order.
    public String payment_id;

    //A note provided by the buyer when the order was created, if any.
    public String buyer_note;

    //A note provided by the merchant when the order's state was set to COMPLETED, if any.
    public String completed_note;

    //A note provided by the merchant when the order's state was set to REFUNDED, if any.
    public String refunded_note;

    //A note provided by the merchant when the order's state was set to CANCELED, if any.
    public String canceled_note;

    //The tender used to pay for the order.
    public Tender tender;

    //The history of actions associated with the order.
    public OrderHistoryEntry[] order_history;

    //The promo code provided by the buyer, if any.
    public String promo_code;

    //For Bitcoin transactions, the address that the buyer sent Bitcoin to.
    public String btc_receive_address;

    //For Bitcoin transactions, the price of the buyer's order in satoshi (100 million satoshi equals 1 BTC).
    public Long btc_price_satoshi;
}
