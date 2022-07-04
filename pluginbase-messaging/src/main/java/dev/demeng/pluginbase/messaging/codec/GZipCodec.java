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

import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * A {@link Codec} wrapper using GZip.
 *
 * @param <M> the message type
 */
public class GZipCodec<M> implements Codec<M> {

  private final Codec<M> delegate;

  public GZipCodec(final Codec<M> delegate) {
    this.delegate = delegate;
  }

  @Override
  public byte[] encode(final M message) throws EncodingException {
    final byte[] in = this.delegate.encode(message);
    final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    try (final GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut)) {
      gzipOut.write(in);
    } catch (final IOException e) {
      throw new EncodingException(e);
    }
    return byteOut.toByteArray();
  }

  @Override
  public M decode(final byte[] buf) throws EncodingException {
    final byte[] uncompressed;
    try (final GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(buf))) {
      uncompressed = ByteStreams.toByteArray(gzipIn);
    } catch (final IOException e) {
      throw new EncodingException(e);
    }
    return this.delegate.decode(uncompressed);
  }
}