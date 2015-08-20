package models.square;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by cdelargy on 1/15/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {
    public String id;

    public String merchant_id;

    public String created_at;

    public String creator_id;

    public Device device;

    public String payment_url;

    public String receipt_url;

    public Money inclusive_tax_money;

    public Money additive_tax_money;

    public Money tax_money;

    public Money tip_money;

    public Money discount_money;

    public Money total_collected_money;

    public Money processing_fee_money;

    public Money net_total_money;

    public Money refunded_money;

    public PaymentTax[] inclusive_tax;

    public PaymentTax[] additive_tax;

    public Tender[] tender;

    public Refund[] refunds;

    public PaymentItemization[] itemizations;

    public Money swedish_rounding_money;

    public Money gross_sales_money;

    public Money net_sales_money;
}

