package com.atguigu.dw.gmall.gmallpublisher2.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.dw.gmall.gmallpublisher2.service.PublisherService;
import com.atguigu.dw.gmall.gmallpublisher2.service.PublisherServiceImp;
import org.jcodings.util.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.toJSONString;

@RestController
public class PublisherController {
    /* http://localhost:8070/realtime-total?date=2020-02-11

         [{"id":"dau","name":"新增日活","value":1200},
         {"id":"new_mid","name":"新增设备","value":233} ]

         想得到json数组, 可以在代码中创建java的数组(集合..), 让后统计
         json统计直接转换成json数组
      */
    @Autowired
    public PublisherService service ;

    @GetMapping("/realtime-total")
    public String getAmount(@RequestParam("date") String date){
        List<Map<String,String>> result = new ArrayList<>();
        HashMap<String,String> map1 = new HashMap<>();
        map1.put("id","dau");
        map1.put("name","新增日活");
        map1.put("value",service.getDau(date)+"");
        result.add(map1);

        HashMap<String,String> map2 = new HashMap<>();
        map2.put("id","new_mid");
        map2.put("name","新增设备");
        map2.put("value","233");
        result.add(map2);

        //{"id":"order_amount","name":"新增交易额","value":1000.2 }]
        HashMap<String,String> map3 = new HashMap<>();
        map3.put("id","order_amount");
        map3.put("name","新增交易额");
        map3.put("value",service.getTotalAmount(date)+"");
        result.add(map3);

       return JSON.toJSONString(result);
//        return "ok";

    }

    /*
    http://localhost:8070/realtime-hour?id=dau&date=2020-02-11
    {"yesterday":{"11":383,"12":123,"17":88,"19":200 },
        "today":{"12":38,"13":1233,"17":123,"19":688 }}

     */

    @GetMapping("/realtime-hour")
    public String getHourCount(@RequestParam("id") String id,@RequestParam("date") String date){

        if("dau".equals(id)){
            Map<String, Long> today = service.getHourDau(date);
            Map<String, Long> yesterday = service.getHourDau(getYesterday(date));

            HashMap<String,Map<String,Long>> result = new HashMap<>();
            result.put("today",today);
            result.put("yesterday",yesterday);
            return JSON.toJSONString(result);

            //{"yesterday":{"11":383,"12":123,"17":88,"19":200 },
            //"today":{"12":38,"13":1233,"17":123,"19":688 }}
        }else if("order_amount".equals(id)){
            Map<String, Double> today = service.getHourAmount(date);
            Map<String, Double> yesterday = service.getHourAmount(getYesterday(date));

            HashMap<String,Map<String,Double>> result = new HashMap<>();
            result.put("today",today);
            result.put("yesterday",yesterday);
            return JSON.toJSONString(result);
        }
        return null;
    }

    private String getYesterday(String date){
        return LocalDate.parse(date).minusDays(1).toString();
    }
}
