/*
 * MIT License
 *
 * Copyright (c) 2021-2022 Demeng Chen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.demeng.pluginbase.input;

import dev.demeng.pluginbase.chat.TextUtils;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.InactivityConversationCanceller;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Requests a chat input from players, with the ability to map the string input into the object you
 * require and a retry system for invalid inputs. Created using the Bukkit
 * {@link org.bukkit.conversations.Conversation} API.
 *
 * @param <T> The expected return type for the input request
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ChatInputRequest<T> extends ValidatingPrompt {

  @NotNull private final Function<@NotNull String, @Nullable T> parser;

  @Nullable private String title;
  @Nullable private String subtitle;
  @Nullable private String initialMessage;
  @Nullable private String retryMessage;
  @Nullable private String timeoutMessage;
  @Nullable private Consumer<T> consumer;
  @Nullable private Runnable exitRunnable;

  @Nullable private T currentResponse;
  private boolean firstAttempt = true;

  /**
   * Creates a new chat input request builder.
   *
   * @param parser The input parser that returns the expected object, or null if the string input is
   *               invalid
   * @param <T>    The expected return type for the input request
   * @return A new chat input request builder
   */
  @NotNull
  public static <T> ChatInputRequest<T> create(
      @NotNull final Function<@NotNull String, @Nullable T> parser) {
    return new ChatInputRequest<>(parser);
  }

  /**
   * Sets the title that is sent when input is requested. If not set, the default localized title
   * will be used.
   *
   * @param title The title sent on input request
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> withTitle(@Nullable final String title) {
    this.title = title;
    return this;
  }

  /**
   * Sets the subttile that is sent when input is requested. If not set, the default localized
   * subtitle will be used.
   *
   * @param subtitle The subtitle sent on input request
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> withSubtitle(@Nullable final String subtitle) {
    this.subtitle = subtitle;
    return this;
  }

  /**
   * The message that is sent the first time input is requested. If not set, an empty message will
   * be used.
   *
   * @param initialMessage The message sent on inital request
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> withInitialMessage(@Nullable final String initialMessage) {
    this.initialMessage = initialMessage;
    return this;
  }

  /**
   * The message that is sent after an invalid input. If not set, the default localized message will
   * be used.
   *
   * @param retryMessage The message sent on invalid input
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> withRetryMessage(@Nullable final String retryMessage) {
    this.retryMessage = retryMessage;
    return this;
  }

  /**
   * The message that is sent after the request timeout of 5 minutes. If not set, the default
   * localized message will be used.
   *
   * @param timeoutMessage The message sent on timeout
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> setTimeoutMessage(@Nullable final String timeoutMessage) {
    this.timeoutMessage = timeoutMessage;
    return this;
  }

  /**
   * The consumer that is accepted with the parsed input when a valid input has been provided. This
   * utility will automatically clear the title and subtitle (if applicable) and end the
   * {@link org.bukkit.conversations.Conversation}.
   *
   * @param consumer The consumer to be accepted with the input
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> onFinish(@Nullable final Consumer<T> consumer) {
    this.consumer = consumer;
    return this;
  }

  /**
   * The runnable ran on exit (conversation abandon). This is called both when an input is accepted
   * and when the input request is cancelled using the localized escape sequence.
   *
   * @param exitRunnable The runnable to run on exit
   * @return this
   */
  @NotNull
  public ChatInputRequest<T> onExit(@Nullable final Runnable exitRunnable) {
    this.exitRunnable = exitRunnable;
    return this;
  }

  /**
   * Requests the specified player for input.
   *
   * @param p The player to request input from
   */
  public void start(@NotNull final Player p) {

    final Locale locale = TextUtils.getLocale(p);

    if (title == null) {
      title = TextUtils.localized("input-requests.default-title", locale);
    }

    if (subtitle == null) {
      subtitle = TextUtils.localized("input-requests.default-subtitle", locale);
    }

    if (retryMessage == null) {
      retryMessage = TextUtils.localized("input-requests.default-retry-message", locale);
    }

    if (timeoutMessage == null) {
      timeoutMessage = TextUtils.localized("input-requests.default-timeout-message", locale);
    }

    TextUtils.sendTitle(p, title, subtitle, 20, 12000, 20);

    new ConversationFactory(BaseManager.getPlugin())
        .withModality(false)
        .withFirstPrompt(this)
        .withEscapeSequence(TextUtils.localized("input-requests.exit-value", locale))
        .addConversationAbandonedListener(e -> {

          if (e.getCanceller() instanceof InactivityConversationCanceller) {
            TextUtils.tell(p, timeoutMessage);
          }

          TextUtils.clearTitle(p);

          if (exitRunnable != null) {
            exitRunnable.run();
          }
        })
        .buildConversation(p)
        .begin();
  }

  @Override
  protected boolean isInputValid(
      @NotNull final ConversationContext context,
      @NotNull final String str) {

    currentResponse = parser.apply(str);

    if (currentResponse == null) {
      firstAttempt = false;
      return false;
    }

    return true;
  }

  @Nullable
  @Override
  protected Prompt acceptValidatedInput(
      @NotNull final ConversationContext context,
      @NotNull final String str) {

    final Player p = (Player) context.getForWhom();

    if (title != null || subtitle != null) {
      TextUtils.clearTitle(p);
    }

    if (consumer != null) {
      consumer.accept(currentResponse);
    }

    return Prompt.END_OF_CONVERSATION;
  }

  @NotNull
  @Override
  public String getPromptText(@NotNull final ConversationContext context) {

    if (firstAttempt) {
      if (initialMessage != null) {
        return TextUtils.colorize(TextUtils.getPrefix() + initialMessage);
      }

    } else if (retryMessage != null) {
      return TextUtils.colorize(TextUtils.getPrefix() + retryMessage);
    }

    return "";
  }
}
