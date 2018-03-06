package models.square;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by cdelargy on 1/15/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Refund {
    public enum RefundType {
        FULL, PARTIAL
    }

    public RefundType type;

    public String reason;

    public Money refunded_money;

    public Money refunded_processing_fee_money;

    public Money refunded_additive_tax_money;

    public Money refunded_inclusive_tax_money;

    public Money refunded_tip_money;

    public String created_at;

    public String processed_at;

    public String payment_id;

    public String merchant_id;
}
