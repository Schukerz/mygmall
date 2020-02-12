package com.atguigu.dw.gmallpublisher.mapper;

import java.util.List;
import java.util.Map;

//从数据库中查询数据
public interface DauMapper {
    //查询日活总数
    long getDau(String date);

}
