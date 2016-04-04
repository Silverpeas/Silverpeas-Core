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

package org.silverpeas.core.web.selection;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelMiniFilterSelect;
import org.silverpeas.core.web.panel.PanelMiniFilterToken;
import org.silverpeas.core.web.panel.PanelOperation;
import org.silverpeas.core.web.panel.PanelProvider;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract public class CacheManager {

  protected Map<String, PanelLine> elementCache = new HashMap<>();
  protected Map<String, PanelLine> setCache = new HashMap<>();
  protected Set<String> selectedElements = new HashSet<>();
  protected Set<String> selectedSets = new HashSet<>();
  protected String language = DisplayI18NHelper.getDefaultLanguage();
  protected Selection selection = null;

  public CacheManager(String language, Selection selection) {
    this.language = language;
    this.selection = selection;
    resetAll();
  }

  abstract public BrowsePanelProvider getSearchPanelProvider(CacheType what,
      SelectionExtraParams sep);

  abstract public BrowsePanelProvider getBrowsePanelProvider(CacheType what,
      SelectionExtraParams sep);

  abstract public PanelProvider getCartPanelProvider(CacheType what, SelectionExtraParams sep);

  abstract public PanelOperation getPanelOperation(String operation);

  abstract public String[][] getContentInfos(CacheType what, String id);

  abstract public String getContentText(CacheType what);

  abstract public String[] getContentColumnsNames(CacheType what);

  abstract public String[][] getContentLines(CacheType what, String id);

  abstract public String[] getColumnsNames(CacheType what);

  abstract protected PanelLine getLineFromId(CacheType what, String id);

  abstract public int getLineCount(CacheType what);

  abstract public PanelMiniFilterToken[] getPanelMiniFilters(CacheType what);

  abstract public PanelMiniFilterSelect getSelectMiniFilter(CacheType what);

  public void resetAll() {

    elementCache.clear();
    setCache.clear();
    selectedElements.clear();
    selectedSets.clear();
  }

  public void setInfos(CacheType what, String id, PanelLine pl) {
    getCache(what).put(id, pl);
  }

  public PanelLine getInfos(CacheType what, String id) {
    PanelLine valret = getCache(what).get(id);

    if (valret == null) {
      valret = getLineFromId(what, id);
      setInfos(what, id, valret);
    }
    return valret;
  }

  public void unselectAll() {
    Collection<PanelLine> en = getCache(CacheType.CM_SET).values();
    for (PanelLine panel : en) {
      panel.m_Selected = false;
    }
    getSelected(CacheType.CM_SET).clear();

    en = getCache(CacheType.CM_ELEMENT).values();
    for (PanelLine panel : en) {
      panel.m_Selected = false;
    }
    getSelected(CacheType.CM_ELEMENT).clear();
  }

  public void setSelected(CacheType what, String id, boolean isSelected) {
    PanelLine theLine;

    theLine = getInfos(what, id);
    if (theLine != null) {
      theLine.m_Selected = isSelected;
      if (isSelected) {
        getSelected(what).add(id);
      } else {
        getSelected(what).remove(id);
      }
    }
  }

  public void setSelected(CacheType what, String[] ids, boolean isSelected) {
    if (ids != null) {
      for (String id : ids) {
        if (StringUtil.isDefined(id)) {
          setSelected(what, id, isSelected);
        }
      }
    }
  }

  public String[] getSelectedIds(CacheType what) {
    return getSelected(what).toArray(new String[getSelected(what).size()]);
  }

  public int getSelectedNumber(CacheType what) {
    return getSelected(what).size();
  }

  public PanelLine[] getSelectedLines(CacheType what) {
    List<PanelLine> en = new ArrayList<>(getCache(what).values());
    Collections.sort(en, (PanelLine o1, PanelLine o2) -> o1.m_Values[0].toUpperCase()
        .compareTo(o2.m_Values[0].toUpperCase()));
    return en.toArray(new PanelLine[en.size()]);
  }

  protected Map<String, PanelLine> getCache(CacheType what) {
    switch (what) {
      case CM_SET:
        return setCache;
      case CM_ELEMENT:
        return elementCache;
      default:
        return null;
    }
  }

  protected Set<String> getSelected(CacheType what) {
    switch (what) {
      case CM_SET:
        return selectedSets;
      case CM_ELEMENT:
        return selectedElements;
      default:
        return null;
    }
  }
}
