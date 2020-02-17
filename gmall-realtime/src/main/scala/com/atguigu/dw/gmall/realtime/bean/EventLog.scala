package com.atguigu.dw.gmall.realtime.bean

import java.text.SimpleDateFormat
import java.util.Date

case class EventLog(mid: String,
                    uid: String,
                    appId: String,
                    area: String,
                    os: String,
                    logType: String,
                    eventId: String,
                    pageId: String,
                    nextPageId: String,
                    itemId: String,
                    ts: Long,
                    var logDate: String =null,
                    var logHour: String =null){
  private val sdf1 = new SimpleDateFormat("yyyy-MM-dd")
  private val sdf2 = new SimpleDateFormat("HH")
  private val date = new Date(ts)
  logDate=sdf1.format(date)
  logHour=sdf2.format(date)

}

