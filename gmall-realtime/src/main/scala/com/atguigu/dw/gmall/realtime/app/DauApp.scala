package com.atguigu.dw.gmall.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.constant.GmallConstant
import com.atguigu.dw.gmall.realtime.bean.StartupLog
import com.atguigu.dw.gmall.realtime.util.{MyKafkaUtil, RedisUtil}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.{SparkConf, streaming}
import redis.clients.jedis.Jedis
import org.apache.phoenix.spark._
object DauApp {
  def main(args: Array[String]): Unit = {

    //1.从kafka中读取数据
    val conf: SparkConf = new SparkConf().setAppName("DauApp").setMaster("local[*]")
    val ssc = new StreamingContext(conf,streaming.Seconds(3))
    val rawStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(ssc,GmallConstant.TOPIC_STARTUP)
    //2.把数据解析,然后封装在样例类中
    val startupLogStream: DStream[StartupLog] = rawStream.map {
      case (_, v) => JSON.parseObject(v, classOf[StartupLog])
    }
    //3.去重
    //3.1从redis中获取语句启动地记录,把启动地过滤掉
    val filteredStream: DStream[StartupLog] = startupLogStream.transform(rdd => {
      //3.2读取redis中已经启动的记录
      val client: Jedis = RedisUtil.getRedisClient
      val midSet: util.Set[String] = client.smembers(GmallConstant.TOPIC_STARTUP + ":" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
      client.close()
      val bd: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
      //3.3过滤掉那些已经启动过的记录
      println("hello")
      rdd.filter(log => {
        !bd.value.contains(log.mid)
      })
        .map(log => (log.mid, log))
        .groupByKey()
        .map {
          case (_, logIt) => logIt.toList.minBy(_.ts)
        }
    })
    //3.4把第一次启动的设备写入到redis中
    filteredStream.foreachRDD(rdd=>{
      rdd.foreachPartition(it=>{
        //获取redis的连接
      val client: Jedis = RedisUtil.getRedisClient
        //写mid到redis中
        it.foreach(log=>{
          client.sadd(GmallConstant.TOPIC_STARTUP+":"+log.logDate,log.mid)
        })
        //关闭连接
        client.close()
      })

    })
    filteredStream.print()
    //4.写入到hbase
    filteredStream.foreachRDD(rdd =>{
    //1.提前在phoenix中创建要保存的表
    //2.保存
      //def saveToPhoenix(tableName : scala.Predef.String,
      // cols : scala.Seq[scala.Predef.String],
      // conf : org.apache.hadoop.conf.Configuration = { /* compiled code */ },
      // zkUrl : scala.Option[scala.Predef.String] = { /* compiled code */ },
      // tenantId : scala.Option[scala.Predef.String] = { /* compiled code */ }) : scala.Unit = { /* compiled code */ }
    rdd.saveToPhoenix(
      "gmall_dau",
      Seq("MID", "UID", "APPID", "AREA", "OS", "CHANNEL", "LOGTYPE", "VERSION", "TS", "LOGDATE", "LOGHOUR"),
      zkUrl=Some("hadoop102,hadoop103,hadoop104;2181")
    )
    })

    //启动流
    ssc.start()
    ssc.awaitTermination()

 }
}
