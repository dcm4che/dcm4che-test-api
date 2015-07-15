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

package org.dcm4che.test.image;

import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.dcm4che.test.common.BasicTest;
import org.dcm4che.test.utils.TestingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assertions to check the equality of images in tests.
 * 
 * @author Hermann Czedik-Eysenberg <hermann-agfa@czedik.net>
 */
public class ImageAssert {
    
    private static final Logger log = LoggerFactory.getLogger(ImageAssert.class);

    private static final int COLOR_BLACK = 0x00000000;
    private static final int COLOR_WHITE = 0x00FFFFFF;
    private static final int COLOR_GRAY = 0x00C0C0C0;

    private static final int redMask = 0xFF0000, greenMask = 0x00FF00, blueMask = 0x0000FF;

    private static final int NUMBER_OF_COLORS = 3;

    private static final int VALUES_PER_COLOR = 255;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##"); //$NON-NLS-1$

    /**
     * Count used for saving failure files with unique names.
     */
    private static int count = 1;

    private static Path failureFilePath = null;

    /**
     * Check that the given image is the same as the reference image.
     * 
     * @param imagePath
     *            path to image
     * @param referenceImagePath
     *            path to reference image
     */
    public static void assertImagesEqual(Path imagePath, Path referenceImagePath)
    {
        log.info("Comparing images: {} and {}", imagePath, referenceImagePath);

        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            BufferedImage referenceImage = ImageIO.read(referenceImagePath.toFile());

            /*
             * Note: Currently the threshold parameters are not used. Once they
             * are needed, additional assertion methods (e.g.
             * assertImagesSimilar()) should be added that use sensible
             * defaults, so that the thresholds do not need to be specified in
             * every test. (contact Hermann)
             */

            Properties properties = TestingProperties.get();

            ComparisonResult comparisonResult = compareAndCreateDifferenceImages(image,
                    referenceImage,
                    Integer.valueOf(properties.getProperty("imageAssert.singleColorChannelThreshold", "0")),
                    Integer.valueOf(properties.getProperty("imageAssert.overallDifferenceTolerance", "0")));

            if (!comparisonResult.isSuccess())
            {
                if (failureFilePath != null)
                {
                    Files.createDirectories(failureFilePath);

                    log.info("Storing failure files at {}", failureFilePath);

                    // store all files to help debugging the problem
                    String filename = imagePath.getFileName().toString();
                    ImageIO.write(image, "PNG", new File(failureFilePath.toFile(), filename + "." + count + ".IMAGE.png"));
                    ImageIO.write(referenceImage, "PNG", new File(failureFilePath.toFile(), filename + "." + count + ".REFERENCE.png"));
                    if (comparisonResult.getBinaryDifferenceImage() != null)
                        ImageIO.write(comparisonResult.getBinaryDifferenceImage(), "PNG", new File(failureFilePath.toFile(), filename + "." + count + ".DIFF.png"));
                    if (comparisonResult.getSubstractionDifferenceImage() != null)
                        ImageIO.write(comparisonResult.getSubstractionDifferenceImage(), "PNG", new File(failureFilePath.toFile(), filename + "." + count + ".SUB.png"));

                    count++;
                }

                fail(comparisonResult.getErrorMessage());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Set the directory where failure and difference files should be stored.
     * This is set up automatically when using a {@link BasicTest}.
     * 
     * @param failureFilePath
     *            directory
     */
    public static void setFailureFilePath(Path failureFilePath)
    {
        ImageAssert.failureFilePath = failureFilePath;
    }

    /**
     * Compares two images.
     * 
     * The result will be a success if the images are equal/similar.
     * 
     * @param image1
     *            first image
     * @param image2
     *            second image
     * @param threshold
     *            A threshold for the acceptable difference per color channel.
     *            If compared pixels differ by less than the threshold per color
     *            channel they will still be treated as equal and the comparison
     *            result is a success. Value has to be from 0 to 255. (Default:
     *            0 for equality).
     * @param allowedOverallDifferencePercentage
     *            Overall percentage of differences that need to be exceeded for
     *            the result to be not a success. Value from 0 to 100. (Default:
     *            0 for equality).
     * @return result of the comparison
     */
    private static ComparisonResult compareAndCreateDifferenceImages(BufferedImage image1, BufferedImage image2, int threshold,
                                                                     double allowedOverallDifferencePercentage)
    {
        int width = image1.getWidth();
        int height = image1.getHeight();
    
        if (width != image2.getWidth())
            fail("The width of the images is different.");
        if (height != image2.getHeight())
            fail("The height of the images is different.");

        BufferedImage binaryDifferenceImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage substractionDifferenceImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        int wrongPixels = 0; // number of pixels that are different (outside the threshold)
        int wrongPixelsWithinThreshold = 0; // pixels not the same, but within the threshold
        
        double maxDifference = 0;
        double sumOfAllDifferences = 0;
        
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                int pixelOrig = image1.getRGB(i, j);
                int pixelComp = image2.getRGB(i, j);

                boolean different = (pixelOrig - pixelComp) != 0;
                
                int differenceColor = COLOR_BLACK;
                int substractionColor = COLOR_BLACK;

                if (different)
                {
                    int redOrig = (pixelOrig & redMask) >> 16;
                    int greenOrig = (pixelOrig & greenMask) >> 8;
                    int blueOrig = pixelOrig & blueMask;
                    
                    int redComp = (pixelComp & redMask) >> 16;
                    int greenComp = (pixelComp & greenMask) >> 8;
                    int blueComp = pixelComp & blueMask;
                    
                    int redDiff = Math.abs( redOrig - redComp );
                    int greenDiff = Math.abs( greenOrig - greenComp );
                    int blueDiff = Math.abs( blueOrig - blueComp );
                    
                    substractionColor = (redDiff << 16) | (greenDiff << 8) | blueDiff;
                    
                    maxDifference = Math.max(maxDifference, Math.max(redDiff, Math.max(greenDiff, blueDiff)));
                    sumOfAllDifferences += redDiff + greenDiff + blueDiff;

                    if ( (threshold == 0) || (redDiff > threshold) || (greenDiff > threshold) || (blueDiff > threshold) )
                    {
                        wrongPixels++;
                    }
                    else
                    {
                        // acceptable difference regarding threshold
                        wrongPixelsWithinThreshold++;
                        differenceColor = COLOR_GRAY;
                    }
                }
                
                substractionDifferenceImage.setRGB(i, j, substractionColor);
                
                // set different pixel to black, matching pixel to white, different within threshold to gray
                binaryDifferenceImage.setRGB(i, j, different ? differenceColor : COLOR_WHITE);
            }
        }
        
        double sumOfDifferencesPerColor = sumOfAllDifferences / NUMBER_OF_COLORS / VALUES_PER_COLOR;
        double sumOfDifferencesPerColorAndPixel = sumOfDifferencesPerColor / ((double) width * height);

        log.info("Tolerance per color channel: {}", threshold);
        //log.info("Number of allowed different pixel: {}", maxAllowedDifferentPixels);
        log.info("Number of allowed differences in percent: {}", decimalFormat.format(allowedOverallDifferencePercentage));
        log.info("Number of different pixels outside threshold: {}", wrongPixels);
        log.info("Number of different pixels within threshold: {}", wrongPixelsWithinThreshold);
        log.info("Maximum difference on one color channel: {}%", decimalFormat.format(maxDifference / VALUES_PER_COLOR * 100.0));
        log.info("Average difference for different pixels on one color channel: {}%", decimalFormat.format((sumOfDifferencesPerColor / (wrongPixels + wrongPixelsWithinThreshold)) * 100.0));
        log.info("Overall percentage of differences: {}%", decimalFormat.format(sumOfDifferencesPerColorAndPixel  * 100.0));

        // too many differences during the comparison
        if (sumOfDifferencesPerColorAndPixel  * 100.0 >= allowedOverallDifferencePercentage)
        {
            String failureMessage = "Images have " + wrongPixels + " different pixels (" + decimalFormat.format(sumOfDifferencesPerColorAndPixel  * 100.0) + "%). [ threshold: " + threshold + ", percent: " + decimalFormat.format(allowedOverallDifferencePercentage) + "]";
            log.warn(failureMessage);
            return new ComparisonResult(false, failureMessage, binaryDifferenceImage, substractionDifferenceImage);
        }
        
        // comparison is ok, no differences
        return new ComparisonResult(true, null, binaryDifferenceImage, substractionDifferenceImage);
    }

    private static class ComparisonResult
    {
        private final boolean result;
        private final String failureMessage;
        private final BufferedImage binaryDifferenceImage;
        private final BufferedImage substractionDifferenceImage;

        public ComparisonResult(boolean result, String errorMessage, BufferedImage binaryDifferenceImage, BufferedImage substractionDifferenceImage) {
            this.result = result;
            this.failureMessage = errorMessage;
            this.binaryDifferenceImage = binaryDifferenceImage;
            this.substractionDifferenceImage = substractionDifferenceImage;
        }

        public String getErrorMessage() {
            return failureMessage;
        }

        public boolean isSuccess() {
            return result;
        }

        public BufferedImage getBinaryDifferenceImage() {
            return binaryDifferenceImage;
        }

        public BufferedImage getSubstractionDifferenceImage() {
            return substractionDifferenceImage;
        }
    }

}
