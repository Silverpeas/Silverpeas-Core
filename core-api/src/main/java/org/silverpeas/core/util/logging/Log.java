/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * method(s). Only DI managed bean are taken in charge by the processor of this annotation.
 * <p>
 * With this annotation, for each invocation of the method, by default, one log record will be
 * generated with either a default message or the one specified with this annotation. The default
 * message is generated from the simple class name, the name of the invoked method, and the values
 * of the arguments passed to the method (if any). With the {@link Log#dualRecord()} property you
 * can indicate to write two log records instead of one for each invocation of the method. In this
 * case the first one will be written before the method execution and the second one just after its
 * completion. The last record will have along the message (the default or the specified one) the
 * time spent by the execution of the method.
 * </p>
 * <p>
 * A custom message or a pattern of a custom message can be specified with the {@link Log#message()}
 * property. In the case of a pattern, it has to satisfy the templating rules of a
 * {@link java.text.MessageFormat} pattern. With a pattern, the values of the method parameters can
 * be injected into the resulting message: {0} designates the first parameter, {1} the second one,
 * and so one.
 * </p>
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
   * simple name, the name and the value of the parameters of the invoked method. If set, this
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

  /**
   * Is the message (the default or the custom one) concerned by a dual record? A dual record
   * consists to split the message in two records: one before the method execution and the second
   * after its completion. In other words, to write the message two times, wrapping the execution of
   * the method. In this case, the time spent by the execution of the method will be indicated along
   * the second record. For a simple record, the message will be written only before the execution
   * of the method.
   *
   * @return true if the message is concerned by a dual record into the log: one before the method
   * execution and the second after its completion. Otherwise, only one message will be written and
   * this before the execution of the method. In the case of a dual record, the time spent by the
   * method execution will be indicated along with the second record.
   */
  @Nonbinding boolean dualRecord() default false;

  /**
   * The logging level to use with this annotation. By default {@link Level#INFO}.
   * @return the level used when logging a message at each annotated method invocation.
   */
  @Nonbinding Level level() default Level.INFO;
}
