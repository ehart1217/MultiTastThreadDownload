
package com.wwj.download.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class CommonTools {
    public static String getEncodePath(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);

        try {
            // URL���루������Ϊ�˽����Ľ���URL���룩
            filename = URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // ת����·��
        path = path.substring(0, path.lastIndexOf("/") + 1) + filename;
        return path;
    }
}
