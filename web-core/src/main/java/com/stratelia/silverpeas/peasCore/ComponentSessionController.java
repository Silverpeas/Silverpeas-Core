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
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @author nicolas eysseric et didier wenzek
 * @version 1.0
 */

package com.stratelia.silverpeas.peasCore;

import com.silverpeas.util.clipboard.ClipboardSelection;
import java.util.List;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBm;
import java.util.Collection;

/**
 * The interface for all component session controllers.
 */

public interface ComponentSessionController {
  /** Return the organizationController */
  public OrganizationController getOrganizationController();

  /** Return the user language */
  public String getLanguage();

  /** Return the UserDetail of the current user */
  public UserDetail getUserDetail();

  /** Return the UserId of the current user */
  public String getUserId();

  /** Return the space label (as known by the user) */
  public String getSpaceLabel();

  /** Return the space id */
  public String getSpaceId();

  /** Return the name of the component (as specified in the xmlComponent) */
  public String getComponentName();

  /** Return the component label (as known by the user) */
  public String getComponentLabel();

  /** Return the component id */
  public String getComponentId();

  /** Return the parameters for this component instance */
  public List getComponentParameters();

  /**
   * Return the parameter value for this component instance and the given parameter name
   */
  public String getComponentParameterValue(String parameterName);

  public String[] getUserAvailComponentIds();

  public String[] getUserAvailSpaceIds();

  public String getComponentUrl();

  /** Return the name of the user's roles */
  public String[] getUserRoles();

  /** Return the higher user's role (admin, publisher or user) */
  public String getUserRoleLevel();

  public PersonalizationBm getPersonalization();

  public ResourceLocator getMultilang();

  public ResourceLocator getIcon();

  public ResourceLocator getSettings();

  // Maintenance Mode
  public boolean isAppInMaintenance();

  public void setAppModeMaintenance(boolean mode);

  public boolean isSpaceInMaintenance(String spaceId);

  public void setSpaceModeMaintenance(String spaceId, boolean mode);

  public Collection getClipboardSelectedObjects() throws Exception;

  public String getClipboardErrorMessage() throws Exception;

  public Exception getClipboardExceptionError() throws Exception;

  public Collection getClipboardObjects() throws Exception;

  public void addClipboardSelection(ClipboardSelection selection) throws Exception;

  public String getClipboardName() throws Exception;

  public Integer getClipboardCount() throws Exception;

  public void clipboardPasteDone() throws Exception;

  public void setClipboardSelectedElement(int index, boolean selected) throws Exception;

  public int getClipboardSize() throws Exception;

  public void removeClipboardElement(int index) throws Exception;

  public void setClipboardError(String messageId, Exception ex) throws Exception;

}
