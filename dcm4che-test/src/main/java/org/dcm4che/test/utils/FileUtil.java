/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che.test.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;

/**
 * @author Umberto Cappellini <umberto.cappellini@agfa.com>
 *
 */
public class FileUtil {

    public static String humanreadable(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    /**
     * Pauses the calling thread until a specified file exists / is created
     *
     * @param timeout  Maximum time to wait before giving up
     * @param filePath The observed filepath
     * @return Returns <code>true</code> if the file exists, returns <code>false</code> otherwise
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean waitUntilFileExists(long timeout, Path filePath) throws IOException,
            InterruptedException {
        if (Files.exists(filePath)) {
            return true;
        }

        Path parentDir = filePath.getParent();
        long timeoutLeft = timeout;
        try (WatchService fsWatcher = filePath.getFileSystem().newWatchService()) {
            WatchKey key = parentDir.register(fsWatcher, StandardWatchEventKinds.ENTRY_CREATE);
            for (; ; ) {
                if (timeoutLeft <= 0) {
                    break;
                }

                long start = System.currentTimeMillis();
                key = fsWatcher.poll(timeoutLeft, TimeUnit.MILLISECONDS);

                // poll() returns null if timeout happened
                if (key == null) {
                    break;
                }

                // subtract time already spent for waiting from left timeout
                timeoutLeft -= System.currentTimeMillis() - start;

                for (WatchEvent<?> fsEvent : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = fsEvent.kind();

                    // This key is registered only
                    // for ENTRY_CREATE events,
                    // but an OVERFLOW event can
                    // occur regardless if events
                    // are lost or discarded.
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // The filename is the context of the event.
                    Path createdFilePath = ((WatchEvent<Path>) fsEvent).context();
                    Path absCreatedFilePath = parentDir.resolve(createdFilePath);
                    if (filePath.equals(absCreatedFilePath)) {
                        return true;
                    }
                }

                // reset the key
                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }

            return Files.exists(filePath);
        }
    }

    /**
     * Pauses the calling thread until a specified file is fully created
     *
     * @param stableTime  Maximum time to wait before returning, considering the file creation complete
     * @param filePath The observed filepath
     * @return Returns when the file size doesn't change after timeout mills
     * @throws IOException
     * @throws InterruptedException
     */
    public static void waitUntilFileFullyCreated(long stableTime, Path filePath) throws IOException, InterruptedException {
        long timeoutLeft = stableTime;
        long lastRead = Files.size(filePath);
        long currentRead, start;

        // if the timeout expired size can be consider stable since it didn't change for timeout millis
        while (timeoutLeft > 0) {
            start = System.currentTimeMillis();

            // Sleep for 10 mills then check the file size
            Thread.sleep(10);

            currentRead = Files.size(filePath);
            if (currentRead == lastRead) {
                // subtract time already spent for waiting from left timeout
                timeoutLeft -= System.currentTimeMillis() - start;
            } else {
                lastRead = currentRead;
                timeoutLeft = stableTime;
            }
        }
    }
}