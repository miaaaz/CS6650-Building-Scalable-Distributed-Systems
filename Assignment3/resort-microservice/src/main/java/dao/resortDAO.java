package dao;

import com.google.gson.Gson;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.HashMap;
import javax.xml.crypto.Data;
import model.LiftDetail;
import model.LiftRide;

public class resortDAO {

  private final DataSource dataSource;
  private final RedisAsyncCommands<String, String> commands;

  public resortDAO() {
    dataSource = new DataSource();
    commands = dataSource.getRedisCommand();
  }

  public void put(String message) {

    Gson gson = new Gson();
    LiftDetail liftDetail = gson.fromJson(message, LiftDetail.class);
    String detailJson = gson.toJson(liftDetail);
    LiftRide messageObject = gson.fromJson(message, LiftRide.class);
    String skierID = String.valueOf(messageObject.getSkierID());
    String liftID = String.valueOf(messageObject.getLiftID());
    commands.hset(skierID, liftID, detailJson);
    System.out.println("Added " + message);


  }

  public void close() {
    this.dataSource.closeDataSource();
  }
}
