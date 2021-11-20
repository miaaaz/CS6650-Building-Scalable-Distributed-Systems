package dynamoDB;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.google.gson.Gson;
import model.LiftRide;

public class DynamoDBDAO {

  final Table table;

  public DynamoDBDAO() {
    this.table = new DynamoDBSource().getTable();
  }

  public void put(String message) {
    Gson gson = new Gson();

    LiftRide messageObject = gson.fromJson(message, LiftRide.class);
    // keys
    int skierID = messageObject.getSkierID();
    int dayID = Integer.parseInt(messageObject.getDayID());
    int seasonID = Integer.parseInt(messageObject.getSeasonID());
    int resortID = messageObject.getResortID();
    int liftID = messageObject.getLiftID();
    int time = messageObject.getTime();

    System.out.println(table.getTableName());
    Item item = new Item().withPrimaryKey("resortID", resortID)
        .withPrimaryKey("dayID", dayID)
        .withNumber("skierID", skierID)
        .withNumber("seasonID", seasonID)
        .withNumber("liftID",liftID)
        .withNumber("time", time);

    table.putItem(item);
    System.out.println("Added " + message);
  }

}
