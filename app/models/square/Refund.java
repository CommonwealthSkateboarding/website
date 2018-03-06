package models.square;

/**
 * Created by cdelargy on 1/15/15.
 */
public class Refund {
    public enum RefundType {
        FULL, PARTIAL
    }

    public RefundType type;

    public String reason;

    public Money refunded_money;
    
    public Money refunded_processing_fee_money;

    public String created_at;

    public String processed_at;

    public String payment_id;

    public String merchant_id;
}
