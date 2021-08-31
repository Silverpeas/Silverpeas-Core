/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.mylinks.model;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.security.Securable;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.util.StringUtil;

import java.io.Serializable;
import java.util.Collection;

public class LinkDetail implements Serializable, Securable {

  private static final long serialVersionUID = 1841282101128766762L;
  private int linkId;
  private int position;
  private boolean hasPosition;
  private String name;
  private String description;
  private String url;
  private boolean visible;
  private boolean popup;
  private String userId;
  private String instanceId;
  private String objectId;

  public LinkDetail() {
  }

  public LinkDetail(String name, String description, String url, boolean visible, boolean popup) {
    this.name = name;
    this.description = description;
    this.url = url;
    this.visible = visible;
    this.popup = popup;
  }

  /**
   * Copy constructor.
   * @param other the instance to copy.
   */
  public LinkDetail(final LinkDetail other) {
    this.linkId = other.linkId;
    this.position = other.position;
    this.hasPosition = other.hasPosition;
    this.name = other.name;
    this.description = other.description;
    this.url = other.url;
    this.visible = other.visible;
    this.popup = other.popup;
    this.userId = other.userId;
    this.instanceId = other.instanceId;
    this.objectId = other.objectId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getLinkId() {
    return linkId;
  }

  public void setLinkId(int linkId) {
    this.linkId = linkId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isPopup() {
    return popup;
  }

  public void setPopup(boolean popup) {
    this.popup = popup;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public boolean hasPosition() {
    return hasPosition;
  }

  public void setHasPosition(boolean hasPosition) {
    this.hasPosition = hasPosition;
  }

  @Override
  public boolean canBeAccessedBy(final User user) {
    boolean canBeAccessed;
    if (StringUtil.isDefined(instanceId)) {
      SilverpeasComponentInstance componentInstance =
          SilverpeasComponentInstance.getById(instanceId).orElse(null);
      canBeAccessed = componentInstance != null &&
          (componentInstance.isPublic() || componentInstance.isPersonal() ||
              ComponentAccessControl.get().isUserAuthorized(user.getId(), instanceId));
    } else {
      canBeAccessed = true;
    }
    return canBeAccessed;
  }

  @Override
  public boolean canBeModifiedBy(final User user) {
    boolean canBeModified;
    if (StringUtil.isDefined(instanceId)) {
      // it's a link associated to a component
      // check if current user is admin of component
      Collection<SilverpeasRole> roles = OrganizationController.get()
          .getUserSilverpeasRolesOn(User.getCurrentRequester(), instanceId);
      canBeModified = roles.contains(SilverpeasRole.ADMIN);
    } else {
      canBeModified = user.getId().equals(userId);
    }
    return canBeModified;
  }
}