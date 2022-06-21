/*
 * MIT License
 *
 * Copyright (c) 2021 Revxrsal
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

package dev.demeng.pluginbase.commands.core;

import dev.demeng.pluginbase.commands.annotation.Cooldown;
import dev.demeng.pluginbase.commands.command.CommandActor;
import dev.demeng.pluginbase.commands.command.ExecutableCommand;
import dev.demeng.pluginbase.commands.exception.CooldownException;
import dev.demeng.pluginbase.commands.process.CommandCondition;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

enum CooldownCondition implements CommandCondition {

  INSTANCE;

  private static final ScheduledExecutorService COOLDOWN_POOL = Executors.newSingleThreadScheduledExecutor();
  private final Map<UUID, Map<Integer, Long>> cooldowns = new ConcurrentHashMap<>();

  @Override
  public void test(@NotNull final CommandActor actor, @NotNull final ExecutableCommand command,
      @NotNull @Unmodifiable final List<String> arguments) {
    final Cooldown cooldown = command.getAnnotation(Cooldown.class);
    if (cooldown == null || cooldown.value() == 0) {
      return;
    }
    final UUID uuid = actor.getUniqueId();
    final Map<Integer, Long> spans = get(uuid);
    final Long created = spans.get(command.getId());
    if (created == null) {
      spans.put(command.getId(), System.currentTimeMillis());
      COOLDOWN_POOL.schedule(() -> spans.remove(command.getId()), cooldown.value(),
          cooldown.unit());
      return;
    }
    final long passed = System.currentTimeMillis() - created;
    long left = cooldown.unit().toMillis(cooldown.value()) - passed;
    if (left > 0 && left < 1000) {
      left = 1000L; // for formatting
    }
    throw new CooldownException(left);
  }

  private Map<Integer, Long> get(@NotNull final UUID uuid) {
    return cooldowns.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>());
  }
}
