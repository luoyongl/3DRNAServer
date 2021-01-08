package com.wtu.sj.ly.rna.utils;

import com.xxl.conf.core.XxlConfClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author ：LY
 * @date ：Created in 2020/5/10 7:33
 * @description：
 * @modified By：
 * @version: $
 */
public class RmsdUtils {
    private static final Logger logger = LoggerFactory.getLogger(RmsdUtils.class);

    public static String rmsd(String py, String file1, String file2) {

        String pythonPath = XxlConfClient.get("default.pyexe.path");
        logger.info("开始计算Rmsd,计算脚本:{},文件1:{},文件2:{}", file1, file2);
        String[] arguments = new String[]{pythonPath, py, file1, file2};
        String line;
        String res = "";
        try {
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            while ((line = in.readLine()) != null) {
                res = line;
            }
            in.close();
            //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功返回值为1表示调用python脚本失败
            logger.info("计算状态{},计算结果{}", process.waitFor(), res);
        } catch (Exception e) {
            e.printStackTrace();
            res = e.getMessage();
        }
        return res;
    }

    public static void main(String[] args) {
        String py = "F://study//3DRNAServer//target//classes//static//py//rmsd.py";
        String file1 = "F:/study/3DRNAServer/target/classes/static/download/20210108/4364891b-f7be-4ef3-9954-a9e2b734513aSelectAlign.pdb";
        String file2 = "F:/study/3DRNAServer/target/classes/static/download/20210108/4364891b-f7be-4ef3-9954-a9e2b734513aSelect.pdb";
        RmsdUtils.rmsd(py, file1, file2);
    }
}
