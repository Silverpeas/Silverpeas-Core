/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import org.apache.log4j.net.SMTPAppender;

import com.silverpeas.util.FileUtil;

/**
 * SilverTrace is the trace tool used in silverpeas to trace debug, running infos and errors. This
 * is a 'fully' static class. All functions could be called directly and is thread-safe. The trace
 * functions are : debug, info, warn, error, fatal.
 * @author Thierry leroi
 */
public class SilverTrace {

  /**
   * Used in setTraceLevel to reset a level trace
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_UNKNOWN = 0x00000000;

  /**
   * Debug-level traces
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_DEBUG = 0x00000001;

  /**
   * Info-level traces
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_INFO = 0x00000002;

  /**
   * Warning-level traces
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_WARN = 0x00000003;

  /**
   * Error-level traces
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_ERROR = 0x00000004;

  /**
   * Fatal-level traces
   * @see #setTraceLevel
   * @see #getTraceLevel
   */
  public static final int TRACE_LEVEL_FATAL = 0x00000005;

  /**
   * Appender sending informations on console
   * @see #addAppenderConsole
   * @see #removeAppender
   */
  public static final int APPENDER_CONSOLE = 0x00000001;

  /**
   * Appender sending informations on file
   * @see #addAppenderFile
   * @see #removeAppender
   */
  public static final int APPENDER_FILE = 0x00000002;

  /**
   * Appender sending informations on rolling file
   * @see #addAppenderRollingFile
   * @see #removeAppender
   * @see #ROLLING_MODE_MOUNTH
   * @see #ROLLING_MODE_WEEK
   * @see #ROLLING_MODE_DAILY
   * @see #ROLLING_MODE_HOUR
   */
  public static final int APPENDER_ROLLING_FILE = 0x00000004;

  /**
   * Appender sending informations mail
   * @see #addAppenderMail
   * @see #removeAppender
   */
  public static final int APPENDER_MAIL = 0x00000008;

  /**
   * Used to remove all appenders attached to a module
   * @see #removeAppender
   */
  public static final int APPENDER_ALL = 0xFFFFFFFF;

  /**
   * HTML layout : Display "Time / Thread / Priority / Category / Message" into a TABLE
   * @see #addAppenderConsole
   * @see #addAppenderFile
   * @see #addAppenderRollingFile
   * @see #addAppenderMail
   */
  public static String LAYOUT_HTML = "LAYOUT_HTML";

  /**
   * Short layout : Display "Time / Priority / Message"
   * @see #addAppenderConsole
   * @see #addAppenderFile
   * @see #addAppenderRollingFile
   * @see #addAppenderMail
   */
  public static String LAYOUT_SHORT = "LAYOUT_SHORT";

  /**
   * Detailed layout : Display "Time / Priority / Calling Class and module / Message"
   * @see #addAppenderConsole
   * @see #addAppenderFile
   * @see #addAppenderRollingFile
   * @see #addAppenderMail
   */
  public static String LAYOUT_DETAILED = "LAYOUT_DETAILED";

  /**
   * Fully detailed layout : Display
   * "Tic count / Time / Priority / Thread / Calling Class and module / Message"
   * @see #addAppenderConsole
   * @see #addAppenderFile
   * @see #addAppenderRollingFile
   * @see #addAppenderMail
   */
  public static String LAYOUT_FULL_DEBUG = "LAYOUT_FULL_DEBUG";

  /**
   * The trace file will be copied every 1st day of a mounth with the name :
   * FileName.ext.year-mounth A new file named FileName.ext is the created and will contains the
   * next mounth's traces Example : MyFile.txt.2001-07
   * @see #addAppenderRollingFile
   */
  public static String ROLLING_MODE_MOUNTH = "'.'yyyy-MM";

  /**
   * The trace file will be copied every 1st day of a week with the name : FileName.ext.year-week A
   * new file named FileName.ext is the created and will contains the next week's traces Example :
   * MyFile.txt.2001-34
   * @see #addAppenderRollingFile
   */
  public static String ROLLING_MODE_WEEK = "'.'yyyy-ww";

  /**
   * The trace file will be copied every day at midnight with the name :
   * FileName.ext.year-mounth-day A new file named FileName.ext is the created and will contains the
   * next day's traces Example : MyFile.txt.2001-07-23
   * @see #addAppenderRollingFile
   */
  public static String ROLLING_MODE_DAILY = "'.'yyyy-MM-dd";

  /**
   * The trace file will be copied every hour with the name : FileName.ext.year-mounth-day-hour A
   * new file named FileName.ext is the created and will contains the next hour's traces Example :
   * MyFile.txt.2001-07-23-18
   * @see #addAppenderRollingFile
   */
  public static String ROLLING_MODE_HOUR = "'.'yyyy-MM-dd-HH";

  // Modules
  // Level 0

  /**
   * The silverpeas root module's name
   */
  public static String MODULE_ROOT = "root";

  /**
   * The old Debug class module's name
   */
  public static String MODULE_OLD_DEBUG = "oldDebug";

  /**
   * The special output for ERROR and FATAL module's name
   */
  public static String MODULE_ERROR_AND_FATAL = "outErrorAndFatal";

  /**
   * The special output for SPY module's name
   */
  public static String MODULE_SPY = "outSpy";

  /**
   * Create action code
   */
  public static String SPY_ACTION_CREATE = "1";
  /**
   * Delete action code
   */
  public static String SPY_ACTION_DELETE = "2";
  /**
   * Update action code
   */
  public static String SPY_ACTION_UPDATE = "3";

  // Level 1

  /**
   * The Bus module's name
   */
  public static String MODULE_BUS = "bus";

  /**
   * The Admin module's name
   */
  public static String MODULE_ADMIN = "admin";

  /**
   * The Components module's name
   */
  public static String MODULE_COMPONENTS = "components";

  /**
   * The Libraries module's name
   */
  public static String MODULE_LIBRARIES = "libraries";

  // Available modules
  protected static Properties availableModules = new Properties();

  // Messages
  protected static MsgTrace traceMessages = new MsgTrace();

  // Directory to the error files
  protected static String errorDir = null;

  // Layouts
  protected static String layoutShort = "%-5p : %m%n";
  protected static String layoutDetailed = "%d{dd/MM/yy-HH:mm:ss,SSS} - %-5p : %m%n";
  protected static String layoutFullDebug =
      "%-15.15r [%-26.26t] - %d{dd/MM/yy-HH:mm:ss,SSS} - %-5p : %m%n";

  // Init finished
  protected static boolean initFinished = false;

  // Initialisation
  static {
    resetAll();
    initFinished = true;
    SilverTrace.info("silvertrace", "SilverTrace.static", "silvertrace.MSG_END_OF_INIT");
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants...
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   */
  static public void debug(String module, String classe, String message) {
    debug(module, classe, message, null, null);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants... This
   * function have one extra parameter : extraInfos to add additional informations
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  static public void debug(String module, String classe, String message,
      String extraInfos) {
    debug(module, classe, message, extraInfos, null);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants... This
   * function have one extra parameters : ex to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   * @param ex the exception to trace
   */
  static public void debug(String module, String classe, String message,
      Throwable ex) {
    debug(module, classe, message, null, ex);
  }

  /**
   * Trace some debug informations. The programmer is free to display the message he wants... This
   * function have two extra parameters : extraInfos to add additional informations ex to display an
   * exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param message a string that will be displayed in the traces
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  static public void debug(String module, String classe, String message,
      String extraInfos, Throwable ex) {
    if (initFinished) {
      try {
        Category cat = getCategory(module, classe);

        if (cat != null) {
          if (cat.isEnabledFor(Priority.DEBUG)) {
            cat.debug(formatTraceMessage(module, classe, null, message,
                extraInfos), ex);
          }
        }
      } catch (RuntimeException e) {
        SilverTrace.error("silvertrace", "SilverTrace.debug()",
            "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "Msg=" + message, e);
        emergencyTrace(module, classe, message, extraInfos, ex);
      }
    } else {
      emergencyTrace(module, classe, message, extraInfos, ex);
    }
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. To add some extra infos, use the function with the 4th parameter : extraInfos
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  static public void info(String module, String classe, String messageID) {
    info(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameter : extraInfos to add additional informations
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  static public void info(String module, String classe, String messageID,
      String extraInfos) {
    info(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameters : ex to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  static public void info(String module, String classe, String messageID,
      Throwable ex) {
    info(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'info' informations. The message MUST BE one of the predefined in the property
   * files. This function have two extra parameters : extraInfos to add additional informations ex
   * to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  static public void info(String module, String classe, String messageID,
      String extraInfos, Throwable ex) {
    if (initFinished) {
      try {
        Category cat = getCategory(module, classe);

        if (cat != null) {
          if (cat.isEnabledFor(Priority.INFO)) {
            cat.info(formatTraceMessage(module, classe, messageID,
                traceMessages.getMsgString(messageID), extraInfos), ex);
          }
        }
      } catch (RuntimeException e) {
        SilverTrace.error("silvertrace", "SilverTrace.info()",
            "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "MsgId=" + messageID, e);
        emergencyTrace(module, classe, messageID, extraInfos, ex);
      }
    } else {
      emergencyTrace(module, classe, messageID, extraInfos, ex);
    }
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. To add some extra infos, use the function with the 4th parameter : extraInfos
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  static public void warn(String module, String classe, String messageID) {
    warn(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameter : extraInfos to add additional informations
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  static public void warn(String module, String classe, String messageID,
      String extraInfos) {
    warn(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameters : ex to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  static public void warn(String module, String classe, String messageID,
      Throwable ex) {
    warn(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'warning' informations. The message MUST BE one of the predefined in the property
   * files. This function have two extra parameters : extraInfos to add additional informations ex
   * to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  static public void warn(String module, String classe, String messageID,
      String extraInfos, Throwable ex) {
    if (initFinished) {
      try {
        Category cat = getCategory(module, classe);

        if (cat != null) {
          if (cat.isEnabledFor(Priority.WARN)) {
            cat.warn(formatTraceMessage(module, classe, messageID,
                traceMessages.getMsgString(messageID), extraInfos), ex);
          }
        }
      } catch (RuntimeException e) {
        SilverTrace.error("silvertrace", "SilverTrace.warn()",
            "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "MsgId=" + messageID, e);
        emergencyTrace(module, classe, messageID, extraInfos, ex);
      }
    } else {
      emergencyTrace(module, classe, messageID, extraInfos, ex);
    }
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. To add some extra infos, use the function with the 4th parameter : extraInfos
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  static public void error(String module, String classe, String messageID) {
    error(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameter : extraInfos to add additional informations
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  static public void error(String module, String classe, String messageID,
      String extraInfos) {
    error(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. This function have one extra parameters : ex to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  static public void error(String module, String classe, String messageID,
      Throwable ex) {
    error(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'error' informations. The message MUST BE one of the predefined in the property
   * files. This function have two extra parameters : extraInfos to add additional informations ex
   * to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  static public void error(String module, String classe, String messageID,
      String extraInfos, Throwable ex) {
    if (initFinished) {
      try {
        // Normal traces
        Category cat = getCategory(module, classe);

        if (cat != null) {
          if (cat.isEnabledFor(Priority.ERROR)) {
            cat.error(formatTraceMessage(module, classe, messageID,
                traceMessages.getMsgString(messageID), extraInfos), ex);
          }
        }
        // Error and Fatal traces
        cat = getCategory(MODULE_ERROR_AND_FATAL, null);
        if (cat != null) {
          if (cat.isEnabledFor(Priority.ERROR)) {
            cat.error(formatErrorAndFatalMessage(module, classe, messageID,
                extraInfos, ex));
          }
        }
      } catch (RuntimeException e) {
        if (module.equals("silvertrace") == false) {
          SilverTrace.error("silvertrace", "SilverTrace.error()",
              "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "MsgId=" + messageID, e);
        }
        emergencyTrace(module, classe, messageID, extraInfos, ex);
      }
    } else {
      emergencyTrace(module, classe, messageID, extraInfos, ex);
    }
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. To add some extra infos, use the function with the 4th parameter : extraInfos
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   */
  static public void fatal(String module, String classe, String messageID) {
    fatal(module, classe, messageID, null, null);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. This function have one extra parameter : extraInfos to add additional
   * informations
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   */
  static public void fatal(String module, String classe, String messageID,
      String extraInfos) {
    fatal(module, classe, messageID, extraInfos, null);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. This function have one extra parameters : ex to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param ex the exception to trace
   */
  static public void fatal(String module, String classe, String messageID,
      Throwable ex) {
    fatal(module, classe, messageID, null, ex);
  }

  /**
   * Trace some 'fatal error' informations. The message MUST BE one of the predefined in the
   * property files. This function have two extra parameters : extraInfos to add additional
   * informations ex to display an exception
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param messageID the name of the message to display (ex : "root.MSG_GEN_FILE_NOT_FOUND")
   * @param extraInfos some extra-informations that are displayed after the message in parentesis
   * @param ex the exception to trace
   */
  static public void fatal(String module, String classe, String messageID,
      String extraInfos, Throwable ex) {
    if (initFinished) {
      try {
        // Normal traces
        Category cat = getCategory(module, classe);

        if (cat != null) {
          if (cat.isEnabledFor(Priority.FATAL)) {
            cat.fatal(formatTraceMessage(module, classe, messageID,
                traceMessages.getMsgString(messageID), extraInfos), ex);
          }
        }
        // Error and Fatal traces
        cat = getCategory(MODULE_ERROR_AND_FATAL, null);
        if (cat != null) {
          if (cat.isEnabledFor(Priority.FATAL)) {
            cat.fatal(formatErrorAndFatalMessage(module, classe, messageID,
                extraInfos, ex));
          }
        }
      } catch (RuntimeException e) {
        SilverTrace.error("silvertrace", "SilverTrace.fatal()",
            "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "MsgId=" + messageID, e);
        emergencyTrace(module, classe, messageID, extraInfos, ex);
      }
    } else {
      emergencyTrace(module, classe, messageID, extraInfos, ex);
    }
  }

  /**
   * Trace some actions (create, delete, update) done by a user on an object of an instance in a
   * space.
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param classe the short name of the classe that call this function (ex : "MyFavoriteClass")
   * Could be followed by the function name (ex : "MyFavoriteClass.myFunction()")
   * @param spaceId
   * @param instanceId
   * @param objectId the object (ex. publication) which is created, deleted ou updated.
   * @param userId the user who has created, deleted or updated.
   * @param actionId One of the constants SPY_ACTION_CREATE, SPY_ACTION_DELETE, SPY_ACTION_UPDATE.
   */
  static public void spy(String module, String classe, String spaceId,
      String instanceId, String objectId, String userId, String actionId) {
    if (initFinished) {
      try {
        Category cat = getCategory(module, classe);

        // Spy traces
        cat = getCategory(MODULE_SPY, null);
        if (cat != null) {
          if (cat.isEnabledFor(Priority.FATAL)) {
            cat.fatal(formatSpyMessage(spaceId, instanceId, objectId, userId,
                actionId));
          }
        }
      } catch (RuntimeException e) {
        SilverTrace.error("silvertrace", "SilverTrace.spy()",
            "silvertrace.ERR_RUNTIME_ERROR_OCCUR", e);
        emergencyTrace(module, classe, null, null, null);
      }
    } else {
      emergencyTrace(module, classe, null, null, null);
    }
  }

  /**
   * Reset all modules, messages, appenders and all set debug levels.
   */
  static public void resetAll() {
    String pathFiles = null; // Path to the properties files
    String languageMessage = null;
    ArrayList theFiles = null;
    int nbFiles;
    int i;
    InputStream is = null;
    Properties currentFileProperties = null;
    java.util.ResourceBundle resources = null;

    availableModules.clear();
    // Reset all appenders and debug levels
    // Category.getRoot().getHierarchy().resetConfiguration();
    Logger.getRootLogger().getLoggerRepository().resetConfiguration();
    // Category.getRoot().getLoggerRepository().resetConfiguration();
    try {
      Logger.getRootLogger().setAdditivity(true);
      Logger.getRootLogger().setLevel(Level.ERROR);
      resources = FileUtil.loadBundle("com.stratelia.silverpeas.silvertrace.settings.silverTrace",
          new Locale("", ""));
      pathFiles = resources.getString("pathSilverTrace");
      languageMessage = resources.getString("language");
      errorDir = resources.getString("ErrorDir");
      // Load the available messages
      traceMessages.initFromProperties(pathFiles, languageMessage);
      // Get available modules
      theFiles = traceMessages.getPropertyFiles(pathFiles, "");
      nbFiles = theFiles.size();
      for (i = 0; i < nbFiles; i++) {
        try {
          is = new FileInputStream((File) theFiles.get(i));
          currentFileProperties = new Properties();
          currentFileProperties.load(is);
          initFromProperties(currentFileProperties);
        } catch (IOException e) {
          if (initFinished) {
            SilverTrace.error("silvertrace", "SilverTrace.resetAll()",
                "silvertrace.ERR_INIT_TRACE_FROM_PROP", "File:["
                + ((File) theFiles.get(i)).getAbsolutePath() + "]", e);
          } else {
            emergencyTrace(
                "Error in SilverTrace initialization : Cant load property file : '"
                + ((File) theFiles.get(i)).getAbsolutePath() + "'", e);
          }
        }
      }
    } catch (Exception e) {
      if (initFinished) {
        SilverTrace.error("silvertrace", "SilverTrace.resetAll()",
            "silvertrace.ERR_INIT_TRACE", e);
      } else {
        emergencyTrace("Error in SilverTrace resetAll : ", e);
      }
    }
  }

  /**
   * Method declaration
   * @param filePath
   * @see
   */
  static public void applyProperties(String filePath) {
    InputStream is = null;
    Properties currentFileProperties = null;

    try {
      is = new FileInputStream(filePath);
      currentFileProperties = new Properties();
      currentFileProperties.load(is);
      initFromProperties(currentFileProperties);
    } catch (Exception e) {
      if (initFinished) {
        SilverTrace.error("silvertrace", "SilverTrace.resetAll()",
            "silvertrace.ERR_INIT_TRACE_FROM_PROP", "File:[" + filePath + "]",
            e);
      } else {
        emergencyTrace(
            "Error in SilverTrace applyProperties(" + filePath + ")", e);
      }
    }
  }

  /**
   * Loads the configuration from the properties given in argument.
   * @param fileProperties the properties to merge with the current configuration
   */
  static public void initFromProperties(Properties fileProperties) {
    int i;
    String appenderTypeStr;
    int appenderTypeInt;
    boolean appenderEnabled;
    String traceLevel;
    int traceLevelInt;
    Enumeration enumModulesNames;
    String enumModulesName;
    String enumModulesPath;

    try {
      // First, append module if not already exist
      // ----------------------------------------
      enumModulesName = fileProperties.getProperty("module.name");
      enumModulesPath = fileProperties.getProperty("module.path");
      // Set the first lettre of the module name to upper case
      if (MsgTrace.stringValid(enumModulesName)
          && MsgTrace.stringValid(enumModulesPath)) {
        i = enumModulesPath.lastIndexOf('.');
        if ((i > 0) && ((i + 1) < enumModulesPath.length())) {
          StringBuilder ws = new StringBuilder(enumModulesPath);

          ws.setCharAt(i + 1, enumModulesPath.toUpperCase().charAt(i + 1));
          enumModulesPath = ws.toString();
        }
        availableModules.setProperty(enumModulesName, enumModulesPath);
      }
      // Secound, read the appenders
      // --------------------------
      i = 0;
      appenderTypeStr = fileProperties.getProperty("appender"
          + Integer.toString(i) + ".type");
      // for each appenders
      while (appenderTypeStr != null) {
        // Translate the appender type from the string to the internal constant
        if (appenderTypeStr.equalsIgnoreCase("APPENDER_CONSOLE")) {
          appenderTypeInt = APPENDER_CONSOLE;
        } else if (appenderTypeStr.equalsIgnoreCase("APPENDER_FILE")) {
          appenderTypeInt = APPENDER_FILE;
        } else if (appenderTypeStr.equalsIgnoreCase("APPENDER_ROLLING_FILE")) {
          appenderTypeInt = APPENDER_ROLLING_FILE;
        } else if (appenderTypeStr.equalsIgnoreCase("APPENDER_MAIL")) {
          appenderTypeInt = APPENDER_MAIL;
        } else {
          appenderTypeInt = APPENDER_ALL;
        }
        appenderEnabled = MsgTrace.getBooleanProperty(fileProperties,
            "appender" + Integer.toString(i) + ".enabled", true);
        if ((appenderTypeInt != APPENDER_ALL) && appenderEnabled) {
          // Create the appender and attach it to his module
          addAppenderFromProperties(fileProperties, i, appenderTypeInt);
        }
        i++;
        appenderTypeStr = fileProperties.getProperty("appender"
            + Integer.toString(i) + ".type");
      }

      // Third, enumerate all modules and look if there are special trace levels
      // on them
      // -------------------------------------------------------------------------------
      enumModulesNames = availableModules.propertyNames();
      while (enumModulesNames.hasMoreElements()) {
        enumModulesName = enumModulesNames.nextElement().toString();
        traceLevel = fileProperties
            .getProperty("traceLevel." + enumModulesName);
        if (traceLevel != null) {
          if (traceLevel.equalsIgnoreCase("TRACE_LEVEL_DEBUG")) {
            traceLevelInt = TRACE_LEVEL_DEBUG;
          } else if (traceLevel.equalsIgnoreCase("TRACE_LEVEL_INFO")) {
            traceLevelInt = TRACE_LEVEL_INFO;
          } else if (traceLevel.equalsIgnoreCase("TRACE_LEVEL_WARN")) {
            traceLevelInt = TRACE_LEVEL_WARN;
          } else if (traceLevel.equalsIgnoreCase("TRACE_LEVEL_ERROR")) {
            traceLevelInt = TRACE_LEVEL_ERROR;
          } else if (traceLevel.equalsIgnoreCase("TRACE_LEVEL_FATAL")) {
            traceLevelInt = TRACE_LEVEL_FATAL;
          } else {
            traceLevelInt = TRACE_LEVEL_UNKNOWN;
          }
          setTraceLevel(enumModulesName, traceLevelInt);
        }
      }
    } catch (Exception e) {
      if (initFinished) {
        SilverTrace.error("silvertrace", "SilverTrace.resetAll()",
            "silvertrace.ERR_INIT_APPENDER_FROM_PROP", e);
      } else {
        emergencyTrace("Error in SilverTrace initFromProperties", e);
      }
    }
  }

  /**
   * Set the minimum trace level of a module. All traces less than val will not be taken into
   * account
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param val the trace level : could be one of the TRACE_LEVEL_... values. Use
   * TRACE_LEVEL_UNKNOWN to remove the level condition for the module.
   */
  static public void setTraceLevel(String module, int val) {
    Category cat = getCategory(module, null);

    if (cat != null) {
      switch (val) {
        case TRACE_LEVEL_UNKNOWN:
          cat.setLevel(null);
          break;
        case TRACE_LEVEL_DEBUG:
          cat.setLevel(Level.DEBUG);
          break;
        case TRACE_LEVEL_INFO:
          cat.setLevel(Level.INFO);
          break;
        case TRACE_LEVEL_WARN:
          cat.setLevel(Level.WARN);
          break;
        case TRACE_LEVEL_ERROR:
          cat.setLevel(Level.ERROR);
          break;
        case TRACE_LEVEL_FATAL:
          cat.setLevel(Level.FATAL);
          break;
      }
    }
  }

  /**
   * Get the trace level of a module. Depending on the value of chained, it could ask for the
   * inherited trace levels or not.
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param chained if false : return a trace level only if the module have been set with one. Else,
   * return TRACE_LEVEL_UNKNOWN. If true, look for the inherited trace level.
   * @return the trace level of the module or TRACE_LEVEL_UNKNOWN if the module was not found
   */
  static public int getTraceLevel(String module, boolean chained) {
    Category cat = getCategory(module, null);
    int log4jLevelInt;
    Level log4jLevel;

    if (cat == null) {
      return (TRACE_LEVEL_UNKNOWN);
    }
    if (chained) {
      // log4jPriority = cat.getChainedPriority();
      log4jLevel = cat.getEffectiveLevel();
    } else {
      // log4jPriority = cat.getPriority();
      log4jLevel = cat.getLevel();
    }

    if (log4jLevel == null) {
      return (TRACE_LEVEL_UNKNOWN);
    }
    // log4jPriorityInt = log4jPriority.toInt();
    log4jLevelInt = log4jLevel.toInt();

    switch (log4jLevelInt) {
      case Level.DEBUG_INT:
        return (TRACE_LEVEL_DEBUG);
      case Level.INFO_INT:
        return (TRACE_LEVEL_INFO);
      case Level.WARN_INT:
        return (TRACE_LEVEL_WARN);
      case Level.ERROR_INT:
        return (TRACE_LEVEL_ERROR);
      case Level.FATAL_INT:
        return (TRACE_LEVEL_FATAL);
      default:
        return (TRACE_LEVEL_UNKNOWN);
    }
  }

  /**
   * Add a new console appender to the module. If an appender with the same type have been
   * previously set, delete it and replace it with the new created one.
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param consoleName Name of the console output. If null or "", "system.out" is used
   */
  static public void addAppenderConsole(String module, String patternLayout,
      String consoleName) {
    Category cat = getCategory(module, null);
    ConsoleAppender a1 = new ConsoleAppender();

    if (cat != null) {
      try {
        cat.removeAppender(getAppenderName(module, APPENDER_CONSOLE));
        a1.setName(getAppenderName(module, APPENDER_CONSOLE));
        a1.setLayout(getLayout(patternLayout));
        if (MsgTrace.stringValid(consoleName)) {
          a1.setTarget(consoleName);
        }
        a1.activateOptions();
        cat.addAppender(a1);
      } catch (Exception e) {
        if (initFinished) {
          SilverTrace.error("silvertrace", "SilverTrace.addAppenderConsole()",
              "silvertrace.ERR_CANT_ADD_APPENDER", "Console " + module + ","
              + patternLayout + "," + consoleName, e);
        } else {
          emergencyTrace("Error in SilverTrace addAppenderConsole", e);
        }
      }
    }
  }

  /**
   * Add a new file appender to the module. If an appender with the same type have been previously
   * set, delete it and replace it with the new created one.
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param fileName full-path name of the file where the trace are written
   * @param appendOnFile true to append at the end of the existing file (if ther is one), false to
   * remove old file before writting
   */
  static public void addAppenderFile(String module, String patternLayout,
      String fileName, boolean appendOnFile) {
    Category cat = getCategory(module, null);
    FileAppender a1 = new FileAppender();

    if (cat != null) {
      try {
        cat.removeAppender(getAppenderName(module, APPENDER_FILE));
        a1.setName(getAppenderName(module, APPENDER_FILE));
        a1.setLayout(getLayout(patternLayout));
        a1.setAppend(appendOnFile);
        a1.setFile(fileName);
        a1.activateOptions();
        cat.addAppender(a1);
      } catch (Exception e) {
        if (initFinished) {
          SilverTrace.error("silvertrace", "SilverTrace.addAppenderFile()",
              "silvertrace.ERR_CANT_ADD_APPENDER", "File " + module + ","
              + patternLayout + "," + fileName, e);
        } else {
          emergencyTrace("Error in SilverTrace addAppenderFile", e);
        }
      }
    }
  }

  /**
   * Add a new rolling file appender to the module. If an appender with the same type have been
   * previously set, delete it and replace it with the new created one.
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param fileName full-path name of the file where the trace are written
   * @param rollingMode frequency of the rolling file, could be one of the ROLLING_MODE_...
   * constants
   */
  static public void addAppenderRollingFile(String module,
      String patternLayout, String fileName, String rollingMode) {
    Category cat = getCategory(module, null);

    if (cat != null) {
      try {
        DailyRollingFileAppender a1 = new DailyRollingFileAppender(
            getLayout(patternLayout), fileName, rollingMode);
        if (MODULE_ROOT.equals(module)) {
          cat = Logger.getRootLogger();
        }
        cat.removeAppender(getAppenderName(module, APPENDER_ROLLING_FILE));
        a1.setName(getAppenderName(module, APPENDER_ROLLING_FILE));
        cat.addAppender(a1);
      } catch (Exception e) {
        if (initFinished) {
          SilverTrace.error("silvertrace",
              "SilverTrace.addAppenderRollingFile()",
              "silvertrace.ERR_CANT_ADD_APPENDER", "RollingFile " + module
              + "," + patternLayout + "," + fileName, e);
        } else {
          emergencyTrace("Error in SilverTrace addAppenderRollingFile", e);
        }
      }
    }
  }

  /**
   * Add a new mail appender to the module. If an appender with the same type have been previously
   * set, delete it and replace it with the new created one. How it works : mails are only sent when
   * an ERROR or FATAL occur. The mail contains the error and the 512 last traces taken into account
   * (ie, higher than the trace level).
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param patternLayout the things displayed in this appender, could be one of the LAYOUT_...
   * constants
   * @param mailHost host name
   * @param mailFrom email of the sender
   * @param mailTo target email, could be multiple targets separeted with comas
   * @param mailSubject subject of the mail
   */
  static public void addAppenderMail(String module, String patternLayout,
      String mailHost, String mailFrom, String mailTo, String mailSubject) {
    Category cat = getCategory(module, null);
    SMTPAppender a1 = new SMTPAppender();

    if (cat != null) {
      try {
        cat.removeAppender(getAppenderName(module, APPENDER_MAIL));
        a1.setName(getAppenderName(module, APPENDER_MAIL));
        a1.setLayout(getLayout(patternLayout));
        a1.setSMTPHost(mailHost);
        a1.setFrom(mailFrom);
        a1.setTo(mailTo);
        a1.setSubject(mailSubject);
        a1.activateOptions();
        cat.addAppender(a1);
      } catch (Exception e) {
        if (initFinished) {
          SilverTrace.error("silvertrace", "SilverTrace.addAppenderMail()",
              "silvertrace.ERR_CANT_ADD_APPENDER", "SMTP " + module + ","
              + patternLayout + "," + mailHost, e);
        } else {
          emergencyTrace("Error in SilverTrace addAppenderMail", e);
        }
      }
    }
  }

  /**
   * Remove appender(s) attached to a module. typeOfAppender could be one value or a mask of
   * multiple appender types
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param typeOfAppender could be a mask of APPENDER_... values or APPENDER_ALL to remove all
   * appenders attached to the module
   */
  static public void removeAppender(String module, int typeOfAppender) {
    Category cat = getCategory(module, null);

    if (cat != null) {
      if ((typeOfAppender & APPENDER_CONSOLE) == APPENDER_CONSOLE) {
        cat.removeAppender(getAppenderName(module, APPENDER_CONSOLE));
      }
      if ((typeOfAppender & APPENDER_FILE) == APPENDER_FILE) {
        cat.removeAppender(getAppenderName(module, APPENDER_FILE));
      }
      if ((typeOfAppender & APPENDER_ROLLING_FILE) == APPENDER_ROLLING_FILE) {
        cat.removeAppender(getAppenderName(module, APPENDER_ROLLING_FILE));
      }
      if ((typeOfAppender & APPENDER_MAIL) == APPENDER_MAIL) {
        cat.removeAppender(getAppenderName(module, APPENDER_MAIL));
      }
    }
  }

  /**
   * The purpose of this function is just to return the list of available modules to the JSP -
   * Exploitation page This function is subject to change and SHOULD NOT BE USED by any other page
   * or java class...
   * @return The list of the modules with pairs (<module name>,<log4j-path to this module>)
   */
  static public Properties getModuleList() {
    return availableModules;
  }

  /**
   * The purpose of this function is just to return the available appenders for the JSP -
   * Exploitation page This function is subject to change and SHOULD NOT BE USED by any other page
   * or java class...
   * @param module the module name (ex : root, bus, outlook, ...)
   * @return a mask of the appenders set to this module (not containing the herited ones)
   */
  static public int getAvailableAppenders(String module) {
    Category cat = getCategory(module, null);
    int valret = 0;

    if (cat != null) {
      if (cat.getAppender(getAppenderName(module, APPENDER_CONSOLE)) != null) {
        valret = valret | APPENDER_CONSOLE;
      }
      if (cat.getAppender(getAppenderName(module, APPENDER_FILE)) != null) {
        valret = valret | APPENDER_FILE;
      }
      if (cat.getAppender(getAppenderName(module, APPENDER_ROLLING_FILE)) != null) {
        valret = valret | APPENDER_ROLLING_FILE;
      }
      if (cat.getAppender(getAppenderName(module, APPENDER_MAIL)) != null) {
        valret = valret | APPENDER_MAIL;
      }
    }
    return valret;
  }

  /**
   * The purpose of this function is just to return informations about an appender for the JSP -
   * Exploitation page This function is subject to change and SHOULD NOT BE USED by any other page
   * or java class...
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param typeOfAppender the type of appender : one of the APPENDER_... constants
   * @return A set of properties discribing the attached appender or null if there is no such
   * appender attached to this module
   */
  static public Properties getAppender(String module, int typeOfAppender) {
    Category cat = getCategory(module, null);

    if (cat == null) {
      return (null);
    }
    Appender app = cat.getAppender(getAppenderName(module, typeOfAppender));

    if (app == null) {
      return (null);
    }
    Layout lay = app.getLayout();
    PatternLayout layPattern;
    Properties valret = new Properties();

    valret.setProperty("Name", app.getName());
    valret.setProperty("Type", Integer.toString(typeOfAppender));

    if (lay != null) {
      if (lay.getClass().getName().equals("org.apache.log4j.HTMLLayout")) {
        valret.setProperty("Layout", LAYOUT_HTML);
      }
      if (lay.getClass().getName().equals("org.apache.log4j.PatternLayout")) {
        layPattern = (PatternLayout) lay;
        if (layPattern.getConversionPattern().equals(layoutShort)) {
          valret.setProperty("Layout", LAYOUT_SHORT);
        }
        if (layPattern.getConversionPattern().equals(layoutDetailed)) {
          valret.setProperty("Layout", LAYOUT_DETAILED);
        }
        if (layPattern.getConversionPattern().equals(layoutFullDebug)) {
          valret.setProperty("Layout", LAYOUT_FULL_DEBUG);
        }
      }
    }

    if ((typeOfAppender & APPENDER_CONSOLE) == APPENDER_CONSOLE) {
      ConsoleAppender capp = (ConsoleAppender) app;

      valret.setProperty("consoleName", capp.getTarget());
    }
    if ((typeOfAppender & APPENDER_FILE) == APPENDER_FILE) {
      FileAppender fapp = (FileAppender) app;

      valret.setProperty("fileName", fapp.getFile());
      if (fapp.getAppend()) {
        valret.setProperty("append", "true");
      } else {
        valret.setProperty("append", "false");
      }
    }
    if ((typeOfAppender & APPENDER_ROLLING_FILE) == APPENDER_ROLLING_FILE) {
      DailyRollingFileAppender rapp = (DailyRollingFileAppender) app;

      valret.setProperty("fileName", rapp.getFile());
      valret.setProperty("rollingMode", rapp.getDatePattern());
    }
    if ((typeOfAppender & APPENDER_MAIL) == APPENDER_MAIL) {
      SMTPAppender sapp = (SMTPAppender) app;

      valret.setProperty("mailHost", sapp.getSMTPHost());
      valret.setProperty("mailFrom", sapp.getFrom());
      valret.setProperty("mailTo", sapp.getTo());
      valret.setProperty("mailSubject", sapp.getSubject());
    }
    return valret;
  }

  /**
   * Returns the message corresponding to the MessageId in the SilverTrace default language
   * @param messageId the message ID (ex. 'admin.MSG_ERR_GENERAL')
   * @return the message if the SilverTrace default language
   */
  static public String getTraceMessage(String messageId) {
    try {
      return traceMessages.getMsgString(messageId);
    } catch (RuntimeException ex) {
      SilverTrace.error("silvertrace", "SilverTrace.getTraceMessage()",
          "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "MsgId=" + messageId, ex);
      return "!!! Messages " + messageId + " NOT FOUND !!!";
    }
  }

  static public String[] getEndFileTrace(String nbLines) {
    int nbl = -1;
    LineNumberReader lnr;
    File theFile = new File(errorDir + "/traces.txt");
    String line;
    ArrayList ar = new ArrayList();

    try {
      // Get file length
      long fileLength = theFile.length();

      if (fileLength == 0) {
        return new String[0];
      }
      try {
        nbl = Integer.parseInt(nbLines);
      } catch (Exception e) {
        nbl = -1;
      }
      lnr = new LineNumberReader(new FileReader(theFile));
      if (nbl > 0) {
        if ((nbl + 1) * 100 < fileLength) {
          lnr.skip(fileLength - ((nbl + 1) * 100));
        }
      }
      line = lnr.readLine();
      while (line != null) {
        line = lnr.readLine();
        if (line != null) {
          ar.add(line);
        }
      }
      return (String[]) ar.toArray(new String[0]);
    } catch (Exception e) {
      SilverTrace.error("silvertrace", "SilverTrace.getEndFileTrace()",
          "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "File NOT FOUND :" + errorDir
          + "/traces.txt", e);
      return new String[0];
    }
  }

  /**
   * Returns the language-dependant message corresponding to the MessageId
   * @param messageId the message ID (ex. 'admin.MSG_ERR_GENERAL')
   * @param language the language to display the message in
   * @return the message if the specified language
   */
  static public String getTraceMessage(String messageId, String language) {
    try {
      return traceMessages.getMsgString(messageId, language);
    } catch (RuntimeException ex) {
      SilverTrace.error("silvertrace", "SilverTrace.getTraceMessage()",
          "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "MsgId=" + messageId
          + " Lang=" + language, ex);
      return "!!! Messages " + messageId + " FOR " + language
          + " NOT FOUND !!!";
    }
  }

  /**
   * Method declaration
   * @param spaceId
   * @param instanceId
   * @param objectId
   * @param userId
   * @param actionId
   * @return
   * @see
   */
  static protected String formatSpyMessage(String spaceId, String instanceId,
      String objectId, String userId, String actionId) {
    StringBuffer valret = new StringBuffer("");

    if (MsgTrace.stringValid(spaceId)) {
      valret.append(spaceId);
      valret.append(",");
    }
    if (MsgTrace.stringValid(instanceId)) {
      valret.append(instanceId);
      valret.append(",");
    }
    if (MsgTrace.stringValid(objectId)) {
      valret.append(objectId);
      valret.append(",");
    }
    if (MsgTrace.stringValid(userId)) {
      valret.append(userId);
      valret.append(",");
    }
    if (MsgTrace.stringValid(actionId)) {
      valret.append(actionId);
    }

    return valret.toString();
  }

  /**
   * Format the trace message for the Error and Fatal specific case
   * @param module
   * @param classe
   * @param messageID
   * @param extraInfos
   * @param ex
   * @return the built message
   */
  static protected String formatErrorAndFatalMessage(String module,
      String classe, String messageID, String extraInfos, Throwable ex) {
    String extraParams;

    if (ex != null) {
      if (MsgTrace.stringValid(extraInfos)) {
        extraParams = extraInfos + ", EXCEPTION : " + ex.toString();
      } else {
        extraParams = "EXCEPTION : " + ex.toString();
      }
    } else {
      extraParams = extraInfos;
    }
    return formatTraceMessage(module, classe, messageID, traceMessages
        .getMsgString(messageID), extraParams);
  }

  /**
   * Format the trace message to send to log4j
   * @param module
   * @param classe
   * @param messageID
   * @param message
   * @param extraInfos
   * @return the built message
   */
  static protected String formatTraceMessage(String module, String classe,
      String messageID, String message, String extraInfos) {
    StringBuffer valret = new StringBuffer("");

    if (MsgTrace.stringValid(messageID)) {
      valret.append(messageID + " | ");
    }
    if (MsgTrace.stringValid(classe)) {
      valret.append("MODULE : " + module + "." + classe + " | ");
    } else {
      valret.append("MODULE : " + module + " | ");
    }
    if (MsgTrace.stringValid(message)) {
      valret.append(message);
    }

    if (MsgTrace.stringValid(extraInfos)) {
      valret.append(" (" + extraInfos + ")");
    }
    return valret.toString();
  }

  /**
   * Translate the @ErrorDir@ into the real value
   * @param fileName
   */
  static protected String translateFileName(String fileName) {
    String valret = fileName;
    int index;

    if (MsgTrace.stringValid(fileName)) {
      index = fileName.indexOf("@ErrorDir@");
      if (index == 0) {
        valret = errorDir + fileName.substring(index + 10, fileName.length());
      } else if (index > 0) {
        valret = fileName.substring(0, index) + errorDir
            + fileName.substring(index + 10, fileName.length());
      }
    }
    return valret;
  }

  /**
   * Read appender information from a property file and attach it to it's module
   * @param fileProperties
   * @param appenderNumber
   * @param appenderType
   */
  static protected void addAppenderFromProperties(Properties fileProperties,
      int appenderNumber, int appenderType) {
    String module;
    String layout;
    String fileName;
    String consoleName;
    boolean append;
    String rollingModeName;
    String rollingMode;
    String mailHost;
    String mailFrom;
    String mailTo;
    String mailSubject;

    // Retrieve the properties of the current appender and call the function
    // that will create and attach it
    //
    module = fileProperties.getProperty("appender"
        + Integer.toString(appenderNumber) + ".module");
    if (module == null) {
      module = MODULE_ROOT;
    }
    layout = fileProperties.getProperty("appender"
        + Integer.toString(appenderNumber) + ".layout");
    if (layout == null) {
      layout = LAYOUT_SHORT;
    }
    switch (appenderType) {
      case APPENDER_CONSOLE:
        consoleName = fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".consoleName");
        addAppenderConsole(module, layout, consoleName);
        break;
      case APPENDER_FILE:
        fileName = translateFileName(fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".fileName"));
        append = MsgTrace.getBooleanProperty(fileProperties, "appender"
            + Integer.toString(appenderNumber) + ".append", true);
        addAppenderFile(module, layout, fileName, append);
        break;
      case APPENDER_ROLLING_FILE:
        fileName = translateFileName(fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".fileName"));
        rollingModeName = fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".rollingMode");
        if (rollingModeName == null) {
          rollingMode = ROLLING_MODE_DAILY;
        } else if (rollingModeName.equalsIgnoreCase("ROLLING_MODE_MOUNTH")) {
          rollingMode = ROLLING_MODE_MOUNTH;
        } else if (rollingModeName.equalsIgnoreCase("ROLLING_MODE_WEEK")) {
          rollingMode = ROLLING_MODE_WEEK;
        } else if (rollingModeName.equalsIgnoreCase("ROLLING_MODE_DAILY")) {
          rollingMode = ROLLING_MODE_DAILY;
        } else if (rollingModeName.equalsIgnoreCase("ROLLING_MODE_HOUR")) {
          rollingMode = ROLLING_MODE_HOUR;
        } else if (rollingModeName.length() == 0) {
          rollingMode = ROLLING_MODE_DAILY;
        } else {
          rollingMode = rollingModeName;
        }
        addAppenderRollingFile(module, layout, fileName, rollingMode);
        break;
      case APPENDER_MAIL:
        mailHost = fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".mailHost");
        mailFrom = fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".mailFrom");
        mailTo = fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".mailTo");
        mailSubject = fileProperties.getProperty("appender"
            + Integer.toString(appenderNumber) + ".mailSubject");
        addAppenderMail(module, layout, mailHost, mailFrom, mailTo, mailSubject);
        break;
    }
  }

  /**
   * Return the layout object depending on it's name
   * @param patternLayout
   * @return
   */
  static protected Layout getLayout(String patternLayout) {
    if (patternLayout.equalsIgnoreCase(LAYOUT_HTML)) {
      return new HTMLLayout();
    } else if (patternLayout.equalsIgnoreCase(LAYOUT_SHORT)) {
      return new PatternLayout(layoutShort);
    } else if (patternLayout.equalsIgnoreCase(LAYOUT_DETAILED)) {
      return new PatternLayout(layoutDetailed);
    } else if (patternLayout.equalsIgnoreCase(LAYOUT_FULL_DEBUG)) {
      return new PatternLayout(layoutFullDebug);
    } else // Custom pattern layout
    {
      return new PatternLayout(patternLayout);
    }
  }

  /**
   * Return the category associated to a module or to a module's class
   * @param module
   * @param classToAppend
   * @return
   */
  static protected Category getCategory(String module, String classToAppend) {
    if ("root".equalsIgnoreCase(module)) {
      return Logger.getRootLogger();
    }
    String modulePath = availableModules.getProperty(module);

    if (modulePath == null) {
      return null;
    } else {
      return Logger.getLogger(modulePath);
    }
  }

  /**
   * Return the name of the appender depending on it's attached module and type
   * @param module
   * @param typeOfAppender
   * @return
   */
  static protected String getAppenderName(String module, int typeOfAppender) {
    if ((typeOfAppender & APPENDER_CONSOLE) == APPENDER_CONSOLE) {
      return (module + ".ConsoleAppender");
    }
    if ((typeOfAppender & APPENDER_FILE) == APPENDER_FILE) {
      return (module + ".FileAppender");
    }
    if ((typeOfAppender & APPENDER_ROLLING_FILE) == APPENDER_ROLLING_FILE) {
      return (module + ".DailyRollingFileAppender");
    }
    if ((typeOfAppender & APPENDER_MAIL) == APPENDER_MAIL) {
      return (module + ".SMTPAppender");
    }
    return null;
  }

  /**
   * Method declaration
   * @param msgToTrace
   * @param ex
   * @see
   */
  static protected void emergencyTrace(String msgToTrace, Throwable ex) {
    StringBuffer sb = new StringBuffer(msgToTrace);

    if (ex != null) {
      sb.append("| Ex : " + ex.getMessage());
    }
    System.err.println(sb.toString());
    if (ex != null) {
      ex.printStackTrace();
    }
  }

  /**
   * Method declaration
   * @param module
   * @param classe
   * @param msgToTrace
   * @param extraInfos
   * @param ex
   * @see
   */
  static protected void emergencyTrace(String module, String classe,
      String msgToTrace, String extraInfos, Throwable ex) {
    StringBuffer sb = new StringBuffer(
        "SilverTrace can't display normaly the message : ");

    if (module != null) {
      sb.append(" Module : " + module);
    }
    if (classe != null) {
      sb.append("| Classe : " + classe);
    }
    if (msgToTrace != null) {
      sb.append("| Msg : " + msgToTrace);
    }
    if (extraInfos != null) {
      sb.append(" (" + extraInfos + ")");
    }
    emergencyTrace(sb.toString(), ex);
  }

}
