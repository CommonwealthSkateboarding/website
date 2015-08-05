package models.square;

/**
 * Created by cdelargy on 1/15/15.
 */
public class PaymentItemization {
    public String name;

    public Double quantity;

    public PaymentItemDetail item_detail;

    public String notes;

    public String item_variation_name;

    public Money total_money;

    public Money single_quantity_money;

    public Money gross_sales_money;

    public Money discount_money;

    public Money net_sales_money;

    public PaymentTax[] taxes;

    public PaymentDiscount[] discounts;

    public PaymentModifier[] modifiers;

    public String itemization_type;
}
