package utils;

import java.text.DecimalFormat;

/**
 * Created by cdelargy on 11/28/14.
 */
public class Formatter {
    public static String prettyDollars(Double dbl) {
        DecimalFormat df = new DecimalFormat("0.00");
        return ((dbl < 0)?"-$":"$") + df.format((null != dbl)?Math.abs(dbl):0.00);
    }
}
