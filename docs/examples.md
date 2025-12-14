# Examples

Practical examples showing how to use PluginBase features together.

## Simple Plugin

A basic plugin with events, commands, and configuration:

```java
public class WelcomePlugin extends BasePlugin {

    private YamlConfig config;

    @Override
    protected void enable() {
        // Load config
        this.config = new YamlConfig("settings.yml");

        // Configure settings
        setBaseSettings(new BaseSettings() {
            @Override
            public String prefix() {
                return "&8[&bWelcome&8]&r ";
            }
        });

        // Welcome message on join
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                String message = config.getConfig().getString("welcome-message")
                    .replace("{player}", player.getName());
                Text.tell(player, message);
                Text.sendTitle(player, "&b&lWelcome!", "&7" + player.getName());
            })
            .bindWith(this);

        // Register commands
        Lamp<BukkitCommandActor> handler = createCommandHandler();
        handler.register(new WelcomeCommands(config));
    }
}

public class WelcomeCommands {
    private final YamlConfig config;

    public WelcomeCommands(YamlConfig config) {
        this.config = config;
    }

    @Command("welcome reload")
    @CommandPermission("welcome.reload")
    public void reload(Player sender) {
        config.reload();
        Text.tell(sender, "&aConfiguration reloaded!");
    }
}
```

**settings.yml:**
```yaml
welcome-message: "&aWelcome to the server, &e{player}&a!"
```

---

## Database Plugin

Plugin with SQL database and dependency injection:

```java
public class PlayerDataPlugin extends BasePlugin {

    @Override
    protected DependencyContainer configureDependencies() {
        // Setup SQL
        SqlCredentials credentials = SqlCredentials.of(
            "localhost",
            3306,
            "minecraft",
            "root",
            "password"
        );

        Sql sql = new Sql("com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabase(),
            credentials);

        // Register dependencies
        return DependencyInjection.builder()
            .register(this)
            .register(Sql.class, sql)
            .register(PlayerDataStorage.class, new PlayerDataStorage(sql))
            .build();
    }

    @Override
    protected void enable() {
        PlayerDataStorage storage = getDependency(PlayerDataStorage.class);
        storage.createTable();

        // Load data on join
        Events.subscribe(PlayerJoinEvent.class)
            .handler(e -> {
                Player player = e.getPlayer();
                Schedulers.async().supply(() -> {
                    return storage.load(player.getUniqueId());
                }).thenApplySync(data -> {
                    if (data != null) {
                        Text.tell(player, "&aWelcome back! Coins: " + data.getCoins());
                    } else {
                        storage.create(player.getUniqueId(), player.getName());
                        Text.tell(player, "&aWelcome! You've been registered.");
                    }
                    return data;
                });
            })
            .bindWith(this);
    }
}

public class PlayerDataStorage {
    private final Sql sql;

    public PlayerDataStorage(Sql sql) {
        this.sql = sql;
    }

    public void createTable() {
        sql.execute(
            "CREATE TABLE IF NOT EXISTS player_data (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "name VARCHAR(16), " +
            "coins INT DEFAULT 0" +
            ")",
            ps -> {});
    }

    public PlayerData load(UUID uuid) {
        return sql.query("SELECT * FROM player_data WHERE uuid = ?",
            ps -> ps.setString(1, uuid.toString()),
            rs -> {
                if (rs.next()) {
                    return new PlayerData(
                        uuid,
                        rs.getString("name"),
                        rs.getInt("coins")
                    );
                }
                return null;
            }).orElse(null);
    }

    public void create(UUID uuid, String name) {
        sql.execute("INSERT INTO player_data (uuid, name) VALUES (?, ?)",
            ps -> {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
            });
    }

    public void addCoins(UUID uuid, int amount) {
        sql.execute("UPDATE player_data SET coins = coins + ? WHERE uuid = ?",
            ps -> {
                ps.setInt(1, amount);
                ps.setString(2, uuid.toString());
            });
    }
}
```

---

## Shop Plugin with GUI

Plugin with menus and items:

```java
public class ShopPlugin extends BasePlugin {

    @Override
    protected void enable() {
        // Register command
        Lamp<BukkitCommandActor> handler = createCommandHandler();
        handler.register(new ShopCommand());
    }
}

public class ShopCommand {

    @Command("shop")
    public void shop(Player sender) {
        new ShopMenu().open(sender);
    }
}

public class ShopMenu extends Menu {

    public ShopMenu() {
        super("&6&lShop", 3);
    }

    @Override
    public void render() {
        // Items
        addButton(11, ItemBuilder.create(Material.DIAMOND_SWORD)
            .name("&bDiamond Sword")
            .lore("&7Price: $500")
            .glow(true)
            .get(), this::buyWeapon);

        addButton(13, ItemBuilder.create(Material.GOLDEN_APPLE)
            .name("&6Golden Apple")
            .lore("&7Price: $100")
            .amount(5)
            .get(), this::buyFood);

        addButton(15, ItemBuilder.create(Material.DIAMOND_CHESTPLATE)
            .name("&bDiamond Armor")
            .lore("&7Price: $1000")
            .enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4)
            .flags(ItemFlag.HIDE_ENCHANTS)
            .get(), this::buyArmor);

        // Close button
        addButton(22, ItemBuilder.create(Material.BARRIER)
            .name("&cClose")
            .get(), click -> {
                Player player = (Player) click.getWhoClicked();
                player.closeInventory();
            });

        // Decoration
        setBackground(ItemBuilder.create(Material.GRAY_STAINED_GLASS_PANE)
            .name(" ")
            .get());
    }

    private void buyWeapon(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
        Text.tell(player, "&aPurchased Diamond Sword!");
        player.closeInventory();
    }

    private void buyFood(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        player.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 5));
        Text.tell(player, "&aPurchased Golden Apples!");
        player.closeInventory();
    }

    private void buyArmor(InventoryClickEvent click) {
        Player player = (Player) click.getWhoClicked();
        player.getInventory().addItem(new ItemStack(Material.DIAMOND_CHESTPLATE));
        Text.tell(player, "&aPurchased Diamond Armor!");
        player.closeInventory();
    }
}
```

---

## Minigame Example

Simple PvP arena manager:

```java
public class ArenaPlugin extends BasePlugin {

    private final List<Arena> arenas = new ArrayList<>();

    @Override
    protected void enable() {
        // Create arenas
        Location spawn = new Location(Bukkit.getWorld("world"), 0, 64, 0);
        Arena arena1 = new Arena("Arena 1", spawn);
        arenas.add(arena1);

        // Commands
        Lamp<BukkitCommandActor> handler = createCommandHandler();
        handler.register(new ArenaCommands(arenas));

        // Prevent death, eliminate instead
        Events.subscribe(EntityDamageByEntityEvent.class)
            .filter(e -> e.getEntity() instanceof Player && e.getDamager() instanceof Player)
            .handler(e -> {
                Player victim = (Player) e.getEntity();
                Arena arena = findPlayerArena(victim);

                if (arena != null && arena.isActive()) {
                    if (victim.getHealth() - e.getFinalDamage() <= 0) {
                        e.setCancelled(true);
                        arena.eliminatePlayer(victim);
                    }
                }
            })
            .bindWith(this);
    }

    private Arena findPlayerArena(Player player) {
        return arenas.stream()
            .filter(a -> a.hasPlayer(player))
            .findFirst()
            .orElse(null);
    }
}

public class Arena {

    private final Set<UUID> players = new HashSet<>();
    private final String name;
    private final Location spawn;
    private final Location lobbySpawn;
    private boolean active;

    public Arena(String name, Location spawn) {
        this.name = name;
        this.spawn = spawn;
        this.lobbySpawn = spawn.getWorld().getSpawnLocation();
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        player.teleport(spawn);
        Text.tell(player, "&aJoined " + name + "!");

        if (players.size() >= 2 && !active) {
            startGame();
        }
    }

    private void startGame() {
        active = true;

        // Give kits
        players.forEach(uuid -> {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                Text.tell(p, "&aFight!");
                giveKit(p);
            }
        });
    }

    public void eliminatePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.setHealth(20.0);
        player.teleport(lobbySpawn);
        Text.tell(player, "&cYou were eliminated!");

        if (players.size() == 1 && active) {
            endGame();
        }
    }

    private void endGame() {
        UUID winner = players.iterator().next();
        Player winnerPlayer = Bukkit.getPlayer(winner);

        if (winnerPlayer != null) {
            Text.broadcast(null, "&6" + winnerPlayer.getName() + " &awon " + name + "!");
            winnerPlayer.teleport(lobbySpawn);
        }

        players.clear();
        active = false;
    }

    private void giveKit(Player player) {
        player.getInventory().clear();
        player.getInventory().addItem(
            new ItemStack(Material.DIAMOND_SWORD),
            new ItemStack(Material.BOW),
            new ItemStack(Material.ARROW, 64)
        );
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player.getUniqueId());
    }

    public boolean isActive() {
        return active;
    }
}
```

---

## Cross-Server Chat

Using Redis for network-wide chat:

```java
public class NetworkChatPlugin extends BasePlugin {

    private Redis redis;
    private String serverName;

    @Override
    protected DependencyContainer configureDependencies() {
        RedisCredentials credentials = RedisCredentials.of(
            "localhost",
            6379,
            null,
            null,
            false
        );

        this.redis = new Redis("server-1", credentials);

        return DependencyInjection.builder()
            .register(this)
            .register(Redis.class, redis)
            .build();
    }

    @Override
    protected void enable() {
        // Load server name from config
        this.serverName = getConfig().getString("server-name", "Server");

        // Subscribe to network chat channel
        redis.subscribe("network-chat");

        // Listen for messages from network
        Events.subscribe(AsyncRedisMessageReceiveEvent.class)
            .filter(e -> e.getChannel().equals("network-chat"))
            .handler(e -> {
                String message = e.getMessageObject(String.class).orElse("");
                String[] parts = message.split("\\|", 3);
                if (parts.length < 3) return;

                String server = parts[0];
                String player = parts[1];
                String msg = parts[2];

                String formatted = Text.colorize("&7[" + server + "] &f" + player + ": " + msg);
                Bukkit.broadcastMessage(formatted);
            })
            .bindWith(this);

        // Publish local chat to network
        Events.subscribe(AsyncPlayerChatEvent.class)
            .handler(e -> {
                String message = serverName + "|" + e.getPlayer().getName() + "|" + e.getMessage();
                redis.publishString("network-chat", message);
                e.setCancelled(true);  // Handle via redis
            })
            .bindWith(this);
    }
}
```
