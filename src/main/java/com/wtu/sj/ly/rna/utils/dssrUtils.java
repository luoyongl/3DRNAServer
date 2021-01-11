package com.wtu.sj.ly.rna.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author ：luoYong
 * @date ：Created in 2021/1/11 20:29
 * @description：
 */
public class dssrUtils{
    private static final Logger logger = LoggerFactory.getLogger(dssrUtils.class);
    public static void dssrJson(String exePath,String filePath,String outputPath) throws FileNotFoundException {
        System.out.println(filePath);
        String dataStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String path = ResourceUtils.getURL("classpath:").getPath()+"static/download/" + dataStr+"/"+outputPath;
        String script = exePath+"\\"+"x3dna-dssr.exe"+" -i="+filePath.substring(1)+" --json "+ "-o="+path.substring(1);
        logger.info("dssr命令:{}",script);
        try {
            Process process = Runtime.getRuntime().exec(script);
        } catch (Exception e) {
        }
    }


    public static void main(String[] args) throws IOException {
        String str ="F:\\study\\3DRNAServer\\target\\classes\\static\\py\\x3dna-dssr.exe -i=F:/study/3DRNAServer/target/classes/static/download/20210111/96eb50e8-13e5-440b-8160-07b620dbc6f0.pdb --json -o=F:/study/3DRNAServer/target/classes/static/download/20210111/a.json";
        Process process = Runtime.getRuntime().exec(str);
    }
}
