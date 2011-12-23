/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;
import java.sql.Connection;

public class OrganizationSchema extends Schema {

  public OrganizationSchema() throws UtilException {
    super();
    init();
  }
  
  public OrganizationSchema(Connection connection) throws UtilException {
    super(connection);
    init();
  }
  
  @Override
  protected String getJNDIName() {
    return JNDINames.ADMIN_DATASOURCE;
  }
  
  public final void init() {
    domain = new DomainTable(this);
    keyStore = new KeyStoreTable(this);
    user = new UserTable(this);
    group = new GroupTable(this);
    space = new SpaceTable(this);
    spaceI18N = new SpaceI18NTable(this);
    instance = new ComponentInstanceTable(this);
    instanceI18N = new ComponentInstanceI18NTable(this);
    instanceData = new InstanceDataTable(this);
    userRole = new UserRoleTable(this);
    spaceUserRole = new SpaceUserRoleTable(this);
    accessLevel = new AccessLevelTable(this);
    groupUserRole = new GroupUserRoleTable(this);
  }
  public DomainTable domain = null;
  public KeyStoreTable keyStore = null;
  public UserTable user = null;
  public GroupTable group = null;
  public SpaceTable space = null;
  public SpaceI18NTable spaceI18N = null;
  public ComponentInstanceTable instance = null;
  public ComponentInstanceI18NTable instanceI18N = null;
  public InstanceDataTable instanceData = null;
  public UserRoleTable userRole = null;
  public SpaceUserRoleTable spaceUserRole = null;
  public AccessLevelTable accessLevel = null;
  public GroupUserRoleTable groupUserRole = null;
}
