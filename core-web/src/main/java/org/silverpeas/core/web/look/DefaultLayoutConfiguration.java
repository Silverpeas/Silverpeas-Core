package org.silverpeas.core.web.look;

import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

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
    return defaultStringIfNotDefined(headerURL,
        settings.getString("layout.header.url", "/admin/jsp/TopBarSilverpeasV5.jsp"));
  }

  @Override
  public String getBodyURL() {
    return defaultStringIfNotDefined(bodyURL,
        settings.getString("layout.body.url", "/admin/jsp/bodyPartSilverpeasV5.jsp"));
  }

  @Override
  public String getBodyNavigationURL() {
    return defaultStringIfNotDefined(bodyNavigationURL,
        settings.getString("layout.body.navigation.url", "/admin/jsp/DomainsBarSilverpeasV5.jsp"));
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
