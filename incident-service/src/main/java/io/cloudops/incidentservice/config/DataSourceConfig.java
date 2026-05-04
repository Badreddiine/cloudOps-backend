package io.cloudops.incidentservice.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    // ─── EU ──────────────────────────────────────────────────────────────────
    @Value("${oracle.eu.url}")
    private String euUrl;
    @Value("${oracle.eu.username}")
    private String euUsername;
    @Value("${oracle.eu.password}")
    private String euPassword;

    // ─── AF ──────────────────────────────────────────────────────────────────
    @Value("${oracle.af.url}")
    private String afUrl;
    @Value("${oracle.af.username}")
    private String afUsername;
    @Value("${oracle.af.password}")
    private String afPassword;

    // ─── US ──────────────────────────────────────────────────────────────────
    @Value("${oracle.us.url}")
    private String usUrl;
    @Value("${oracle.us.username}")
    private String usUsername;
    @Value("${oracle.us.password}")
    private String usPassword;

    // ─── Beans ───────────────────────────────────────────────────────────────

    @Bean(name = "euDataSource")
    public DataSource euDataSource() {
        return buildHikari(euUrl, euUsername, euPassword, "IncidentPool-EU", 20);
    }

    @Bean(name = "afDataSource")
    public DataSource afDataSource() {
        return buildHikari(afUrl, afUsername, afPassword, "IncidentPool-AF", 10);
    }

    @Bean(name = "usDataSource")
    public DataSource usDataSource() {
        return buildHikari(usUrl, usUsername, usPassword, "IncidentPool-US", 10);
    }

    @Bean(name = "routingDataSource")
    @Primary
    public DataSource routingDataSource() {
        Map<Object, Object> targets = new HashMap<>();
        targets.put("eu", euDataSource());
        targets.put("af", afDataSource());
        targets.put("us", usDataSource());

        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(targets);
        routing.setDefaultTargetDataSource(euDataSource()); // EU = primary
        routing.afterPropertiesSet();
        return routing;
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private HikariDataSource buildHikari(String url, String username,
                                         String password, String poolName,
                                         int maxPoolSize) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("oracle.jdbc.OracleDriver");
        ds.setPoolName(poolName);
        ds.setMinimumIdle(2);
        ds.setMaximumPoolSize(maxPoolSize);
        ds.setConnectionTimeout(30000);
        ds.setConnectionTestQuery("SELECT 1 FROM DUAL");
        return ds;
    }
}