import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  private final static String QUEUE_NAME = "postSkiers";
  private GenericObjectPool<Channel> channelPool;

  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    ChannelPoolFactory poolFactory = new ChannelPoolFactory(factory);
    channelPool = new GenericObjectPool<>(poolFactory);
  }


  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    String urlPath = request.getPathInfo();

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
    } else if (!isPayloadValid(payload)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.getWriter().write("Invalid request body. \"time\" and \"liftId\" required.");
    }

    else {
      response.setStatus(HttpServletResponse.SC_OK);

      Gson gson = new GsonBuilder()
          .registerTypeAdapter(LiftRide.class, new AnnotatedDeserializer<LiftRide>())
          .create();
      LiftRide liftRide = gson.fromJson(payload, LiftRide.class);
      String message = gson.toJson(liftRide);


      try {
        Channel channel = channelPool.borrowObject();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(" [x] Sent '" + message + "'");
        channelPool.returnObject(channel);
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to send message to queue");
      }

//      response.setContentType("application/json");
//      response.setCharacterEncoding("UTF-8");
//      response.getWriter().write("lift: " + liftRide.getLiftId());
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
      while ((line = reader.readLine()) != null)
        builder.append(line);
      return builder.toString();
    } catch (Exception e) {
      return "";
    }

  }

  private boolean isPayloadValid(String payload) {
    try {
      Gson gson = new GsonBuilder()
          .registerTypeAdapter(LiftRide.class, new AnnotatedDeserializer<LiftRide>())
          .create();
      LiftRide liftRide = gson.fromJson(payload, LiftRide.class);
      return true;
    } catch (Exception e) {
      return false;
    }

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
        Integer.parseInt(s[4]) <= 365 &&
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
