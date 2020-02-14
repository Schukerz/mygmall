package com.atguigu.dw.gmall.gmallpublisher2.service;

import com.atguigu.dw.gmall.gmallpublisher2.mapper.DauMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublisherServiceImp implements PublisherService {

    @Autowired
    DauMapper dauMapper;

    @Override
    public Long getDau(String date) {
        //从数据层读取数据,然后给控制层使用
        return dauMapper.getDau(date);

    }

    @Override
    public Map<String, Long> getHourDau(String date) {
        /*
        List<Map<hour: 10, count: 100; >
        Map<10: 100;  11: 110>
         */
        List<Map> mapList = dauMapper.getHourDau(date);
        HashMap<String,Long> result = new HashMap<>();
        for (Map map: mapList) {
            String loghour = (String)map.get("LOGHOUR");
            //System.out.println("loghour:"+loghour);
            Long count = (Long)map.get("COUNT");
            //System.out.println("count:"+count);
            result.put(loghour,count);
        }

        return result;
    }

}
