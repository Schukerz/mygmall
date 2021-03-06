package com.atguigu.dw.gmall.gmallpublisher2.service;

import java.util.Map;

public interface PublisherService {
    Long getDau(String date);
    /*
        hour: 10点  count: 100
        hour: 11点 count: 110
        hour: 12点 count: 120
        ...

        每行用 Map
        多行用List把每行封装起来

        List<Map> => Map<String, Long>

        10点 :100
        11点 : 110
        12点 :120
     */
    Map<String,Long> getHourDau(String date );

    Double getTotalAmount(String date);
    Map<String,Double> getHourAmount(String date);

}
