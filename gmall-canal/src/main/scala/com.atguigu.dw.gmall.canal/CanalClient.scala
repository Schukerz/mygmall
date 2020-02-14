package com.atguigu.dw.gmall.canal

import java.net.{InetSocketAddress, SocketAddress}
import java.util

import com.alibaba.otter.canal.client.{CanalConnector, CanalConnectors}
import com.alibaba.otter.canal.protocol.CanalEntry.{EntryType, RowChange, RowData}
import com.alibaba.otter.canal.protocol.{CanalEntry, Message}
import com.google.protobuf.ByteString

/*

1. 从canal服务器读取数据   order_info

    1. 创建一个canal的客户端

    2. 订阅数据

    3. 连接到canal服务器

    4. 服务器返回数据, 客户端对返回的数据做解析  难点
        数据封装了很多层, 需要一层层的展开
        Message 一次拉一个Message, 一个Message可以看成是由多条sql语句执行的结果
        Entry  实体, 一个Message会封装多个Entry , 一个Entry可以看成是由1 条sql执行的结果   (会对表中的多行数据产生影响)
            StoreValue 存储的值, 一个Entry内部封装了一个StoreValue, 序列化的值
        RowChange 行变化. 一个SoreValue中有1个RowChange, 表示一个sql导致的多行的数据的变化
            RowData 行数据. 一个RowChange中封装了多个RowData, 一个RowData表示一行变化后的数据
        Column 列. 一个RowData中会封装多个Column
            列名和列值

2. 把读到数据变成json字符串写入到kafka中
        {"c1": "v1", "c2": "v2", ...  }

* */
object CanalClient {
  def main(args: Array[String]): Unit = {
    val address: SocketAddress = new InetSocketAddress("hadoop102",11111)
    //1.创建客户端连接器
    val connector: CanalConnector = CanalConnectors.newSingleConnector(address,"example","","")

    //2.连接
    connector.connect()

    //订阅数据,获取gmall这个数据库下的所有请求
    connector.subscribe("gmall.*")

    //3.获取数据
    import scala.collection.JavaConversions._
    while(true){
    val message: Message = connector.get(1)//表示最多有多少条语句发生了变化
    val entries: util.List[CanalEntry.Entry] = if(message == null) message.getEntries else null
      if((entries != null) && (!entries.isEmpty)){
      for(entry <- entries){
        if(entry!=null && entry.getEntryType==EntryType.ROWDATA){
          val value: ByteString = entry.getStoreValue
          val rowChange: RowChange = RowChange.parseFrom(value)
          val datasList: util.List[RowData] = rowChange.getRowDatasList
          CanalHandler.handle(entry.getHeader.getTableName,datasList,rowChange.getEventType)

        }else{
          print("没有拉取到数据,2s后继续...")
          Thread.sleep(2000)
        }
      }
      }

    }
  }
}
