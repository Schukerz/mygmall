package com.atguigu.dw.gmall.realtime.app

import java.util

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.constant.GmallConstant
import com.atguigu.dw.gmall.realtime.bean.{AlertInfo, EventLog}
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Minutes, StreamingContext}
import org.apache.spark.{SparkConf, streaming}

object AlertApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName("AlertApp").setMaster("local[*]")
    val scc = new StreamingContext(conf,streaming.Seconds(5))
    //获取kafka中的事件日志
    val rawStream: DStream[(String, String)] = MyKafkaUtil.getKafkaStream(scc,GmallConstant.TOPIC_EVENT).window(Minutes(5))
    //封装为样例类
    val eventLogStream: DStream[EventLog] = rawStream.map {
      case (_, jsonString) => {
        JSON.parseObject(jsonString, classOf[EventLog])
      }
    }

    //预警需求
    //同一设备，5分钟内三次及以上用不同账号登录并领取优惠劵，
    // 并且在登录到领劵过程中没有浏览商品。
    // 同时达到以上要求则产生一条预警日志。
    // 同一设备，每分钟只记录一次预警。
    val midAndLogGroupedStream: DStream[(String, Iterable[EventLog])] = eventLogStream.map(log=>(log.mid,log)).groupByKey()
    val alertInfoStream: DStream[(Boolean, AlertInfo)] = midAndLogGroupedStream.map {
      case (mid, logIt) =>
        var uidSet = new util.HashSet[String]()
        var itemSet = new util.HashSet[String]()
        var eventList = new util.ArrayList[String]()
        var isClickEvent=false
        import scala.util.control.Breaks._
        breakable {
          logIt.foreach(log => {
            eventList.add(log.eventId)
            log.eventId match {
              case "coupon" =>
                uidSet.add(log.uid)
                itemSet.add(log.itemId)
              case "clickItem" =>
                isClickEvent = true
                break
              case _ =>
            }
          })
        }
        (uidSet.size() >= 3 && !isClickEvent,
          AlertInfo(mid, uidSet, itemSet, eventList, System.currentTimeMillis()))
    }
    import com.atguigu.dw.gmall.realtime.util.EsUtil._
    alertInfoStream
      .filter(_._1)
      .map(_._2)
      .foreachRDD(rdd=>{
      rdd.saveToES("gmall_coupon_alert")
    })


    alertInfoStream.print(100)
    scc.start()
    scc.awaitTermination()

  }

}
