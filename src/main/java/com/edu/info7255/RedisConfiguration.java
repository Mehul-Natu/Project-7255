package com.edu.info7255;

import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

@Configuration
public class RedisConfiguration {

    private static ConnectionPoolConfig poolConfig;

    public static void configure() {
        if (poolConfig == null) {
            poolConfig = buildPoolConfig();
        }
    }

    public static JedisPooled getResources() {
        if (poolConfig == null)
            configure();
        return new JedisPooled(poolConfig, "localhost", 6379);
    }

    /*
    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration standaloneConf = new RedisStandaloneConfiguration();
        standaloneConf.setHostName("localhost");
        standaloneConf.setPort(6379);
        return new JedisConnectionFactory(standaloneConf);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

     */


    private static ConnectionPoolConfig buildPoolConfig() {
        final ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        /*poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
         */
        return poolConfig;
    }
}
