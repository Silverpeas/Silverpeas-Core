/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.mvc.controller;

import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;

/**
 * Built by the main session controller the ComponentContext objects store the context of a component
 * instance : space, user, ... Used by the abstract component session controllers.
 * @author nicolas and didier
 */
public class ComponentContext {

  private String curSpaceName;
  private String curSpaceId;
  private String curCompoId;
  private String curCompoName;
  private String[] curProfiles;

  ComponentContext() {
    curSpaceName = "";
    curSpaceId = "";
    curCompoId = "";
    curCompoName = "";
    curProfiles = ArrayUtil.emptyStringArray();
  }

  public void setCurrentSpaceName(String currentSpaceName) {
    curSpaceName = StringUtil.defaultStringIfNotDefined(currentSpaceName);
  }

  public String getCurrentSpaceName() {
    return curSpaceName;
  }

  public void setCurrentSpaceId(String currentSpaceId) {
    curSpaceId = StringUtil.defaultStringIfNotDefined(currentSpaceId);
  }

  public String getCurrentSpaceId() {
    return curSpaceId;
  }

  public void setCurrentComponentId(String clientComponentId) {
    curCompoId = StringUtil.defaultStringIfNotDefined(clientComponentId);
  }

  public String getCurrentComponentId() {
    return curCompoId;
  }

  public void setCurrentComponentName(String currentComponentName) {
    curCompoName = StringUtil.defaultStringIfNotDefined(currentComponentName);
  }

  public String getCurrentComponentName() {
    return curCompoName;
  }

  public String[] getCurrentProfile() {
    return curProfiles;
  }

  void setCurrentProfile(String[] asCurrentProfile) {
    if (asCurrentProfile != null) {
      curProfiles = asCurrentProfile.clone();
    } else {
      curProfiles = ArrayUtil.emptyStringArray();
    }
  }
}