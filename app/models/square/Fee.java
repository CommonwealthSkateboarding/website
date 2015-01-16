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
    public String id;
    public String name;
    // value in form of x.xx i.e. 0.07 = 7%
    public String rate;
    public CalculationPhase calculation_phase;
    public

}
