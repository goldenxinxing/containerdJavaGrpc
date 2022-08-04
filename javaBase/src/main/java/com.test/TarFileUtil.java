package com.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * https://blog.csdn.net/qq_42389764/article/details/120973484
 */
@Slf4j
public final class TarFileUtil {
    /**
     * 通过文件在压缩包中的路径和文件名称来模糊匹配文件，
     * 并返回第一个匹配上的文件
     */
    public static String getContentFromTarFile(InputStream tarFileInputStream, String targetFilePath, String targetFileName) {
        ArchiveInputStream archiveInputStream = null;
        try {
            archiveInputStream = getArchiveInputStream(tarFileInputStream);
            TarArchiveEntry entry = null;
            while ((entry = (TarArchiveEntry) archiveInputStream.getNextEntry()) != null) {
                if (entry.getSize() <= 0) {
                    continue;
                }
                if (!StringUtils.isEmpty(targetFilePath) && !entry.getName().startsWith(targetFilePath)) {
                    continue;
                }
                if (!StringUtils.isEmpty(targetFileName) && !entry.getName().endsWith(targetFileName)) {
                    continue;
                }
                return new String(FileUtil.getContent(archiveInputStream));
            }

        } catch (Exception e) {
            log.error("获取压缩包文件失败!", e);
        } finally {
            if (null != archiveInputStream) {
                try {
                    archiveInputStream.close();
                } catch (IOException e) {
                    log.error("file close error!", e);
                }
            }
        }

        return null;
    }

    private static ArchiveInputStream getArchiveInputStream(File tarFile) throws IOException, ArchiveException {
        if (StringUtils.endsWithIgnoreCase(tarFile.getName(), ".gz")) {
            return new ArchiveStreamFactory()
                .createArchiveInputStream("tar", new GZIPInputStream(new BufferedInputStream(new FileInputStream(tarFile))));
        } else {
            return new ArchiveStreamFactory()
                .createArchiveInputStream("tar", new BufferedInputStream(new FileInputStream(tarFile)));
        }
    }

    private static ArchiveInputStream getArchiveInputStream(InputStream in) throws ArchiveException {

        return new ArchiveStreamFactory()
            .createArchiveInputStream("tar", new BufferedInputStream(in));

    }

    public static void main(String[] args) throws FileNotFoundException {
        String content = TarFileUtil.getContentFromTarFile(new FileInputStream("C:\\Users\\gaoxinxing\\test.swmp"), "src", "eval_jobs.yaml");
        System.out.println(content);
    }
}
