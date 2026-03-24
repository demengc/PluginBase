---
description: Functional event handling with lambda expressions instead of listener classes.
---

# Events

The `Events` utility provides a fluent builder API for subscribing to Bukkit events, firing custom events, and managing listener lifecycle.

## Single event subscription

```java
Events.subscribe(PlayerJoinEvent.class)
    .handler(event -> {
        Text.tell(event.getPlayer(), "&aWelcome!");
    })
    .bindWith(this);
```

`handler()` returns a `SingleSubscription` (which implements `Terminable`), so `.bindWith(this)` registers it for automatic cleanup when the plugin disables. `this` must be a `TerminableConsumer`, which `BasePlugin` implements.

## Event priority

```java
Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST)
    .handler(e -> {
        e.setFormat("[Server] %s: %s");
    })
    .bindWith(this);
```

The default priority is `EventPriority.NORMAL`.

## Filters

Filters are evaluated in registration order. The handler is only called if all filters pass.

```java
Events.subscribe(PlayerMoveEvent.class)
    .filter(EventFilters.ignoreSameBlock())
    .filter(e -> e.getPlayer().hasPermission("special"))
    .handler(e -> {
        Text.tell(e.getPlayer(), "&eYou moved to a new block.");
    })
    .bindWith(this);
```

### Built-in filters (EventFilters)

| Method | Applies to | Passes when |
|---|---|---|
| `ignoreCancelled()` | `Cancellable` | Event is not cancelled |
| `ignoreNotCancelled()` | `Cancellable` | Event is cancelled |
| `ignoreSameBlock()` | `PlayerMoveEvent` | Player crossed a block boundary (X, Y, or Z) |
| `ignoreSameBlockAndY()` | `PlayerMoveEvent` | Player crossed a block boundary on X or Z only (ignores jumping) |
| `ignoreSameChunk()` | `PlayerMoveEvent` | Player crossed a chunk boundary |
| `ignoreDisallowedLogin()` | `PlayerLoginEvent` | Login result is `ALLOWED` |
| `ignoreDisallowedPreLogin()` | `AsyncPlayerPreLoginEvent` | Login result is `ALLOWED` |
| `playerHasPermission(String)` | `PlayerEvent` | Player has the given permission |

### Built-in handlers (EventHandlers)

| Method | Applies to | Action |
|---|---|---|
| `EventHandlers.cancel()` | `Cancellable` | Cancels the event |
| `EventHandlers.uncancel()` | `Cancellable` | Un-cancels the event |

```java
Events.subscribe(PlayerInteractEvent.class)
    .filter(EventFilters.ignoreCancelled())
    .handler(EventHandlers.cancel())
    .bindWith(this);
```

## Expiry

Subscriptions can be configured to unregister themselves automatically.

| Method | Behavior |
|---|---|
| `expireAfter(long maxCalls)` | Unregister after the handler has been called `maxCalls` times |
| `expireAfter(long duration, TimeUnit unit)` | Unregister after the given wall-clock duration |
| `expireIf(Predicate)` | Unregister when the predicate returns `true` (tested `PRE` and `POST_HANDLE`) |
| `expireIf(BiPredicate, ExpiryTestStage...)` | Unregister when the predicate returns `true`, tested at the specified stages |

`ExpiryTestStage` values:

| Value | Tested |
|---|---|
| `PRE` | Before filtering or handling |
| `POST_FILTER` | After filters pass, before handling |
| `POST_HANDLE` | After the handler completes |

```java
Events.subscribe(PlayerInteractEvent.class)
    .expireAfter(10)
    .handler(e -> {})
    .bindWith(this);

Events.subscribe(PlayerMoveEvent.class)
    .expireAfter(30, TimeUnit.SECONDS)
    .handler(e -> {})
    .bindWith(this);
```

## Merged event subscription

Merged subscriptions listen to multiple event types through a shared supertype.

The convenience overload pre-binds the subclasses with an identity mapping:

```java
Events.merge(EntityEvent.class, EntityDamageEvent.class, EntityDeathEvent.class)
    .handler(event -> {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Text.console("&7Entity event on player: " + entity.getName());
        }
    })
    .bindWith(this);
```

For unrelated event types or custom mapping, start with `Events.merge(Class)` and call `bindEvent()` manually:

```java
Events.merge(Player.class)
    .bindEvent(PlayerJoinEvent.class, PlayerJoinEvent::getPlayer)
    .bindEvent(PlayerQuitEvent.class, PlayerQuitEvent::getPlayer)
    .handler(player -> {
        Text.console("&7Player connection event: " + player.getName());
    })
    .bindWith(this);
```

`bindEvent()` accepts an optional `EventPriority` parameter between the class and the mapping function. The convenience `merge(Class, Class...)` overload defaults to `EventPriority.NORMAL`. A variant `merge(Class, EventPriority, Class...)` applies the given priority to all bound events.

## biHandler

Both `SingleSubscriptionBuilder` and `MergedSubscriptionBuilder` offer `biHandler()`, which provides access to the `Subscription` instance inside the handler. This is useful for self-unregistering based on runtime conditions.

```java
Events.subscribe(PlayerJoinEvent.class)
    .biHandler((sub, event) -> {
        if (event.getPlayer().getName().equals("Notch")) {
            Text.tell(event.getPlayer(), "&6First Notch sighting!");
            sub.unregister();
        }
    })
    .bindWith(this);
```

## Additional builder options

| Method | Available on | Purpose |
|---|---|---|
| `exceptionConsumer(BiConsumer)` | Both | Custom exception handling for the handler |
| `handleSubclasses()` | `SingleSubscriptionBuilder` | Accept subclasses of the event type |
| `handlers()` | Both | Returns a `HandlerList` for registering multiple handlers |

## Firing events

`Events` provides methods for dispatching custom events.

| Method | Thread | Returns |
|---|---|---|
| `Events.call(Event)` | Current thread | `void` |
| `Events.callAsync(Event)` | New async thread | `void` |
| `Events.callSync(Event)` | Main server thread | `void` |
| `Events.callAndReturn(Event)` | Current thread | The event |
| `Events.callAsyncAndJoin(Event)` | Async thread, blocks until done | The event |
| `Events.callSyncAndJoin(Event)` | Main thread, blocks until done | The event |

```java
MyCustomEvent event = Events.callAndReturn(new MyCustomEvent("data"));
if (!event.isCancelled()) {
    // proceed
}
```

## Complete example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        Events.subscribe(PlayerJoinEvent.class)
            .filter(EventFilters.playerHasPermission("vip"))
            .handler(e -> Text.tell(e.getPlayer(), "&6Welcome VIP!"))
            .bindWith(this);

        Events.subscribe(PlayerMoveEvent.class)
            .filter(EventFilters.ignoreSameBlock())
            .filter(e -> shouldFreeze(e.getPlayer()))
            .expireAfter(5, TimeUnit.SECONDS)
            .handler(EventHandlers.cancel())
            .bindWith(this);

        Events.subscribe(EntityDamageEvent.class)
            .filter(e -> e.getEntity() instanceof Player)
            .handler(e -> {
                Player player = (Player) e.getEntity();
                Text.console("&7" + player.getName() + " took " + e.getDamage() + " damage");
            })
            .bindWith(this);
    }
}
```
