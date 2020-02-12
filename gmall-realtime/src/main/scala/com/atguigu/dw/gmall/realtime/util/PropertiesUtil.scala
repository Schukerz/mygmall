package com.atguigu.dw.gmall.realtime.util

import java.io.InputStream
import java.util.Properties

object PropertiesUtil {
  private val is: InputStream = PropertiesUtil.getClass.getClassLoader.getResourceAsStream("config.properties")
  private val properties = new Properties()
  properties.load(is)
  def getProperty(propertyName:String)={
    properties.getProperty(propertyName)
  }
}
