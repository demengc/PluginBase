package dev.demeng.pluginbase;

import com.google.gson.Gson;
import org.intellij.lang.annotations.Language;

import java.lang.reflect.Type;

public interface JsonSerializable<T> {

  /**
   * Converts the object into JSON, for storage.
   *
   * @return The object converted into JSON
   */
  default String toJson() {
    return new Gson().toJson(this);
  }


  /**
   * Converts JSON into an object.
   *
   * @return The JSON to be converted to the object
   */
  default T fromJson(@Language("JSON") String json) {
    return new Gson().fromJson(json, (Type) this.getClass());
  }
}
