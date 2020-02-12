package com.atguigu.dw.gmall.realtime.util

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

object RedisUtil {

  private val host: String = PropertiesUtil.getProperty("redis.host")
  private val port: Int = PropertiesUtil.getProperty("redis.port").toInt
  private val config: JedisPoolConfig = new JedisPoolConfig
  config.setMaxTotal(100)
  config.setMaxIdle(40)
  config.setMinIdle(10)
  config.setBlockWhenExhausted(true)
  config.setMaxWaitMillis(1000 * 60 * 2)
  config.setTestOnBorrow(true)
  config.setTestOnReturn(true)
  private val jedisPool = new JedisPool(config,host,port)

  def getRedisClient={
  jedisPool.getResource
  }

}
