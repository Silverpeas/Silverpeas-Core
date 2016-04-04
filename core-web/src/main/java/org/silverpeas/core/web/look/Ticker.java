package org.silverpeas.core.web.look;

import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.util.SettingBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class Ticker {

  private String label = "";
  private Map<String, String> params = new HashMap<String, String>();
  private List<TickerItem> items;
  private boolean manager = false;
  private boolean linkOnItem = false;

  public Ticker(List<PublicationDetail> pubs, SettingBundle settings) {
    items = new ArrayList<>();
    for (PublicationDetail pub : pubs) {
      items.add(new TickerItem(pub));
    }

    for (String key : settings.keySet()) {
      if (key.startsWith("ticker.plugin")) {
        String param = settings.getString(key, null);
        if (param != null) {
          params.put(key.substring(key.lastIndexOf(".")+1), param);
        }
      }
    }

    linkOnItem = settings.getBoolean("ticker.linkOnItem", false);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public String getParam(String key) {
    return params.get(key);
  }

  public List<TickerItem> getItems() {
    return items;
  }

  public void setManager(boolean manager) {
    this.manager = manager;
  }

  public boolean isManager() {
    return manager;
  }

  public boolean isLinkOnItem() {
    return linkOnItem;
  }

}
