package dynamoDB;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DynamoDBSource {

  private AmazonDynamoDB client;
  private DynamoDB dynamoDB;
  private static final String TABLE_NAME = "resort";
  private final Table table;

  public DynamoDBSource() {
    this.client = AmazonDynamoDBClientBuilder.standard().build();
    this.dynamoDB = new DynamoDB(client);
    this.table = dynamoDB.getTable(TABLE_NAME);
  }

  public Table getTable() {
    return table;
  }
}
