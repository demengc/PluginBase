# Events

Functional event handling with lambda expressions instead of listener classes.

## Basic Usage

```java
Events.subscribe(PlayerJoinEvent.class)
    .handler(event -> {
        Player player = event.getPlayer();
        Text.tell(player, "&aWelcome!");
    })
    .bindWith(this);  // Auto-unregister on disable
```

## With Filters

```java
Events.subscribe(PlayerMoveEvent.class)
    .filter(e -> e.getPlayer().hasPermission("special"))
    .filter(e -> !e.getFrom().getBlock().equals(e.getTo().getBlock()))
    .handler(e -> {
        // Only called for players with permission
        // And only when moving to different block
    })
    .bindWith(this);
```

## Method References

```java
Events.subscribe(PlayerJoinEvent.class)
    .handler(this::onPlayerJoin)
    .bindWith(this);

private void onPlayerJoin(PlayerJoinEvent event) {
    Text.tell(event.getPlayer(), "&aWelcome!");
}
```

## Event Priority

```java
Events.subscribe(AsyncPlayerChatEvent.class, EventPriority.HIGHEST)
    .handler(e -> {
        // Handled at HIGHEST priority
    })
    .bindWith(this);
```

## Expiry

```java
// Unregister after 10 calls
Events.subscribe(PlayerInteractEvent.class)
    .expireAfter(10)
    .handler(e -> {})
    .bindWith(this);

// Unregister after 30 seconds
Events.subscribe(PlayerMoveEvent.class)
    .expireAfter(30, TimeUnit.SECONDS)
    .handler(e -> {})
    .bindWith(this);
```

## Merged Events

Listen to multiple **related** events:

```java
// Both EntityDamageEvent and EntityDeathEvent are related to EntityEvent
Events.merge(EntityEvent.class, EntityDamageEvent.class, EntityDeathEvent.class)
    .handler(event -> {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            // Handle either event type
        }
    })
    .bindWith(this);
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Welcome VIP players
        Events.subscribe(PlayerJoinEvent.class)
            .filter(e -> e.getPlayer().hasPermission("vip"))
            .handler(e -> Text.tell(e.getPlayer(), "&6Welcome VIP!"))
            .bindWith(this);

        // Freeze player for 5 seconds
        Events.subscribe(PlayerMoveEvent.class)
            .filter(e -> shouldFreeze(e.getPlayer()))
            .expireAfter(5, TimeUnit.SECONDS)
            .handler(e -> e.setCancelled(true))
            .bindWith(this);

        // Log damage to players
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
