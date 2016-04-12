/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.process.util;

import org.silverpeas.core.process.management.ProcessExecutionContext;
import org.silverpeas.core.process.SilverpeasProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Managing list of processes (tasks in other words) and setting global parameters for execution.
 * @author Yohann Chastagnier
 */
public class ProcessList<C extends ProcessExecutionContext> {

  /** List of Silverpeas processes */
  private final List<SilverpeasProcess<C>> processes = new ArrayList<>();

  /** Common session parameters */
  private final Map<String, Object> sessionParameters = new HashMap<>();

  /**
   * Default constructor
   */
  public ProcessList() {
    // Nothing to do.
  }

  /**
   * Default constructor
   */
  @SafeVarargs
  public ProcessList(final SilverpeasProcess<C>... processes) {
    addAll(processes);
  }

  /**
   * Default constructor
   */
  public ProcessList(final List<SilverpeasProcess<C>> processes) {
    addAll(processes);
  }

  /**
   * Gets the list of Silverpeas processes.
   * @return the list of processes to execute.
   */
  public List<SilverpeasProcess<C>> getList() {
    return processes;
  }

  /**
   * Adds several Silverpeas processes.
   * @param processes the processes to execute.
   */
  @SafeVarargs
  public final void addAll(final SilverpeasProcess<C>... processes) {
    if (processes != null) {
      for (final SilverpeasProcess<C> process : processes) {
        add(process);
      }
    }
  }

  /**
   * Adds several Silverpeas processes
   * @param processes the processes to execute.
   */
  public void addAll(final List<SilverpeasProcess<C>> processes) {
    if (processes != null) {
      processes.forEach(this::add);
    }
  }

  /**
   * Adds a Silverpeas process.
   * @param process a process to execute.
   */
  public void add(final SilverpeasProcess<C> process) {
    if (process != null) {
      processes.add(process);
    }
  }

  /**
   * Adds a session parameter.
   * @param key the key of the session parameter.
   * @param value the value of the session parameter.
   */
  public void put(final String key, final Object value) {
    sessionParameters.put(key, value);
  }

  /**
   * Gets the common session parameters.
   * @return the map of session parameters.
   */
  public Map<String, Object> getSessionParameters() {
    return sessionParameters;
  }

  /**
   * Indicates if the list of Silverpeas is empty.
   * @return true if it does not exist process to execute.
   */
  public boolean isEmpty() {
    return processes.isEmpty();
  }

  /**
   * Indicates if the list of Silverpeas is not empty.
   * @return true if it exists at least one process to execute.
   */
  public boolean isNotEmpty() {
    return !isEmpty();
  }
}
