package com.wtu.sj.ly.rna.Utils;

import org.apache.commons.io.FileUtils;
import org.springframework.scheduling.annotation.Async;

import java.io.File;
import java.net.URL;

/**
 * @author ：LY
 * @date ：Created in 2020/4/2 2:12
 * @description：
 * @modified By：
 * @version: $
 */

public class AsyncTaskUtil {
    @Async
    public void executeAsyncTask(URL httpurl, File file) throws Exception {
        FileUtils.copyURLToFile(httpurl, file);
        GZIPUtil.decompress(file);
    }
}
