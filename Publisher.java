import java.net.*;
import java.util.*;

public class Publisher {
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Topic (e.g., news): ");
        String topic = sc.nextLine();
        System.out.print("Enter Message: ");
        String msg = sc.nextLine();

        String pubUrl = "http://localhost:8000/publish?topic=" + topic + "&msg=" + URLEncoder.encode(msg, "UTF-8");
        String reply = new String(new URL(pubUrl).openStream().readAllBytes());
        System.out.println("[Publisher] ESB Reply: " + reply);
        sc.close();
    }
}