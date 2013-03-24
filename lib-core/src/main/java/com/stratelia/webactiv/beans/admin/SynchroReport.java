/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.beans.admin;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.List;

public class SynchroReport {

  // Niveaux de trace
  public static final int TRACE_LEVEL_UNKNOWN = 0x00000000;
  public static final int TRACE_LEVEL_DEBUG = 0x00000001;
  public static final int TRACE_LEVEL_INFO = 0x00000002;
  public static final int TRACE_LEVEL_WARN = 0x00000003;
  public static final int TRACE_LEVEL_ERROR = 0x00000004;
  public static final int TRACE_LEVEL_FATAL = 0x00000005;
  // Etats de la synchro
  public static final int STATE_NOSYNC = 0x00000000;
  public static final int STATE_WAITSTART = 0x00000001;
  public static final int STATE_STARTED = 0x00000002;
  public static final int STATE_ENDED = 0x00000003;

  private static final String moduleName = "synchro";

  private static int iTraceLevel;
  @SuppressWarnings("unchecked")
  private static final List<String> VMessage = (List<String>)Collections.synchronizedList(
      new ArrayList<String>());
  private static int iState;

  // Initialisation
  static {
    iTraceLevel = TRACE_LEVEL_WARN;
    iState = STATE_NOSYNC;
  }

  static public String getModuleName() {
    return moduleName;
  }

  /**
   * Fixe le niveau de trace
   */
  static public void setTraceLevel(int iTraceLevelFixed) {
    SilverTrace.setTraceLevel(getModuleName(), iTraceLevelFixed);
    iTraceLevel = iTraceLevelFixed;
  }

  /**
   * Recupere le niveau de trace
   */
  static public int getTraceLevel() {
    return iTraceLevel;
  }

  /**
   * Recupere le niveau de trace ds une chaine
   * @return
   */
  static public String getTraceLevelStr() {
    switch (iTraceLevel) {
      case TRACE_LEVEL_WARN:
        return "warning";
      case TRACE_LEVEL_DEBUG:
        return "debug";
      case TRACE_LEVEL_INFO:
        return "info";
      case TRACE_LEVEL_ERROR:
        return "error";
      case TRACE_LEVEL_FATAL:
        return "fatal";
      default:
        return "unknown";
    }
  }

  /**
   * Fixe l'etat
   */
  static synchronized public void setState(int iStateCours) {
    if ((iState < STATE_WAITSTART) || (iStateCours != STATE_WAITSTART)) {
      iState = iStateCours;
    } // Else, do nothing
  }

  /**
   * Recupere l'etat
   */
  static public int getState() {
    return iState;
  }

  /**

     */
  static public String getMessage() {
    String message = null;
    synchronized (VMessage) {
      if (!VMessage.isEmpty()) {
        message = VMessage.remove(0);
      }
    }
    return message;
  }

  static public void startSynchro() {
    synchronized (VMessage) {
      VMessage.clear();
    }
    setState(STATE_STARTED);
    warn("SynchroReport.startSynchro", "Debut de Synchronisation", null);
  }

  static public void stopSynchro() {
    warn("SynchroReport.stopSynchro", "Fin de Synchronisation", null);
    setState(STATE_ENDED);
  }

  static public void debug(String classe, String message, Throwable ex) {
    if (isSynchroActive()) {
      addMessage(msgFormat(TRACE_LEVEL_DEBUG, classe, message, ex),
          TRACE_LEVEL_DEBUG);
      SilverTrace.debug(getModuleName(), classe, message, ex);
    }
  }

  static public void info(String classe, String message, Throwable ex) {
    if (isSynchroActive()) {
      addMessage(msgFormat(TRACE_LEVEL_INFO, classe, message, ex),
          TRACE_LEVEL_INFO);
      SilverTrace.info(getModuleName(), classe, "root.MSG_GEN_PARAM_VALUE", message, ex);
    }
  }

  static public void warn(String classe, String message, Throwable ex) {
    if (isSynchroActive()) {
      addMessage(msgFormat(TRACE_LEVEL_WARN, classe, message, ex),
          TRACE_LEVEL_WARN);
      SilverTrace.warn(getModuleName(), classe, "root.MSG_GEN_PARAM_VALUE", message, ex);
    }
  }

  static public void error(String classe, String message, Throwable ex) {
    if (isSynchroActive()) {
      addMessage(msgFormat(TRACE_LEVEL_ERROR, classe, message, ex),
          TRACE_LEVEL_ERROR);
      SilverTrace.error(getModuleName(), classe, "root.MSG_GEN_PARAM_VALUE", message, ex);
    }
  }

  static protected void addMessage(String msg, int priority) {
    if (priority >= iTraceLevel) {
      synchronized (VMessage) {
        VMessage.add(msg);
      }
    }
  }

  static public boolean isSynchroActive() {
    return (iState == STATE_STARTED);
  }

  static protected String msgFormat(int traceLvl, String classe, String msgToTrace, Throwable ex) {
    StringBuilder sb = new StringBuilder();
    switch (traceLvl) {
      case TRACE_LEVEL_DEBUG:
        sb.append("D_");
        break;
      case TRACE_LEVEL_INFO:
        sb.append("I_");
        break;
      case TRACE_LEVEL_WARN:
        sb.append("W_");
        break;
      case TRACE_LEVEL_ERROR:
        sb.append("E_");
        break;
      case TRACE_LEVEL_FATAL:
        sb.append("F_");
        break;
      default:
        sb.append("U_");
        break;
    }
    sb.append(msgToTrace);
    if ((classe != null) && (classe.length() > 0)) {
      sb.append(" | Classe : ").append(classe);
    }
    if (ex != null) {
      sb.append(" | !!! EXCEPTION !!! : ").append(ex.getMessage());
    }
    return (sb.toString());
  }
}
