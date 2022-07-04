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

package dev.demeng.pluginbase.messaging.codec;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Implementation of {@link Codec} using {@link Gson}.
 *
 * @param <M> the message type
 */
public class GsonCodec<M> implements Codec<M> {

  private static final Gson STANDARD_GSON = new GsonBuilder()
      .serializeNulls()
      .disableHtmlEscaping()
      .create();

  private final Gson gson;
  private final TypeToken<M> type;

  public GsonCodec(final Gson gson, final TypeToken<M> type) {
    this.gson = gson;
    this.type = type;
  }

  public GsonCodec(final TypeToken<M> type) {
    this.gson = STANDARD_GSON;
    this.type = type;
  }

  @Override
  public byte[] encode(final M message) {
    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (final Writer writer = new OutputStreamWriter(byteOut, StandardCharsets.UTF_8)) {
      this.gson.toJson(message, this.type.getType(), writer);
    } catch (final IOException e) {
      throw new EncodingException(e);
    }
    return byteOut.toByteArray();
  }

  @Override
  public M decode(final byte[] buf) {
    final ByteArrayInputStream byteIn = new ByteArrayInputStream(buf);
    try (final Reader reader = new InputStreamReader(byteIn, StandardCharsets.UTF_8)) {
      return this.gson.fromJson(reader, this.type.getType());
    } catch (final IOException e) {
      throw new EncodingException(e);
    }
  }
}
