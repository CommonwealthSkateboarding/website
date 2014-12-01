package utils;

import models.skatepark.Visit;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cdelargy on 11/29/14.
 */
public class ChartUtil {

    static String expectedPattern = "yyyy-MM-dd";
    static SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);

    public static HashMap<String, HashMap<String, Integer>> breakDownVisitsToDays(List<Visit> visits) {
        HashMap<String, HashMap<String, Integer>> map = new HashMap<String, HashMap<String, Integer>>();
        for (Visit visit : visits) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(DateUtils.truncate(visit.time, Calendar.DATE));
            String weekDay = formatter.format(cal1.getTime());
            map.putIfAbsent(weekDay, new HashMap<String, Integer>());
            map.get(weekDay).putIfAbsent(visit.visitType.name(), 0);
            map.get(weekDay).put(visit.visitType.name(), (map.get(weekDay).get(visit.visitType.name()) + 1));
        }
        return map;
    }
}
