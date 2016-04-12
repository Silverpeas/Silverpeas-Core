/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.util.logging;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation applicable to methods and types for which you wish to trace the invocation of the
 * method. Only DI managed bean are taken in charge by the processor of this annotation.
 * </p>
 * With this annotation, for each invocation of a method two log records will be generated:
 * one for the start of the method execution and the second for its end with as information
 * at least the method's name and the user behind the invocation (if any).
 * @author mmoquillon
 */
@InterceptorBinding
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Log {

  /**
   * A message to record into the log. If not set, a default message will be used from the class
   * simple name and the invoked method name. It set, this message will be used and only one
   * log record will be written before the method invocation (no log record after the invocation).
   * @return a message to record or an empty string to use the default one.
   */
  @Nonbinding String message() default "";
}
