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
package org.silverpeas.web.jobstartpage;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.of;

public class NavBarManager {
  // Constants used by urlFactory

  UserDetail user = null;
  AdminController adminCtrl = null;
  AbstractComponentSessionController sessionCtrl = null;
  String spContext;
  HashSet<String> manageableSpaces = new HashSet<>();
  String currentSpaceId = null;
  String currentSubSpaceId = null;
  DisplaySortedCache spaces = new DisplaySortedCache();
  DisplaySortedCache spaceComponents = new DisplaySortedCache();
  DisplaySortedCache subSpaces = new DisplaySortedCache();
  DisplaySortedCache subSpaceComponents = new DisplaySortedCache();
  long elCounter = 0;

  public void resetSpaceCache(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);
    DisplaySorted elmt = getSpaceCache(spaceId);
    if (elmt != null) {
      elmt.copy(buildSpaceObject(spaceId));
      if (spaceId.equals(currentSpaceId)) {
        setCurrentSpace(currentSpaceId);
      } else if (spaceId.equals(currentSubSpaceId)) {
        setCurrentSubSpace(spaceId);
      }
    }
  }

  public void addSpaceInCache(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);
    manageableSpaces.add(spaceId);
    DisplaySorted newElmt = buildSpaceObject(spaceId);
    if (newElmt != null) {
      if (newElmt.getType() == DisplaySorted.TYPE_SPACE) {
        spaces.put(spaceId, newElmt);
      } else {
        // Sub Space case
        setCurrentSpace(currentSpaceId);
      }
    }
  }

  public void removeSpaceInCache(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);
    Optional<DisplaySorted> elmt = ofNullable(getSpaceCache(spaceId));
    elmt.ifPresent(e -> {
      if (e.getType() == DisplaySorted.TYPE_SPACE) {
        removeRootSpaceInCache(spaceId);
      } else {
        removeSubSpaceInCache(spaceId);
      }
    });
  }

  private void removeRootSpaceInCache(final String spaceId) {
    spaces.remove(spaceId);
    if (currentSpaceId != null && currentSpaceId.equals(spaceId)) {
      setCurrentSpace(null);
    }
  }

  private void removeSubSpaceInCache(final String spaceId) {
    subSpaces.remove(spaceId);
    if (currentSubSpaceId != null && currentSubSpaceId.equals(spaceId)) {
      setCurrentSubSpace(null);
    }
  }

  public void resetAllCache() {
    final String cSpaceId = this.currentSpaceId;
    final String cSubSpaceId = this.currentSubSpaceId;
    initWithUser(sessionCtrl, user);
    if (cSpaceId != null) {
      setCurrentSpace(cSpaceId);
    }
    if (cSubSpaceId != null) {
      setCurrentSubSpace(cSubSpaceId);
    }
  }

  public void initWithUser(AbstractComponentSessionController msc, UserDetail user) {
    String sUserId = user.getId();

    spContext = URLUtil.getApplicationURL();
    adminCtrl = ServiceProvider.getService(AdminController.class);
    sessionCtrl = msc;
    this.user = user;
    elCounter = 0;
    currentSpaceId = null;
    currentSubSpaceId = null;
    subSpaces.clear();
    spaceComponents.clear();
    subSpaceComponents.clear();

    if (!this.user.isAccessAdmin()) {
      String[] allManageableSpaceIds = adminCtrl.getUserManageableSpaceIds(sUserId);
      // First of all, add the manageable spaces into the set
      manageableSpaces.clear();
      for (String manageableSpaceId : allManageableSpaceIds) {
        manageableSpaces.add(getShortSpaceId(manageableSpaceId));
      }
    }

    String[] spaceIds = adminCtrl.getAllRootSpaceIds();
    spaces.set(createSpaceObjects(stream(spaceIds), false));
  }

  public boolean hasBeenInitialized() {
    return user != null;
  }

  // Spaces functions
  // ----------------
  public Collection<DisplaySorted> getAvailableSpaces() {
    return spaces.getSorted();
  }

  public String getCurrentSpaceId() {
    return currentSpaceId;
  }

  public DisplaySorted getSpace(String theSpaceId) {
    return getSpaceCache(getShortSpaceId(theSpaceId));
  }

  public boolean setCurrentSpace(String theSpaceId) {
    String spaceId = getShortSpaceId(theSpaceId);
    currentSpaceId = spaceId;
    // Reset the selected sub space
    currentSubSpaceId = null;
    subSpaceComponents.clear();
    if (StringUtil.isDefined(currentSpaceId) && getSpaceCache(currentSpaceId) == null) {
      currentSpaceId = null;
    }
    if (!StringUtil.isDefined(spaceId) || (currentSpaceId == null)) {
      spaceComponents.clear();
      subSpaces.clear();
    } else {
      SpaceInst spaceInst = adminCtrl.getSpaceInstById(spaceId);
      // Get the space's components and sub-spaces
      if (spaceInst == null) {
        spaceComponents.clear();
        subSpaces.clear();
        currentSpaceId = null;
      } else {
        spaceComponents.set(createComponentObjects(spaceInst));
        subSpaces.set(createSpaceObjects(spaceInst.getSubSpaces().stream().map(SpaceInst::getId), true));
      }
    }
    return StringUtil.isDefined(currentSpaceId);
  }

  public Collection<DisplaySorted> getAvailableSpaceComponents() {
    if (currentSpaceId == null) {
      return emptyList();
    }
    return spaceComponents.getSorted();
  }

  // Sub-Spaces functions
  // --------------------
  public Collection<DisplaySorted> getAvailableSubSpaces() {
    if (currentSpaceId == null) {
      return emptyList();
    }
    return subSpaces.getSorted();
  }

  public String getCurrentSubSpaceId() {
    return currentSubSpaceId;
  }

  public boolean setCurrentSubSpace(String theSpaceId) {
    String subSpaceId = getShortSpaceId(theSpaceId);
    SpaceInst sp = null;
    currentSubSpaceId = subSpaceId;
    if (StringUtil.isDefined(currentSubSpaceId) && (getSpaceCache(currentSubSpaceId) == null)) {
      currentSubSpaceId = null;
    }
    if (StringUtil.isDefined(currentSubSpaceId)) {
      sp = adminCtrl.getSpaceInstById(currentSubSpaceId);
      if (sp == null) {
        currentSubSpaceId = null;
      }
    }
    if (sp != null && StringUtil.isDefined(currentSubSpaceId)) {
      subSpaceComponents.set(createComponentObjects(sp));
    } else {
      subSpaceComponents.clear();
    }
    return StringUtil.isDefined(currentSubSpaceId);
  }

  public Collection<DisplaySorted> getAvailableSubSpaceComponents() {
    if (currentSubSpaceId == null) {
      return emptyList();
    }
    return subSpaceComponents.getSorted();
  }

  protected DisplaySorted getSpaceCache(String spaceId) {
    if (spaceId == null) {
      return null;
    }
    return ofNullable(spaces.get(spaceId)).orElseGet(() -> subSpaces.get(spaceId));
  }

  protected Stream<DisplaySorted> createSpaceObjects(Stream<String> spaceIds, boolean goRecurs) {
    Stream<DisplaySorted> valRet = spaceIds.map(this::buildSpaceObject);
    if (goRecurs) {
      valRet = valRet.flatMap(s -> {
        Stream<String> subSpaceIds = stream(adminCtrl.getAllSubSpaceIds(s.getId()));
        return Stream.concat(of(s), createSpaceObjects(subSpaceIds, true));
      });
    }
    return valRet;
  }

  protected DisplaySorted buildSpaceObject(String spaceId) {
    DisplaySorted valRet = new DisplaySorted();
    valRet.setId(getShortSpaceId(spaceId));
    valRet.setVisible(true);
    SpaceInstLight spaceInst = adminCtrl.getSpaceInstLight(spaceId);
    if (spaceInst.isRoot()) {
      valRet.setType(DisplaySorted.TYPE_SPACE);
      valRet.setAdmin(user.isAccessAdmin() || manageableSpaces.contains(valRet.getId()));
    } else {
      valRet.setParentId(getShortSpaceId(spaceInst.getFatherId()));
      valRet.setType(DisplaySorted.TYPE_SUBSPACE);
      valRet.setAdmin(user.isAccessAdmin() || isAdminOfSpace(spaceInst));
    }
    if (!valRet.isAdmin()) { // Rattrapage....
      valRet.setVisible(isAtLeastOneSubSpaceManageable(valRet.getId()));
    }
    valRet.setName(spaceInst.getName(sessionCtrl.getLanguage()));
    valRet.setOrderNum(spaceInst.getOrderNum());
    valRet.setDeep(spaceInst.getLevel());
    return valRet;
  }

  private boolean isAtLeastOneSubSpaceManageable(String spaceId) {
    String[] subSpaceIds = adminCtrl.getAllSubSpaceIds(spaceId);
    for (String subSpaceId : subSpaceIds) {
      if (manageableSpaces.contains(getShortSpaceId(subSpaceId))) {
        return true;
      }
    }
    for (String subSpaceId : subSpaceIds) {
      if (isAtLeastOneSubSpaceManageable(subSpaceId)) {
        return true;
      }
    }
    return false;
  }

  protected String getShortSpaceId(String spaceId) {
    if ((spaceId != null) && (spaceId.startsWith("WA"))) {
      return spaceId.substring(2);
    } else {
      return (spaceId == null) ? "" : spaceId;
    }
  }

  protected boolean isAdminOfSpace(SpaceInstLight spaceInst) {
    boolean valret = manageableSpaces.contains(String.valueOf(spaceInst.getLocalId())) ||
        manageableSpaces.contains(getShortSpaceId(spaceInst.getFatherId()));
    SpaceInstLight parcSpaceInst = spaceInst;
    while (!valret && !parcSpaceInst.isRoot()) {
      parcSpaceInst = adminCtrl.getSpaceInstLight(parcSpaceInst.getFatherId());
      valret = manageableSpaces.contains(String.valueOf(parcSpaceInst.getLocalId()));
    }
    return valret;
  }

  protected Stream<DisplaySorted> createComponentObjects(SpaceInst spaceInst) {
    // Get the space's components
    final boolean isTheSpaceAdmin = user.isAccessAdmin() || isAdminOfSpace(new SpaceInstLight(spaceInst));
    return spaceInst.getAllComponentsInst().stream()
        .map(ci -> {
          final DisplaySorted ds = new DisplaySorted();
          ds.setName(ci.getLabel(sessionCtrl.getLanguage()));
          if (ds.getName() == null) {
            ds.setName(ci.getName());
          }
          ds.setOrderNum(ci.getOrderNum());
          ds.setId(ci.getId());
          ds.setParentId(getShortSpaceId(ci.getSpaceId()));
          ds.setType(DisplaySorted.TYPE_COMPONENT);
          ds.setTypeName(ci.isWorkflow() ? "processManager" : ci.getName());
          ds.setAdmin(isTheSpaceAdmin);
          ds.setDeep(spaceInst.getLevel());
          ds.setVisible(isTheSpaceAdmin);
          return ds;
        });
  }

  private static class DisplaySortedCache {

    private final SortedSet<DisplaySorted> sortedData = synchronizedSortedSet(new TreeSet<>());
    private final Map<String, DisplaySorted> cache = synchronizedMap(new HashMap<>());

    DisplaySortedCache() {
      super();
    }

    /**
     * Indexes a new {@link DisplaySorted} instance by its identifier.
     * @param id the identifier.
     * @param data the {@link DisplaySorted} representing a generic data.
     * @return the indexed instance.
     */
    public DisplaySorted put(final String id, final DisplaySorted data) {
      sortedData.add(data);
      return cache.put(id, data);
    }

    /**
     * Removes a {@link DisplaySorted} by its identifier.
     * @param id an identifier.
     * @return the removed instance if any, null otherwise.
     */
    public DisplaySorted remove(final String id) {
      final DisplaySorted removed = cache.remove(id);
      if (removed != null) {
        sortedData.removeIf(removed::equals);
      }
      return removed;
    }

    /**
     * Gets the {@link DisplaySorted} instances registered into the cache and sorted by their
     * natural ordering.
     * @return sorted instances.
     */
    public SortedSet<DisplaySorted> getSorted() {
      return sortedData;
    }

    /**
     * Gets a registered {@link DisplaySorted} instance.
     * @param id an identifier.
     * @return a {@link DisplaySorted} if any, null otherwise.
     */
    public DisplaySorted get(final String id) {
      return cache.get(id);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
      sortedData.clear();
      cache.clear();
    }

    /**
     * Clears the cache and registers the given data.
     * @param data the data to set into cache.
     */
    public void set(final Stream<DisplaySorted> data) {
      clear();
      data.forEach(d -> put(d.getId(), d));
    }
  }
}
