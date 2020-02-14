package com.atguigu.dw.gmall.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.dw.gmall.common.constant.GmallConstant
import com.atguigu.dw.gmall.realtime.bean.OrderInfo
import com.atguigu.dw.gmall.realtime.util.MyKafkaUtil
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.{SparkConf, streaming}

object OrderApp {

  def main(args: Array[String]): Unit = {

    val conf: SparkConf = new SparkConf().setAppName("OrderApp").setMaster("local[*]")
    val scc = new StreamingContext(conf,streaming.Seconds(3))
    //从kafka中消费数据
    val sourceStream: InputDStream[(String, String)] = MyKafkaUtil.getKafkaStream(scc,GmallConstant.TOPIC_ORDER)
    //对数据进行封装
    val orderInfoStream: DStream[OrderInfo] = sourceStream.map {
      case (_, jsonString) => JSON.parseObject(jsonString, classOf[OrderInfo])
    }
    //写入到hbase中
    import org.apache.phoenix.spark._
    orderInfoStream.foreachRDD(rdd=>{
      rdd.foreach(println)
      rdd.saveToPhoenix("gmall_order_info",
        Seq("ID", "PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT",
          "CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY", "USER_ID", "IMG_URL",
          "TOTAL_AMOUNT", "EXPIRE_TIME", "DELIVERY_ADDRESS", "CREATE_TIME",
          "OPERATE_TIME", "TRACKING_NO",
          "PARENT_ORDER_ID", "OUT_TRADE_NO", "TRADE_BODY", "CREATE_DATE", "CREATE_HOUR"),
        zkUrl=Some("hadoop102,hadoop103,hadoop104:2181")

      )
    })
    scc.start()
    scc.awaitTermination()
  }
}
