package org.dcm4che.test.annotations.markers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Denotes that the test can take very long to complete (> 1 min)
 * Such tests will be ran in a dedicated manner
 * @author Roman K
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Heavy {
}
