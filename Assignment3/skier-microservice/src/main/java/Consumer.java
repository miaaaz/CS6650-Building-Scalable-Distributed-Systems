
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


    SkierDAO skierDAO = new SkierDAO();

    Runnable runnable = () -> {
      try {
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(5);
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

          // process messages
          skierDAO.put(message);


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



  };








}
