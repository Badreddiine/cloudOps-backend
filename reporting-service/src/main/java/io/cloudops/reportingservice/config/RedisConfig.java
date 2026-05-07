package io.cloudops.reportingservice.config;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration du cache Redis avec TTL personnalisé par type de données.
 *
 * Caches définis :
 *  - "dashboard"  : 5 min  (données globales, invalidées par events Kafka)
 *  - "metrics"    : 5 min  (données filtrées)
 *  - "sla-stats"  : 2 min  (changent plus souvent)
 *  - "team-stats" : 10 min (données moins volatiles)
 */
@Configuration
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {

        // Sérialisation JSON (lisible dans Redis CLI)
        RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // Config par défaut : 5 min, JSON
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
            )
            .disableCachingNullValues();

        // TTLs spécifiques par cache
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("dashboard",  defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("metrics",    defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("sla-stats",  defaultConfig.entryTtl(Duration.ofMinutes(2)));
        configs.put("team-stats", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(configs)
            .transactionAware()
            .build();
    }
}
