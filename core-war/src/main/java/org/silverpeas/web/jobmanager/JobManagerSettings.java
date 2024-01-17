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
package org.silverpeas.web.jobmanager;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Singleton;

/**
 * This class manage the information needed for job manager
 * @t.leroi
 */
@Singleton
public class JobManagerSettings {

  private boolean isKMVisible = false;
  private boolean isToolSpecificAuthentVisible = false;
  private boolean isToolWorkflowDesignerVisible = false;
  private boolean isTemplateDesignerVisible = false;
  private boolean isPortletDeployerVisible = false;

  protected JobManagerSettings() {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.jobManagerPeas.settings.jobManagerPeasSettings");

    isKMVisible = rs.getBoolean("IsKMVisible", false);
    isToolSpecificAuthentVisible = rs.getBoolean("IsToolSpecificAuthentVisible", false);
    isToolWorkflowDesignerVisible = rs.getBoolean("IsToolWorkflowDesignerVisible", false);
    isTemplateDesignerVisible = rs.getBoolean("IsTemplateDesignerVisible", false);
    isPortletDeployerVisible = rs.getBoolean("IsPortletDeployerVisible", false);
  }

  public static final JobManagerSettings get() {
    return ServiceProvider.getService(JobManagerSettings.class);
  }

  public boolean isKMVisible() {
    return isKMVisible;
  }

  public boolean isToolSpecificAuthentVisible() {
    return isToolSpecificAuthentVisible;
  }

  public boolean isToolWorkflowDesignerVisible() {
    return isToolWorkflowDesignerVisible;
  }

  public boolean isTemplateDesignerVisible() {
    return isTemplateDesignerVisible;
  }

  public boolean isPortletDeployerVisible() {
    return isPortletDeployerVisible;
  }
}
