package net.cubespace.geSuit.configs.SubConfig;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.YamlConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
@SuppressWarnings("CanBeFinal")
public class Database extends YamlConfig {
    @Comment("Your Database Host")
    public String Host = "localhost";
    @Comment("Your Database name")
    public String Database = "minecraft";
    @Comment("Your Database Port")
    public String Port = "3306";
    @Comment("Database Username")
    public String Username = "username";
    @Comment("Database Password")
    public String Password = "password";
    @Comment("Database Threads - now deprecated")
    public Integer Threads = 5;
    @Comment("Database useSSL")
    public Boolean useSSL = false;
    @Comment("A list of optional properties for the Hikari Connection Pool see https://github" +
            ".com/brettwooldridge/HikariCP#configuration-knobs-baby.  You must use the correct " +
            "object type")
    public Map<String, Object> properties = new HashMap<>();
    
    public Database() {
        properties.putIfAbsent("cachePrepStmts", true);
        properties.putIfAbsent("prepStmtCacheSize", 250);
        properties.putIfAbsent("prepStmtCacheSqlLimit", 2048);
        properties.putIfAbsent("useServerPrepStmts", true);
        properties.putIfAbsent("useLocalSessionState", true);
        properties.putIfAbsent("rewriteBatchedStatements", true);
        properties.putIfAbsent("elideSetAutoCommits", true);
        properties.putIfAbsent("maintainTimeStats", false);
        properties.putIfAbsent("initializationFailTimeout", 10000);
        properties.putIfAbsent("maximumPoolSize", 20);
    }
}
