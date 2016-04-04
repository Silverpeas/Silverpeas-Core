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

package org.silverpeas.core.persistence.jdbc.bean;

import java.io.Serializable;

import org.silverpeas.core.WAPrimaryKey;

/**
 * SilverpeasBean represents an Entity in old silverpeas persistence layer
 * @Deprecated Replaced with a model Entity from org.silverpeas.core.persistence.datasource.model package.
 */
@Deprecated
public class SilverpeasBean implements SilverpeasBeanIntf, Serializable {

  private static final long serialVersionUID = -7843189803570333207L;
  private WAPrimaryKey pk;

  public SilverpeasBean() {
    pk = new IdPK();
  }

  @Override
  public WAPrimaryKey getPK() {
    return pk;
  }

  @Override
  public void setPK(WAPrimaryKey value) {
    pk = value;
  }

  @Override
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_EJBDATASOURCE_SILVERPEAS;
  }

  @Override
  public String _getDatasourceName() {
    return null;
  }

  @Override
  public JdbcData _getJdbcData() {
    return null;
  }

  @Override
  public String _getTableName() {
    return null;
  }

  @Override
  public String getSureString(String theString) {
    return (theString == null) ? "" : theString;
  }
}
