package com.qingclass.squirrel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 *
 * @author 苏天奇
 * */
@Configuration
public class RedisConfiguration {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Bean(name = "jedisPool")
	@Autowired
	public JedisPool jedisPool(@Qualifier("jedisPoolConfig") JedisPoolConfig config,
			@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port,
			@Value("${spring.redis.timeout}") int timeout, @Value("${spring.redis.password:}") String password) {
		
		logger.info("timeout---------------------------->:{}",timeout);
		
		if(!StringUtils.isEmpty(password)) {
			return new JedisPool(config, host, port, timeout, password);
		}else {
			return new JedisPool(config,host,port,timeout);
		}
		
	}

	@Bean(name = "jedisPoolConfig")
	public JedisPoolConfig jedisPoolConfig(
			@Value("${spring.redis.pool.max-active}") int maxTotal,
			@Value("${spring.redis.pool.max-idle}") int maxIdle,
			@Value("${spring.redis.pool.max-wait}") int maxWaitMillis) {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(maxTotal);
		config.setMaxIdle(maxIdle);
		config.setMaxWaitMillis(maxWaitMillis);
		return config;
	}
}
