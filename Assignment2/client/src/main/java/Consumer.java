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


  public static void main(String[] args) throws IOException, TimeoutException {
    Map<Integer, List<LiftRide>> liftRideMap = new ConcurrentHashMap<>();
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
    final Connection connection = factory.newConnection();

    Runnable runnable = () -> {
      try {
        final Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        // max one message per receiver
        channel.basicQos(1);
        System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          System.out.println(
              "Callback thread ID = " + Thread.currentThread().getId() + " Received '" + message
                  + "'");
          Gson gson = new Gson();
          Message messageObject = gson.fromJson(message, Message.class);
          int skierId = messageObject.getSkierId();
          LiftRide liftRide = messageObject.getBody();

          if (!liftRideMap.containsKey(skierId)) {
            liftRideMap.put(skierId,  new ArrayList<>());
          }
          List<LiftRide> rides = liftRideMap.get(skierId);
          rides.add(liftRide);

          System.out.println(liftRideMap);

        };
        // process messages
        channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
      } catch (IOException ex) {
        Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
      }
    };

    // start threads and block to receive messages
    Thread recv1 = new Thread(runnable);
    Thread recv2 = new Thread(runnable);
    recv1.start();
    recv2.start();
  }
}
