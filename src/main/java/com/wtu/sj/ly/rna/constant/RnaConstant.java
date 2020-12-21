package com.wtu.sj.ly.rna.constant;

import com.xxl.conf.core.XxlConfClient;

/**
 * @author ：luoYong
 * @date ：Created in 2020/12/21 10:58
 * @description：
 */
public class RnaConstant {
    public static class SystemConfig {
        /**
         * 下载超时时间
         */
        public static final int DOWNLOAD_TIME_OUT =  Integer.parseInt(XxlConfClient.get("default.download.waittime"));


        /**
         * MC解析超时时间
         */
        public static final int MCFOLD_TIME_OUT =  Integer.parseInt(XxlConfClient.get("default.mcfold.timeout"));

        /**
         * python脚本位置
         */
        public static final String CALCULATE_PY_PATH =XxlConfClient.get("default.py.path");

        /**
         * 实验体文件位置
         */
        public static final String CALCULATE_PDB_PATH=XxlConfClient.get("default.pdb.path");

        /**
         *  python.exe位置
         */
        public static final String PYTHON_EXE_PATH= XxlConfClient.get("default.pyexe.path");
    }

    public static class UrlConfig{
        /**
         * mc请求url
         */
        public static final String MCFOLD_URL ="https://www.major.iric.ca/cgi-bin/MC-Fold/mcfold.static.cgi";
        /**
         * VFOLD请求url
         */
        public static final String VFOLD_URL ="http://rna.physics.missouri.edu/vfold3D/3D_run.pl";
        /**
         * rnacomposer请求url
         */
        public static final String RNACOMPOSER_URL ="http://rnacomposer.ibch.poznan.pl";
    }

}
