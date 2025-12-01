package com.long_bus_distance.tickets.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configure ObjectMapper to handle Java 8 Date/Time and Generic Types
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Register MixIns for Spring Data objects
        objectMapper.addMixIn(PageImpl.class, PageImplMixin.class);
        objectMapper.addMixIn(PageRequest.class, PageRequestMixin.class);
        objectMapper.addMixIn(Sort.class, SortMixin.class);
        objectMapper.addMixIn(Sort.Order.class, OrderMixin.class); // Add this line for Sort.Order

        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Cấu hình serialize: Key là String, Value là JSON (để dễ đọc trong Redis)
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Cache tồn tại 30 phút
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .transactionAware()
                .build();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static abstract class PageImplMixin<T> {
        @JsonCreator
        public PageImplMixin(@JsonProperty("content") List<T> content,
                @JsonProperty("pageable") Pageable pageable,
                @JsonProperty("totalElements") long total) {
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static abstract class PageRequestMixin {
        @JsonCreator
        public PageRequestMixin(@JsonProperty("pageNumber") int page,
                @JsonProperty("pageSize") int size,
                @JsonProperty("sort") Sort sort) {
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static abstract class SortMixin {
        @JsonCreator
        public SortMixin(@JsonProperty("orders") List<Sort.Order> orders) {
        }

        @JsonProperty("orders")
        abstract List<Sort.Order> toList();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static abstract class OrderMixin {
        @JsonCreator
        public OrderMixin(@JsonProperty("direction") Sort.Direction direction,
                @JsonProperty("property") String property,
                @JsonProperty("ignoreCase") boolean ignoreCase,
                @JsonProperty("nullHandling") Sort.NullHandling nullHandling) {
        }
    }
}