/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.selectionPeas;


import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterSelect;
import com.stratelia.silverpeas.genericPanel.PanelMiniFilterToken;
import com.stratelia.silverpeas.genericPanel.PanelOperation;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionExtraParams;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract public class CacheManager {

  protected Map<String, PanelLine> elementCache = new HashMap<String, PanelLine>();
  protected Map<String, PanelLine> setCache = new HashMap<String, PanelLine>();
  protected Set<String> selectedElements = new HashSet<String>();
  protected Set<String> selectedSets = new HashSet<String>();
  protected ResourceLocator localResourceLocator = null;
  protected ResourceLocator globalResourceLocator = null;
  protected ResourceLocator iconResourceLocator = null;
  protected String language = DisplayI18NHelper.getDefaultLanguage();
  protected Selection selection = null;

  public CacheManager(String language, ResourceLocator local,
      ResourceLocator icon, Selection selection) {
    this.language = language;
    this.localResourceLocator = local;
    globalResourceLocator = GeneralPropertiesManager.getGeneralMultilang(this.language);
    iconResourceLocator = icon;
    this.selection = selection;
    resetAll();
  }

  abstract public BrowsePanelProvider getSearchPanelProvider(CacheType what, SelectionExtraParams sep);

  abstract public BrowsePanelProvider getBrowsePanelProvider(CacheType what, SelectionExtraParams sep);

  abstract public PanelProvider getCartPanelProvider(CacheType what,  SelectionExtraParams sep);

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
    SilverTrace.info("selectionPeas", "CacheManager.resetAll", "root.MSG_GEN_PARAM_VALUE");
    elementCache.clear();
    setCache.clear();
    selectedElements.clear();
    selectedSets.clear();
  }

  public void setInfos(CacheType what, String id, PanelLine pl) {
    SilverTrace.info("selectionPeas", "CacheManager.setInfos", "root.MSG_GEN_PARAM_VALUE",
        "What = " + what + ", Id=" + id + ", Name = " + pl.m_Values[0] + ", Selected=" + pl.m_Selected);
    getCache(what).put(id, pl);
  }

  public PanelLine getInfos(CacheType what, String id) {
    PanelLine valret = getCache(what).get(id);

    if (valret != null) {
      SilverTrace.info("selectionPeas", "CacheManager.getInfos",
          "root.MSG_GEN_PARAM_VALUE", "What = " + what + ", Id=" + id
              + ", Name = " + valret.m_Values[0] + ", Selected="
              + valret.m_Selected);
    } else {
      SilverTrace.info("selectionPeas", "CacheManager.getInfos",
          "root.MSG_GEN_PARAM_VALUE", "What = " + what + ", Id=" + id
              + ", NULL");
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
    Set<String> selectedStrings =  getSelected(what);
    return getSelected(what).toArray(new String[getSelected(what).size()]);
  }

  public int getSelectedNumber(CacheType what) {
    return getSelected(what).size();
  }

  public PanelLine[] getSelectedLines(CacheType what) {
    List<PanelLine> en = new ArrayList<PanelLine>(getCache(what).values());
    Collections.sort(en, new Comparator<PanelLine>() {
      @Override
      public int compare(PanelLine o1, PanelLine o2) {
        return o1.m_Values[0].toUpperCase().compareTo(
            o2.m_Values[0].toUpperCase());
      }
    });
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
