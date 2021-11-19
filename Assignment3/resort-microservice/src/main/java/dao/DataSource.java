package dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DirContextDnsResolver;

public class DataSource {

  private static final String HOST = "redis://redis.sjlr22.ng.0001.usw2.cache.amazonaws.com:6379/1";
  private final StatefulRedisConnection<String, String> redisConnection;
  private final RedisClient redisClient;

  public DataSource() {
    DefaultClientResources clientResources = DefaultClientResources.builder() //
        .dnsResolver(new DirContextDnsResolver()) // Does not cache DNS lookups
        .build();

    // 0 is the database number
    redisClient = RedisClient.create(clientResources, HOST);
    redisConnection = redisClient.connect();
  }

  public RedisAsyncCommands<String, String> getRedisCommand() {
    return this.redisConnection.async();
  }

  public void closeDataSource() {
    redisConnection.close();
    redisClient.shutdown();
  }
}
