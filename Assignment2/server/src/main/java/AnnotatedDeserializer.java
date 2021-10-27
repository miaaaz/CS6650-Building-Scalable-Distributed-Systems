import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.rabbitmq.tools.json.JSONWriter;


// Sources: https://stackoverflow.com/questions/21626690/gson-optional-and-required-fields
public class AnnotatedDeserializer<T> implements JsonDeserializer<T>
{


  public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException
  {
    T pojo = new Gson().fromJson(je, type);

    Field[] fields = pojo.getClass().getDeclaredFields();
    for (Field f : fields)
    {
      if (f.getAnnotation(JsonRequired.class) != null)
      {
        try
        {
          f.setAccessible(true);
          if (f.get(pojo) == null)
          {
            throw new JsonParseException("Missing field in JSON: " + f.getName());
          }
        }
        catch (IllegalArgumentException | IllegalAccessException ex)
        {
          Logger.getLogger(AnnotatedDeserializer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    return pojo;

  }
}
