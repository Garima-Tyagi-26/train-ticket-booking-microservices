package com.algoworks.trainticketbooking.config;

import java.net.InetSocketAddress;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.spy.memcached.MemcachedClient;

@Configuration
public class MemcachedConfig {

	    @Bean
	    public MemcachedClient memcachedClient() throws Exception {
	        return new MemcachedClient(new InetSocketAddress("localhost", 11211));
	    }
}
