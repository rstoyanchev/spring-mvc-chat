package org.springframework.samples.async.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.samples.async.chat.ChatController;

@Configuration
@PropertySource("classpath:redis.properties")
public class RootConfig {

	@Autowired
	Environment env;

	@Autowired
	ChatController chatController;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		JedisConnectionFactory cf = new JedisConnectionFactory();
		cf.setHostName(this.env.getProperty("redis.host"));
		cf.setPort(this.env.getProperty("redis.port", int.class));
		cf.setPassword(this.env.getProperty("redis.password"));
		return cf;
	}

	@Bean
	public StringRedisTemplate redisTemplate() {
		return new StringRedisTemplate(redisConnectionFactory());
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer() {
		RedisMessageListenerContainer mlc = new RedisMessageListenerContainer();
		mlc.setConnectionFactory(redisConnectionFactory());
		mlc.addMessageListener(this.chatController , new PatternTopic("chat"));
		return mlc;
	}

}
