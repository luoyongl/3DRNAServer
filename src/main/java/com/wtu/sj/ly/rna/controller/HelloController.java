package com.wtu.sj.ly.rna.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：LY
 * @date ：Created in 2019/12/17 16:16
 * @description：
 * @modified By：
 * @version: $
 */
@RestController
public class HelloController {

/*    @GetMapping("/index")
    public ModelAndView sayHello(){
        ModelAndView mv = new ModelAndView("index");
        return mv;
    }*/

    public static void main(String[] args) throws IOException, InterruptedException {
    String result=null;
        String[] arguments = new String[] {"C://Users//m1887//AppData//Local//Programs//Python//Python38//python.exe",
                "F://pyCharm-workspace//calculate_rmsd.py","F://pyCharm-workspace//ci2_1.pdb",
                "F://pyCharm-workspace//ci2_2.pdb"};
        try {
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
            //返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
            int re = process.waitFor();
            System.out.println(re);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //测试mcfold爬取
    public static void test1() throws IOException {
        Document doc = Jsoup.connect("https://www.major.iric.ca/cgi-bin/MC-Fold/mcfold.static.cgi?pass=lucy&sequence=UCGGACCAUCAGGAGAAAUCC&top=20&explore=15&name=&mask=&singlehigh=&singlemed=&singlelow=")
                .timeout(300000).get();
        //Document doc = Jsoup.parse("");
        List<String> titles = new ArrayList<String>();
        List<String> urls = new ArrayList<String>();
        Element elements = doc.select("pre").last();
        elements.children().select("a").remove();

        String s1=elements.text().replaceAll("\\[.*?\\]","");
        Elements e=elements.children();
        String s=elements.text();

        String[] split = s1.split("\n|\r");

        System.out.println(elements);
        //System.out.println(s1);
        for (String s2:split){
            System.out.println(s2);
        }
    }

    //测试mcfold 展示
    public static void test2() throws IOException {
        String viewUrl="https://major.iric.ca/cgi-bin/2DRender/render.cgi?structure=%3Estructure|UCGGACCAUCAGGAGAAAUCC|..(((...)))((((..))))&structno=1";
        Document doc= Jsoup.connect(viewUrl).timeout(300000).get();
        System.out.println(doc.select("a").eq(2));
    }



}
