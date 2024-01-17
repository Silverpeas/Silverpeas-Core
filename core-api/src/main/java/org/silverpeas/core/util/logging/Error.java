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
 * "https://www.silverpeas.org/legal/licensing"
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
 * An annotation applicable to methods and types for which you wish to trace the exceptions
 * occurring within the execution of the method. Only DI managed bean are taken in charge by the
 * processor of this annotation.
 * @author mmoquillon
 */
@InterceptorBinding
@Documented
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
public @interface Error {

  /**
   * A message to record into the log. If not set, the default message will be the message of the
   * caught exception. If set, this message will be used instead. In order to write the message
   * with the method parameters, a pattern of such a message can be provided here. For doing,
   * the pattern has to follow the pattern rules of {@link java.text.MessageFormat} with two
   * additional variable placeholders:
   * <ul>
   *   <li><code>{e}</code> for displaying the exception itself (exception stacktrace),</li>
   *   <li><code>{m}</code> for displaying the message of the exception.</li>
   * </ul>
   * Hence, for example, with a pattern like {@code "Error in process instance {1}
   * for {0}: {m}"}, the resulting message will have in it, at the variable placeholders, the value
   * of the second parameter of the method, followed by the value of the first one, and then the
   * message of the caught exception.
   *
   * @return a message or a pattern of a message to record or an empty string to use the default
   * message.
   */
  @Nonbinding String message() default "";

  /**
   * A flag indicating if the caught exception has to be rethrown once the message recording into
   * the log. If false, and if the method returns a value, the returned value of the method will be
   * automatically null.
   * @return true if the error has to be reported, false otherwise. By default true.
   */
  @Nonbinding boolean rethrown() default true;
}
