package org.silverpeas.accesscontrol;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation qualifies an implementation of access controller of a Publication resource.
 * @author Yohann Chastagnier
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface PublicationAccessControl {
}
