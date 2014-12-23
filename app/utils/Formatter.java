package utils;

import java.text.DecimalFormat;

/**
 * Created by cdelargy on 11/28/14.
 */
public class Formatter {
    public static String prettyDollars(Double dbl) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (null != dbl) {
            return ((dbl < 0)?"-$":"$") + df.format(Math.abs(dbl));
        } else {
            return ("$0.00");
        }
    }
}
