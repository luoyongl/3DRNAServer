package com.wtu.sj.ly.rna.controller;

import com.wtu.sj.ly.rna.Utils.DownloadUtil;
import com.wtu.sj.ly.rna.Utils.RmsdUtils;
import com.wtu.sj.ly.rna.Utils.TrustSSL;
import com.wtu.sj.ly.rna.Utils.WriteFileUtils;
import com.wtu.sj.ly.rna.openapi.pojo.RnaResult;
import com.xxl.conf.core.XxlConfClient;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ：LY
 * @date ：Created in 2020/3/2 22:36
 * @description：
 * @modified By：
 * @version: $
 */
@RestController
@RequestMapping("/calculate")
public class CalculateController {

    /**
     * 关于xxl-conf处理一些配置数据的说明
     * 这是我目前的一种思路，通过xxl-conf对这些基本数据进行配置，方便在于修改这些
     * 数据时不需要动代码，目前我就配置了几个项，后续如果想配置更多的项可以根据你的
     * 想法进行修改
     */
    int downloadWaittime=Integer.parseInt(XxlConfClient.get("default.download.waittime", null));
    //py文件路径
    String pyPath=XxlConfClient.get("default.py.path", null);
    //试验体pdb文件路径
    String pdbPath=XxlConfClient.get("default.pdb.path", null);
    /**
     * MC-Fold 二级结构预测接口
     * @param Sequence 一级结构序列
     * @return
     * @throws IOException
     */
    @RequestMapping("/mcfold")
    public RnaResult mcFold(String Sequence) throws IOException {

        //解析地址拼接
        String requestUrl="https://www.major.iric.ca/cgi-bin/MC-Fold/mcfold.static.cgi?pass=lucy&sequence="
                +Sequence+
                "&top=20&explore=15&name=&mask=&singlehigh=&singlemed=&singlelow=";
        TrustSSL.trustEveryone();
        //得到解析后的Document对象  通过jsoup接口操作获取我们需要的元素数据
        int timeout;
        try {
             timeout=Integer.parseInt(XxlConfClient.get("default.mcfold.timeout", null));
        }catch (Exception e){
            return RnaResult.fail("请检查MC-fold相关配置");
        }
        Document doc= Jsoup.connect(requestUrl).timeout(timeout).get();
        /**
         * mc-fold二级结构数据通过浏览器查看可以知道
         * 位于html页面中最后一个<pre><pre/>标签中
         */
        Element element = doc.select("pre").last();
        String[] split = element.text().replaceAll("\\[.*?\\]","").split("\n|\r");
        return RnaResult.ok(split);

    }

    @RequestMapping("/showmc")
    public RnaResult showMc(String PdbID,String Strcuture, String Sequence) throws Exception {
        //同上 解析路径
        String submitUrl="http://www.major.iric.ca/cgi-bin/MC-Sym/mcsym.cgi?scriptgen=>structure|"
                +Sequence+"|"+Strcuture+"&action=Submit";
        TrustSSL.trustEveryone();
        Map<String,String> map=new HashMap<>();
        Map<String,String> resmap;
        Map<String,String> filemap;
        String rmsd="";
        int timeout;
        try {
            timeout=Integer.parseInt(XxlConfClient.get("default.mcfold.timeout", null));
        }catch (Exception e){
            return RnaResult.fail("请检查MC-fold相关配置");
        }
        Document structpath=Jsoup.connect(submitUrl).timeout(timeout).get();
        //由于mc-fold原网站预测后会进行一次跳转，所以这需要再次解析 先获取跳转路径  再去解析跳转后的页面
        String downloadurl=structpath.select("meta").attr("CONTENT").toString().trim().substring(7)+"structure-0001.pdb.gz";
        //执行文件下载 方法详解有注释 idea中可通过ctrl+左键点击方法名进入方法查看
        resmap= DownloadUtil.download(downloadurl,true,downloadWaittime);
        //如果需要进行计算rmsd
        if(PdbID!=null||PdbID!=""){
            String file1=pdbPath+PdbID+".pdb";
            String filePath=resmap.get("savaPath").substring(1);
            String fileSelct=filePath.substring(0,filePath.lastIndexOf("."))+"Select.pdb";
            //筛选数据
            WriteFileUtils.read(filePath,fileSelct);
            //将要进行计算的两个文件处理成满足脚本的要求
            filemap = WriteFileUtils.AliginFile(file1,fileSelct);
            //计算rmsd
            rmsd= RmsdUtils.rmsd(pyPath,filemap.get("file1"),filemap.get("file2"));
        }

        map.put("structPath",resmap.get("returnPath"));
        map.put("prePath",resmap.get("prePath"));
        map.put("rmsd",rmsd);
        return RnaResult.ok(map);
    }

    /**
     * 下面的代码类似mc-fold 区别在于可能正对不同的服务器 要解析的元素不同
     * 解析的元素可以通过Chrome等浏览器访问对应的预测网站，右键查看元素，然
     * 后根据自己需要哪些数据对应解析
     */

    @RequestMapping("/vfold")
      public RnaResult VFold(String PdbID,String Sequence,String Strcuture) throws Exception {

        String requestUrl="http://rna.physics.missouri.edu/vfold3D/3D_run.pl?sequence="
                +Sequence+ "&bps="+Strcuture;
        Map<String,String> map=new HashMap<>();
        Map<String,String> resmap=new HashMap<>();
        Map<String,String> filemap;
        String rmsd="";
        TrustSSL.trustEveryone();
        int timeout;
        try {
            timeout=Integer.parseInt(XxlConfClient.get("default.vfold.timeout", null));
        }catch (Exception e){
            return RnaResult.fail("请检查vfold相关配置");
        }
        Document skip=Jsoup.connect(requestUrl).timeout(timeout).get();
        String downloadurl=skip.select("meta").attr("CONTENT").toString().trim().substring(6);
        Document doc= Jsoup.connect(requestUrl).timeout(timeout).get();
        Document result=Jsoup.connect(downloadurl).timeout(timeout).get();
        String pdburl=result.select("a").eq(0).attr("href");
        String suffix = pdburl.substring(pdburl.lastIndexOf("."));
        if (suffix.equals(".pdb")){
            String download="http://rna.physics.missouri.edu/OUTPUT/"+pdburl;
            resmap= DownloadUtil.download(download,false,downloadWaittime);
            if(PdbID!=null||PdbID!=""){
                String file1=pdbPath+PdbID+".pdb";
                String file2=resmap.get("savaPath");
                String filePath=file2.substring(1);
                String fileSelct=filePath.substring(0,filePath.lastIndexOf("."))+"Select.pdb";
                WriteFileUtils.read(filePath,fileSelct);
                filemap = WriteFileUtils.AliginFile(file1,fileSelct);
                rmsd= RmsdUtils.rmsd(pyPath,filemap.get("file1"),filemap.get("file2"));
            }
            map.put("structPath",resmap.get("returnPath"));
            map.put("prePath",resmap.get("prePath"));
            map.put("rmsd",rmsd);
            return  RnaResult.ok(map);
        }else {
            return RnaResult.fail();
        }

    }


    @RequestMapping("/RNAComposer")
    public RnaResult RNAComposer(String PdbID,String Sequence,@RequestParam(required = false) String Strcuture) throws Exception {
        String data="content=>example1"+"\n" + Sequence+"\n" +Strcuture+"&_addPredict2dTool=on&send=Compose";
        TrustSSL.trustEveryone();
        Map<String, String> cookies = null;
        Map<String,String> map=new HashMap<>();
        Map<String,String> resmap=new HashMap<>();
        Map<String,String> filemap;
        int timeout;
        try {
            timeout=Integer.parseInt(XxlConfClient.get("default.vfold.timeout", null));
        }catch (Exception e){
            return RnaResult.fail("请检查MC-fold相关配置");
        }
        //先获取cookies
        Connection.Response res = Jsoup.connect("http://rnacomposer.ibch.poznan.pl").timeout(timeout).execute();
        cookies = res.cookies();
        Document document=Jsoup.connect("http://rnacomposer.ibch.poznan.pl")
                .requestBody(data)
                .header("Access-Control-Allow-Credentials", "true")
                .header("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Content-Type","application/x-www-form-urlencoded")
                .header("Accept-Encoding","gzip, deflate")
                .cookies(cookies).timeout(timeout).post();

        //rnacomposer 每个任务会有一个taskid，信息都通过这个taskid获取
        String taskId = document.select("form#resultsForm >input").get(0).attr("value");
        String downloadurl="http://rnacomposer.ibch.poznan.pl/task/result?taskID="+taskId;
        Document downloadDoc;
        downloadDoc=pdb(downloadurl,cookies);
        Thread.sleep(timeout);
        String rmsd="";
        String downloadPDB=downloadDoc.select("a").get(0).attr("href");
        String fileUrl = "http://rnacomposer.ibch.poznan.pl"+downloadPDB;
        resmap= DownloadUtil.downloadWithCookie(fileUrl);
        if(PdbID!=null||PdbID!=""){
            String file1=pdbPath+PdbID+".pdb";
            String file2=resmap.get("savaPath");
            String filePath=file2.substring(1);
            String fileSelct=filePath.substring(0,filePath.lastIndexOf("."))+"Select.pdb";
            WriteFileUtils.read(filePath,fileSelct);
            filemap = WriteFileUtils.AliginFile(file1,fileSelct);
            rmsd= RmsdUtils.rmsd(pyPath,filemap.get("file1"),filemap.get("file2"));
        }

        map.put("structPath",resmap.get("returnPath"));
        map.put("prePath",resmap.get("prePath"));
        map.put("rmsd",rmsd);
        return  RnaResult.ok(map);

    }


    @RequestMapping("/3dRNA")
    public RnaResult dRNA(String PdbID,String Sequence,String Strcuture) throws Exception {
/*        String submitUrl="http://www.major.iric.ca/cgi-bin/MC-Sym/mcsym.cgi?scriptgen=>structure|"
                +Sequence+"|"+Strcuture+"&action=Submit";
        TrustSSL.trustEveryone();
        Map<String,String> map=new HashMap<>();
        Map<String,String> resmap;
        Map<String,String> filemap;
        String rmsd="";
        String file1="";
        Document structpath=Jsoup.connect(submitUrl).timeout(1000*2000).get();
        String downloadurl=structpath.select("meta").attr("CONTENT").toString().trim().substring(7)+"structure-0001.pdb.gz";
        resmap= DownloadUtil.download(downloadurl,true,downloadWaittime);

        String py="F://Idea-workspace//RNAServer//src//main//resources//static//py//calculate_rmsd.py";
        if(PdbID!=null||PdbID!=""){
            file1="F://Idea-workspace//RNAServer//src//main//resources//static//py//"+PdbID+".pdb";
            String file2=resmap.get("savaPath");
            String filePath=file2.substring(1);

            Thread.sleep(1000*5);
            String fileSelct=filePath.substring(0,filePath.lastIndexOf("."))+"Select.pdb";
            WriteFileUtils.read(filePath,fileSelct);
            Thread.sleep(1000*5);
            filemap = WriteFileUtils.AliginFile(file1,fileSelct);
            Thread.sleep(1000*5);
            rmsd= RmsdUtils.rmsd(py,filemap.get("file1"),filemap.get("file2"));
        }


        // String rmsd= RmsdUtils.rmsd(py,filemap.get(file1),filemap.get(file2));
        map.put("structPath",resmap.get("returnPath"));
        map.put("prePath",resmap.get("prePath"));
        map.put("rmsd",rmsd);
        return RnaResult.ok(map)*/;
        return RnaResult.ok();

    }


    /**
     * 测试 xxl-conf
     * @return
     * @throws Exception
     */
    @RequestMapping("/testXxlConf")
    public RnaResult testXxlConf() throws Exception {
        String paramByApi = XxlConfClient.get("default.key01", null);
        System.out.println(paramByApi);

        return RnaResult.ok();
    }



    public static Document pdb(String url,Map<String, String> cookies) throws IOException {
        Document document=Jsoup.connect("http://rnacomposer.ibch.poznan.pl/task/result?taskID=1e5de1bb-6ba6-4d7c-8227-12608115cf9c").cookies(cookies).timeout(1000*10).post();
        return document;
    }




}
