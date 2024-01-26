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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A processor of the {@link Error} annotations. Each time a method annotated with this
 * annotation or a method of a class annotated with is annotation is invoked, this processor will
 * produce a log record when the method execution throws an exception. With the message, either the
 * default one (the message of the exception) or the one specified with the annotation, the name
 * and the unique identifier of the user behind the method execution is also written with the
 * message.
 *
 * @author mmoquillon
 */
@Interceptor
@Error
@Priority(APPLICATION)
public class ErrorAnnotationProcessor {

  protected static final String SYSTEM_DEFAULT_PATTERN = "[SYSTEM] Error in {0}#{1}:";
  protected static final String USER_DEFAULT_PATTERN = "[{0} ({1})] Error in {2}#{3}:";
  protected static final String SYSTEM_CUSTOM_PATTERN = "[SYSTEM] {0}";
  protected static final String USER_CUSTOM_PATTERN = "[{0} ({1})] {2}";

  @AroundInvoke
  public Object produceLogRecords(InvocationContext context) throws Exception {
    try {
      return context.proceed();
    } catch (Exception e) {
      SilverLogger logger = SilverLogger.getLogger(context.getTarget());
      var errorProps = getErrorProperties(context).withException(e);
      if (StringUtil.isDefined(errorProps.getMessage())) {
        logCustomMessage(logger, errorProps, context);
      } else {
        logDefaultMessage(logger, errorProps, context);
      }
      if (errorProps.isRethrown()) {
        throw e;
      }
      return null;
    }
  }

  private void logCustomMessage(SilverLogger logger, ErrorProperties errorProps,
      InvocationContext context) {
    UserDetail currentUser = UserDetail.getCurrentRequester();
    String message = computeCustomMessage(errorProps.getMessage(), errorProps.getException(),
        context);
    if (currentUser == null) {
      logger.error(SYSTEM_CUSTOM_PATTERN, message);
    } else {
      logger.error(USER_CUSTOM_PATTERN,
          currentUser.getDisplayedName(),
          currentUser.getId(),
          message);
    }
  }

  private void logDefaultMessage(SilverLogger logger, ErrorProperties errorProps,
      InvocationContext context) {
    User currentUser = UserDetail.getCurrentRequester();
    String className = context.getMethod().getDeclaringClass().getSimpleName();
    String message = computeDefaultMessage(context);
    if (currentUser == null) {
      logger.error(SYSTEM_DEFAULT_PATTERN,
          new Object[]{className, message},
          errorProps.getException());
    } else {
      logger.error(USER_DEFAULT_PATTERN,
          new Object[]{currentUser.getDisplayedName(), currentUser.getId(), className, message},
          errorProps.getException());
    }
  }

  private ErrorProperties getErrorProperties(InvocationContext context) {
    Method method = context.getMethod();
    Error error = method.getAnnotation(Error.class);
    if (error == null) {
      error = method.getDeclaringClass().getAnnotation(Error.class);
    }
    Objects.requireNonNull(error);
    return new ErrorProperties(error);
  }

  private String computeDefaultMessage(InvocationContext context) {
    Object[] parameters = context.getParameters();
    String p = Stream.of(parameters)
        .map(Object::toString)
        .collect(Collectors.joining(", "));
    return context.getMethod().getName() + "(" + p + ")";
  }

  @SuppressWarnings("ConfusingArgumentToVarargsMethod")
  private String computeCustomMessage(String messagePattern, Exception e,
      InvocationContext context) {
    String pattern = messagePattern.replace("{m}", e.getMessage());
    if (messagePattern.contains("{e}")) {
      StringWriter stacktrace = new StringWriter();
      e.printStackTrace(new PrintWriter(stacktrace, true));
      pattern = pattern.replace("{e}", stacktrace.toString());
    }
    String[] parameters = Stream.of(context.getParameters())
        .map(Object::toString)
        .toArray(String[]::new);
    return MessageFormat.format(pattern, parameters);
  }

  private static class ErrorProperties {
    private final String message;
    private final boolean rethrown;
    private Exception exception;

    public ErrorProperties(Error error) {
      this.message = error.message().trim();
      this.rethrown = error.rethrown();
    }

    public String getMessage() {
      return message;
    }

    public boolean isRethrown() {
      return rethrown;
    }

    public Exception getException() {
      return exception;
    }

    ErrorProperties withException(Exception e) {
      this.exception = e;
      return this;
    }
  }
}
