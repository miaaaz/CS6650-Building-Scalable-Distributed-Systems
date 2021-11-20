
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
import dao.ResortDAO;
import dynamoDB.DynamoDBDAO;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;



public class Consumer {

  private final static String QUEUE_NAME = "resortQueue";
  private final static int NUM_THREADS = 16;
  private static final String EXCHANGE_NAME = "skiers";

  // configure rabbitmq server
  private final static String HOST = "ec2-54-185-196-16.us-west-2.compute.amazonaws.com";
  private final static int PORT = 5672;
//  private final static String RABBITMQ_USERNAME = "admin";
//  private final static String RABBITMQ_PASSWORD = "admin";

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
    ResortDAO resortDAO = new ResortDAO();
//    DynamoDBDAO dynamoDBDAO = new DynamoDBDAO();

    Runnable runnable = () -> {
      try {
        final Channel channel = connection.createChannel();
//        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "resort");
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(5);
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");


          // process messages
          resortDAO.put(message);
//          dynamoDBDAO.put(message);
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
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
