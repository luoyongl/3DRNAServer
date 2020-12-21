package com.wtu.sj.ly.rna.utils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * @author ：LY
 * @date ：Created in 2020/4/1 15:31
 * @description：
 * @modified By：
 * @version: $
 */
public class GZIPUtil {


    public static final int BUFFER = 1024;
    public static final String EXT = ".gz";

    /**
     * 文件解压缩
     *
     * @param file
     * @throws Exception
     */
    public static void decompress(File file) throws Exception {
        decompress(file, false);
    }

    /**
     * 文件解压缩
     *
     * @param file
     * @param delete
     *            是否删除原始文件
     * @throws Exception
     */
    public static void decompress(File file, boolean delete) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file.getPath().replace(EXT,
                ""));
        decompress(fis, fos);
        fis.close();
        fos.flush();
        fos.close();

        if (delete) {
            file.delete();
        }
    }

    /**
     * 文件解压缩
     *
     * @param file
     * @param delete
     *            是否删除原始文件
     * @throws Exception
     */
    public static void decompress(String inputFileName, String outputFileName)
            throws Exception {
        FileInputStream inputFile = new FileInputStream(inputFileName);
        FileOutputStream outputFile = new FileOutputStream(outputFileName);
        decompress(inputFile, outputFile);
        inputFile.close();
        outputFile.flush();
        outputFile.close();
    }


    /**
     * 数据解压缩
     *
     * @param is
     * @param os
     * @throws Exception
     */
    public static void decompress(InputStream is, OutputStream os)
            throws Exception {

        GZIPInputStream gis = new GZIPInputStream(is);

        int count;
        byte data[] = new byte[BUFFER];
        while ((count = gis.read(data, 0, BUFFER)) != -1) {
            os.write(data, 0, count);
        }

        gis.close();
    }

    /**
     * 文件解压缩
     *
     * @param path
     * @throws Exception
     */
    public static void decompress(String path) throws Exception {
        decompress(path, false);
    }

    /**
     * 文件解压缩
     *
     * @param path
     * @param delete
     *            是否删除原始文件
     * @throws Exception
     */
    public static void decompress(String path, boolean delete) throws Exception {
        File file = new File(path);
        decompress(file, delete);
    }


    public static void main(String[] args) throws Exception {
        String path="F://Download/20200401/44dfe4b2-1663-4a6f-a538-eec51956397f.pdb.gz";
        decompress(path);
    }
}
