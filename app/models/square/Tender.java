package models.square;

/**
 * Created by cdelargy on 12/25/14.
 */
public class Tender {

    public enum Type {
        //A credit card processed with Square.
        CREDIT_CARD,
        CASH,
        //A credit card processed with a card processor other than Square - This value applies only to merchants in countries where Square does not yet provide card processing.
        THIRD_PARTY_CARD,
        //No sale.
        NO_SALE,
        //A Square Wallet payment.
        SQUARE_WALLET,
        //A Square gift card.
        SQUARE_GIFT_CARD,
        //An unknown tender type.
        UNKNOWN,
        //A form of tender that does not match any other value.
        OTHER
    }

    public enum CardBrand {
        UNKNOWN, VISA, MASTER_CARD, AMERICAN_EXPRESS, DISCOVER, DISCOVER_DINERS, JCB
    }

    public enum EntryMethod {
        MANUAL, SCANNED, SQUARE_CASH, SQUARE_WALLET, SWIPED, WEB_FORM, OTHER
    }

    public String id;

    //The type of tender.
    public Type type;

    //A human-readable description of the tender.
    public String name;

    //The brand of credit card provided - Only present if the tender's type is CREDIT_CARD.
    public CardBrand card_brand;

    //The last four digits of the provided credit card's account number - Only present if the tender's type is CREDIT_CARD.
    public String pan_suffix;

    //The method with which the tender was entered.
    public EntryMethod entry_method;

    //Notes entered by the merchant about the tender at the time of payment, if any. Typically only present for tender with the type OTHER.
    public String payment_note;

    //The total amount of money provided in this form of tender.
    public Money total_money;

    //The amount of total_money applied to the payment.
    public Money tendered_money;

    //The amount of total_money returned to the buyer as change.
    public Money change_back_money;

    public Money refunded_money;

    public Money tip_money;

    public String receipt_url;

    public String employee_id;

    public Boolean is_exchange;
}
