package com.atguigu.dw.gmall.realtime.util

import com.atguigu.dw.gmall.realtime.bean.AlertInfo
import io.searchbox.client.{JestClient, JestClientFactory}
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.core.{Bulk, Index}
import org.apache.spark.rdd.RDD

object EsUtil {

val esUrl="http://hadoop102:9200"
  //1.创建客户端工厂
  private val factory: JestClientFactory = new JestClientFactory

  var conf: HttpClientConfig = new HttpClientConfig.Builder(esUrl)
      .maxTotalConnection(100)
      .connTimeout(1000*100)
      .readTimeout(1000*100)
      .multiThreaded(true)
    .build()

  factory.setHttpClientConfig(conf)
  //返回一个客户端对象
  def getClient()=factory.getObject
  //批量插入
  def insertBulk(index:String,sources:TraversableOnce[Any])={
val client: JestClient = getClient()
    val builder: Bulk.Builder = new Bulk.Builder()
      .defaultIndex(index)
      .defaultType("_doc")
    sources.foreach{
      case (s,id:String)=>
        val action: Index = new Index.Builder(s).id(id).build()
        builder.addAction(action)
      case(s)=>
        val action: Index = new Index.Builder(s).build()
        builder.addAction(action)
    }
    client.execute(builder.build())
    client.shutdownClient()

  }
  //插入单条数据
  def insertSingle(index:String,source:Any,id:String=null)=
  {
    val client: JestClient = getClient()
    val action: Index = new Index.Builder()
      .index(index)
      .`type`("_doc")
      .id(id)
      .build()
    client.execute(action)
    client.shutdownClient()
  }
  implicit class EsFunction(rdd:RDD[AlertInfo]){
    def saveToES(index:String)={
      rdd.foreachPartition((alertInfo:Iterator[AlertInfo])=>{
        val result: Iterator[(AlertInfo, String)] = alertInfo.map(info =>
          (info, info.mid + "_" + info.ts / 1000 / 60))

        EsUtil.insertBulk("gmall_coupon_alert",result)
      })
    }
  }
}
