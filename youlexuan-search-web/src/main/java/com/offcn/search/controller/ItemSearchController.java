package com.offcn.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/search")
public class ItemSearchController {
    @Reference
    private ItemSearchService itemSearchService;


    @RequestMapping("/searchItem")
    public Map<String,Object> searchItem(@RequestBody Map searchMap){//接收前端传过来的json字符串时要加请求注解

        return itemSearchService.search(searchMap);

    }
}
