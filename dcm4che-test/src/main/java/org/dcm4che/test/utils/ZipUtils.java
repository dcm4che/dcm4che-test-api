package org.dcm4che.test.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.dcm4che3.util.StreamUtils;

/**
 * Utility methods for working with ZIP files.
 */
public class ZipUtils {

    /**
     * Extracts a zip file to the given directory.
     *
     * @param zipFile       path to zip file
     * @param destDirectory path to directory, must already exist
     *
     * @throws IOException IO exception
     */
    public static void unzip(Path zipFile, Path destDirectory) throws IOException {
        if (!Files.isDirectory(destDirectory))
            throw new IOException(destDirectory + " is not a directory");

        try (ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(Files.newInputStream(zipFile)))) {
            for (ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry()) {
                Path entryPath = destDirectory.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    extractFile(zipIn, entryPath);
                } else {
                    Files.createDirectories(entryPath);
                }
                zipIn.closeEntry();
            }
        }
    }

    private static void extractFile(ZipInputStream zipIn, Path dstFilePath) throws IOException {
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(dstFilePath))) {
            StreamUtils.copy(zipIn, out);
        }
    }
}
