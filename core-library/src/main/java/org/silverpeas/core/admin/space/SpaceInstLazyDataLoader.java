/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.space;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.SpaceInstManager;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.TriConsumer;
import org.silverpeas.core.util.TriFunction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles the lazy loading of following data of a SpaceInst:
 * <ul>
 *   <li>profiles</li>
 *   <li>sub spaces</li>
 *   <li>components</li>
 * </ul>
 * <p>
 *  The aim of this lazy loading is about avoiding to walk through the entire space tree when
 *  loading a space from repository.<br/>
 *  It permits to get rapidly a space from repository if not yet into cache, and to load later the
 *  linked data which are not necessary used just after repository get.
 * </p>
 * <p>
 *   Technically, if load has not yet been performed, then all data are loaded before performing
 *   process given to {@link #safeRead(TriFunction)} or {@link #safeWrite(TriConsumer)} method.
 * </p>
 * @author silveryocha
 */
public class SpaceInstLazyDataLoader implements Serializable {
  private static final long serialVersionUID = -1770031353832624141L;

  private final transient Object mutex = new Object();

  private final SpaceInst space;

  /* Collection of components Instances */
  private final List<ComponentInst> components = new ArrayList<>();

  /* Collection of subspaces Instances */
  private final List<SpaceInst> subSpaces = new ArrayList<>();

  /* Collection of space profiles Instances */
  private final List<SpaceProfileInst> spaceProfiles = new ArrayList<>();

  private boolean loaded = false;

  SpaceInstLazyDataLoader(final SpaceInst space) {
    this.space = space;
  }

  /**
   * Reset all data loaded lazily.
   * <p>
   *   Data will be loaded again at next {@link #safeRead(TriFunction)} or
   *   {@link #safeWrite(TriConsumer)} method call.
   * </p>
   */
  void reset() {
    synchronized (mutex) {
      loaded = false;
      spaceProfiles.clear();
      subSpaces.clear();
      components.clear();
    }
  }


  /**
   * Allows performing data reading.
   * <p>
   *   If not yet done, data are loaded before the read process is performed.
   * </p>
   * @param process the read process.
   */
  <T> T safeRead(
      final TriFunction<List<SpaceProfileInst>, List<SpaceInst>, List<ComponentInst>, T> process) {
    synchronized (mutex) {
      if (!loaded) {
        loaded = true;
        load();
      }
      return process.apply(spaceProfiles, subSpaces, components);
    }
  }

  /**
   * Allows performing data modification without taking care if data have been loaded or not.
   * <p>
   *   This method does not perform a clear of data, so if data MUST be cleared before calling
   *   the method, please call {@link #reset()} before.
   * </p>
   * <p>
   *   Please notice that data will be marked as loaded.
   * </p>
   * @param process the modification process.
   */
  void manualWrite(
      final TriConsumer<List<SpaceProfileInst>, List<SpaceInst>, List<ComponentInst>> process) {
    synchronized (mutex) {
      loaded = true;
      process.accept(spaceProfiles, subSpaces, components);
    }
  }

  /**
   * Allows performing data modifications.
   * <p>
   *   If not yet done, data are loaded before the modification process is performed.
   * </p>
   * @param process the modification process.
   */
  void safeWrite(
      final TriConsumer<List<SpaceProfileInst>, List<SpaceInst>, List<ComponentInst>> process) {
    synchronized (mutex) {
      if (!loaded) {
        loaded = true;
        load();
      }
      process.accept(spaceProfiles, subSpaces, components);
    }
  }

  /**
   * This method handles the load of the missing data.
   */
  protected void load() {
    Transaction.getTransaction().perform(() -> {
      final SpaceInstManager spaceInstManager = ServiceProvider.getService(SpaceInstManager.class);
      spaceInstManager.loadSpaceInstData(space);
      return null;
    });
  }
}
