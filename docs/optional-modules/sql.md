# SQL Module

HikariCP-based SQL database module with async operations.

## Setup

```java
import dev.demeng.pluginbase.sql.Sql;
import dev.demeng.pluginbase.sql.SqlCredentials;

// Create credentials
SqlCredentials credentials = SqlCredentials.of(
    "localhost",
    3306,
    "minecraft",
    "root",      // user
    "password"
);

// Create SQL instance
Sql sql = new Sql(
    "com.mysql.cj.jdbc.Driver",
    "jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabase(),
    credentials
);
```

## Basic Queries

### Execute Update

```java
// INSERT
sql.execute("INSERT INTO players (uuid, name, coins) VALUES (?, ?, ?)",
    ps -> {
        ps.setString(1, uuid.toString());
        ps.setString(2, player.getName());
        ps.setInt(3, 100);
    });

// UPDATE
sql.execute("UPDATE players SET coins = ? WHERE uuid = ?",
    ps -> {
        ps.setInt(1, 500);
        ps.setString(2, uuid.toString());
    });

// DELETE
sql.execute("DELETE FROM players WHERE uuid = ?",
    ps -> ps.setString(1, uuid.toString()));
```

### Query Data

```java
// Single result
Integer coins = sql.query(
    "SELECT coins FROM players WHERE uuid = ?",
    ps -> ps.setString(1, uuid.toString()),
    rs -> {
        if (rs.next()) {
            return rs.getInt("coins");
        }
        return 0;
    }).orElse(0);

// Multiple results
List<PlayerData> players = sql.query(
    "SELECT * FROM players ORDER BY coins DESC LIMIT 10",
    ps -> {},  // No parameters
    rs -> {
        List<PlayerData> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new PlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getInt("coins")
            ));
        }
        return list;
    }).orElse(new ArrayList<>());
```

## Async Operations

```java
// Async load on join
Events.subscribe(PlayerJoinEvent.class)
    .handler(e -> {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        // Load data async
        Schedulers.async().supply(() -> {
            return sql.query(
                "SELECT * FROM players WHERE uuid = ?",
                ps -> ps.setString(1, uuid.toString()),
                rs -> {
                    if (rs.next()) {
                        return new PlayerData(
                            uuid,
                            rs.getString("name"),
                            rs.getInt("coins"),
                            rs.getInt("level")
                        );
                    }
                    return null;
                }).orElse(null);
        }).thenApplySync(data -> {
            // Apply data on main thread
            if (data != null) {
                applyPlayerData(player, data);
            } else {
                createNewPlayer(uuid, player.getName());
            }
            return data;
        });
    })
    .bindWith(this);
```

## Table Creation

```java
@Override
protected void enable() {
    // Create tables
    sql.execute(
        "CREATE TABLE IF NOT EXISTS players (" +
        "uuid VARCHAR(36) PRIMARY KEY, " +
        "name VARCHAR(16) NOT NULL, " +
        "coins INT DEFAULT 0, " +
        "level INT DEFAULT 1, " +
        "last_login BIGINT" +
        ")",
        ps -> {});

    sql.execute(
        "CREATE TABLE IF NOT EXISTS stats (" +
        "uuid VARCHAR(36), " +
        "stat_name VARCHAR(32), " +
        "value INT, " +
        "PRIMARY KEY (uuid, stat_name)" +
        ")",
        ps -> {});
}
```

## Batch Operations

```java
// Batch insert
BatchBuilder batch = sql.batch("INSERT INTO players (uuid, name) VALUES (?, ?)");

for (Player player : Bukkit.getOnlinePlayers()) {
    batch.bind(ps -> {
        ps.setString(1, player.getUniqueId().toString());
        ps.setString(2, player.getName());
    });
}

sql.executeBatch(batch);
```

## Complete Example

```java
public class MyPlugin extends BasePlugin {

    private Sql sql;

    @Override
    protected DependencyContainer configureDependencies() {
        // Setup SQL
        SqlCredentials credentials = SqlCredentials.of(
            getConfig().getString("database.host", "localhost"),
            getConfig().getInt("database.port", 3306),
            getConfig().getString("database.database", "minecraft"),
            getConfig().getString("database.user", "root"),
            getConfig().getString("database.password", "password")
        );

        this.sql = new Sql(
            "com.mysql.cj.jdbc.Driver",
            "jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabase(),
            credentials
        );

        return DependencyInjection.builder()
            .register(this)
            .register(Sql.class, sql)
            .build();
    }

    @Override
    protected void enable() {
        // Create tables
        createTables();

        // Load player data on join
        Events.subscribe(PlayerJoinEvent.class)
            .handler(this::loadPlayerData)
            .bindWith(this);

        // Save player data on quit
        Events.subscribe(PlayerQuitEvent.class)
            .handler(this::savePlayerData)
            .bindWith(this);
    }

    private void createTables() {
        sql.execute(
            "CREATE TABLE IF NOT EXISTS players (" +
            "uuid VARCHAR(36) PRIMARY KEY, " +
            "name VARCHAR(16) NOT NULL, " +
            "coins INT DEFAULT 0, " +
            "level INT DEFAULT 1" +
            ")",
            ps -> {});
    }

    private void loadPlayerData(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Schedulers.async().supply(() -> {
            return sql.query(
                "SELECT * FROM players WHERE uuid = ?",
                ps -> ps.setString(1, uuid.toString()),
                rs -> {
                    if (rs.next()) {
                        return new PlayerData(
                            uuid,
                            rs.getString("name"),
                            rs.getInt("coins"),
                            rs.getInt("level")
                        );
                    }
                    return null;
                }).orElse(null);
        }).thenApplySync(data -> {
            if (data != null) {
                // Apply loaded data
                playerDataCache.put(uuid, data);
                Text.tell(player, "&aData loaded! Coins: " + data.getCoins());
            } else {
                // Create new player
                createNewPlayer(uuid, player.getName());
            }
            return data;
        });
    }

    private void savePlayerData(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        PlayerData data = playerDataCache.get(uuid);

        if (data != null) {
            Schedulers.async().run(() -> {
                sql.execute(
                    "INSERT INTO players (uuid, name, coins, level) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE name=?, coins=?, level=?",
                    ps -> {
                        ps.setString(1, uuid.toString());
                        ps.setString(2, player.getName());
                        ps.setInt(3, data.getCoins());
                        ps.setInt(4, data.getLevel());
                        ps.setString(5, player.getName());
                        ps.setInt(6, data.getCoins());
                        ps.setInt(7, data.getLevel());
                    });
            });
        }
    }

    private void createNewPlayer(UUID uuid, String name) {
        sql.execute("INSERT INTO players (uuid, name) VALUES (?, ?)",
            ps -> {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
            });
        playerDataCache.put(uuid, new PlayerData(uuid, name, 0, 1));
    }
}
```

## Connection Pooling

HikariCP is used for connection pooling automatically. Connection pool settings are configured internally.

## Cleanup

SQL connections are automatically closed when plugin disables (implements AutoCloseable).
