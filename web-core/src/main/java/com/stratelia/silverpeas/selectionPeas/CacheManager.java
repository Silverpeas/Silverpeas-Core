/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract public class CacheManager {

  public final static int CM_SET = 0;
  public final static int CM_ELEMENT = 1;
  public final static int CM_NBTOT = 2;
  protected Map<String, PanelLine> m_ElementCache = new HashMap<String, PanelLine>();
  protected Map<String, PanelLine> m_SetCache = new HashMap<String, PanelLine>();
  protected Set<String> m_SelectedElements = new HashSet<String>();
  protected Set<String> m_SelectedSets = new HashSet<String>();
  protected ResourceLocator m_Local = null;
  protected ResourceLocator m_Global = null;
  protected ResourceLocator m_Icon = null;
  protected String m_Language = "fr";
  protected Selection m_Selection = null;
  protected String m_Context = null;

  public CacheManager(String language, ResourceLocator local,
      ResourceLocator icon, Selection selection) {
    m_Context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    m_Language = language;
    m_Local = local;
    m_Global = GeneralPropertiesManager.getGeneralMultilang(m_Language);
    m_Icon = icon;
    m_Selection = selection;
    resetAll();
  }

  abstract public BrowsePanelProvider getSearchPanelProvider(int what,
      CacheManager cm, SelectionExtraParams sep);

  abstract public BrowsePanelProvider getBrowsePanelProvider(int what,
      CacheManager cm, SelectionExtraParams sep);

  abstract public PanelProvider getCartPanelProvider(int what, CacheManager cm,
      SelectionExtraParams sep);

  abstract public PanelOperation getPanelOperation(String operation);

  abstract public String[][] getContentInfos(int what, String id);

  abstract public String getContentText(int what);

  abstract public String[] getContentColumnsNames(int what);

  abstract public String[][] getContentLines(int what, String id);

  abstract public String[] getColumnsNames(int what);

  abstract protected PanelLine getLineFromId(int what, String id);

  abstract public int getLineCount(int what);

  abstract public PanelMiniFilterToken[] getPanelMiniFilters(int what);

  abstract public PanelMiniFilterSelect getSelectMiniFilter(int what);

  public void resetAll() {
    SilverTrace.info("selectionPeas", "CacheManager.resetAll",
        "root.MSG_GEN_PARAM_VALUE");
    m_ElementCache.clear();
    m_SetCache.clear();
    m_SelectedElements.clear();
    m_SelectedSets.clear();
  }

  public void setInfos(int what, String id, PanelLine pl) {
    SilverTrace.info("selectionPeas", "CacheManager.setInfos",
        "root.MSG_GEN_PARAM_VALUE", "What = " + what + ", Id=" + id
        + ", Name = " + pl.m_Values[0] + ", Selected=" + pl.m_Selected);
    getCache(what).put(id, pl);
  }

  public PanelLine getInfos(int what, String id) {
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
    Collection<PanelLine> en = getCache(CM_SET).values();
    for (PanelLine panel : en) {
      panel.m_Selected = false;
    }
    getSelected(CM_SET).clear();

    en = getCache(CM_ELEMENT).values();
    for (PanelLine panel : en) {
      panel.m_Selected = false;
    }
    getSelected(CM_ELEMENT).clear();
  }

  public void setSelected(int what, String id, boolean isSelected) {
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

  public void setSelected(int what, String[] ids, boolean isSelected) {
    if (ids != null) {
      for (int i = 0; i < ids.length; i++) {
        if (StringUtil.isDefined(ids[i])) {
          setSelected(what, ids[i], isSelected);
        }
      }
    }
  }

  public String[] getSelectedIds(int what) {
    return getSelected(what).toArray(new String[0]);
  }

  public int getSelectedNumber(int what) {
    return getSelected(what).size();
  }

  public PanelLine[] getSelectedLines(int what) {
    Collection<PanelLine> en = getCache(what).values();
    PanelLine[] valret = en.toArray(new PanelLine[en.size()]);
    Arrays.sort(valret, new Comparator<PanelLine>() {
      @Override
      public int compare(PanelLine o1, PanelLine o2) {
        return o1.m_Values[0].toUpperCase().compareTo(
            o2.m_Values[0].toUpperCase());
      }
    });
    return valret;
  }

  protected Map<String, PanelLine> getCache(int what) {
    if (what == CM_SET) {
      return m_SetCache;
    } else if (what == CM_ELEMENT) {
      return m_ElementCache;
    } else {
      return null;
    }
  }

  protected Set<String> getSelected(int what) {
    if (what == CM_SET) {
      return m_SelectedSets;
    } else if (what == CM_ELEMENT) {
      return m_SelectedElements;
    } else {
      return null;
    }
  }
}
