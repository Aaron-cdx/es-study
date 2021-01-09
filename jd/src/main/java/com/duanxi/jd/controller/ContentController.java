package com.duanxi.jd.controller;

import com.duanxi.jd.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author caoduanxi
 * @Date 2021/1/9 14:34
 * @Motto Keep thinking, keep coding!
 */
@RestController
public class ContentController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/goods/{keyword}")
    public String dataInElasticSearch(@PathVariable("keyword") String keyword) throws IOException {
        boolean b = contentService.parseContent(keyword);
        if (!b) {
            return "成功";
        }
        return "失败";
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public Object searchObjectForKeyWord(@PathVariable("keyword") String keyword,
                                         @PathVariable("pageNo") int pageNo,
                                         @PathVariable("pageSize") int pageSize) throws IOException {
        return contentService.highlightSearchElasticSearch(pageNo,pageSize,keyword);
    }
}
