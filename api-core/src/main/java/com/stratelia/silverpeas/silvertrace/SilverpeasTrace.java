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

package com.stratelia.silverpeas.silvertrace;

import org.silverpeas.util.ServiceProvider;

import java.util.Properties;

/**
 * @author Yohann Chastagnier
 */
public interface SilverpeasTrace {

  public static SilverpeasTrace get() {
    return ServiceProvider.getService(SilverpeasTrace.class);
  }

  void debug(String module, String classe, String message);

  void debug(String module, String classe, String message, String extraInfos);

  void debug(String module, String classe, String message, Throwable ex);

  void debug(String module, String classe, String message, String extraInfos, Throwable ex);

  void info(String module, String classe, String messageID);

  void info(String module, String classe, String messageID, String extraInfos);

  void info(String module, String classe, String messageID, Throwable ex);

  void info(String module, String classe, String messageID, String extraInfos, Throwable ex);

  void warn(String module, String classe, String messageID);

  void warn(String module, String classe, String messageID, String extraInfos);

  void warn(String module, String classe, String messageID, Throwable ex);

  void warn(String module, String classe, String messageID, String extraInfos, Throwable ex);

  void error(String module, String classe, String messageID);

  void error(String module, String classe, String messageID, String extraInfos);

  void error(String module, String classe, String messageID, Throwable ex);

  void error(String module, String classe, String messageID, String extraInfos, Throwable ex);

  void fatal(String module, String classe, String messageID);

  void fatal(String module, String classe, String messageID, String extraInfos);

  void fatal(String module, String classe, String messageID, Throwable ex);

  void fatal(String module, String classe, String messageID, String extraInfos, Throwable ex);

  void spy(String module, String classe, String spaceId, String instanceId, String objectId,
      String userId, String actionId);

  void resetAll();

  void applyProperties(String filePath);

  void initFromProperties(Properties fileProperties);

  void setTraceLevel(String module, int val);

  int getTraceLevel(String module, boolean chained);

  void addAppenderConsole(String module, String patternLayout, String consoleName);

  void addAppenderFile(String module, String patternLayout, String fileName, boolean appendOnFile);

  void addAppenderRollingFile(String module, String patternLayout, String fileName,
      String rollingMode);

  void addAppenderMail(String module, String patternLayout, String mailHost, String mailFrom,
      String mailTo, String mailSubject);

  void removeAppender(String module, int typeOfAppender);

  Properties getModuleList();

  int getAvailableAppenders(String module);

  Properties getAppender(String module, int typeOfAppender);

  String getTraceMessage(String messageId);

  String[] getEndFileTrace(String nbLines);

  String getTraceMessage(String messageId, String language);
}
