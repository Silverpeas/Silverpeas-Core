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
package com.stratelia.silverpeas.containerManager;

import java.util.*;

/**
 * This is the data structure that the container JSP is going to use (built by the container router)
 */
public class ContainerWorkspace {
  private List asContainerUserRoles = null; // container roles for the logged
  // user
  private List asContentUserRoles = null; // content roles for the logged user
  private List auContentURLIcones = null; // URLIcones of the content
  private List alSilverContents = null; // List of SilverContent to display

  public ContainerWorkspace() {
  }

  public void setContainerUserRoles(List asGivenContainerUserRoles) {
    asContainerUserRoles = asGivenContainerUserRoles;
  }

  public List getContainerUserRoles() {
    return asContainerUserRoles;
  }

  public void setContentUserRoles(List asGivenContentUserRoles) {
    asContentUserRoles = asGivenContentUserRoles;
  }

  public List getContentUserRoles() {
    return asContentUserRoles;
  }

  public void setContentURLIcones(List auGivenContentURLIcones) {
    auContentURLIcones = auGivenContentURLIcones;
  }

  public List getContentURLIcones() {
    return auContentURLIcones;
  }

  public void setSilverContents(List alGivenSilverContents) {
    alSilverContents = alGivenSilverContents;
  }

  public List getSilverContents() {
    return alSilverContents;
  }

}
