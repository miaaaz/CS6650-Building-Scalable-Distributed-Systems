package servlet;

import java.util.concurrent.TimeUnit;
import model.LiftPayload;
import model.Message;
import org.apache.commons.lang3.concurrent.EventCountCircuitBreaker;
import util.AnnotatedDeserializer;
import util.ChannelPoolFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "SkierServlet", urlPatterns = "/skiers/*")
public class SkierServlet extends HttpServlet {

  // These two queues accept same messages
  private final static String SKIER_QUEUE = "skierQueue";
  private final static String RESORT_QUEUE = "resortQueue";

  // configure rabbitmq server
  private final static String HOST = "ec2-54-185-196-16.us-west-2.compute.amazonaws.com";
  private final static int PORT = 5672;
  private final static String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
  private final static String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");
  private static final String EXCHANGE_NAME = "skiers";
  private EventCountCircuitBreaker breaker;

  private GenericObjectPool<Channel> channelPool;
//  DynamoDB dynamoDB;

  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST);
    factory.setPort(PORT);

    factory.setUsername(RABBITMQ_USERNAME);
    factory.setPassword(RABBITMQ_PASSWORD);
    ChannelPoolFactory poolFactory = new ChannelPoolFactory(factory);

    breaker = new EventCountCircuitBreaker(10000, 1, TimeUnit.SECONDS, 9800);
    channelPool = new GenericObjectPool<>(poolFactory);



  }



  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {


    String urlPath = request.getPathInfo();
    System.out.println("Getting a request: " + urlPath);

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid inputs supplied");
      return;
    }

    String[] urlParts = urlPath.split("/");
    String payload = getPayload(request);
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid inputs supplied");
      System.out.println("Invalid inputs supplied");
    } else if (!isPayloadValid(payload)) {
      System.out.println(payload);
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request body. \"time\" and \"liftId\" required.");
      System.out.println("Invalid request body");
    } else {
//      response.setStatus(HttpServletResponse.SC_OK);
//
//      // Generate message
//      String message = generateMessage(urlParts, payload);
//
//      // Send message to queue
//      try {
//        Channel channel = channelPool.borrowObject();
//        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//
//        // For skier microservice
////        channel.queueDeclare(SKIER_QUEUE, true, false, false, null);
////        channel.queueBind(SKIER_QUEUE, EXCHANGE_NAME, "");
//        // For resort microservice
//        channel.queueDeclare(RESORT_QUEUE, true, false, false, null);
//        channel.queueBind(RESORT_QUEUE, EXCHANGE_NAME, "");
//
//        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
//        System.out.println(" [x] Sent '" + message + "'");
//        channelPool.returnObject(channel);
//      } catch (Exception e) {
//        e.printStackTrace();
//        System.out.println("Failed to send message to queue");
//      }
//
//      response.setContentType("application/json");
//      response.setCharacterEncoding("UTF-8");
//      response.getWriter().write("Successfully POST");
      if (breaker.incrementAndCheckState()) {
        response.setStatus(HttpServletResponse.SC_OK);

        // Generate message
        String message = generateMessage(urlParts, payload);

        // Send message to queue
        try {
          Channel channel = channelPool.borrowObject();
          channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

          // For skier microservice
          channel.queueDeclare(SKIER_QUEUE, true, false, false, null);
          channel.queueBind(SKIER_QUEUE, EXCHANGE_NAME, "");
          // For resort microservice
          channel.queueDeclare(RESORT_QUEUE, true, false, false, null);
          channel.queueBind(RESORT_QUEUE, EXCHANGE_NAME, "");

          channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
          System.out.println(" [x] Sent '" + message + "'");
          channelPool.returnObject(channel);
        } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Failed to send message to queue");
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("Successfully POST");
      } else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }

    }

  }

  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();

    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid inputs supplied");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid inputs supplied");
    } else {
      response.setStatus(HttpServletResponse.SC_OK);

      // process url params in `urlParts`
      // /skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
      if (urlParts.length == 8) {
        String resortId = urlParts[1];
        String seasonId = urlParts[3];
        String dayId = urlParts[5];
        String skierId = urlParts[7];
        // return dummy data as response body for assignment 1
        String message = "Resort: " + resortId + "\nSeason: " + seasonId
            + "\nDay: " + dayId + "\nSkier: " + skierId;
        response.getWriter().write(message);
      }

    }

  }

  private String getPayload(HttpServletRequest request) {
    StringBuilder builder = new StringBuilder();
    String line;
    try {
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null) {
        builder.append(line);
      }
      return builder.toString();
    } catch (Exception e) {
      return "";
    }

  }

  private boolean isPayloadValid(String payload) {
    try {
      Gson gson = new GsonBuilder()
          .registerTypeAdapter(LiftPayload.class, new AnnotatedDeserializer<LiftPayload>())
          .create();
      LiftPayload liftRide = gson.fromJson(payload, LiftPayload.class);
      return true;
    } catch (Exception e) {
      return false;
    }

  }

  private String generateMessage(String[] urlParts, String payload) {
    int resortId = Integer.parseInt(urlParts[1]);
    String seasonId = urlParts[3];
    String dayId = urlParts[5];
    int skierId = Integer.parseInt(urlParts[7]);

    Gson gson = new GsonBuilder()
        .registerTypeAdapter(LiftPayload.class, new AnnotatedDeserializer<LiftPayload>())
        .create();
    LiftPayload liftPayload = gson.fromJson(payload, LiftPayload.class);

    Message message = new Message(skierId, dayId, seasonId, resortId, liftPayload.getLiftId(),
        liftPayload.getTime());
    return new Gson().toJson(message);

  }

  private boolean isUrlValid(String[] urlParts) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]

    // sanity check
    if (urlParts == null) {
      return false;
    } else if (urlParts.length != 8 && urlParts.length != 3) {
      return false;
    }

    return isValid(Arrays.copyOfRange(urlParts, 1, urlParts.length)) || isValidVerticalCall(
        Arrays.copyOfRange(urlParts, 1, urlParts.length));
  }

  private boolean isValid(String[] s) {
    if (s.length != 7) {
      return false;
    }
    return isNumber(s[0]) &&
        s[1].equals("seasons") &&
        s[3].equals("days") &&
        isNumber(s[4]) &&
        Integer.parseInt(s[4]) >= 1 &&
        Integer.parseInt(s[4]) <= 366 &&
        s[5].equals("skiers") &&
        isNumber(s[6]);
  }

  private boolean isValidVerticalCall(String[] s) {
    if (s.length == 0 || s.length > 2) {
      return false;
    }
//    String regex = "[0-9]";
    if (s.length == 1) {
      return isNumber(s[0]);
    } else {
      return isNumber(s[0]) && s[1].equals("vertical");
    }
  }

  private boolean isNumber(String s) {
    if (s == null) {
      return false;
    }
    try {
      double d = Integer.parseInt(s);
    } catch (Exception nfe) {
      return false;
    }
    return true;
  }
}
