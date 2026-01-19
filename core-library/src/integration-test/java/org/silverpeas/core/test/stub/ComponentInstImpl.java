/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.stub;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of the component instance business object for integration tests that don't require
 * to dependent on the whole core admin package.
 *
 * @author mmoquillon
 */
public class ComponentInstImpl implements SilverpeasComponentInstance {

  private String spaceId;
  private String name;
  private String id;

  public static Builder builder(String componentInstanceId) {
    return new Builder(componentInstanceId);
  }

  @Override
  public String getSpaceId() {
    return spaceId;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLabel() {
    return "";
  }

  @Override
  public String getLabel(String language) {
    return "";
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public String getDescription(String language) {
    return "";
  }

  @Override
  public int getOrderPosition() {
    return 0;
  }

  @Override
  public Collection<SilverpeasRole> getSilverpeasRolesFor(User user) {
    return List.of();
  }

  @Override
  public String getId() {
    return id;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setId(String id) {
    this.id = id;
  }

  public static class Builder {

    private final ComponentInstImpl componentInst;

    public Builder(String compoInstId) {
      componentInst = new ComponentInstImpl();
      componentInst.setId(compoInstId);
    }

    public Builder setName(String name) {
      componentInst.setName(name);
      return this;
    }

    public Builder setSpaceId(String spaceId) {
      componentInst.setSpaceId(spaceId);
      return this;
    }

    public ComponentInstImpl build() {
      return componentInst;
    }
  }
}
  