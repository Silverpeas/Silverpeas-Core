/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
 * <p>
 * With this annotation, for each invocation of a method, by default, two log records will be
 * generated: one before the method execution and the second just after its completion with as
 * information at least the user identifier behind the invocation (if any), the simple class name,
 * and the name with the values of the parameters (if any) of the invoked method. It is possible to
 * override the default message by passing as value to the annotation either a custom message or a
 * pattern of a custom message with the method parameters. For latter, the pattern has to follow the
 * rules in the {@link java.text.MessageFormat} class. If a custom message or a pattern of a
 * custom message is specified, then only one record will be written and this before the method
 * execution.
 *
 * @author mmoquillon
 */
@InterceptorBinding
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Log {

  /**
   * A message to record into the log. If not set, a default message will be computed from the class
   * simple name, the name and the value of the parameters of the invoked method. It set, this
   * message will be used instead and only one log record will be written before the method
   * invocation (no log record after the invocation). In order to write the message with the method
   * parameters, a pattern of such a message can be provided here. For doing, the pattern has to
   * follow the pattern rules of {@link java.text.MessageFormat}. Hence, for example, a pattern like
   * {@code "Process instance {1} for {0}"} expects to be injected into the resulting message the
   * value of the second parameter of the method followed by the value of the first one.
   *
   * @return a message or a pattern of a message to record or an empty string to use the default
   * message.
   */
  @Nonbinding String message() default "";
}
