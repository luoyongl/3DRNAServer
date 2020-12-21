package com.wtu.sj.ly.rna.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：LY
 * @date ：Created in 2020/5/12 6:28
 * @description：
 * @modified By：
 * @version: $
 */
public class WriteFileUtils {
    public static void read(String filePath,String path){
        BufferedReader br = null;
        String line =null;
        try {
            //根据文件路径创建缓冲输入流
            br = new BufferedReader(new FileReader(filePath));//filePath中是aaa.txt文件
            String str = "";
            //循环读取文件的每一行，对需要修改的行进行修改，放入缓冲对象中
            Boolean flag=true;
                while ((line = br.readLine()) != null && flag){
                    //设置正则将多余空格都转为一个空格
                    String[] dictionary = line.split("\\s+");
                    if (dictionary[0].equals("ATOM")) {
                        if(dictionary[2].equals("C4")){
                            if (dictionary[3].equals("A")||dictionary[3].equals("G")){
                                FileWriter fw = null;
                                File f=new File(path);
                                fw = new FileWriter(f, true);
                                PrintWriter pw = new PrintWriter(fw);
                                pw.println(line);
                                pw.flush();
                                fw.flush();
                                pw.close();
                                fw.close();
                            }
                        }

                    if (dictionary[0].equals("MODEL") && dictionary[1].equals("2")){
                        flag=false;
                        br.close();
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (br != null) {// 关闭流
                try {
                    br.close();
                } catch (IOException e) {
                    br = null;
                }
            }
        }
        return ;
    }
    public static long getLineNumber(String filePath) {
        try {
            return Files.lines(Paths.get(filePath)).count();
        } catch (IOException e) {
            return -1;
        }
    }

    public static Map<String,String> AliginFile(String file1, String file2) throws IOException, InterruptedException {
        int len1=(int)getLineNumber(file1);
        int len2=(int)getLineNumber(file2);
        String line =null;
        String fileAlign=null;
        Map<String ,String> map=new HashMap<>();
        if (len1>=len2){
            fileAlign=file2.substring(0,file2.lastIndexOf("."))+"Align.pdb";
            map.put("file1",fileAlign);
            map.put("file2",file2);
            BufferedReader br = new BufferedReader(new FileReader(file1));//filePath中是aaa.txt文件
            int count=0;
            while( (line=br.readLine())!=null && count<len2){
                FileWriter fw = null;
                File f=new File(fileAlign);
                fw = new FileWriter(f, true);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(line);
                pw.flush();
                fw.flush();
                pw.close();
                count++;
            }

        }else {
            fileAlign=file2.substring(0,file2.lastIndexOf("."))+"Align.pdb";;//前缀UUID.randomUUID().toString()+".pdb";
            map.put("file1",file1);
            map.put("file2",fileAlign);
            BufferedReader br = new BufferedReader(new FileReader(file2));//filePath中是aaa.txt文件
            int count=0;
            while( (line=br.readLine())!=null && count<len1){
                FileWriter fw = null;
                File f=new File(fileAlign);
                fw = new FileWriter(f, true);
                PrintWriter pw = new PrintWriter(fw);
                pw.println(line);
                pw.flush();
                fw.flush();
                pw.close();
                count++;
            }
        }
        return map;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
      //  System.out.println(getLineNumber("C://Users//m1887//Desktop//pdb//1ajf.pdb"));
      //  read("C://Users//m1887//Desktop//pdb//1afx.pdb","C://Users//m1887//Desktop//pdb//cc3.pdb");
        String file1="C://Users//m1887//Desktop//pdb//1AFX.pdb";
        String file2="C://Users//m1887//Desktop//pdb//111.pdb";
        String file3="C://Users//m1887//Desktop//pdb//cc.pdb";
//        read(file1,file2);
//        Thread.sleep(1000*3);
        AliginFile(file1,file2);
      //  read("C://Users//m1887//Desktop//pdb//1ajf.pdb","C://Users//m1887//Desktop//pdb//cc.pdb");
    }
}
