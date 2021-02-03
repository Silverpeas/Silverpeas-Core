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
package org.silverpeas.core.admin.persistence;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;

@Repository
public class OrganizationSchema {

  @Inject
  private DomainTable domain;
  @Inject
  private KeyStoreTable keyStore;
  @Inject
  private SpaceTable space;
  @Inject
  private SpaceI18NTable spaceI18N;
  @Inject
  private ComponentInstanceTable instance;
  @Inject
  private ComponentInstanceI18NTable instanceI18N;
  @Inject
  private InstanceDataTable instanceData;
  @Inject
  private UserRoleTable userRole;
  @Inject
  private SpaceUserRoleTable spaceUserRole;
  @Inject
  private AccessLevelTable accessLevel;
  @Inject
  private GroupUserRoleTable groupUserRole;

  public static OrganizationSchema get() {
    return ServiceProvider.getSingleton(OrganizationSchema.class);
  }

  protected OrganizationSchema() {

  }

  public DomainTable domain() {
    return domain;
  }

  public KeyStoreTable keyStore() {
    return keyStore;
  }

  public SpaceTable space() {
    return space;
  }

  public SpaceI18NTable spaceI18N() {
    return spaceI18N;
  }

  public ComponentInstanceTable instance() {
    return instance;
  }

  public ComponentInstanceI18NTable instanceI18N() {
    return instanceI18N;
  }

  public InstanceDataTable instanceData() {
    return instanceData;
  }

  public UserRoleTable userRole() {
    return userRole;
  }

  public SpaceUserRoleTable spaceUserRole() {
    return spaceUserRole;
  }

  public AccessLevelTable accessLevel() {
    return accessLevel;
  }

  public GroupUserRoleTable groupUserRole() {
    return groupUserRole;
  }
}
