package com.oss.ffcs.redis.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/**
 * Redis连接池工具类
 * @author Administrator
 *
 */
public final class RedisUtil {

    // Redis服务器IP
    private static String ADDR = "192.168.32.130";

    // Redis的端口号
    private static int PORT = 6379;

    // 访问密码
    private static String AUTH = "linxq";

    // 可用连接实例的最大数目，默认值为8；
    // 如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
    private static int MAX_ACTIVE = 1024;

    // 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = 200;

    // 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
    private static int MAX_WAIT = 10000;

    private static int TIMEOUT = 10000;

    // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = true;
   
    // 缓存生存时间 
    private final int expire = 60000;  
    
    private static JedisPool jedisPool = null;
    
    private static JedisPoolConfig config = null;

    /**
     * 初始化Redis连接池
     */
    static {
        try {
            // 在高版本的jedis
            // jar包，比如2.8.2，我们在使用中发现使用JedisPoolConfig时，没有setMaxActive和setMaxWait属性了，这是因为高版本中官方废弃了此方法，用以下两个属性替换。
            // maxActive ==> maxTotal
            // maxWait ==> maxWaitMillis
        	config = new JedisPoolConfig();
            config.setMaxTotal(MAX_ACTIVE);
            config.setMaxIdle(MAX_IDLE);
            config.setMaxWaitMillis(MAX_WAIT);
            config.setTestOnBorrow(TEST_ON_BORROW);
            jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, AUTH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Jedis实例
     * 
     * @return
     */
    public synchronized static Jedis getJedis() {
    	Jedis resource = null;
        try {
            if (jedisPool == null) {
            	 jedisPool = new JedisPool(config, ADDR, PORT, TIMEOUT, AUTH);
            }
            resource = jedisPool.getResource();
            
             
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resource;
    }

    
    /**
     * 释放 jedis资源
     *
     * @param jedis
     */
    public static void close(final Jedis jedis) {
    	if(jedis != null){
    		jedis.close(); 
    	}
    }
    
    public static void returnJedis(Jedis jedis){
    	if(jedisPool != null){
    		jedisPool.returnResource(jedis);
    	}
    }
    
    /**
     * 设置过期时间
     * @param key
     * @param seconds
     */
    public static void expire(String key, int seconds){
    	if(seconds <= 0){
    		return ;
    	}
    	Jedis jedis = getJedis();
    	jedis.expire(key, seconds);
    	returnJedis(jedis);
    	
    }
    
    /**
     * 默认设置过期时间
     * @param key
     */
    public void expire(String key){
    	expire(key, expire);
    }
    
    
    
}