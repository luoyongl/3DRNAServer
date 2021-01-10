package com.wtu.sj.ly.rna.controller;

import com.wtu.sj.ly.rna.constant.RnaConstant;
import com.wtu.sj.ly.rna.openapi.pojo.RnaResult;
import com.wtu.sj.ly.rna.utils.DownloadUtil;
import com.wtu.sj.ly.rna.utils.RmsdUtils;
import com.wtu.sj.ly.rna.utils.TrustSSL;
import com.wtu.sj.ly.rna.utils.WriteFileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(CalculateController.class);

    /**
     * 超时时间
     */
    public static final int downloadWaittime = RnaConstant.SystemConfig.DOWNLOAD_TIME_OUT;

    /**
     * 超时时间
     */
    public static final int MCFOLD_TIME_OUT = RnaConstant.SystemConfig.MCFOLD_TIME_OUT;

    /**
     * python脚本位置
     */
    public static final String pyPath = RnaConstant.SystemConfig.CALCULATE_PY_PATH;

    /**
     * 试验体pdb文件路径
     */
    public static final String pdbPath = RnaConstant.SystemConfig.CALCULATE_PDB_PATH;

    /**
     * mc请求url
     */
    public static final String MCFOLD_URL = RnaConstant.UrlConfig.MCFOLD_URL;

    /**
     * vfold请求url
     */
    public static final String VFOLD_URL = RnaConstant.UrlConfig.VFOLD_URL;

    /**
     * rnacomposer请求url
     */
    public static final String RNACOMPOSER_URL = RnaConstant.UrlConfig.RNACOMPOSER_URL;

    /**
     * MC-Fold 二级结构预测接口
     *
     * @param Sequence 一级结构序列
     * @return
     * @throws IOException
     */
    @RequestMapping("/mcfold")
    public RnaResult mcFold(String Sequence) throws IOException {

        //解析地址拼接
        String requestUrl = MCFOLD_URL + "?pass=lucy&sequence=" + Sequence + "&top=20&explore=15&name=&mask=&singlehigh=&singlemed=&singlelow=";
        logger.info("获取mc二级结构地址：{}",requestUrl);
        TrustSSL.trustEveryone();
        //得到解析后的Document对象  通过jsoup接口操作获取我们需要的元素数据
        Document doc = Jsoup.connect(requestUrl).timeout(MCFOLD_TIME_OUT).get();
        /**
         * mc-fold二级结构数据通过浏览器查看可以知道
         * 位于html页面中最后一个<pre><pre/>标签中
         */
        Element element = doc.select("pre").last();
        String[] split = element.text().replaceAll("\\[.*?\\]", "").split("\n|\r");
        return RnaResult.ok(split);

    }

    @RequestMapping("/showmc")
    public RnaResult showMc(String PdbID, String Strcuture, String Sequence) throws Exception {
        //同上 解析路径
        String submitUrl ="https://major.iric.ca/cgi-bin/MC-Sym/mcsym.cgi?scriptgen=>structure|" + Sequence + "|" + Strcuture + "&action=Submit";
        logger.info("MC解析地址：{}",submitUrl);
        TrustSSL.trustEveryone();
        Map<String, String> map = new HashMap<>();
        Map<String, String> resmap;
        Map<String, String> filemap;
        String rmsd = "";
        Document structpath = Jsoup.connect(submitUrl).timeout(MCFOLD_TIME_OUT).get();
        //由于mc-fold原网站预测后会进行一次跳转，所以这需要再次解析 先获取跳转路径  再去解析跳转后的页面
        String downloadurl = structpath.select("meta").attr("CONTENT").toString().trim().substring(7) + "structure-0001.pdb.gz";
        //执行文件下载 方法详解有注释 idea中可通过ctrl+左键点击方法名进入方法查看
        resmap = DownloadUtil.download(downloadurl, true, downloadWaittime);
        //如果需要进行计算rmsd
        if (PdbID != null || PdbID != "") {
            String file1 = pdbPath + PdbID + ".pdb";
            String filePath = resmap.get("savaPath").substring(1);
            String fileSelct = filePath.substring(0, filePath.lastIndexOf(".")) + "Select.pdb";
            //筛选数据
            WriteFileUtils.read(filePath, fileSelct);
            //将要进行计算的两个文件处理成满足脚本的要求
            filemap = WriteFileUtils.AliginFile(file1, fileSelct);
            //计算rmsd
            rmsd = RmsdUtils.rmsd(pyPath, filemap.get("file1"), filemap.get("file2"));
        }

        map.put("structPath", resmap.get("returnPath"));
        map.put("prePath", resmap.get("prePath"));
        map.put("rmsd", rmsd);
        return RnaResult.ok(map);
    }

    @RequestMapping("/vfold")
    public RnaResult VFold(String PdbID, String Sequence, String Strcuture) throws Exception {

        String requestUrl = VFOLD_URL + "?sequence=" + Sequence + "&bps=" + Strcuture;
        Map<String, String> map = new HashMap<>();
        Map<String, String> resmap = new HashMap<>();
        Map<String, String> filemap = new HashMap<>();
        String rmsd = "";
        TrustSSL.trustEveryone();
        try {
            logger.info("==========>开始处理vfold");
            logger.info("==========>预期超时时间" + MCFOLD_TIME_OUT + "ms");
            Document skip = Jsoup.connect(requestUrl).timeout(MCFOLD_TIME_OUT).get();
            String downloadurl = skip.select("meta").attr("CONTENT").toString().trim().substring(6);
            Document doc = Jsoup.connect(requestUrl).timeout(MCFOLD_TIME_OUT).get();
            Document result = Jsoup.connect(downloadurl).timeout(MCFOLD_TIME_OUT).get();
            String pdburl = result.select("a").eq(0).attr("href");
            String suffix = pdburl.substring(pdburl.lastIndexOf("."));
            if (suffix.equals(".pdb")) {
                String download = "http://rna.physics.missouri.edu/OUTPUT/" + pdburl;
                resmap = DownloadUtil.download(download, false, downloadWaittime);
                if (StringUtils.isNotEmpty(PdbID)) {
                    String file1 = pdbPath + PdbID + ".pdb";
                    String file2 = resmap.get("savaPath");
                    String filePath = file2.substring(1);
                    String fileSelct = filePath.substring(0, filePath.lastIndexOf(".")) + "Select.pdb";
                    WriteFileUtils.read(filePath, fileSelct);
                    filemap = WriteFileUtils.AliginFile(file1, fileSelct);
                    rmsd = RmsdUtils.rmsd(pyPath, filemap.get("file1"), filemap.get("file2"));
                }
                map.put("structPath", resmap.get("returnPath"));
                map.put("prePath", resmap.get("prePath"));
                map.put("rmsd", rmsd);
                return RnaResult.ok(map);
            } else {
                return RnaResult.fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RnaResult.ok();
    }


    @RequestMapping("/RNAComposer")
    public RnaResult RNAComposer(String PdbID, String Sequence, @RequestParam(required = false) String Strcuture) throws Exception {
        String data = "content=>example1" + "\n" + Sequence + "\n" + Strcuture + "&_addPredict2dTool=on&send=Compose";
        TrustSSL.trustEveryone();
        Map<String, String> cookies;
        Map<String, String> map = new HashMap<>();
        Map<String, String> resmap;
        Map<String, String> filemap;
        //先获取cookies
        Connection.Response res = Jsoup.connect(RNACOMPOSER_URL).timeout(MCFOLD_TIME_OUT).execute();
        cookies = res.cookies();
        Document document = Jsoup.connect("http://rnacomposer.ibch.poznan.pl")
                .requestBody(data)
                .header("Access-Control-Allow-Credentials", "true")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept-Encoding", "gzip, deflate")
                .cookies(cookies).timeout(MCFOLD_TIME_OUT).post();

        //rnacomposer 每个任务会有一个taskid，信息都通过这个taskid获取
        String taskId = document.select("form#resultsForm >input").get(0).attr("value");
        String downloadurl = "http://rnacomposer.ibch.poznan.pl/task/result?taskID=" + taskId;
        Document downloadDoc;
        downloadDoc = pdb(downloadurl, cookies);
        Thread.sleep(30000);
        String rmsd = "";
        String downloadPDB = downloadDoc.select("a").get(0).attr("href");
        String fileUrl = "http://rnacomposer.ibch.poznan.pl" + downloadPDB;
        resmap = DownloadUtil.downloadWithCookie(fileUrl);
        if (PdbID != null || PdbID != "") {
            String file1 = pdbPath + PdbID + ".pdb";
            String file2 = resmap.get("savaPath");
            String filePath = file2.substring(1);
            String fileSelct = filePath.substring(0, filePath.lastIndexOf(".")) + "Select.pdb";
            WriteFileUtils.read(filePath, fileSelct);
            filemap = WriteFileUtils.AliginFile(file1, fileSelct);
            rmsd = RmsdUtils.rmsd(pyPath, filemap.get("file1"), filemap.get("file2"));
        }

        map.put("structPath", resmap.get("returnPath"));
        map.put("prePath", resmap.get("prePath"));
        map.put("rmsd", rmsd);
        return RnaResult.ok(map);

    }


    public static Document pdb(String url, Map<String, String> cookies) throws IOException {
        Document document = Jsoup.connect(url).cookies(cookies).timeout(1000 * 10).post();
        return document;
    }

}
