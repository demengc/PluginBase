/*
 * MIT License
 *
 * Copyright (c) 2025 Demeng Chen
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

package dev.demeng.pluginbase.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a component that can be auto-created by the dependency injection container.
 *
 * <p>Classes marked with this annotation will be automatically instantiated when requested, with
 * their dependencies injected from the container. All components are singletons - only one instance
 * is created and cached.
 *
 * <p>Dependencies are injected via constructor. The container supports Lombok's
 * {@code @RequiredArgsConstructor} and {@code @AllArgsConstructor}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @Component
 * public class PlayerDataManager {
 *   private final DatabaseConnection database;
 *   private final CacheService cache;
 *
 *   // Dependencies auto-injected
 *   public PlayerDataManager(DatabaseConnection database, CacheService cache) {
 *     this.database = database;
 *     this.cache = cache;
 *   }
 * }
 *
 * // With Lombok:
 * @Component
 * @RequiredArgsConstructor
 * public class PlayerDataManager {
 *   private final DatabaseConnection database;
 *   private final CacheService cache;
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

}
