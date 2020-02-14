package com.atguigu.dw.gmall.gmallpublisher2.mapper;

import java.util.List;
import java.util.Map;

public interface OrderMapper {

    Double getTotalAmount(String date);

    List<Map> getHourAmount(String date);
}
