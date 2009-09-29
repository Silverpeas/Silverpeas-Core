package com.stratelia.silverpeas.containerManager;

import java.util.*;

/**
 * This is the data structure that the container JSP is going to use (built by
 * the container router)
 * 
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
