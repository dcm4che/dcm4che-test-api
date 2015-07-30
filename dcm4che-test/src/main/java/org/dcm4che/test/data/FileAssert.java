//
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

package org.dcm4che.test.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

import org.junit.Assert;

/**
 * A set of assert methods specially targeted to asserting files.
 * </p>
 * See: http://junit-addons.sourceforge.net/junitx/framework/FileAssert.html
 * 
 * @author Alexander Hoermandinger <alexander.hoermandinger@agfa.com>
 */
public class FileAssert {
    
    private FileAssert() {
        // NOOP
    }

    /**
     * Asserts that two files are equal. Throws an <tt>AssertionError</tt>
     * if they are not.
     */
    public static void assertEquals(String message, File expected, File actual) {
        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);

        Assert.assertTrue("File does not exist [" + expected.getAbsolutePath() + "]",
                expected.exists());
        Assert.assertTrue("File does not exist [" + actual.getAbsolutePath() + "]", actual.exists());

        Assert.assertTrue("Expected file not readable", expected.canRead());
        Assert.assertTrue("Actual file not readable", actual.canRead());

        FileInputStream eis = null;
        FileInputStream ais = null;

        try {
            try {
                eis = new FileInputStream(expected);
                ais = new FileInputStream(actual);

                BufferedReader expData = new BufferedReader(new InputStreamReader(eis));
                BufferedReader actData = new BufferedReader(new InputStreamReader(ais));

                Assert.assertNotNull(message, expData);
                Assert.assertNotNull(message, actData);

                assertEquals(message, expData, actData);
            } finally {
                eis.close();
                ais.close();
            }
        } catch (IOException e) {
            throw new FileAssertionError("I/O error while comparing files", e);
        }
    }

    /**
     * Asserts that two files are equal. Throws an <tt>FileAssertionError</tt>
     * if they are not.
     */
    public static void assertEquals(File expected, File actual) {
        assertEquals(null, expected, actual);
    }

    /**
     * <b>Testing only</b> Asserts that two readers are equal. Throws an
     * <tt>AssertionError</tt> if they are not.
     */
    protected static void assertEquals(String message, Reader expected, Reader actual) {
        Assert.assertNotNull(message, expected);
        Assert.assertNotNull(message, actual);

        LineNumberReader expReader = new LineNumberReader(expected);
        LineNumberReader actReader = new LineNumberReader(actual);

        Assert.assertNotNull(message, expReader);
        Assert.assertNotNull(message, actReader);

        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }

        String expLine;
        String actLine;
        try {
            while (true) {
                if (!expReader.ready() && !actReader.ready()) {
                    return;
                }

                expLine = expReader.readLine();
                actLine = actReader.readLine();

                if (expLine == null && actLine == null) {
                    return;
                }

                int line = expReader.getLineNumber() + 1;

                if (expReader.ready()) {
                    if (actReader.ready()) {
                        Assert.assertEquals(formatted + "Line [" + line + "]", expLine, actLine);
                    } else {
                        Assert.fail(formatted + "Line [" + line + "] expected <" + expLine
                                + "> but was <EOF>");
                    }
                } else {
                    if (actReader.ready()) {
                        Assert.fail(formatted + "Line [" + line + "] expected <EOF> but was <"
                                + actLine + ">");
                    } else {
                        Assert.assertEquals(formatted + "Line [" + line + "]", expLine, actLine);
                    }
                }
            }
        } catch (IOException e) {
            throw new FileAssertionError("I/O error while comparing files", e);
        }
    }

    /**
     * Asserts that two binary files are equal. Throws an
     * <tt>FileAssertionError</tt> if they are not.
     * <p>
     */
    public static void assertBinaryEquals(File expected, File actual) {
        assertBinaryEquals(null, expected, actual);
    }

    /**
     * Asserts that two binary files are equal. Throws an
     * <tt>AssertionError</tt> if they are not.
     * <p>
     */
    public static void assertBinaryEquals(String message, File expected, File actual) {
        Assert.assertNotNull(message, expected);
        Assert.assertNotNull(message, actual);

        Assert.assertTrue("File does not exist [" + expected.getAbsolutePath() + "]",
                expected.exists());
        Assert.assertTrue("File does not exist [" + actual.getAbsolutePath() + "]", actual.exists());

        Assert.assertTrue("Expected file not readable", expected.canRead());
        Assert.assertTrue("Actual file not readable", actual.canRead());

        FileInputStream eis = null;
        FileInputStream ais = null;

        try {
            try {
                eis = new FileInputStream(expected);
                ais = new FileInputStream(actual);

                Assert.assertNotNull(message, expected);
                Assert.assertNotNull(message, actual);

                byte[] expBuff = new byte[8192];
                byte[] actBuff = new byte[8192];

                long pos = 0;
                while (true) {
                    int expLength = eis.read(expBuff, 0, 8192);
                    int actLength = ais.read(actBuff, 0, 8192);

                    if (expLength < actLength) {
                        Assert.fail("actual file is longer");
                    }
                    if (expLength > actLength) {
                        Assert.fail("actual file is shorter");
                    }

                    if (expLength == 0) {
                        return;
                    }

                    for (int i = 0; i < expBuff.length; ++i) {
                        if (expBuff[i] != actBuff[i]) {
                            String formatted = "";
                            if (message != null) {
                                formatted = message + " ";
                            }
                            
                            // i starts at 0 so +1
                            Assert.fail(formatted + "files differ at byte " + (pos + i + 1)); 
                        }
                    }

                    pos += expBuff.length;
                    return;
                }
            } finally {
                eis.close();
                ais.close();
            }
        } catch (IOException e) {
            throw new FileAssertionError("I/O error while comparing files", e);
        }
    }

    private static class FileAssertionError extends AssertionError {
        private static final long serialVersionUID = -4235383959415998630L;

        public FileAssertionError(String message, Throwable cause) {
            super(message, cause);
        }

    }

}
