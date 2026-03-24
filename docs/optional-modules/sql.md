---
description: HikariCP-based SQL database module with connection pooling and async operations.
---

# SQL

The `pluginbase-sql` module wraps [HikariCP 7.x](https://github.com/brettwooldridge/HikariCP) with preconfigured connection pooling, prepared statement helpers, batch operations, and built-in async variants for every operation.

## Setup

### Credentials

`SqlCredentials` holds the connection details. Create one directly or load from a config section:

```java
SqlCredentials credentials = SqlCredentials.of(
    "localhost",  // host
    3306,         // port
    "minecraft",  // database
    "root",       // user
    "password"    // password
);
```

You can also load credentials from a Bukkit `ConfigurationSection` (expects keys `host`, `port`, `database`, `user`, `password` with sensible defaults):

```java
SqlCredentials credentials = SqlCredentials.of(getConfig().getConfigurationSection("database"));
```

### Creating a connection

The `Sql` constructor accepts a driver class, JDBC URL, and credentials. Both driver and URL can be `null` to use the built-in defaults (`Sql.MYSQL_DRIVER` and `Sql.DEFAULT_JDBC_URL`).

```java
// Minimal setup (uses default MySQL driver and JDBC URL):
Sql sql = new Sql(null, null, credentials);

// Equivalent explicit setup:
Sql sql = new Sql(Sql.MYSQL_DRIVER, Sql.DEFAULT_JDBC_URL, credentials);
```

`DEFAULT_JDBC_URL` is `jdbc:mysql://{host}:{port}/{database}?autoReconnect=true&useSSL=false`. The `{host}`, `{port}`, and `{database}` placeholders are replaced automatically from the credentials. You can provide a custom JDBC URL with the same placeholders, or a fully hardcoded URL.

### Constants

| Constant | Value |
|---|---|
| `Sql.MYSQL_DRIVER` | `com.mysql.cj.jdbc.Driver` |
| `Sql.MYSQL_LEGACY_DRIVER` | `com.mysql.jdbc.Driver` |
| `Sql.DEFAULT_JDBC_URL` | `jdbc:mysql://{host}:{port}/{database}?autoReconnect=true&useSSL=false` |

## Executing statements

`execute` runs any non-query SQL statement (INSERT, UPDATE, DELETE, CREATE TABLE, etc.). The second argument is a `SqlConsumer<PreparedStatement>` that binds parameters.

```java
sql.execute("INSERT INTO players (uuid, name, coins) VALUES (?, ?, ?)",
    ps -> {
        ps.setString(1, uuid.toString());
        ps.setString(2, player.getName());
        ps.setInt(3, 100);
    });

sql.execute("UPDATE players SET coins = ? WHERE uuid = ?",
    ps -> {
        ps.setInt(1, 500);
        ps.setString(2, uuid.toString());
    });

sql.execute("DELETE FROM players WHERE uuid = ?",
    ps -> ps.setString(1, uuid.toString()));
```

For statements with no parameters, you can omit the preparer:

```java
sql.execute("CREATE TABLE IF NOT EXISTS players ("
    + "uuid VARCHAR(36) PRIMARY KEY, "
    + "name VARCHAR(16) NOT NULL, "
    + "coins INT DEFAULT 0, "
    + "level INT DEFAULT 1, "
    + "last_login BIGINT"
    + ")");
```

## Querying data

`query` returns an `Optional<R>`. It takes the SQL string, a preparer for binding parameters, and a `SqlFunction<ResultSet, R>` handler that maps the result set to your return type. If the handler returns `null` or an `SQLException` occurs, `Optional.empty()` is returned.

```java
Optional<Integer> coins = sql.query(
    "SELECT coins FROM players WHERE uuid = ?",
    ps -> ps.setString(1, uuid.toString()),
    rs -> {
        if (rs.next()) {
            return rs.getInt("coins");
        }
        return null;
    });

List<PlayerData> topPlayers = sql.query(
    "SELECT * FROM players ORDER BY coins DESC LIMIT 10",
    ps -> {},
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

For queries with no parameters, the preparer can be omitted:

```java
Optional<Integer> count = sql.query(
    "SELECT COUNT(*) FROM players",
    rs -> {
        rs.next();
        return rs.getInt(1);
    });
```

## Async operations

Every synchronous method has an async counterpart that returns a `Promise`:

| Synchronous | Asynchronous | Return type |
|---|---|---|
| `execute(String, SqlConsumer)` | `executeAsync(String, SqlConsumer)` | `Promise<Void>` |
| `execute(String)` | `executeAsync(String)` | `Promise<Void>` |
| `query(String, SqlConsumer, SqlFunction)` | `queryAsync(String, SqlConsumer, SqlFunction)` | `Promise<Optional<R>>` |
| `query(String, SqlFunction)` | `queryAsync(String, SqlFunction)` | `Promise<Optional<R>>` |
| `executeBatch(BatchBuilder)` | `executeBatchAsync(BatchBuilder)` | `Promise<Void>` |

```java
Events.subscribe(PlayerJoinEvent.class)
    .handler(e -> {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        sql.queryAsync(
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
            }).thenApplySync(data -> {
                if (data.isPresent()) {
                    applyPlayerData(player, data.get());
                } else {
                    createNewPlayer(uuid, player.getName());
                }
                return data;
            });
    })
    .bindWith(this);
```

## Batch operations

Use `BatchBuilder` when you need to execute the same statement many times with different parameters. This uses a single connection and `addBatch`/`executeBatch` under the hood.

```java
BatchBuilder batch = sql.batch("INSERT INTO players (uuid, name) VALUES (?, ?)");

for (Player player : Bukkit.getOnlinePlayers()) {
    batch.batch(ps -> {
        ps.setString(1, player.getUniqueId().toString());
        ps.setString(2, player.getName());
    });
}

sql.executeBatch(batch);
```

`BatchBuilder` also exposes:
- `execute()` to execute the batch directly (delegates to `sql.executeBatch(this)`)
- `reset()` to clear all handlers so the builder can be reused

If the batch contains only one handler, `executeBatch` optimizes by calling `execute` instead.

## Low-level access

For cases not covered by the helper methods:

```java
SqlStream stream = sql.stream();

Connection connection = sql.getConnection();

HikariDataSource hikari = sql.getHikari();
```

`getConnection()` returns a pooled connection that should be closed after use (returned to the pool). Prefer the `execute`/`query` helpers whenever possible.

## Cleanup

`Sql` implements `ISql`, which extends `Terminable` (which extends `AutoCloseable`). Calling `close()` shuts down the HikariCP connection pool. If the `Sql` instance is bound to a `TerminableConsumer` (such as the plugin itself), it will be closed automatically when the plugin disables.

## Complete example

```java
public class MyPlugin extends BasePlugin {

    private Sql sql;

    @Override
    protected DependencyContainer configureDependencies() {
        SqlCredentials credentials = SqlCredentials.of(
            getConfig().getString("database.host", "localhost"),
            getConfig().getInt("database.port", 3306),
            getConfig().getString("database.database", "minecraft"),
            getConfig().getString("database.user", "root"),
            getConfig().getString("database.password", "password")
        );

        this.sql = new Sql(null, null, credentials);

        return DependencyInjection.builder()
            .register(this)
            .register(Sql.class, sql)
            .build();
    }

    @Override
    protected void enable() {
        createTables();

        Events.subscribe(PlayerJoinEvent.class)
            .handler(this::loadPlayerData)
            .bindWith(this);

        Events.subscribe(PlayerQuitEvent.class)
            .handler(this::savePlayerData)
            .bindWith(this);
    }

    private void createTables() {
        sql.execute(
            "CREATE TABLE IF NOT EXISTS players ("
            + "uuid VARCHAR(36) PRIMARY KEY, "
            + "name VARCHAR(16) NOT NULL, "
            + "coins INT DEFAULT 0, "
            + "level INT DEFAULT 1"
            + ")");
    }

    private void loadPlayerData(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        sql.queryAsync(
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
            }).thenApplySync(data -> {
                if (data.isPresent()) {
                    playerDataCache.put(uuid, data.get());
                    Text.tell(player, "&aData loaded! Coins: " + data.get().getCoins());
                } else {
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
            sql.executeAsync(
                "INSERT INTO players (uuid, name, coins, level) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE name=?, coins=?, level=?",
                ps -> {
                    ps.setString(1, uuid.toString());
                    ps.setString(2, player.getName());
                    ps.setInt(3, data.getCoins());
                    ps.setInt(4, data.getLevel());
                    ps.setString(5, player.getName());
                    ps.setInt(6, data.getCoins());
                    ps.setInt(7, data.getLevel());
                });
        }
    }

    private void createNewPlayer(UUID uuid, String name) {
        sql.executeAsync("INSERT INTO players (uuid, name) VALUES (?, ?)",
            ps -> {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
            });
        playerDataCache.put(uuid, new PlayerData(uuid, name, 0, 1));
    }
}
```
