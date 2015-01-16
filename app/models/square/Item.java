package models.square;

import java.util.Locale;

/**
 * Created by cdelargy on 12/26/14.
 */
public class Item {

    public enum Visibility {
        PUBLIC, PRIVATE
    }

    public String id;

    public String name;

    public String description;

    //The color of the item's display label in Square Register, if not the default color.
    public String color;

    //Indicates whether the item is viewable in Square Market (PUBLIC) or PRIVATE.
    public Visibility visibility;

    //If true, the item is available for purchase from Square Market.
    Boolean available_online;

    //The item's master image, if any.
    public ItemImage master_image;

    //The category the item belongs to, if any.
    public Category category;

    //The item's variations.
    public ItemVariation[] variations;

    //The modifier lists that apply to the item, if any.
    public ModifierList[] modifier_lists;

    //The fees that apply to the item, if any.
    public Fee[] fees;

    //Deprecated. This field is not used.
    public Boolean taxable;
}
