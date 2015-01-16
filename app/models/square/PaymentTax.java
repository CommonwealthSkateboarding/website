package models.square;

/**
 * Created by cdelargy on 1/15/15.
 */
public class PaymentTax {
    public String name;
    public Money applied_money;
    public String rate;
    public Fee.InclusionType inclusion_type;
    public String fee_id;
}
