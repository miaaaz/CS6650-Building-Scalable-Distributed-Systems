package dao;

import com.google.gson.Gson;
import io.lettuce.core.api.async.RedisAsyncCommands;
import model.LiftDetail;
import model.LiftRide;

public class ResortDAO {

  private final DataSource dataSource;
  private final RedisAsyncCommands<String, String> commands;

  public ResortDAO() {
    dataSource = new DataSource();
    commands = dataSource.getRedisCommand();
  }

  public void put(String message) {

    Gson gson = new Gson();
    LiftDetail liftDetail = gson.fromJson(message, LiftDetail.class);
    String detailJson = gson.toJson(liftDetail);
    LiftRide messageObject = gson.fromJson(message, LiftRide.class);
    // keys
    String resortID = String.valueOf(messageObject.getResortID());
    String dayID = String.valueOf(messageObject.getDayID());

    commands.hset(resortID, dayID, detailJson);
    System.out.println("Added " + message);


  }

  public void close() {
    this.dataSource.closeDataSource();
  }
}
