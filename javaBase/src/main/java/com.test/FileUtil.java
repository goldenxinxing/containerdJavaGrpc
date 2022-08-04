package com.test;

import java.io.*;

/**
 * https://www.i4k.xyz/article/weixin_34396941/114132913
 */
public class FileUtil {
    public static byte[] getContent(File file) {
        try {
            return getContent(new FileInputStream(file));

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }

        return new byte[]{};

    }

    public static byte[] getContent(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            byte[] buffer = new byte[1024];

//byte[] buffer = new byte[16 * 1024];

            while (true) {
                int len = is.read(buffer);

                if (len == -1) {
                    break;

                }

                baos.write(buffer, 0, len);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        return baos.toByteArray();

    }

}