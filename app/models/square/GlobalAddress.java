package models.square;

/**
 * Created by cdelargy on 12/25/14.
 */
public class GlobalAddress {
    //The first line of the address.
    public String address_line_1;
    //The second line of the address, if any.
    public String address_line_2;
    //The third line of the address, if any.
    public String address_line_3;
    //The fourth line of the address, if any.
    public String address_line_4;
    //The fifth line of the address, if any.
    public String address_line_5;
    //The city or town of the address.
    public String locality;
    //A civil region within the address's locality, if any.
    public String sublocality;
    //A civil region within the address's sublocality, if any.
    public String sublocality_1;
    //A civil region within the address's sublocality_1, if any.
    public String sublocality_2;
    //A civil region within the address's sublocality_2, if any.
    public String sublocality_3;
    //A civil region within the address's sublocality_3, if any.
    public String sublocality_4;
    //A civil region within the address's sublocality_4, if any.
    public String sublocality_5;
    //A civil entity within the address's country. In the United States, this is the state.
    public String administrative_district_level_1;
    //A civil entity within the address's administrative_district_level_1, if any. In the United States, this is the county.
    public String administrative_district_level_2;
    //A civil entity within the address's administrative_district_level_2, if any.
    public String administrative_district_level_3;
    //The address's postal code.
    public String postal_code;
    //The address's country, in ISO 3166-1-alpha-2 format.
    public String country_code;
    //The coordinates of the address.
    public Coordinates address_coordinates;
}
