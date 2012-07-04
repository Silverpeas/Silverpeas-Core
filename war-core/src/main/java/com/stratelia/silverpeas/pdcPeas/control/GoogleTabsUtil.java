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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.pdcPeas.control;

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.pdcPeas.model.GoogleSite;
import com.stratelia.silverpeas.pdcPeas.model.GoogleTab;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

public class GoogleTabsUtil {

  private static List<GoogleTab> tabs = new ArrayList<GoogleTab>();
  private static String key;
  private static boolean branding = false;
  private static boolean searchBox = false;
  private static String drawMode = "google.search.SearchControl.DRAW_MODE_LINEAR";
  private static String expandMode = "google.search.SearchControl.EXPAND_MODE_CLOSED";
  private static String css;

  static {
    try {
      ResourceLocator settings =
          new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.google", "");

      key = settings.getString("google.key");
      drawMode = settings.getString("google.sites.drawMode");
      expandMode = settings.getString("google.sites.expandMode");
      branding = settings.getBoolean("google.branding", false);
      searchBox = settings.getBoolean("google.searchBox", false);
      css = settings.getString("google.css");
      boolean enabled = settings.getBoolean("google.enable", false);

      boolean endReached = false;
      boolean endSitesReached = false;
      String tabLabel;
      GoogleTab tab;
      GoogleSite site;
      String siteLabel;
      String siteURL;
      for (int i = 1; enabled && i <= 20 && !endReached; i++) {
        // get tabs
        tabLabel = settings.getString("tab" + i + ".label");
        if (StringUtil.isDefined(tabLabel)) {
          tab = new GoogleTab(i, tabLabel);
          tabs.add(tab);

          // get sites of tab
          endSitesReached = false;
          for (int j = 1; j <= 20 && !endSitesReached; j++) {
            siteLabel = settings.getString("tab" + i + ".site" + j + ".label");
            if (StringUtil.isDefined(siteLabel)) {
              siteURL = settings.getString("tab" + i + ".site" + j + ".URL");
              site = new GoogleSite(siteLabel, siteURL);
              tab.addSite(site);
            } else {
              endSitesReached = true;
            }
          }
        } else {
          endReached = true;
        }
      }
    } catch (Exception e) {
      SilverTrace.info("pdcPeas", "GoogleTabsUtil.init", "root.MSG_GEN_PARAM_VALUE",
          "The file google.properties does not exist !");
    }
  }

  public static boolean isSearchBox() {
    return searchBox;
  }

  public static String getCss() {
    return css;
  }

  public static boolean isBranding() {
    return branding;
  }

  public static String getDrawMode() {
    return drawMode;
  }

  public static String getExpandMode() {
    return expandMode;
  }

  public static List<GoogleTab> getTabs() {
    return tabs;
  }

  public static List<GoogleSite> getSites(int tabId) {
    GoogleTab tab = tabs.get(tabId);
    return tab.getSites();
  }

  public static String getKey() {
    return key;
  }

}
