/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SynchroGroupReport {

  private static final String REPORT_NAME = "DomainGroupSynchro";
  private static final String REPORT_NAMESPACE = "silverpeas.core.admin.domain.synchro.group";
  private static final String LOG_FORMAT = "[{0}] {1}: {2}";

  // synchronization states
  public static final int STATE_NOSYNC = 0x00000000;
  public static final int STATE_WAITSTART = 0x00000001;
  public static final int STATE_STARTED = 0x00000002;
  public static final int STATE_ENDED = 0x00000003;

  private static Level level;
  private static List<String> messages = Collections.synchronizedList(new ArrayList<>());
  private static int state = STATE_NOSYNC;

  static public String getReportName() {
    return REPORT_NAME;
  }

  /**
   * Sets the level from which the messages will be reported. This level doesn't apply on the level
   * of the logs in the report file.
   */
  static public void setReportLevel(Level reportLevel) {
    level = reportLevel;
  }

  public static Level getReportLevel() {
    return level;
  }

  /**
   * Sets the state of the synchronization.
   */
  static synchronized private void setState(int iStateCours) {
    if ((state < STATE_WAITSTART) || (iStateCours != STATE_WAITSTART)) {
      state = iStateCours;
    }
  }

  /**
   * Gets the current state of the report.
   * @return the state of the report.
   */
  public static int getState() {
    return state;
  }

  static public String getMessages() {
    String Message = null;
    synchronized (messages) {
      if (messages.size() > 0) {
        Message = messages.remove(0);
      }
    }
    return Message;
  }

  static public void startSynchro() {
    synchronized (messages) {
      messages.clear();
    }
    setState(STATE_STARTED);
    warn("SynchroGroupReport.startSynchro", "Synchronisation Start");
  }

  static public void stopSynchro() {
    warn("SynchroGroupReport.stopSynchro", "Synchronisation End");
    setState(STATE_ENDED);
  }

  public static void reset() {
    setState(STATE_NOSYNC);
  }

  public static void waitForStart() {
    setState(STATE_WAITSTART);
  }

  static public void debug(String classe, String message) {
    if (isSynchroActive()) {
      addMessage(Level.DEBUG, msgFormat(Level.DEBUG, classe, message, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).debug(LOG_FORMAT, getReportName(), classe, message);
    }
  }

  static public void info(String classe, String message) {
    if (isSynchroActive()) {
      addMessage(Level.INFO, msgFormat(Level.INFO, classe, message, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).info(LOG_FORMAT, getReportName(), classe, message);
    }
  }

  static public void warn(String classe, String message) {
    if (isSynchroActive()) {
      addMessage(Level.WARNING, msgFormat(Level.WARNING, classe, message, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).warn(LOG_FORMAT, getReportName(), classe, message);
    }
  }

  static public void error(String classe, String message, Throwable ex) {
    if (isSynchroActive()) {
      addMessage(Level.ERROR, msgFormat(Level.ERROR, classe, message, ex));
      SilverLogger.getLogger(REPORT_NAMESPACE)
          .error(LOG_FORMAT, new Object[]{getReportName(), classe, message}, ex);
    }
  }

  static protected void addMessage(Level msgLevel, String msg) {
    if (msgLevel.value() >= level.value()) {
      synchronized (messages) {
        messages.add(msg);
      }
    }
  }

  static public boolean isSynchroActive() {
    return (state == STATE_STARTED);
  }

  static protected String msgFormat(Level level, String from,
      String msgToTrace, Throwable ex) {
    StringBuilder sb = new StringBuilder();
    switch(level) {
      case DEBUG:
        sb.append("[DEBUG] ");
        break;
      case INFO:
        sb.append("[INFO] ");
        break;
      case WARNING:
        sb.append("[WARN] ");
        break;
      case ERROR:
        sb.append("[ERROR] ");
        break;
      default:
        sb.append("[UNKNOWN] ");
        break;
    }
    sb.append(msgToTrace);
    if ((from != null) && (from.length() > 0)) {
      sb.append(" | From: ").append(from);
    }
    if (ex != null) {
      sb.append(" | !!! EXCEPTION !!! : ").append(ex.getMessage());
    }
    return (sb.toString());
  }
}
