
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import dao.SkierDAO;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DirContextDnsResolver;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.LiftDetail;
import model.LiftRide;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
//import org.redisson.Redisson;
//import org.redisson.api.RBucket;
//import org.redisson.api.RMap;
//import org.redisson.api.RSetMultimap;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
import io.lettuce.core.*;


public class Consumer {

  private final static String QUEUE_NAME = "skierQueue";
  private final static int NUM_THREADS = 16;
  private static final String EXCHANGE_NAME = "skiers";

  // configure rabbitmq server
  private final static String HOST = "ec2-54-185-196-16.us-west-2.compute.amazonaws.com";
  private final static int PORT = 5672;

  private final static String RABBITMQ_USERNAME = System.getenv("RABBITMQ_USERNAME");
  private final static String RABBITMQ_PASSWORD = System.getenv("RABBITMQ_PASSWORD");


  public static void main(String[] args) throws IOException, TimeoutException {

    // connect to rabbitMQ
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST);
    factory.setPort(PORT);
    factory.setUsername(RABBITMQ_USERNAME);
    factory.setPassword(RABBITMQ_PASSWORD);
    final Connection connection = factory.newConnection();



    // redis
    SkierDAO skierDAO = new SkierDAO();

    Runnable runnable = () -> {
      try {
        final Channel channel = connection.createChannel();
//        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        String queueName = channel.queueDeclare().getQueue();
//        channel.queueBind(queueName, EXCHANGE_NAME, "");
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(5);
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//          System.out.println(
//              "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message
//                  + "'");

          // process messages
          skierDAO.put(message);


//          Gson gson = new Gson();
//          LiftDetail liftDetail = gson.fromJson(message, LiftDetail.class);
//          String detailJson = gson.toJson(liftDetail);
//          LiftRide messageObject = gson.fromJson(message, LiftRide.class);
//          String skierID = String.valueOf(messageObject.getSkierID());
//          String liftID = String.valueOf(messageObject.getLiftID());
//          jedis.hset(skierID, liftID, message);
//          redisClient.setDefaultTimeout(5, TimeUnit.SECONDS);
//          RedisClient redisClient = RedisClient.create(clientResources, "redis://redis.sjlr22.ng.0001.usw2.cache.amazonaws.com:6379/0");
//
//          StatefulRedisConnection<String, String> redisConnection = redisClient.connect();
//          RedisAsyncCommands<String, String> commands = redisConnection.async();

//          commands.set(skierID, detailJson);

//          commands.hset(skierID, liftID, detailJson);
//
//          System.out.println(skierID);
//          System.out.println(liftID);
//          redisConnection.close();
//          redisClient.shutdown();
//          try (Jedis jedis = pool.getResource()) {
//            /// ... do stuff here ... for example
//            jedis.set(skierID, "12");
//            String foobar = jedis.get(skierID);
//            System.out.println(foobar);
//          }
//
//          pool.close();

//          int skierID = messageObject.getSkierID();
//          RSetMultimap<Integer, LiftRide> skierDB = redisson.getSetMultimap("skier");
////          RMap<String, String> map = redisson.getMap("anyMap");
//          System.out.println(skierDB);
////          System.out.println(gson.toJson(messageObject));
//          System.out.println("123");


//          skierDB.put(skierID, messageObject);
//
//          System.out.println("Added: " + message);
//          SkierDAO skierDAO = new SkierDAO();
//          skierDAO.put(messageObject);


        };


        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
      } catch (IOException ex) {
        System.out.println(ex.getMessage());
        Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
      }
    };

    // start threads to receive messages
    for (int i = 0; i < NUM_THREADS; i++) {
      new Thread(runnable).start();
    }

    // Clean up
    skierDAO.close();
//    redisConnection.close();
//    redisClient.shutdown();

//    redisson.shutdown();



  };








}
