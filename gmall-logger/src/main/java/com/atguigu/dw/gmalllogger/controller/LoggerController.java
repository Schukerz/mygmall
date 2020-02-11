package com.atguigu.dw.gmalllogger.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.dw.gmall.common.constant.GmallConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoggerController {
    @PostMapping("/log")
    public String doLog(@RequestParam("log") String log){
        //1.添加时间戳
        log = addTS(log);

        //2.日志落盘
        save2file(log);

        //3.把日志写入到kafka
        send2kafka(log);

        return "ok";
    }


    /**
     * 添加时间戳
     * @param logObj
     * @return
     */

    private String addTS(String log){
        JSONObject jsonObj = JSON.parseObject(log);
        jsonObj.put("ts",System.currentTimeMillis());
        return jsonObj.toJSONString();
    }

    //创建一个可以写入日志的logger对象
    Logger logger = LoggerFactory.getLogger(LoggerController.class);
    //把文件保存到日志中
    private void save2file(String log){
        logger.info(log);
    }



    //对象的自动注入
    @Autowired
    private KafkaTemplate<String,String> kafka;
    private void send2kafka(String log){
        String topic= GmallConstant.TOPIC_STARTUP;
        if(log.contains("event")){
            topic=GmallConstant.TOPIC_EVENT;
        }
        kafka.send(topic,log);

    }
}
