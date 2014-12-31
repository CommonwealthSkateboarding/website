package utils;

import models.skatepark.Membership;
import play.Logger;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private static String hex(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < array.length; ++i) {
            sb.append(Integer.toHexString((array[i]
                    & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }
    private static String md5Hex (String message) {
        try {
            MessageDigest md =
                    MessageDigest.getInstance("MD5");
            return hex(md.digest(message.getBytes("CP1252")));
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Cannot create md5 hash", e);
        } catch (UnsupportedEncodingException e) {
            Logger.error("Cannot create md5 hash", e);
        }
        return null;
    }

    public static String getGravatarUrl(Membership membership) {
        if (null != membership && null != membership.email) {
            return "http://www.gravatar.com/avatar/" + md5Hex(membership.email);
        }
        return null;
    }
}
