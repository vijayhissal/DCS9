import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class ESBBroker {
    // Topic -> List of Subscriber callback URLs
    static Map<String, List<String>> topics = new HashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Endpoint 1: Subscribers register to a Topic
        server.createContext("/subscribe", ex -> {
            String[] q = ex.getRequestURI().getQuery().split("&"); // ?topic=news&url=http://localhost:8001/notify
            String topic = q[0].split("=")[1];
            String subUrl = q[1].split("=")[1];

            topics.computeIfAbsent(topic, k -> new ArrayList<>()).add(subUrl);
            System.out.println("[ESB] Subscribed " + subUrl + " to Topic: " + topic);
            sendResponse(ex, "Subscribed to " + topic);
        });

        // Endpoint 2: Publisher publishes a message to a Topic
        server.createContext("/publish", ex -> {
            String[] q = ex.getRequestURI().getQuery().split("&"); // ?topic=news&msg=Hello
            String topic = q[0].split("=")[1];
            String msg = URLDecoder.decode(q[1].split("=")[1], "UTF-8");

            System.out.println("[ESB] Routing message on '" + topic + "': " + msg);
            List<String> subs = topics.getOrDefault(topic, Collections.emptyList());

            // Push message to every subscriber of this topic
            for (String subUrl : subs) {
                try {
                    new URL(subUrl + "?msg=" + URLEncoder.encode(msg, "UTF-8")).openStream().close();
                } catch (Exception e) {
                    System.out.println("[ESB] Failed delivery to " + subUrl);
                }
            }
            sendResponse(ex, "Delivered to " + subs.size() + " subscribers.");
        });

        server.start();
        System.out.println("ESB / JMS Topic Broker running on port 8000...");
    }

    static void sendResponse(HttpExchange ex, String resp) throws IOException {
        ex.sendResponseHeaders(200, resp.length());
        ex.getResponseBody().write(resp.getBytes());
        ex.close();
    }
}