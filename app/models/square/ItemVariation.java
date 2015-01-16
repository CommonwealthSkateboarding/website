package models.square;

/**
 * Created by cdelargy on 12/26/14.
 */
public class ItemVariation {
    public enum PricingType {
        FIXED_PRICING, VARIABLE_PRICING
    }

    public enum InventoryAlertType {
        LOW_QUANTITY, NONE
    }
    public String id;

    public String name;

    //The ID of the variation's associated item.
    public String item_id;

    //Indicates the variation's list position when displayed in Square Register and the merchant dashboard. If more than one variation for the same item has the same ordinal value, those variations are displayed in alphabetical order.
    //An item's variation with the lowest ordinal value is displayed first.
    public int ordinal;

    //Indicates whether the item variation's price is fixed or determined at the time of sale.
    public PricingType pricing_type;

    public Money price_money;

    public String sku;

    //If true, inventory tracking is active for the variation.
    public Boolean track_inventory;

    //Indicates whether the item variation displays an alert when its inventory quantity is less than or equal to its inventory_alert_threshold.
    public InventoryAlertType inventory_alert_type;

    //If the inventory quantity for the variation is less than or equal to this value and inventory_alert_type is LOW_QUANTITY, the variation displays an alert in the merchant dashboard.
    public Integer inventory_alert_threshold;

    //Arbitrary metadata associated with the variation. Cannot exceed 255 characters.
    public String user_data;
}
