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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import java.lang.reflect.Method;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A processor of {@code org.silverpeas.core.util.logging.Log} annotations. Each time a method annotated
 * with this annotation is invoked, this processor will produce a log record with as information
 * the user behind the invocation and another log record at the method execution end with as
 * additional information the time spent at the method execution.
 * @author mmoquillon
 */
@Interceptor
@Log
@Priority(APPLICATION)
public class LogAnnotationProcessor {

  protected static final String SYSTEM_DEFAULT_BEFORE_PATTERN = "[SILVERPEAS] Invocation of {0}#{1}";
  protected static final String SYSTEM_DEFAULT_AFTER_PATTERN =
      "[SILVERPEAS] Invocation of {0}#{1} done in {2}ms";
  protected static final String USER_DEFAULT_BEFORE_PATTERN = "[{0} ({1})] Invocation of {2}#{3}";
  protected static final String USER_DEFAULT_AFTER_PATTERN =
      "[{0} ({1})] Invocation of {2}#{3} done in {4}ms";
  protected static final String SYSTEM_PATTERN = "[SILVERPEAS] {0}";
  protected static final String USER_PATTERN = "[{0} ({1})] {2}";

  @AroundInvoke
  public Object produceLogRecords(InvocationContext context) throws Exception {
    Object result;
    SilverLogger logger = SilverLogger.getLogger(context.getTarget());
    UserDetail currentUser = UserDetail.getCurrentRequester();
    String message = getAnyLogMessageFor(context.getMethod());
    if (StringUtil.isDefined(message)) {
      if (currentUser == null) {
        logger.info(SYSTEM_PATTERN,
            message);
      } else {
        logger.info(USER_PATTERN,
            currentUser.getDisplayedName(),
            currentUser.getId(),
            message);
      }
      result = context.proceed();
    } else {
      if (currentUser == null) {
        logger.info(SYSTEM_DEFAULT_BEFORE_PATTERN,
            context.getMethod().getDeclaringClass().getSimpleName(),
            context.getMethod().getName());
        long start = System.currentTimeMillis();
        result = context.proceed();
        long duration = System.currentTimeMillis() - start;
        logger.info(SYSTEM_DEFAULT_AFTER_PATTERN,
            context.getMethod().getDeclaringClass().getSimpleName(),
            context.getMethod().getName(),
            duration);
      } else {
        logger.info(USER_DEFAULT_BEFORE_PATTERN,
            currentUser.getDisplayedName(),
            currentUser.getId(),
            context.getMethod().getDeclaringClass().getSimpleName(),
            context.getMethod().getName());
        long start = System.currentTimeMillis();
        result = context.proceed();
        long duration = System.currentTimeMillis() - start;
        logger.info(USER_DEFAULT_AFTER_PATTERN,
            currentUser.getDisplayedName(),
            currentUser.getId(),
            context.getMethod().getDeclaringClass().getSimpleName(),
            context.getMethod().getName(),
            duration);
      }
    }

    return result;
  }

  private String getAnyLogMessageFor(Method method) {
    Log log = method.getAnnotation(Log.class);
    if (log == null) {
      log = method.getDeclaringClass().getAnnotation(Log.class);
    }
    return (log != null ? log.message():null);
  }

}
