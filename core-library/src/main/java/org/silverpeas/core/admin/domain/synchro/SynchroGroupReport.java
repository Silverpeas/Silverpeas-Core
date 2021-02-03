/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.util.logging.Level;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class SynchroGroupReport {

  private static final String REPORT_NAME = "DomainGroupSynchro";
  private static final String REPORT_NAMESPACE = "silverpeas.core.admin.domain.synchro.group";
  private static final String LOG_FORMAT = "[{0}] {1}: {2}";

  // synchronization states
  private static final int STATE_NOSYNC = 0x00000000;
  private static final int STATE_WAITSTART = 0x00000001;
  private static final int STATE_STARTED = 0x00000002;
  private static final int STATE_ENDED = 0x00000003;

  private static Level level;
  private static List<String> messages = Collections.synchronizedList(new ArrayList<>());
  private static int state = STATE_NOSYNC;

  private SynchroGroupReport() {

  }

  public static String getReportName() {
    return REPORT_NAME;
  }

  /**
   * Sets the level from which the messages will be reported. This level doesn't apply on the level
   * of the logs in the report file.
   */
  public static void setReportLevel(Level reportLevel) {
    level = reportLevel;
  }

  public static Level getReportLevel() {
    return level == null ? Level.WARNING : level;
  }

  /**
   * Sets the state of the synchronization.
   */
  private static synchronized void setState(int iStateCours) {
    if ((state < STATE_WAITSTART) || (iStateCours != STATE_WAITSTART)) {
      state = iStateCours;
    }
  }

  /**
   * Gets the current state of the report.
   * @return the state of the report.
   */
  public static synchronized int getState() {
    return state;
  }

  public static String getMessages() {
    String message = null;
    synchronized (messages) {
      if (!messages.isEmpty()) {
        message = messages.remove(0);
      }
    }
    return message;
  }

  public static void startSynchro() {
    synchronized (messages) {
      messages.clear();
    }
    setState(STATE_STARTED);
    warn("SynchroGroupReport.startSynchro", "Synchronisation Start");
  }

  public static void stopSynchro() {
    warn("SynchroGroupReport.stopSynchro", "Synchronisation End");
    setState(STATE_ENDED);
  }

  public static void reset() {
    setState(STATE_NOSYNC);
  }

  public static void waitForStart() {
    setState(STATE_WAITSTART);
  }

  public static void debug(String classe, String message) {
    if (isSynchroActive()) {
      addMessage(Level.DEBUG, msgFormat(Level.DEBUG, classe, message, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).debug(LOG_FORMAT, getReportName(), classe, message);
    }
  }

  public static void debug(String classe, Supplier<String> msgSupplier) {
    if (isSynchroActive() && getReportLevel().value() >= Level.DEBUG.value()) {
      String msg = msgSupplier.get();
      addMessage(Level.DEBUG, msgFormat(Level.DEBUG, classe, msg, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).debug(LOG_FORMAT, getReportName(), classe, msg);
    }
  }

  public static void info(String classe, String message) {
    if (isSynchroActive()) {
      addMessage(Level.INFO, msgFormat(Level.INFO, classe, message, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).info(LOG_FORMAT, getReportName(), classe, message);
    }
  }

  public static void warn(String classe, String message) {
    if (isSynchroActive()) {
      addMessage(Level.WARNING, msgFormat(Level.WARNING, classe, message, null));
      SilverLogger.getLogger(REPORT_NAMESPACE).warn(LOG_FORMAT, getReportName(), classe, message);
    }
  }

  public static void error(String classe, String message, Throwable ex) {
    if (isSynchroActive()) {
      addMessage(Level.ERROR, msgFormat(Level.ERROR, classe, message, ex));
      SilverLogger.getLogger(REPORT_NAMESPACE)
          .error(LOG_FORMAT, new Object[]{getReportName(), classe, message}, ex);
    }
  }

  protected static void addMessage(Level msgLevel, String msg) {
    if (msgLevel.value() >= getReportLevel().value()) {
      synchronized (messages) {
        messages.add(msg);
      }
    }
  }

  public static boolean isSynchroActive() {
    return state == STATE_STARTED;
  }

  static String msgFormat(Level level, String from,
      String msgToTrace, Throwable ex) {
    return SynchroDomainReport.msgFormat(level, from, msgToTrace, ex);
  }
}
