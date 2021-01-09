package com.duanxi.jd.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author caoduanxi
 * @Date 2021/1/9 13:47
 * @Motto Keep thinking, keep coding!
 */
@Controller
public class IndexController {
    @GetMapping({"/", "/index"})
    public String getIndex() {
        return "index";
    }

}
