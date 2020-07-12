package com.wtu.sj.ly.rna.Utils;

import com.xxl.conf.core.XxlConfClient;

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
    public static String rmsd(String py,String file1,String file2) {

//        String pythonpath="C://Users//m1887//AppData//Local//Programs//Python//Python38//python.exe";
//        String pypath="F://pyCharm-workspace//calculate_rmsd.py";
//        String[] arguments = new String[] {pythonpath,py,
//                "F://pyCharm-workspace//ci2_1.pdb",
//                "F://pyCharm-workspace//ci2_2.pdb"};
        //python 执行文件路径
        String pythonpath= XxlConfClient.get("default.pyexe.path", null);
        String[] arguments = new String[] {pythonpath,py, file1, file2};
        String line = null;
        String res="";
        try {
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            while ((line = in.readLine()) != null) {
                res=line;
                System.out.println(line);
            }
            in.close();
            //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
            //返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
            int re = process.waitFor();
            System.out.println("状态"+re);
        } catch (Exception e) {
            e.printStackTrace();
            res=e.getMessage();
        }
        return res;
    }

    public static void main(String[] args) {
        String py="F://Idea-workspace//RNAServer//src//main//resources//static//py/rmsd.py";
        String file1="F://Idea-workspace//RNAServer//src//main//resources//static//py//1AFX.pdb";
        String file2="F:/Idea-workspace/RNAServer/target/classes/static/download/20200512/79759b9b-b4af-46bf-96c0-9371bfc6f28fSelectAlign.pdb";
       RmsdUtils.rmsd(py,file1,file2);
        //  read("C://Users//m1887//Desktop//pdb//1ajf.pdb");
       // String a="abcd";
      //  System.out.println(a.substring(1));
    }



}
