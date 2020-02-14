package com.atguigu.dw.gmall.canal

import java.util

import com.alibaba.fastjson.JSONObject
import com.alibaba.otter.canal.protocol.CanalEntry
import com.alibaba.otter.canal.protocol.CanalEntry.EventType
import com.atguigu.dw.gmall.common.constant.GmallConstant

import scala.collection.JavaConversions._

object CanalHandler {
def handle(tableName :String,rowsDatas:java.util.List[CanalEntry.RowData],
           eventType:CanalEntry.EventType) ={
    val jsonObject = new JSONObject
    if("order_info".equals(tableName) && rowsDatas!=null
        && !rowsDatas.isEmpty && eventType == EventType.INSERT){
      for(rowData <- rowsDatas){
        val columns: util.List[CanalEntry.Column] = rowData.getAfterColumnsList
        for(column <- columns){
          val key: String = column.getName
          val value: String = column.getValue
          jsonObject.put(key,value)
        }
      }
    }
  MyKafkaUtil.send(GmallConstant.TOPIC_ORDER,jsonObject.toJSONString)
}
}
