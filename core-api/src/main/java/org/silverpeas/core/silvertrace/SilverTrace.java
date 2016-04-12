/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
* This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
* You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silvertrace;

import org.silverpeas.core.util.ServiceProvider;

import java.util.Properties;

/**
 * SilverTrace is the trace tool used in silverpeas to trace debug, running infos and errors. This
 * is a 'fully' static class. All functions could be called directly and is thread-safe. The trace
 * functions are : debug, info, warn, error, fatal.
 *
 * This class is deprecated now and it uses the Silverpeas Logging API. Please use directly the
 * latter as the Silver Trace API will be removed in the future.
 * @see org.silverpeas.core.util.logging.SilverLogger
 *
 * @author Thierry leroi
 */
@Deprecated
public class SilverTrace {
  /**
   * Used in setTraceLevel to reset a level trace.
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  @Deprecated
  public static final int TRACE_LEVEL_UNKNOWN = 0x00000000;
  /**
   * Debug-level traces.
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  @Deprecated
  public static final int TRACE_LEVEL_DEBUG = 0x00000001;
  /**
   * Info-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  @Deprecated
  public static final int TRACE_LEVEL_INFO = 0x00000002;
  /**
   * Warning-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  @Deprecated
  public static final int TRACE_LEVEL_WARN = 0x00000003;
  /**
   * Error-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  @Deprecated
  public static final int TRACE_LEVEL_ERROR = 0x00000004;
  /**
   * Fatal-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  @Deprecated
  public static final int TRACE_LEVEL_FATAL = 0x00000005;
  /**
   * Appender sending informations on console
   *
   * @see #addAppenderConsole
   * @see #removeAppender
   */
  @Deprecated
  public static final int APPENDER_CONSOLE = 0x00000001;
  /**
   * Appender sending informations on file
   *
   * @see #addAppenderFile
   * @see #removeAppender
   */
  @Deprecated
  public static final int APPENDER_FILE = 0x00000002;
  /**
   * Appender sending informations on rolling file
   *
   * @see #addAppenderRollingFile
   * @see #removeAppender
   * @see #ROLLING_MODE_MONTH
   * @see #ROLLING_MODE_WEEK
   * @see #ROLLING_MODE_DAILY
   * @see #ROLLING_MODE_HOUR
   */
  @Deprecated
  public static final int APPENDER_ROLLING_FILE = 0x00000004;
  /**
   * Appender sending informations mail
   *
   * @see #addAppenderMail
   * @see #removeAppender
   */
  @Deprecated
  public static final int APPENDER_MAIL = 0x00000008;
  /**
   * Used to remove all appenders attached to a module
   *
   * @see #removeAppender
   */
  @Deprecated
  public static final int APPENDER_ALL = 0xFFFFFFFF;
  /**
   * The trace file will be copied every 1st day of a mounth with the name :
   * FileName.ext.year-mounth A new file named FileName.ext is the created and will contains the
   * next mounth's traces Example : MyFile.txt.2001-07
   *
   * @see #addAppenderRollingFile
   */
  @Deprecated
  public final static String ROLLING_MODE_MONTH = "'.'yyyy-MM";
  /**
   * The trace file will be copied every 1st day of a week with the name : FileName.ext.year-week A
   * new file named FileName.ext is the created and will contains the next week's traces Example :
   * MyFile.txt.2001-34
   *
   * @see #addAppenderRollingFile
   */
  @Deprecated
  public final static String ROLLING_MODE_WEEK = "'.'yyyy-WW";
  /**
   * The trace file will be copied every day at midnight with the name :
   * FileName.ext.year-mounth-day A new file named FileName.ext is the created and will contains the
   * next day's traces Example : MyFile.txt.2001-07-23
   *
   * @see #addAppenderRollingFile
   */
  @Deprecated
  public final static String ROLLING_MODE_DAILY = "'.'yyyy-MM-dd";
  /**
   * The trace file will be copied every hour with the name : FileName.ext.year-mounth-day-hour A
   * new file named FileName.ext is the created and will contains the next hour's traces Example :
   * MyFile.txt.2001-07-23-18
   *
   * @see #addAppenderRollingFile
   */
  @Deprecated
  public final static String ROLLING_MODE_HOUR = "'.'yyyy-MM-dd-HH";
  /**
   * The silverpeas root module's name
   */
  @Deprecated
  public final static String MODULE_ROOT = "root";
  /**
   * The special output for ERROR and FATAL module's name
   */
  @Deprecated
  public final static String MODULE_ERROR_AND_FATAL = "outErrorAndFatal";
  /**
   * The special output for SPY module's name
   */
  @Deprecated
  public final static String MODULE_SPY = "outSpy";
  /**
   * Create action code
   */
  @Deprecated
  public static final String SPY_ACTION_CREATE = "1";
  /**
   * Delete action code
   */
  @Deprecated
  public static final String SPY_ACTION_DELETE = "2";
  /**
   * Update action code
   */
  @Deprecated
  public static final String SPY_ACTION_UPDATE = "3";
  // Level 1
  /**
   * The Bus module's name
   */
  @Deprecated
  public static final String MODULE_BUS = "bus";
  /**
   * The Admin module's name
   */
  @Deprecated
  public static final String MODULE_ADMIN = "admin";
  /**
   * The Components module's name
   */
  @Deprecated
  public static final String MODULE_COMPONENTS = "components";
  /**
   * The Libraries module's name
   */
  @Deprecated
  public static final String MODULE_LIBRARIES = "libraries";

  @Deprecated
  private static SilverpeasTrace getSilverpeasTrace() {
    return ServiceProvider.getService(SilverpeasTrace.class);
  }

  @Deprecated
  public static void debug(String module, String classe, String message) {
    debug(module, classe, message, null, null);
  }

  @Deprecated
  public static void debug(String module, String classe, String message, String extraInfos) {
    debug(module, classe, message, extraInfos, null);
  }

  @Deprecated
  public static void debug(String module, String classe, String message, Throwable ex) {
    debug(module, classe, message, null, ex);
  }

  @Deprecated
  public static void debug(String module, String classe, String message, String extraInfos,
      Throwable ex) {
    getSilverpeasTrace().debug(module, classe, message, extraInfos, ex);
  }

  @Deprecated
  public static void info(String module, String classe, String messageID) {
    info(module, classe, messageID, null, null);
  }

  @Deprecated
  public static void info(String module, String classe, String messageID, String extraInfos) {
    info(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  public static void info(String module, String classe, String messageID, Throwable ex) {
    info(module, classe, messageID, null, ex);
  }

  @Deprecated
  public static void info(String module, String classe, String messageID, String extraInfos, Throwable ex) {
    getSilverpeasTrace().info(module, classe, messageID, extraInfos, ex);
  }

  @Deprecated
  public static void warn(String module, String classe, String messageID) {
    warn(module, classe, messageID, null, null);
  }

  @Deprecated
  public static void warn(String module, String classe, String messageID, String extraInfos) {
    warn(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  public static void warn(String module, String classe, String messageID, Throwable ex) {
    warn(module, classe, messageID, null, ex);
  }

  @Deprecated
  public static void warn(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    getSilverpeasTrace().warn(module, classe, messageID, extraInfos, ex);
  }

  @Deprecated
  public static void error(String module, String classe, String messageID) {
    error(module, classe, messageID, null, null);
  }

  @Deprecated
  public static void error(String module, String classe, String messageID, String extraInfos) {
    error(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  public static void error(String module, String classe, String messageID, Throwable ex) {
    error(module, classe, messageID, null, ex);
  }

  @Deprecated
  public static void error(String module, String classe, String messageID, String extraInfos, Throwable ex) {
    getSilverpeasTrace().error(module, classe, messageID, extraInfos, ex);
  }

  @Deprecated
  public static void fatal(String module, String classe, String messageID) {
    fatal(module, classe, messageID, null, null);
  }

  @Deprecated
  public static void fatal(String module, String classe, String messageID, String extraInfos) {
    fatal(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  public static void fatal(String module, String classe, String messageID, Throwable ex) {
    fatal(module, classe, messageID, null, ex);
  }

  @Deprecated
  public static void fatal(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    getSilverpeasTrace().fatal(module, classe, messageID, extraInfos, ex);
  }

  @Deprecated
  public static void spy(String module, String classe, String spaceId, String instanceId, String objectId, String userId, String actionId) {
    getSilverpeasTrace().spy(module, classe, spaceId, instanceId, objectId, userId, actionId);
  }

  @Deprecated
  public static void resetAll() {
    getSilverpeasTrace().resetAll();
  }

  @Deprecated
  public static void applyProperties(String filePath) {
    getSilverpeasTrace().applyProperties(filePath);
  }

  @Deprecated
  public static void initFromProperties(Properties fileProperties) {
    getSilverpeasTrace().initFromProperties(fileProperties);
  }

  @Deprecated
  public static void setTraceLevel(String module, int val) {
    getSilverpeasTrace().setTraceLevel(module, val);
  }

  @Deprecated
  public static int getTraceLevel(String module, boolean chained) {
    return getSilverpeasTrace().getTraceLevel(module, chained);
  }

  @Deprecated
  public static void addAppenderConsole(String module, String patternLayout, String consoleName) {
    getSilverpeasTrace().addAppenderConsole(module, patternLayout, consoleName);
  }

  @Deprecated
  public static void addAppenderFile(String module, String patternLayout, String fileName,
      boolean appendOnFile) {
    getSilverpeasTrace().addAppenderFile(module, patternLayout, fileName, appendOnFile);
  }

  @Deprecated
  public static void addAppenderRollingFile(String module, String patternLayout, String fileName,
      String rollingMode) {
    getSilverpeasTrace().addAppenderRollingFile(module, patternLayout, fileName, rollingMode);
  }

  @Deprecated
  public static void addAppenderMail(String module, String patternLayout, String mailHost,
      String mailFrom, String mailTo, String mailSubject) {
    getSilverpeasTrace()
        .addAppenderMail(module, patternLayout, mailHost, mailFrom, mailTo, mailSubject);
  }

  @Deprecated
  public static void removeAppender(String module, int typeOfAppender) {
    getSilverpeasTrace().removeAppender(module, typeOfAppender);
  }

  @Deprecated
  public static Properties getModuleList() {
    return getSilverpeasTrace().getModuleList();
  }

  @Deprecated
  public static int getAvailableAppenders(String module) {
    return getSilverpeasTrace().getAvailableAppenders(module);
  }

  @Deprecated
  public static Properties getAppender(String module, int typeOfAppender) {
    return getSilverpeasTrace().getAppender(module, typeOfAppender);
  }

  @Deprecated
  public static String getTraceMessage(String messageId) {
    return getSilverpeasTrace().getTraceMessage(messageId);
  }

  @Deprecated
  public static String[] getEndFileTrace(String nbLines) {
    return getSilverpeasTrace().getEndFileTrace(nbLines);
  }

  @Deprecated
  public static String getTraceMessage(String messageId, String language) {
    return getSilverpeasTrace().getTraceMessage(messageId, language);
  }
}
