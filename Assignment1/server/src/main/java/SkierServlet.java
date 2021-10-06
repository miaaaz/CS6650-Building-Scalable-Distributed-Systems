import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet")
public class SkierServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request,
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

  private boolean isUrlValid(String[] urlParts) {
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]


    // sanity check
    if (urlParts == null) {
      return false;
    } else if (urlParts.length != 8 && urlParts.length != 3) {
      return false;
    }
    System.out.println("Validating urls");
    return isValid(Arrays.copyOfRange(urlParts, 1, urlParts.length)) || isValidVerticalCall(Arrays.copyOfRange(urlParts, 1, urlParts.length));
  }

  private boolean isValid(String[] s) {
    if (s.length != 7)
      return false;
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
    String regex = "[0-9]";
    if (s.length == 1)
      return s[0].matches(regex);
    else
      return s[0].matches(regex) && s[1].equals("Vertical");
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
