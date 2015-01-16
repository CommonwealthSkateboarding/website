package models.square;

/**
 * Created by cdelargy on 12/25/14.
 */
public class Money {
    public enum CurrencyCode {
        CAD,
        JPY,
        USD
    }
    public Double amount;
    public CurrencyCode currency_code;
}
