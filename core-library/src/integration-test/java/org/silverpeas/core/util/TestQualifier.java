package org.silverpeas.core.util;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to verify the Qualifier behaviour.
 * @author Yohann Chastagnier
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface TestQualifier {
}
