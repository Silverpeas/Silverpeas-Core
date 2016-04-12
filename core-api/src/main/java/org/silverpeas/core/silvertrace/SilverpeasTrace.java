/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.silvertrace;

import org.silverpeas.core.util.ServiceProvider;

import java.util.Properties;

/**
 * This class is deprecated. It is now replaced by the Silverpeas Logging API.
 * @see org.silverpeas.core.util.logging.SilverLogger
 * @deprecated
 */
@Deprecated
public interface SilverpeasTrace {

  @Deprecated
  static SilverpeasTrace get() {
    return ServiceProvider.getService(SilverpeasTrace.class);
  }

  @Deprecated
  void debug(String module, String classe, String message);

  @Deprecated
  void debug(String module, String classe, String message, String extraInfos);

  @Deprecated
  void debug(String module, String classe, String message, Throwable ex);

  @Deprecated
  void debug(String module, String classe, String message, String extraInfos, Throwable ex);

  @Deprecated
  void info(String module, String classe, String messageID);

  @Deprecated
  void info(String module, String classe, String messageID, String extraInfos);

  @Deprecated
  void info(String module, String classe, String messageID, Throwable ex);

  @Deprecated
  void info(String module, String classe, String messageID, String extraInfos, Throwable ex);

  @Deprecated
  void warn(String module, String classe, String messageID);

  @Deprecated
  void warn(String module, String classe, String messageID, String extraInfos);

  @Deprecated
  void warn(String module, String classe, String messageID, Throwable ex);

  @Deprecated
  void warn(String module, String classe, String messageID, String extraInfos, Throwable ex);

  @Deprecated
  void error(String module, String classe, String messageID);

  @Deprecated
  void error(String module, String classe, String messageID, String extraInfos);

  @Deprecated
  void error(String module, String classe, String messageID, Throwable ex);

  @Deprecated
  void error(String module, String classe, String messageID, String extraInfos, Throwable ex);

  @Deprecated
  void fatal(String module, String classe, String messageID);

  @Deprecated
  void fatal(String module, String classe, String messageID, String extraInfos);

  @Deprecated
  void fatal(String module, String classe, String messageID, Throwable ex);

  @Deprecated
  void fatal(String module, String classe, String messageID, String extraInfos, Throwable ex);

  @Deprecated
  void spy(String module, String classe, String spaceId, String instanceId, String objectId,
      String userId, String actionId);

  @Deprecated
  void resetAll();

  @Deprecated
  void applyProperties(String filePath);

  @Deprecated
  void initFromProperties(Properties fileProperties);

  @Deprecated
  void setTraceLevel(String module, int val);

  @Deprecated
  int getTraceLevel(String module, boolean chained);

  @Deprecated
  void addAppenderConsole(String module, String patternLayout, String consoleName);

  @Deprecated
  void addAppenderFile(String module, String patternLayout, String fileName, boolean appendOnFile);

  @Deprecated
  void addAppenderRollingFile(String module, String patternLayout, String fileName,
      String rollingMode);

  @Deprecated
  void addAppenderMail(String module, String patternLayout, String mailHost, String mailFrom,
      String mailTo, String mailSubject);

  @Deprecated
  void removeAppender(String module, int typeOfAppender);

  @Deprecated
  Properties getModuleList();

  @Deprecated
  int getAvailableAppenders(String module);

  @Deprecated
  Properties getAppender(String module, int typeOfAppender);

  @Deprecated
  String getTraceMessage(String messageId);

  @Deprecated
  String[] getEndFileTrace(String nbLines);

  @Deprecated
  String getTraceMessage(String messageId, String language);
}
