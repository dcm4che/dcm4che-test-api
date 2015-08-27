package org.dcm4che.test.annotations.markers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to associate a currently failing test with an Issue reported in the bug
 * tracker.
 * 
 * @author Hermann Czedik-Eysenberg
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportedIssue {

    /**
     * Issue number within the bug tracker (e.g. "LIB-123"). Multiple values are
     * allowed if multiple issues have been reported for one test.
     */
    String[] value();
}
