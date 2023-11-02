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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A processor of {@code org.silverpeas.core.util.logging.Log} annotations. Each time a method
 * annotated with this annotation is invoked, this processor will produce a log record with as
 * information the user behind the invocation and another log record at the method execution end
 * with as additional information the time spent at the method execution.
 *
 * @author mmoquillon
 */
@Interceptor
@Log
@Priority(APPLICATION)
public class LogAnnotationProcessor {

  protected static final String SYSTEM_DEFAULT_BEFORE_PATTERN = "[SYSTEM] Invocation of {0}#{1}";
  protected static final String SYSTEM_DEFAULT_AFTER_PATTERN =
      "[SYSTEM] Invocation of {0}#{1} done in {2}ms";
  protected static final String USER_DEFAULT_BEFORE_PATTERN = "[{0} ({1})] Invocation of {2}#{3}";
  protected static final String USER_DEFAULT_AFTER_PATTERN =
      "[{0} ({1})] Invocation of {2}#{3} done in {4}ms";
  protected static final String SYSTEM_BEFORE_PATTERN = "[SILVERPEAS] {0}";
  protected static final String SYSTEM_AFTER_PATTERN = "[SILVERPEAS] {0}. Done in {1}ms";
  protected static final String USER_BEFORE_PATTERN = "[{0} ({1})] {2}";
  protected static final String USER_AFTER_PATTERN = "[{0} ({1})] {2}. Done in {3}ms";

  @AroundInvoke
  public Object produceLogRecords(InvocationContext context) throws Exception {
    Object result;
    var logProps = getLogProperties(context);
    SilverLogger logger = SilverLogger.getLogger(context.getTarget());
    if (StringUtil.isDefined(logProps.getMessage())) {
      result = logCustomMessage(logger, logProps, context);
    } else {
      result = logDefaultMessage(logger, logProps, context);
    }

    return result;
  }

  private Object logCustomMessage(SilverLogger logger, LogProperties logProps,
      InvocationContext context)
      throws Exception {
    UserDetail currentUser = UserDetail.getCurrentRequester();
    Result result;
    String message = computeCustomMessage(logProps.getMessage(), context);
    if (currentUser == null) {
      logger.log(logProps.getLevel(), SYSTEM_BEFORE_PATTERN, message);
      result = executeMethod(context);
      if (logProps.isDualRecord()) {
        logger.log(logProps.getLevel(), SYSTEM_AFTER_PATTERN, message, result.getTime());
      }
    } else {
      logger.log(logProps.getLevel(), USER_BEFORE_PATTERN,
          currentUser.getDisplayedName(),
          currentUser.getId(),
          message);
      result = executeMethod(context);
      if (logProps.isDualRecord()) {
        logger.log(logProps.getLevel(), USER_AFTER_PATTERN,
            currentUser.getDisplayedName(),
            currentUser.getId(),
            message,
            result.getTime());
      }
    }
    return result.getValue();
  }

  private Object logDefaultMessage(SilverLogger logger, LogProperties logProps,
      InvocationContext context)
      throws Exception {
    UserDetail currentUser = UserDetail.getCurrentRequester();
    Result result;
    String className = context.getMethod().getDeclaringClass().getSimpleName();
    String message = computeDefaultMessage(context);
    if (currentUser == null) {
      logger.log(logProps.getLevel(), SYSTEM_DEFAULT_BEFORE_PATTERN,
          className,
          message);
      result = executeMethod(context);
      if (logProps.isDualRecord()) {
        logger.log(logProps.getLevel(), SYSTEM_DEFAULT_AFTER_PATTERN,
            className,
            message,
            result.getTime());
      }
    } else {
      logger.log(logProps.getLevel(), USER_DEFAULT_BEFORE_PATTERN,
          currentUser.getDisplayedName(),
          currentUser.getId(),
          className,
          message);
      result = executeMethod(context);
      if (logProps.isDualRecord()) {
        logger.log(logProps.getLevel(), USER_DEFAULT_AFTER_PATTERN,
            currentUser.getDisplayedName(),
            currentUser.getId(),
            className,
            message,
            result.getTime());
      }
    }
    return result.getValue();
  }

  private LogProperties getLogProperties(InvocationContext context) {
    Method method = context.getMethod();
    Log log = method.getAnnotation(Log.class);
    if (log == null) {
      log = method.getDeclaringClass().getAnnotation(Log.class);
    }
    Objects.requireNonNull(log);
    return new LogProperties(log);
  }

  private String computeDefaultMessage(InvocationContext context) {
    Object[] parameters = context.getParameters();
    String p = Stream.of(parameters)
        .map(Object::toString)
        .collect(Collectors.joining(", "));
    return context.getMethod().getName() + "(" + p + ")";
  }

  @SuppressWarnings("ConfusingArgumentToVarargsMethod")
  private String computeCustomMessage(String messagePattern, InvocationContext context) {
    String[] parameters = Stream.of(context.getParameters())
        .map(Object::toString)
        .toArray(String[]::new);
    return MessageFormat.format(messagePattern, parameters);
  }

  private Result executeMethod(InvocationContext context) throws Exception {
    long start = System.currentTimeMillis();
    Object result = context.proceed();
    long duration = System.currentTimeMillis() - start;
    return new Result(duration, result);
  }

  private static class LogProperties {
    private final String message;
    private final boolean dualRecord;
    private final Level level;

    public LogProperties(Log log) {
      this.message = log.message().trim();
      this.dualRecord = log.dualRecord();
      this.level = log.level();
    }

    public String getMessage() {
      return message;
    }

    public boolean isDualRecord() {
      return dualRecord;
    }

    public Level getLevel() {
      return level;
    }
  }

  private static class Result {
    private final long time;
    private final Object value;

    public Result(long time, Object value) {
      this.time = time;
      this.value = value;
    }

    public long getTime() {
      return time;
    }

    public Object getValue() {
      return value;
    }
  }
}
