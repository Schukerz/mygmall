package com.atguigu.dw.gmalllogger.controller;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
//@Controller
//@ResponseBody
public class LoggerController {
    @GetMapping("/log")
    public String doLog(@RequestParam("log") String log){
        System.out.println("log");
        return "success";
    }

    //1.添加时间戳
    /**
     * 添加时间戳
     * @param logObj
     * @return
     */
    public JSONObject addTS(JSONObject logObj){
        logObj.put("ts", System.currentTimeMillis());
        return logObj;
    }

    //2.日志落盘
    // 初始化 Logger 对象
    private final Logger logger = LoggerFactory.getLogger(LoggerController.class);
    /**
     * 日志落盘
     * 使用 log4j
     * @param logObj
     */
    public void saveLog(JSONObject logObj) {
        logger.info(logObj.toJSONString());
    }


}
