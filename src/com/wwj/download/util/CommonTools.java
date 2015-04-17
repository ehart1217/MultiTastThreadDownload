
package com.wwj.download.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CommonTools {
    public static String getEncodePath(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);

        try {
            // URL编码（这里是为了将中文进行URL编码）
            filename = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // 转码后的路径
        path = path.substring(0, path.lastIndexOf("/") + 1) + filename;
        return path;
    }
}
