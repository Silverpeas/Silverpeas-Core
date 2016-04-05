/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @author nicolas et didier
 * @version 1.0
 */
package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.util.ArrayUtil;

/**
 * Built by the main sesion controller the ComponentContext objects store the context of a component
 * instance : space, user, ... Used by the abstract component session controllers.
 */
public class ComponentContext {

  private String m_sCurSpaceName;
  private String m_sCurSpaceId;
  private String m_sCurCompoId;
  private String m_sCurCompoName;
  private String m_sCurCompoLabel;
  private String[] m_asCurProfile;

  ComponentContext() {
    m_sCurSpaceName = "";
    m_sCurSpaceId = "";
    m_sCurCompoId = "";
    m_sCurCompoName = "";
    m_sCurCompoLabel = "";
    m_asCurProfile = ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public void setCurrentSpaceName(String CurrentSpaceName) {
    if (CurrentSpaceName != null) {
      m_sCurSpaceName = CurrentSpaceName;
    } else {
      m_sCurSpaceName = "";
    }
  }

  public String getCurrentSpaceName() {
    return m_sCurSpaceName;
  }

  public void setCurrentSpaceId(String CurrentSpaceId) {
    if (CurrentSpaceId != null) {
      m_sCurSpaceId = CurrentSpaceId;
    } else {
      m_sCurSpaceId = "";
    }
  }

  public String getCurrentSpaceId() {
    return m_sCurSpaceId;
  }

  public void setCurrentComponentId(String sClientComponentId) {
    if (sClientComponentId != null) {
      m_sCurCompoId = sClientComponentId;
    } else {
      m_sCurCompoId = "";
    }
  }

  public String getCurrentComponentId() {
    return m_sCurCompoId;
  }

  public void setCurrentComponentName(String sCurrentComponentName) {
    if (sCurrentComponentName != null) {
      m_sCurCompoName = sCurrentComponentName;
    } else {
      m_sCurCompoName = "";
    }
  }

  public String getCurrentComponentName() {
    return m_sCurCompoName;
  }

  public void setCurrentComponentLabel(String sCurrentComponentLabel) {
    if (sCurrentComponentLabel != null) {
      m_sCurCompoLabel = sCurrentComponentLabel;
    } else {
      m_sCurCompoLabel = "";
    }
  }

  public String getCurrentComponentLabel() {
    return m_sCurCompoLabel;
  }

  public void setCurrentProfile(String[] asCurrentProfile) {
    if (asCurrentProfile != null) {
      m_asCurProfile = asCurrentProfile.clone();
    } else {
      m_asCurProfile = ArrayUtil.EMPTY_STRING_ARRAY;
    }
  }

  public String[] getCurrentProfile() {
    return m_asCurProfile;
  }
}
