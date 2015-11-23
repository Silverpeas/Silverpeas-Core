/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.silverpeas.silvertrace;

import org.silverpeas.util.ServiceProvider;

import java.util.Properties;

/**
 * SilverTrace is the trace tool used in silverpeas to trace debug, running infos and errors. This
 * is a 'fully' static class. All functions could be called directly and is thread-safe. The trace
 * functions are : debug, info, warn, error, fatal.
 * 
* @author Thierry leroi
 */
public class SilverTrace {
  /**
   * Used in setTraceLevel to reset a level trace.
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_UNKNOWN = 0x00000000;
  /**
   * Debug-level traces.
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_DEBUG = 0x00000001;
  /**
   * Info-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_INFO = 0x00000002;
  /**
   * Warning-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_WARN = 0x00000003;
  /**
   * Error-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_ERROR = 0x00000004;
  /**
   * Fatal-level traces
   *
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_FATAL = 0x00000005;
  /**
   * Appender sending informations on console
   *
   * @see #addAppenderConsole
   * @see #removeAppender
   */
  public static final int APPENDER_CONSOLE = 0x00000001;
  /**
   * Appender sending informations on file
   *
   * @see #addAppenderFile
   * @see #removeAppender
   */
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
  public static final int APPENDER_ROLLING_FILE = 0x00000004;
  /**
   * Appender sending informations mail
   *
   * @see #addAppenderMail
   * @see #removeAppender
   */
  public static final int APPENDER_MAIL = 0x00000008;
  /**
   * Used to remove all appenders attached to a module
   *
   * @see #removeAppender
   */
  public static final int APPENDER_ALL = 0xFFFFFFFF;
  /**
   * The trace file will be copied every 1st day of a mounth with the name :
   * FileName.ext.year-mounth A new file named FileName.ext is the created and will contains the
   * next mounth's traces Example : MyFile.txt.2001-07
   *
   * @see #addAppenderRollingFile
   */
  public final static String ROLLING_MODE_MONTH = "'.'yyyy-MM";
  /**
   * The trace file will be copied every 1st day of a week with the name : FileName.ext.year-week A
   * new file named FileName.ext is the created and will contains the next week's traces Example :
   * MyFile.txt.2001-34
   *
   * @see #addAppenderRollingFile
   */
  public final static String ROLLING_MODE_WEEK = "'.'yyyy-WW";
  /**
   * The trace file will be copied every day at midnight with the name :
   * FileName.ext.year-mounth-day A new file named FileName.ext is the created and will contains the
   * next day's traces Example : MyFile.txt.2001-07-23
   *
   * @see #addAppenderRollingFile
   */
  public final static String ROLLING_MODE_DAILY = "'.'yyyy-MM-dd";
  /**
   * The trace file will be copied every hour with the name : FileName.ext.year-mounth-day-hour A
   * new file named FileName.ext is the created and will contains the next hour's traces Example :
   * MyFile.txt.2001-07-23-18
   *
   * @see #addAppenderRollingFile
   */
  public final static String ROLLING_MODE_HOUR = "'.'yyyy-MM-dd-HH";
  /**
   * The silverpeas root module's name
   */
  public final static String MODULE_ROOT = "root";
  /**
   * The special output for ERROR and FATAL module's name
   */
  public final static String MODULE_ERROR_AND_FATAL = "outErrorAndFatal";
  /**
   * The special output for SPY module's name
   */
  public final static String MODULE_SPY = "outSpy";
  /**
   * Create action code
   */
  public static final String SPY_ACTION_CREATE = "1";
  /**
   * Delete action code
   */
  public static final String SPY_ACTION_DELETE = "2";
  /**
   * Update action code
   */
  public static final String SPY_ACTION_UPDATE = "3";
  // Level 1
  /**
   * The Bus module's name
   */
  public static final String MODULE_BUS = "bus";
  /**
   * The Admin module's name
   */
  public static final String MODULE_ADMIN = "admin";
  /**
   * The Components module's name
   */
  public static final String MODULE_COMPONENTS = "components";
  /**
   * The Libraries module's name
   */
  public static final String MODULE_LIBRARIES = "libraries";

  private static SilverpeasTrace getSilverpeasTrace() {
    return ServiceProvider.getService(SilverpeasTrace.class);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants...
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   */
  public static void debug(String module, String classe, String message) {
    debug(module, classe, message, null, null);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants... This
   * function have one extra parameter : extraInfos to add additional informations
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  public static void debug(String module, String classe, String message, String extraInfos) {
    debug(module, classe, message, extraInfos, null);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants... This
   * function have one extra parameters : ex to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   * @param ex the exception to trace
   */
  public static void debug(String module, String classe, String message, Throwable ex) {
    debug(module, classe, message, null, ex);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants... This
   * function have two extra parameters : extraInfos to add additional informations ex to display an
   * exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  public static void debug(String module, String classe, String message, String extraInfos,
      Throwable ex) {
    getSilverpeasTrace().debug(module, classe, message, extraInfos, ex);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. To add some extra infos, use the function with the 4th parameter : extraInfos
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  public static void info(String module, String classe, String messageID) {
    info(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameter : extraInfos to add additional informations
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  public static void info(String module, String classe, String messageID, String extraInfos) {
    info(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameters : ex to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to tracer
   */
  public static void info(String module, String classe, String messageID, Throwable ex) {
    info(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. This function have two extra parameters : extraInfos to add additional informations ex
   * to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  public static void info(String module, String classe, String messageID, String extraInfos, Throwable ex) {
    getSilverpeasTrace().info(module, classe, messageID, extraInfos, ex);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. To add some extra infos, use the function with the 4th parameter : extraInfos
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  public static void warn(String module, String classe, String messageID) {
    warn(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameter : extraInfos to add additional informations
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  public static void warn(String module, String classe, String messageID, String extraInfos) {
    warn(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameters : ex to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  public static void warn(String module, String classe, String messageID, Throwable ex) {
    warn(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. This function have two extra parameters : extraInfos to add additional informations ex
   * to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  public static void warn(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    getSilverpeasTrace().warn(module, classe, messageID, extraInfos, ex);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. To add some extra infos, use the function with the 4th parameter : extraInfos
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  public static void error(String module, String classe, String messageID) {
    error(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameter : extraInfos to add additional informations
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  public static void error(String module, String classe, String messageID, String extraInfos) {
    error(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameters : ex to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  public static void error(String module, String classe, String messageID, Throwable ex) {
    error(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. This function have two extra parameters : extraInfos to add additional informations ex
   * to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  public static void error(String module, String classe, String messageID, String extraInfos, Throwable ex) {
    getSilverpeasTrace().error(module, classe, messageID, extraInfos, ex);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. To add some extra infos, use the function with the 4th parameter : extraInfos
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  public static void fatal(String module, String classe, String messageID) {
    fatal(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. This function have one extra parameter : extraInfos to add additional
   * informations
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  public static void fatal(String module, String classe, String messageID, String extraInfos) {
    fatal(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. This function have one extra parameters : ex to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  public static void fatal(String module, String classe, String messageID, Throwable ex) {
    fatal(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. This function have two extra parameters : extraInfos to add additional
   * informations ex to display an exception
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  public static void fatal(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    getSilverpeasTrace().fatal(module, classe, messageID, extraInfos, ex);
  }

  /**
   * Trace some actions (create, delete, update) done by a user on an object of an instance in a
   * space.
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param spaceId
   * @param instanceId
   * @param objectId the object (ex. publication) which is created, deleted ou updated.
   * @param userId the user who has created, deleted or updated.
   * @param actionId One of the constants SPY_ACTION_CREATE, SPY_ACTION_DELETE, SPY_ACTION_UPDATE.
   */
  public static void spy(String module, String classe, String spaceId, String instanceId, String objectId, String userId, String actionId) {
    getSilverpeasTrace().spy(module, classe, spaceId, instanceId, objectId, userId, actionId);
  }

  /**
   * Reset all modules, messages, appenders and all set debug levels.
   */
  public static void resetAll() {
    getSilverpeasTrace().resetAll();
  }

  public static void applyProperties(String filePath) {
    getSilverpeasTrace().applyProperties(filePath);
  }

  /**
   * Loads the configuration from the properties given in argument.
   *   
* @param fileProperties the properties to merge with the current configuration
   */
  public static void initFromProperties(Properties fileProperties) {
    getSilverpeasTrace().initFromProperties(fileProperties);
  }

  /**
   * Set the minimum trace level of a module. All traces less than val will not be taken into
   * account
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param val the trace level : could be one of the TRACE_LEVEL_... values. Use
   * TRACE_LEVEL_UNKNOWN to remove the level condition for the module.
   */
  public static void setTraceLevel(String module, int val) {
    getSilverpeasTrace().setTraceLevel(module, val);
  }

  /**
   * Get the trace level of a module. Depending on the value of chained, it could ask for the
   * inherited trace levels or not.
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param chained if false : return a trace level only if the module have been set with one. Else,
   * return TRACE_LEVEL_UNKNOWN. If true, look for the inherited trace level.
   * @return the trace level of the module or TRACE_LEVEL_UNKNOWN if the module was not found
   */
  public static int getTraceLevel(String module, boolean chained) {
    return getSilverpeasTrace().getTraceLevel(module, chained);
  }

  /**
   * Add a new console appender to the module. If an appender with the same type have been
   * previously set, delete it and replace it with the new created one.
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param consoleName Name of the console output. If null or "", "system.out" is used
   */
  public static void addAppenderConsole(String module, String patternLayout, String consoleName) {
    getSilverpeasTrace().addAppenderConsole(module, patternLayout, consoleName);
  }

  /**
   * Add a new file appender to the module. If an appender with the same type have been previously
   * set, delete it and replace it with the new created one.
   *   
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param fileName full-path name of the file where the trace are written
   * @param appendOnFile true to append at the end of the existing file (if ther is one), false to
   * remove old file before writting
   */
  public static void addAppenderFile(String module, String patternLayout, String fileName,
      boolean appendOnFile) {
    getSilverpeasTrace().addAppenderFile(module, patternLayout, fileName, appendOnFile);
  }

  /**
   * Add a new rolling file appender to the module. If an appender with the same type have been
   * previously set, delete it and replace it with the new created one.
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param fileName full-path name of the file where the trace are written
   * @param rollingMode frequency of the rolling file, could be one of the ROLLING_MODE_...
   * constants
   */
  public static void addAppenderRollingFile(String module, String patternLayout, String fileName,
      String rollingMode) {
    getSilverpeasTrace().addAppenderRollingFile(module, patternLayout, fileName, rollingMode);
  }

  /**
   * Add a new mail appender to the module. If an appender with the same type have been previously
   * set, delete it and replace it with the new created one. How it works : mails are only sent when
   * an ERROR or FATAL occur. The mail contains the error and the 512 last traces taken into account
   * (ie, higher than the trace level).
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param mailHost host name
   * @param mailFrom email of the sender
   * @param mailTo target email, could be multiple targets separeted with comas
   * @param mailSubject subject of the mail
   */
  public static void addAppenderMail(String module, String patternLayout, String mailHost,
      String mailFrom, String mailTo, String mailSubject) {
    getSilverpeasTrace()
        .addAppenderMail(module, patternLayout, mailHost, mailFrom, mailTo, mailSubject);
  }

  /**
   * Remove appender(s) attached to a module. typeOfAppender could be one value or a mask of
   * multiple appender types
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param typeOfAppender could be a mask of APPENDER_... values or APPENDER_ALL to remove all
   * appenders attached to the module
   */
  public static void removeAppender(String module, int typeOfAppender) {
    getSilverpeasTrace().removeAppender(module, typeOfAppender);
  }

  /**
   * The purpose of this function is just to return the list of available modules to the JSP -
   * Exploitation page This function is subject to change and SHOULD NOT BE USED by any other page
   * or java class...
   *   
* @return The list of the modules with pairs (<module name>,<log4j-path to this module>)
   */
  public static Properties getModuleList() {
    return getSilverpeasTrace().getModuleList();
  }

  /**
   * The purpose of this function is just to return the available appenders for the JSP -
   * Exploitation page This function is subject to change and SHOULD NOT BE USED by any other page
   * or java class...
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @return a mask of the appenders set to this module (not containing the herited ones)
   */
  public static int getAvailableAppenders(String module) {
    return getSilverpeasTrace().getAvailableAppenders(module);
  }

  /**
   * The purpose of this function is just to return informations about an appender for the JSP -
   * Exploitation page This function is subject to change and SHOULD NOT BE USED by any other page
   * or java class...
   *   
* @param module the module name (ex : root, bus, outlook, ...)
   * @param typeOfAppender the type of appender : one of the APPENDER_... constants
   * @return A set of properties discribing the attached appender or null if there is no such
   * appender attached to this module
   */
  public static Properties getAppender(String module, int typeOfAppender) {
    return getSilverpeasTrace().getAppender(module, typeOfAppender);
  }

  /**
   * Returns the message corresponding to the MessageId in the SilverTrace default language
   *   
   * @param messageId the message ID (ex. 'admin.MSG_ERR_GENERAL')
   * @return the message if the SilverTrace default language
   */
  public static String getTraceMessage(String messageId) {
    return getSilverpeasTrace().getTraceMessage(messageId);
  }

  public static String[] getEndFileTrace(String nbLines) {
    return getSilverpeasTrace().getEndFileTrace(nbLines);
  }

  /**
   * Returns the language-dependant message corresponding to the MessageId
   *   
* @param messageId the message ID (ex. 'admin.MSG_ERR_GENERAL')
   * @param language the language to display the message in
   * @return the message if the specified language
   */
  public static String getTraceMessage(String messageId, String language) {
    return getSilverpeasTrace().getTraceMessage(messageId, language);
  }
}
