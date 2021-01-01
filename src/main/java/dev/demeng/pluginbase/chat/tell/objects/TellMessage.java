package dev.demeng.pluginbase.chat.tell.objects;

import dev.demeng.pluginbase.JsonSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/** A chat message. Prefix is not added and the message is not colored. */
public class TellMessage implements JsonSerializable<TellMessage> {

  private final boolean json;
  private final String[] lines;
  private final boolean addPrefix;
  private final boolean center;

  /**
   * A new message to tell.
   *
   * @param json If this message is in JSON format
   * @param lines The messages to send
   * @param addPrefix If a prefix should be added
   * @param center If the message should be centered
   */
  public TellMessage(boolean json, String[] lines, boolean addPrefix, boolean center) {
    this.json = json;
    this.lines = Objects.requireNonNull(lines, "Lines cannot be null");
    this.addPrefix = addPrefix;
    this.center = center;

    if (json || center) {
      addPrefix = false;
    }
  }

  /**
   * A new message to tell.
   *
   * @param lines The messages to send
   */
  public TellMessage(String... lines) {
    this.json = false;
    this.lines = Objects.requireNonNull(lines, "Lines cannot be null");
    this.addPrefix = true;
    this.center = false;
  }

  /**
   * If this message is in JSON format. This is generally false unless the message is interactive
   * (hold actions or click actions). Default to false.
   *
   * @return If the message is in JSON format
   */
  public boolean isJson() {
    return json;
  }

  /**
   * All messages to send. Each element is a new line.
   *
   * @return All messages to send
   */
  @NotNull
  public String[] getLines() {
    return lines;
  }

  /**
   * If a prefix should be added to each line. Automatically off if the message is centered or in
   * JSON format. Default to true.
   *
   * @return If a prefix should be added to each line
   */
  public boolean isAddPrefix() {
    return addPrefix;
  }

  /**
   * If the message should be centered. Prefix will not be shown.
   *
   * @return If the message should be centered
   */
  public boolean isCenter() {
    return center;
  }

  /**
   * Creates a new builder.
   *
   * @return A new builder
   */
  @NotNull
  public static TellMessage.Builder builder() {
    return new TellMessage.Builder();
  }

  /** Builder for this object. */
  private static class Builder {

    private boolean json = false;
    private final List<String> lines = new ArrayList<>();
    private boolean addPrefix = true;
    private boolean center = false;

    @NotNull
    public Builder json(boolean json) {
      this.json = json;
      return this;
    }

    @NotNull
    public Builder line(String... lines) {
      this.lines.addAll(Arrays.asList(lines));
      return this;
    }

    @NotNull
    public Builder addPrefix(boolean addPrefix) {
      this.addPrefix = addPrefix;
      return this;
    }

    @NotNull
    public Builder center(boolean center) {
      this.center = center;
      return this;
    }

    @NotNull
    public TellMessage build() {
      return new TellMessage(json, lines.toArray(new String[0]), addPrefix, center);
    }
  }
}
