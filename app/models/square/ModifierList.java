package models.square;

/**
 * Created by cdelargy on 12/26/14.
 */
public class ModifierList {

    public enum SelectionType {
        SINGLE, MULTIPLE
    }
    public String id;
    public String name;
    public SelectionType selection_type;
    public ModifierOption[] modifier_options;
}
