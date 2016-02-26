package com.silverpeas.look;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.EnumerationUtils;

import org.silverpeas.util.SettingBundle;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.ResourceLocator;

public class TickerSettings {

  private String label = "";
  private Map<String, String> params = new HashMap<String, String>();
  private boolean linkOnItem = false;
  private int refreshDelay = 60;

  public TickerSettings(SettingBundle settings) {
    for (String key : settings.keySet()) {
      if (key.startsWith("ticker.plugin")) {
        String param = settings.getString(key, null);
        if (param != null) {
          params.put(key.substring(key.lastIndexOf(".")+1), param);
        }
      }
    }
    linkOnItem = settings.getBoolean("ticker.linkOnItem", false);
    refreshDelay = settings.getInteger("ticker.autocheck.delay", 60);
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public String getParam(String key, String defaultValue) {
    if (!StringUtil.isDefined(params.get(key))) {
      return defaultValue;
    }
    return params.get(key);
  }

  public boolean isLinkOnItem() {
    return linkOnItem;
  }

  public int getRefreshDelay() {
    return refreshDelay;
  }

}