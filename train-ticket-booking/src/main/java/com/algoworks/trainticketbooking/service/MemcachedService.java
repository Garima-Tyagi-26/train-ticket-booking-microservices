package com.algoworks.trainticketbooking.service;

import org.springframework.stereotype.Service;

import net.spy.memcached.MemcachedClient;

@Service
public class MemcachedService {

	private final MemcachedClient memcachedClient;

	public MemcachedService(MemcachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}
	
	public void set(String key, String value, int expiry) {
	    try {
	        boolean success = memcachedClient.set(key, expiry, value).get();

	        System.out.println("CACHE SET: " + key + " -> " + success);

	    } catch (Exception e) {
	        throw new RuntimeException("Error setting cache", e);
	    }
	}

	public String get(String key) {

	    Object value = memcachedClient.get(key);

	    System.out.println("CACHE GET: " + key + " -> " + value);

	    if (value != null) {
	        return value.toString();
	    }

	    return null;
	}
}
