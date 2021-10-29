import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {

  private final static String QUEUE_NAME = "postSkiers";
  private final static int NUM_THREADS = 16;

  private final static String HOST = "ec2-34-210-182-60.us-west-2.compute.amazonaws.com";
  private final static int PORT = 5672;
  private final static String RABBITMQ_USERNAME = "admin";
  private final static String RABBITMQ_PASSWORD = "admin";


  public static void main(String[] args) throws IOException, TimeoutException {
    Map<Integer, List<LiftRide>> liftRideMap = new ConcurrentHashMap<>();

    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(HOST);
    factory.setPort(PORT);
    factory.setUsername(RABBITMQ_USERNAME);
    factory.setPassword(RABBITMQ_PASSWORD);
    final Connection connection = factory.newConnection();

    Runnable runnable = () -> {
      try {
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        channel.basicQos(5);
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          System.out.println(
              "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message
                  + "'");

          // process messages
          Gson gson = new Gson();
          Message messageObject = gson.fromJson(message, Message.class);
          int skierId = messageObject.getSkierID();
          LiftRide liftRide = messageObject.getBody();

          if (!liftRideMap.containsKey(skierId)) {
            liftRideMap.put(skierId, new ArrayList<>());
          }
          List<LiftRide> rides = liftRideMap.get(skierId);
          rides.add(liftRide);


        };

        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
      } catch (IOException ex) {
        Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
      }
    };

    // start threads to receive messages
    for (int i = 0; i < NUM_THREADS; i++) {
      new Thread(runnable).start();
    }



  };








}
