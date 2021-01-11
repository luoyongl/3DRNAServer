package com.wtu.sj.ly.rna.utils;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author ：LY
 * @date ：Created in 2020/3/30 14:21
 * @description：
 * @modified By：
 * @version: $
 */


@Component
public class DownloadUtil {

    private static String serverPort;

    @Value("${server.port}")
    public void serverPort(String param) {
        DownloadUtil.serverPort= param;
    }

    /**
     *
     * @param fileUrl 执行下载的文件路径
     * @param UNZip   是否执行解压
     * @param time    下之前等待时间
     * @return
     * @throws Exception
     **/
    public static Map<String, String> download(String fileUrl, Boolean UNZip, int time) throws Exception {
        String path = null;
        String savaPath=null;
        String returnPath=null;
        String prePath=null;
        File file=null;
        File prefile=null;
        Map<String,String> map=new HashMap<>();
        if (fileUrl != null) {
            URL httpurl = new URL(fileUrl);
            String dataStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String uuidName = UUID.randomUUID().toString();
            path = ResourceUtils.getURL("classpath:").getPath()+"static/download/" + dataStr;
            savaPath=path + "/" + uuidName + ".pdb";
            prePath="http://localhost:"+serverPort+"/download/"+ dataStr+ "/" + uuidName + ".txt";
            returnPath="http://localhost:"+serverPort+"/download/"+ dataStr+ "/" + uuidName + ".pdb";
            if (UNZip){
                file = new File(savaPath+".gz");
            }else {
                file = new File(savaPath);
            }

            File dir = new File(path);
            if(!dir.exists()){
                dir.mkdirs();
            }
            try {
                /**设置休眠时间的作用 是为了让程序等待一段时间进行下载，这在我看来只是一种
                 * 缓解措施，是不是你没有根本上解决问题？
                 */

                Thread.sleep(time);
                //FileUtils是Spring提供的工具类
                FileUtils.copyURLToFile(httpurl,file);
                if (UNZip){
                    //如果是压缩包 执行解压操作
                    GZIPUtil.decompress(file);
                    //保存文件
                    copyFile(savaPath);
                }else {
                    copyFile(savaPath);
                }
            } catch (Exception e) {
                Thread.sleep(time);
                FileUtils.copyURLToFile(httpurl, file);
                if (UNZip){
                    GZIPUtil.decompress(file);
                    copyFile(savaPath);
                }else {
                    copyFile(savaPath);
                }
                e.printStackTrace();
            }
            dssrUtils.dssrJson("F:\\study\\3DRNAServer\\target\\classes\\static\\py",savaPath,uuidName+".json");
            map.put("jsonPath","http://localhost:"+serverPort+"/download/"+ dataStr+ "/"+uuidName+".json");
        }
        map.put("returnPath",returnPath);
        map.put("prePath",prePath);
        map.put("savaPath",savaPath);
        return map;
    }


    public static Map<String, String>  downloadWithCookie(String fileUrl) throws Exception {
        String path = null;
        String savaPath=null;
        String returnPath=null;
        String prePath=null;
        File file=null;
        if (fileUrl != null) {
            String dataStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String uuidName = UUID.randomUUID().toString();
            path = ResourceUtils.getURL("classpath:").getPath()+"static/download/" + dataStr;
            savaPath=path + "/" + uuidName + ".pdb";
            prePath="http://localhost:"+serverPort+"/download/"+ dataStr+ "/" + uuidName + ".txt";
            returnPath="http://localhost:"+serverPort+"/download/"+ dataStr+ "/" + uuidName + ".pdb";
            File dir = new File(path);
            if(!dir.exists()){
                dir.mkdirs();
            }
            try {
                Connection connection = Jsoup.connect(fileUrl);
                Connection.Response response = connection.method(Connection.Method.GET).ignoreContentType(true).timeout(10*1000).execute();
                BufferedInputStream bufferedInputStream = response.bodyStream();
                //保存文件的地址
                saveFile(bufferedInputStream,savaPath);
                copyFile(savaPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Map<String,String> map=new HashMap<>();
        map.put("returnPath",returnPath);
        map.put("prePath",prePath);
        map.put("savaPath",savaPath);
        return map;
    }

    /**
     * 下载文件方法
     * @param bufferedInputStream  文件流
     * @param savePath 保存文件路径
     * @throws IOException
     */
    public static void saveFile(BufferedInputStream bufferedInputStream, String savePath) throws IOException {
        //一次最多读取1k
        byte[] buffer = new byte[1024];
        //实际读取的长度
        int readLenghth;
        //根据文件保存地址，创建文件输出流
        FileOutputStream fileOutputStream = new FileOutputStream(new File(savePath));
        //创建的一个写出的缓冲流
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        //文件逐步写入本地
        while ((readLenghth = bufferedInputStream.read(buffer,0,1024)) != -1){//先读出来，保存在buffer数组中
            bufferedOutputStream.write(buffer,0,readLenghth);//再从buffer中取出来保存到本地
        }
        //关闭缓冲流
        bufferedOutputStream.close();
        fileOutputStream.close();
        bufferedInputStream.close();
    }


    /**
     * 复制一份txt用于预览  pdb文件目前不知道如何在html中预览 所以就通过这种方式
     * @param filePath 下载文具的路径
     */
    public static void copyFile ( String filePath ) {
        int len = 0 ;
        FileInputStream fis = null ;
        FileOutputStream fos = null ;
        File file = new File(filePath) ;
        try{
            fis = new FileInputStream(filePath) ;
            String newFileName = file.getPath().replaceAll(".pdb", ".txt");
            fos = new FileOutputStream( new File( newFileName ) );
            byte[] bt = new byte[1024];
            while ( ( len = fis.read( bt )) != -1){
                fos.write( bt , 0 , len );
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                fis.close();
                fos.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
