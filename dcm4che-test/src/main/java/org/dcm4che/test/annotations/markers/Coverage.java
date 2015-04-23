package org.dcm4che.test.annotations.markers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Roman K
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Coverage {

    /**
     * List of requirements covered by this test
     * @return
     */
    String[] value();
}
