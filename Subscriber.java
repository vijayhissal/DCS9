import com.sun.net.httpserver.*;
import java.net.*;

public class Subscriber {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        String topic = args[1];

        // 1. Start local Web Service callback to receive pushed events
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/notify", ex -> {
            String msg = URLDecoder.decode(ex.getRequestURI().getQuery().split("=")[1], "UTF-8");
            System.out.println("[Subscriber " + port + " | Topic: " + topic + "] Received: " + msg);
            ex.sendResponseHeaders(200, 2);
            ex.getResponseBody().write("OK".getBytes());
            ex.close();
        });
        server.start();

        // 2. Register with ESB Broker
        String regUrl = "http://localhost:8000/subscribe?topic=" + topic + "&url=http://localhost:" + port + "/notify";
        new URL(regUrl).openStream().close();
        System.out.println("Subscriber " + port + " subscribed to '" + topic + "'...");
    }
}