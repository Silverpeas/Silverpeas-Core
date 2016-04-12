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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * SilverpeasTrace is the trace tool used in silverpeas to trace debug, running infos and errors. This
 * is a 'fully' static class. All functions could be called directly and is thread-safe. The trace
 * functions are : debug, info, warn, error, fatal.
 *
 * This class is deprecated now and it uses the Silverpeas Logging API. Please use directly the
 * latter as the Silver Trace API will be removed in the future.
 * @see org.silverpeas.core.util.logging.SilverLogger
 *
 * @author Thierry leroi
 * @deprecated
 */
@Deprecated
@Singleton
public class DefaultSilverpeasTrace implements SilverpeasTrace {

  // Directory to the error files
  protected static String errorDir = null;

  @Deprecated
  @Override
  public void debug(String module, String classe, String message) {
    debug(module, classe, message, null, null);
  }

  @Deprecated
  @Override
  public void debug(String module, String classe, String message, String extraInfos) {
    debug(module, classe, message, extraInfos, null);
  }

  @Deprecated
  @Override
  public void debug(String module, String classe, String message, Throwable ex) {
    debug(module, classe, message, null, ex);
  }

  @Deprecated
  @Override
  public void debug(String module, String classe, String message, String extraInfos, Throwable ex) {
    SilverLogger.getLogger(computeNamespace(module)).debug(formatMessage(message, extraInfos));
  }

  @Deprecated
  @Override
  public void info(String module, String classe, String messageID) {
    info(module, classe, messageID, null, null);
  }

  @Deprecated
  @Override
  public void info(String module, String classe, String messageID, String extraInfos) {
    info(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  @Override
  public void info(String module, String classe, String messageID, Throwable ex) {
    info(module, classe, messageID, null, ex);
  }

  @Deprecated
  @Override
  public void info(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    SilverLogger.getLogger(computeNamespace(module)).info(formatMessage(messageID, extraInfos));
  }

  @Deprecated
  @Override
  public void warn(String module, String classe, String messageID) {
    warn(module, classe, messageID, null, null);
  }

  @Deprecated
  @Override
  public void warn(String module, String classe, String messageID, String extraInfos) {
    warn(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  @Override
  public void warn(String module, String classe, String messageID, Throwable ex) {
    warn(module, classe, messageID, null, ex);
  }

  @Deprecated
  @Override
  public void warn(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    SilverLogger.getLogger(computeNamespace(module)).warn(formatMessage(messageID, extraInfos));
  }

  @Deprecated
  @Override
  public void error(String module, String classe, String messageID) {
    error(module, classe, messageID, null, null);
  }

  @Deprecated
  @Override
  public void error(String module, String classe, String messageID, String extraInfos) {
    error(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  @Override
  public void error(String module, String classe, String messageID, Throwable ex) {
    error(module, classe, messageID, null, ex);
  }

  @Deprecated
  @Override
  public void error(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    SilverLogger.getLogger(computeNamespace(module)).error(formatMessage(messageID, extraInfos), ex);
  }

  @Deprecated
  @Override
  public void fatal(String module, String classe, String messageID) {
    fatal(module, classe, messageID, null, null);
  }

  @Deprecated
  @Override
  public void fatal(String module, String classe, String messageID, String extraInfos) {
    fatal(module, classe, messageID, extraInfos, null);
  }

  @Deprecated
  @Override
  public void fatal(String module, String classe, String messageID, Throwable ex) {
    fatal(module, classe, messageID, null, ex);
  }

  @Deprecated
  @Override
  public void fatal(String module, String classe, String messageID, String extraInfos,
      Throwable ex) {
    SilverLogger.getLogger(computeNamespace(module)).error(formatMessage(messageID, extraInfos), ex);
  }

  @Deprecated
  @Override
  public void spy(String module, String classe, String spaceId, String instanceId, String objectId,
      String userId, String actionId) {
    SilverLogger.getLogger(computeNamespace(module))
        .debug(formatSpyMessage(spaceId, instanceId, objectId, userId, actionId));
  }

  /**
   * Does nothing.
   */
  @Deprecated
  @Override
  public void resetAll() {
  }

  /**
   * Does nothing
   */
  @Deprecated
  @Override
  public void applyProperties(String filePath) {

  }

  /**
   * Does nothing
   */
  @Deprecated
  @Override
  public void initFromProperties(Properties fileProperties) {

  }

  /**
   * Set the minimum trace level of a module. All traces less than val will not be taken into
   * account
   *
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param val the trace level : could be one of the TRACE_LEVEL_... values. Use
   * TRACE_LEVEL_UNKNOWN to remove the level condition for the module.
   * @deprecated
   */
  @Deprecated
  @Override
  public void setTraceLevel(String module, int val) {
    SilverLogger logger = SilverLogger.getLogger(module);

    if (logger != null) {
      switch (val) {
        case SilverTrace.TRACE_LEVEL_UNKNOWN:
          logger.setLevel(null);
          break;
        case SilverTrace.TRACE_LEVEL_DEBUG:
          logger.setLevel(Level.DEBUG);
          break;
        case SilverTrace.TRACE_LEVEL_INFO:
          logger.setLevel(Level.INFO);
          break;
        case SilverTrace.TRACE_LEVEL_WARN:
          logger.setLevel(Level.WARNING);
          break;
        case SilverTrace.TRACE_LEVEL_ERROR:
          logger.setLevel(Level.ERROR);
          break;
        case SilverTrace.TRACE_LEVEL_FATAL:
          logger.setLevel(Level.ERROR);
          break;
      }
    }
  }

  /**
   * Get the trace level of a module.
   *
   * @param module the module name (ex : root, bus, outlook, ...)
   * @param chained it is not more taken into account.
   * @return the trace level value (as defined by {@code org.silverpeas.core.util.logging.Level})
   * of the module or 0 if it can be figured out.
   * @see org.silverpeas.core.util.logging.Level
   * @deprecated
   */
  @Deprecated
  @Override
  public int getTraceLevel(String module, boolean chained) {
    Level level = SilverLogger.getLogger(module).getLevel();
    if (level == null) {
      return 0;
    }
    return level.value();
  }

  /**
   * Does nothing.
   */
  @Deprecated
  @Override
  public void addAppenderConsole(String module, String patternLayout, String consoleName) {

  }

  /**
   * Does nothing.
   */
  @Deprecated
  @Override
  public void addAppenderFile(String module, String patternLayout, String fileName,
      boolean appendOnFile) {
  }

  /**
   * Does nothing.
   */
  @Deprecated
  @Override
  public void addAppenderRollingFile(String module, String patternLayout, String fileName,
      String rollingMode) {
  }

  /**
   * Does nothing.
   */
  @Deprecated
  @Override
  public void addAppenderMail(String module, String patternLayout, String mailHost, String mailFrom,
      String mailTo, String mailSubject) {

  }

  /**
   * Does nothing.
   */
  @Deprecated
  @Override
  public void removeAppender(String module, int typeOfAppender) {
  }

  /**
   * Returns an empty Properties object.
   */
  @Deprecated
  @Override
  public Properties getModuleList() {
    return new Properties();
  }

  /**
   * Returns 0.
   */
  @Deprecated
  @Override
  public int getAvailableAppenders(String module) {
    return 0;
  }

  /**
   * Returns an empty Properties object.
   */
  @Deprecated
  @Override
  public Properties getAppender(String module, int typeOfAppender) {
    return new Properties();
  }

  /**
   * Returns the message id itself.
   *
   * @param messageId the message ID (ex. 'admin.MSG_ERR_GENERAL')
   * @return the message id itself.
   */
  @Deprecated
  @Override
  public String getTraceMessage(String messageId) {
    return messageId;
  }

  @Deprecated
  @Override
  public String[] getEndFileTrace(String nbLines) {
    LineNumberReader lnr = null;
    File theFile = new File(errorDir + "/traces.txt");
    List<String> ar = new ArrayList<String>();
    try {
      // Get file length
      long fileLength = theFile.length();
      if (fileLength == 0L) {
        return ArrayUtils.EMPTY_STRING_ARRAY;
      }
      int nbl = Integer.parseInt(nbLines);
      lnr = new LineNumberReader(new FileReader(theFile));
      if (nbl > 0) {
        if ((nbl + 1) * 100 < fileLength) {
          lnr.skip(fileLength - ((nbl + 1) * 100));
        }
      }
      String line = lnr.readLine();
      while (line != null) {
        line = lnr.readLine();
        if (line != null) {
          ar.add(line);
        }
      }
      return ar.toArray(new String[ar.size()]);
    } catch (Exception e) {
      error("silvertrace", "SilverTrace.getEndFileTrace()",
          "silvertrace.ERR_RUNTIME_ERROR_OCCUR", "File NOT FOUND :" + errorDir + "/traces.txt", e);
      return ArrayUtils.EMPTY_STRING_ARRAY;
    } finally {
      IOUtils.closeQuietly(lnr);
    }
  }

  /**
   * Returns the message id itself.
   *
   * @param messageId the message ID (ex. 'admin.MSG_ERR_GENERAL')
   * @param language the language to display the message in
   * @return the message id itself.
   */
  @Deprecated
  @Override
  public String getTraceMessage(String messageId, String language) {
    return messageId;
  }

  protected static String formatSpyMessage(String spaceId, String instanceId,
      String objectId, String userId, String actionId) {
    StringBuilder valret = new StringBuilder("");

    if (StringUtil.isDefined(spaceId)) {
      valret.append(spaceId);
      valret.append(",");
    }
    if (StringUtil.isDefined(instanceId)) {
      valret.append(instanceId);
      valret.append(",");
    }
    if (StringUtil.isDefined(objectId)) {
      valret.append(objectId);
      valret.append(",");
    }
    if (StringUtil.isDefined(userId)) {
      valret.append(userId);
      valret.append(",");
    }
    if (StringUtil.isDefined(actionId)) {
      valret.append(actionId);
    }

    return valret.toString();
  }

  private static String formatMessage(String text, String extraInfos) {
    StringBuilder message = new StringBuilder();
    if (StringUtil.isDefined(text)) {
      message.append(text).append(". ");
    }
    if (StringUtil.isDefined(extraInfos)) {
      message.append(extraInfos);
    }
    return message.toString();
  }

  private static String computeNamespace(String module) {
    return "silverpeas.silvertrace." + module;
  }

}
