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

package com.stratelia.silverpeas.containerManager;

import java.util.List;

import com.stratelia.silverpeas.contentManager.SilverContentInterface;

/**
 * This is the data structure that the container JSP is going to use (built by the container router)
 */
public class ContainerWorkspace {
  private List<String> asContainerUserRoles = null; // container roles for the logged user
  private List<String> asContentUserRoles = null; // content roles for the logged user
  private List<URLIcone> auContentURLIcones = null; // URLIcones of the content
  private List<SilverContentInterface> alSilverContents = null; // List of SilverContent to display

  public ContainerWorkspace() {
  }

  public void setContainerUserRoles(List<String> asGivenContainerUserRoles) {
    asContainerUserRoles = asGivenContainerUserRoles;
  }

  public List<String> getContainerUserRoles() {
    return asContainerUserRoles;
  }

  public void setContentUserRoles(List<String> asGivenContentUserRoles) {
    asContentUserRoles = asGivenContentUserRoles;
  }

  public List<String> getContentUserRoles() {
    return asContentUserRoles;
  }

  public void setContentURLIcones(List<URLIcone> auGivenContentURLIcones) {
    auContentURLIcones = auGivenContentURLIcones;
  }

  public List<URLIcone> getContentURLIcones() {
    return auContentURLIcones;
  }

  public void setSilverContents(List<SilverContentInterface> alGivenSilverContents) {
    alSilverContents = alGivenSilverContents;
  }

  public List<SilverContentInterface> getSilverContents() {
    return alSilverContents;
  }

}
