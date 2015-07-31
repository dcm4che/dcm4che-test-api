package org.dcm4che.test.annotations.markers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to link auto-tests to requirements and issues in
 * the bug tracking system (Jira).
 * 
 * <p>
 * It can be used both on single test methods (preferred) and also on complete
 * test classes (to link every test within the class).
 * 
 * <p>
 * e.g. <code>@Coverage("DCMEEREQ-XXX", "DCMEEREQ-YYY")</code>
 * 
 * @author Roman K
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface Coverage {

    /**
     * List of requirements or issues covered by this test
     */
    String[] value();

}
