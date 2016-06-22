package org.silverpeas.core.web.look;

import org.silverpeas.core.util.SettingBundle;

/**
 * @author Nicolas Eysseric
 */
public class DefaultLayoutConfiguration implements LayoutConfiguration {

  private SettingBundle settings;
  private String headerURL;
  private String bodyURL;
  private String bodyNavigationURL;

  public DefaultLayoutConfiguration(SettingBundle settings) {
    this.settings = settings;
  }

  @Override
  public String getHeaderURL() {
    return headerURL;
  }

  @Override
  public String getBodyURL() {
    return bodyURL;
  }

  @Override
  public String getBodyNavigationURL() {
    return bodyNavigationURL;
  }

  public void setHeaderURL(String url) {
    headerURL = url;
  }

  public void setBodyURL(String url) {
    bodyURL = url;
  }

  public void setBodyNavigationURL(String url) {
    bodyNavigationURL = url;
  }
}
