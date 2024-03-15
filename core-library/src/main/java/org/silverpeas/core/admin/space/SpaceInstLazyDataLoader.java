/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.SpaceInstManager;
import org.silverpeas.core.admin.service.cache.AdminCache;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * This class handles the lazy loading of following data of a SpaceInst:
 * <ul>
 *   <li>profiles, {@link SpaceProfileInst} are managed. Specifically about space profiles, the
 *   list of profiles of this loader can be used into processes of space creation and space
 *   profile spreading</li>
 *   <li>sub spaces, the loader hosts only the identifier of sub spaces but provided in reading
 *   mode copies of linked {@link SpaceInst} from {@link AdminCache}</li>
 *   <li>components, same mechanism as sub spaces one, but with component instances</li>
 * </ul>
 * <p>
 *  The aim of this lazy loading is about avoiding to walk through the entire space tree when
 *  loading a space from repository.<br/>
 *  It permits to get rapidly a space from repository if not yet into cache, and to load later the
 *  linked data which are not necessary used just after repository get.
 * <p>
 *   When a {@link SpaceInst} is put into {@link AdminCache}, cached instances MUST be loaded in
 *   order to avoid that the cache provides each time unloaded data (cache provides copies of data).
 * </p>
 * <p>
 *   Technically, if load has not yet been performed, then all data are loaded before performing
 *   process given to {@link #safeRead(Function)} or {@link #safeWrite(Consumer)} method.
 * </p>
 * @author silveryocha
 */
public class SpaceInstLazyDataLoader implements Serializable {
  private static final long serialVersionUID = -1770031353832624141L;

  private final transient Object mutex = new Object();

  private final SpaceInst space;

  /* Collection of component instance ids */
  private final List<String> componentIds = new ArrayList<>();

  /* Collection of subspace ids */
  private final List<String> subSpaceIds = new ArrayList<>();

  /* Collection of space profile instances */
  private final List<SpaceProfileInst> spaceProfiles = new ArrayList<>();

  private boolean loaded = false;

  SpaceInstLazyDataLoader(final SpaceInst space) {
    this.space = space;
  }

  /**
   * Copies data of given instance into the current one.
   * <p>
   *   Current data are lost (replaced by the copied ones).
   * </p>
   * <p>
   *   The source data are loaded before the real copy.
   * </p>
   * @param other {@link SpaceInstLazyDataLoader} instance data to copy.
   */
  void copy(final SpaceInstLazyDataLoader other) {
    final List<String> componentIdsToCopy;
    final List<String> subSpaceIdsToCopy;
    final List<SpaceProfileInst> spaceProfilesToCopy;
    synchronized (other.mutex) {
      other.load();
      componentIdsToCopy = new ArrayList<>(other.componentIds);
      subSpaceIdsToCopy = new ArrayList<>(other.subSpaceIds);
      spaceProfilesToCopy = new ArrayList<>(other.spaceProfiles);
    }
    synchronized (mutex) {
      reset();
      // if one of list to copy is not null, it means the other data was loaded and the copy
      // must be effective
      manualWrite(d -> {
        d.getComponentIds().addAll(componentIdsToCopy);
        d.getSubSpaceIds().addAll(subSpaceIdsToCopy);
        d.getProfiles().addAll(spaceProfilesToCopy);
      });
    }
  }

  /**
   * Reset all data loaded lazily.
   * <p>
   *   Data will be loaded again at next {@link #safeRead(Function)} or
   *   {@link #safeWrite(Consumer)} method call.
   * </p>
   */
  void reset() {
    synchronized (mutex) {
      loaded = false;
      spaceProfiles.clear();
      subSpaceIds.clear();
      componentIds.clear();
    }
  }


  /**
   * Allows performing data reading.
   * <p>
   *   If not yet done, data are loaded before the read process is performed.
   * </p>
   * @param process the read process.
   */
  <T> T safeRead(final Function<SafeDataAccessor, T> process) {
    synchronized (mutex) {
      load();
      return process.apply(new SafeDataAccessor(this));
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
  void manualWrite(final Consumer<SafeDataModifier> process) {
    synchronized (mutex) {
      loaded = true;
      process.accept(new SafeDataModifier(this));
    }
  }

  /**
   * Allows performing data modifications.
   * <p>
   *   If not yet done, data are loaded before the modification process is performed.
   * </p>
   * @param process the modification process.
   */
  void safeWrite(final Consumer<SafeDataModifier> process) {
    synchronized (mutex) {
      load();
      process.accept(new SafeDataModifier(this));
    }
  }

  /**
   * This method handles the load of the missing data.
   * @apiNote the load is performed only if the loader is not yet marked as loaded
   * ({@link #loaded} at false value).
   * @implNote be very carefully to call this method into synchronized context {@link #mutex}.
   */
  protected void load() {
    if (!loaded) {
      loaded = true;
      Transaction.getTransaction().perform(() -> {
        final SpaceInstManager spaceInstManager = ServiceProvider.getSingleton(
            SpaceInstManager.class);
        spaceInstManager.loadSpaceInstData(space);
        return null;
      });
    }
  }

  /**
   * This class allows callers of {@link #safeRead(Function)} method to access data safely.
   * <p>
   *   All list of data are copied (but not each item instance of lists).
   * </p>
   */
  static class SafeDataAccessor {
    private final SpaceInstLazyDataLoader loader;

    private SafeDataAccessor(SpaceInstLazyDataLoader loader) {
      this.loader = loader;
    }

    /**
     * Gets an unmodifiable list of direct subspaces.
     * <p>
     *   Each instance of the list is provided from {@link AdminCache}.
     * </p>
     * @return a list of {@link SpaceInst} instances.
     */
    List<SpaceInst> getCopyOfSubSpaces() {
      return getCopyOfResource(loader.subSpaceIds, Administration::getSpaceInstById, i -> format(
          "Space with id %s does not exist anymore, removing it from loaded subspace list", i));
    }

    /**
     * Gets an unmodifiable list of components.
     * <p>
     *   Each instance of the list is provided from {@link AdminCache}.
     * </p>
     * @return list of {@link ComponentInst} instance.
     */
    List<ComponentInst> getCopyOfComponents() {
      return getCopyOfResource(loader.componentIds, Administration::getComponentInst, i -> format(
          "Component with id %s does not exist anymore, removing it from loaded component list",
          i));
    }

    /**
     * Gets an unmodifiable list of space profiles.
     * @return list of {@link SpaceProfileInst} instance.
     */
    List<SpaceProfileInst> getCopyOfProfiles() {
      return List.copyOf(loader.spaceProfiles);
    }

    /**
     * Stream the list of space profiles.
     * @return stream of {@link SpaceProfileInst} instance.
     */
    Stream<SpaceProfileInst> streamProfiles() {
      return loader.spaceProfiles.stream();
    }

    private <T> List<T> getCopyOfResource(final List<String> ids,
        final ResourceGetter<String, T> get, final UnaryOperator<String> removeWarningMessage) {
      final Administration service = Administration.get();
      final Iterator<String> it = ids.iterator();
      Stream<T> resources = Stream.of();
      while (it.hasNext()) {
        final String id = it.next();
        try {
          resources = Stream.concat(resources, Stream.of(get.perform(service, id)));
        } catch (AdminException e) {
          it.remove();
          SilverLogger.getLogger(this)
              .warn("{0} (error: {1})", removeWarningMessage.apply(id), e.getMessage());
        }
      }
      return resources.collect(Collectors.toUnmodifiableList());
    }
    
    @FunctionalInterface
    private interface ResourceGetter<I, R> {
      R perform(Administration service, I id) throws AdminException;
    }
  }

  /**
   * This class allows callers of {@link #safeWrite(Consumer)} method to modify data safely.
   * <p>
   *   All lists are directly provided.
   * </p>
   */
  static class SafeDataModifier {
    private final SpaceInstLazyDataLoader loader;

    private SafeDataModifier(SpaceInstLazyDataLoader loader) {
      this.loader = loader;
    }

    /**
     * Gets the container of {@link SpaceProfileInst} of linked {@link SpaceInst}.
     * <p>
     * The list of profiles of this loader can be used into processes of space creation and
     * space profile spreading. Be carefully into a such process to handle properly the loading
     * flag.
     * </p>
     * @return list of {@link SpaceProfileInst}.
     */
    List<SpaceProfileInst> getProfiles() {
      return loader.spaceProfiles;
    }

    /**
     * Gets the container of space identifiers.
     * @return list of string representing space identifier.
     */
    List<String> getSubSpaceIds() {
      return loader.subSpaceIds;
    }

    /**
     * Gets the container of component instance identifiers.
     * @return list of string representing component instance identifier.
     */
    List<String> getComponentIds() {
      return loader.componentIds;
    }
  }
}
