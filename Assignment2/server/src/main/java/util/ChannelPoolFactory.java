import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelPoolFactory implements PooledObjectFactory<Channel> {

  private ConnectionFactory connectionFactory;

  public ChannelPoolFactory(ConnectionFactory factory) {
    this.connectionFactory = factory;
  }


  @Override
  public void activateObject(PooledObject<Channel> pooledObject) throws Exception {

  }

  @Override
  public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {
    if (validateObject(pooledObject)) {
      pooledObject.getObject().close();
    }
  }

  @Override
  public PooledObject<Channel> makeObject() throws Exception {
    Channel channel = connectionFactory.newConnection().createChannel();
    return new DefaultPooledObject<>(channel);
  }

  @Override
  public void passivateObject(PooledObject<Channel> pooledObject) throws Exception {

  }

  @Override
  public boolean validateObject(PooledObject<Channel> pooledObject) {
    return pooledObject != null && pooledObject.getObject() != null && pooledObject.getObject()
        .isOpen();
  }
}
