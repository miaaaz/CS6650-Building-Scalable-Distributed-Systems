package dao;

import com.google.gson.Gson;
import io.lettuce.core.api.async.RedisAsyncCommands;
import java.util.HashMap;
import javax.xml.crypto.Data;
import model.LiftDetail;
import model.LiftRide;

public class SkierDAO {

  private DataSource dataSource;
  private RedisAsyncCommands<String, String> commands;

  public SkierDAO() {
    dataSource = new DataSource();

  }

  public void put(String message) {

    dataSource = new DataSource();
    commands = dataSource.getRedisCommand();
    Gson gson = new Gson();
    LiftDetail liftDetail = gson.fromJson(message, LiftDetail.class);
    String detailJson = gson.toJson(liftDetail);
    LiftRide messageObject = gson.fromJson(message, LiftRide.class);
    String skierID = String.valueOf(messageObject.getSkierID());
    String liftID = String.valueOf(messageObject.getLiftID());
    commands.hset(skierID, liftID, detailJson);
    System.out.println("Put " + message);


  }

  public void close() {
    this.dataSource.closeDataSource();
  }
}
