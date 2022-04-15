package com.test.dockerjava;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.AccessDeniedException;

/**
 * https://www.tabnine.com/code/java/methods/org.apache.commons.compress.archivers.tar.TarArchiveEntry/getMode
 */
public class MyTar {
    public static void extractor(InputStream inputStream, String destinationDirectory) throws IOException {
        // TarArchiveInputStream can be constructed with a normal FileInputStream if
        // we ever need to extract regular '.tar' files.
        TarArchiveInputStream tarIn = null;
        try {
            tarIn = new TarArchiveInputStream(inputStream);

            TarArchiveEntry tarEntry = tarIn.getNextTarEntry();
            String canonicalDestinationDirectory = new File(destinationDirectory).getCanonicalPath();
            while (tarEntry != null) {
                // Create a file for this tarEntry
                final File destPath = new File(destinationDirectory + File.separator + tarEntry.getName());
                prepDestination(destPath, tarEntry.isDirectory());

                if (!startsWithPath(destPath.getCanonicalPath(), canonicalDestinationDirectory)) {
                    throw new IOException(
                            "Expanding " + tarEntry.getName() + " would create file outside of " + canonicalDestinationDirectory
                    );
                }

                if (!tarEntry.isDirectory()) {
                    destPath.createNewFile();
                    boolean isExecutable = (tarEntry.getMode() & 0100) > 0;
                    destPath.setExecutable(isExecutable);

                    OutputStream out = null;
                    try {
                        out = new FileOutputStream(destPath);
                        IOUtils.copy(tarIn, out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                }
                tarEntry = tarIn.getNextTarEntry();
            }
        } finally {
            IOUtils.closeQuietly(tarIn);
        }
    }

    /**
     * Do multiple file system checks that should enable the plugin to work on any file system
     * whether or not it's case sensitive or not.
     *
     * @param destPath
     * @param destDir
     * @return
     */
    private static boolean startsWithPath(String destPath, String destDir) {
        if (destPath.startsWith(destDir)) {
            return true;
        } else if (destDir.length() > destPath.length()) {
            return false;
        } else {
            if (new File(destPath).exists() && !(new File(destPath.toLowerCase()).exists())) {
                return false;
            }

            return destPath.toLowerCase().startsWith(destDir.toLowerCase());
        }
    }
    private static void prepDestination(File path, boolean directory) throws IOException {
        if (directory) {
            path.mkdirs();
        } else {
            if (!path.getParentFile().exists()) {
                path.getParentFile().mkdirs();
            }
            if (!path.getParentFile().canWrite()) {
                throw new AccessDeniedException(
                        String.format("Could not get write permissions for '%s'", path.getParentFile().getAbsolutePath()));
            }
        }
    }
}
