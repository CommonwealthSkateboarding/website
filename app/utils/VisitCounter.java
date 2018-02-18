package utils;
import models.skatepark.Visit;
import play.libs.F;
import play.mvc.*;

import java.util.*;
public class VisitCounter {

    // collect all websockets here
    private static List<WebSocket.Out<String>> connections = new ArrayList<WebSocket.Out<String>>();
    private static int visits = Visit.countSince2015();

    public static void start(WebSocket.In<String> in, WebSocket.Out<String> out){
        out.write(Integer.toString(visits));

        connections.add(out);

        in.onClose(() -> connections.remove(out));
    }

    // Iterate connection list and write visit count
    public static void newVisit(){
        visits++;
        for (WebSocket.Out<String> out : connections) {
            out.write(Integer.toString(visits));
        }
    }
}
