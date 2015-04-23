package org.dcm4che.test.annotations.markers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Description of what this test tests
 * @author Roman K
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {
    String value();
}
