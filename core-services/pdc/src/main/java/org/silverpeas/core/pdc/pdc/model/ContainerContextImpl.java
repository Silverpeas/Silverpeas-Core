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

package org.silverpeas.core.pdc.pdc.model;

import java.util.*;

import org.silverpeas.core.contribution.contentcontainer.container.ContainerContext;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerInterface;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerPeas;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerPositionInterface;
import org.silverpeas.core.contribution.contentcontainer.container.URLIcone;
import org.silverpeas.core.silvertrace.*;
import org.silverpeas.core.contribution.contentcontainer.container.*;

/**
 * This is the data structure that the content JSP is going to use (built by the container router)
 */
public class ContainerContextImpl implements ContainerContext, java.io.Serializable {

  private static final long serialVersionUID = 8684263472892754224L;
  private int nContainerInstanceId = -1; // The instance of the container on
  // which the content is going to use
  private String sReturnURL = null; // URL to get back on the container
  // private String BrowseBar = null; //implement a BrowseBar object
  private URLIcone uClassifyURLIcone = null; // URL on the JSP to add classify
  // positions on the container
  // private URLIcone uGenericClassifyURLIcone = null; // URL on the JSP to add
  // classify positions across containers
  private ContainerPositionInterface curPosition = null; // Current position in
  // the container
  private ContainerPeas containerPeas = null; // ContainerPeas

  public ContainerContextImpl() {
  }

  public void setContainerInstanceId(int nGivenContainerInstanceId) {
    nContainerInstanceId = nGivenContainerInstanceId;
  }

  public int getContainerInstanceId() {
    return nContainerInstanceId;
  }

  public void setReturnURL(String sGivenReturnURL) {
    sReturnURL = sGivenReturnURL;
  }

  public String getReturnURL() {
    return sReturnURL;
  }

  public void setClassifyURLIcone(URLIcone uGivenClassifyURLIcone) {
    uClassifyURLIcone = uGivenClassifyURLIcone;
  }

  public URLIcone getClassifyURLIcone() {
    return uClassifyURLIcone;
  }

  public ContainerPositionInterface getContainerPositionInterface() {
    return curPosition;
  }

  public void setContainerPositionInterface(
      ContainerPositionInterface GivenPosition) {
    curPosition = GivenPosition;
  }

  public void setContainerPeas(ContainerPeas givenContainerPeas) {
    containerPeas = givenContainerPeas;
  }

  // -------------------------------------------------------------------------------
  // METHODS OF THE INTERFACE
  // -------------------------------------------------------------------------------

  /*
   * Get the classify URL with parameters to put as link on the Classify Icone
   */
  public String getClassifyURLWithParameters(String sComponentId,
      String sSilverContentId) {
    try {
      return uClassifyURLIcone.getActionURL()
          + "?"
          + containerPeas.getContainerInterface().getCallParameters(
          sComponentId, sSilverContentId);
    } catch (Exception e) {
      SilverTrace.error("containerManager",
          "ContainerContext.getClassifyURLWithParameters",
          "root.MSG_GEN_PARAM_VALUE", "Fatal Error", e);
      return null;
    }
  }

  /*
   * Find the SearchContext for the given SilverContentId
   */
  public ContainerPositionInterface getSilverContentIdSearchContext(
      int nSilverContentId, String sComponentId) {
    try {
      ContainerInterface ci = containerPeas.getContainerInterface();
      return ci.getSilverContentIdSearchContext(nSilverContentId, sComponentId);
    } catch (Exception e) {
      SilverTrace.error("containerManager",
          "ContainerContext.getSilverContentIdPositions",
          "root.MSG_GEN_PARAM_VALUE", "Fatal Error", e);
      return null;
    }
  }

  /*
   * Get All the SilverContentIds corresponding to the given position in the given Components
   */
  public List<Integer> getSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List<String> alComponentIds) {
    try {
      ContainerInterface ci = containerPeas.getContainerInterface();
      return ci
          .findSilverContentIdByPosition(containerPosition, alComponentIds);
    } catch (Exception e) {
      SilverTrace.error("containerManager",
          "ContainerContext.getSilverContentIdByPosition",
          "root.MSG_GEN_PARAM_VALUE", "Fatal Error", e);
      return null;
    }
  }
}
