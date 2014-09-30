package org.silverpeas.accesscontrol;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation qualifies an implementation of access controller of a Node resource.
 * @author: Yohann Chastagnier
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface NodeAccessControl {
}
