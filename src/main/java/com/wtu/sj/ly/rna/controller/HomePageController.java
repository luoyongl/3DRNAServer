package com.wtu.sj.ly.rna.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author ：LY
 * @date ：Created in 2020/2/28 20:09
 * @description：
 * @modified By：
 * @version: $
 */

@RestController("/")
public class HomePageController {

    @RequestMapping("/")
    public ModelAndView goHome(){
        ModelAndView mv = new ModelAndView("index");
        return mv;
    }
    @RequestMapping("/toPage")
    public ModelAndView toPage(@RequestParam("page") String page, @RequestParam("url") String url){
        ModelAndView mv = new ModelAndView(page);
        if (url!=null||url!=""){
            mv.addObject("key", url);
        }
        return mv;
    }

}
