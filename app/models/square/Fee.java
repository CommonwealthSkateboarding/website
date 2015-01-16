package models.square;

/**
 * Created by cdelargy on 12/26/14.
 */
public class Fee {
    public enum CalculationPhase {
        FEE_SUBTOTAL_PHASE, FEE_TOTAL_PHASE, OTHER
    }
    public enum AdjustmentType {
        TAX
    }
    public enum InclusionType {
        ADDITIVE, INCLUSIVE
    }

    public enum FeeType {
        CA_GST, CA_HST, CA_PEI_PST, CA_PST, CA_QST, JP_CONSUMPTION_TAX, US_SALES_TAX, OTHER
    }
    public String id;
    public String name;
    // value in form of x.xx i.e. 0.07 = 7%
    public String rate;
    public CalculationPhase calculation_phase;

    //The type of adjustment the fee applies to a payment. Currently, this value is TAX for all fees.
    public AdjustmentType adjustment_type;

    public Boolean applies_to_custom_amounts;

    public Boolean enabled;

    public InclusionType inclusion_type;

    public FeeType type;
}
