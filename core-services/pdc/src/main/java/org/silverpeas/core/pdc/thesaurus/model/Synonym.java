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

package org.silverpeas.core.pdc.thesaurus.model;

import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;

/**
 * This class contains a full information about a Synonym a Synonym is linked to a Vocabulary and a
 * Value (idTree - idTerm)
 */

public class Synonym extends SilverpeasBean implements Comparable {

  private static final long serialVersionUID = -8487966625309884721L;
  private long idVoca;
  private long idTree;
  private long idTerm;
  private String name;

  public Synonym() {
  }

  public long getIdVoca() {
    return idVoca;
  }

  public void setIdVoca(long idVoca) {
    this.idVoca = idVoca;
  }

  public long getIdTree() {
    return idTree;
  }

  public void setIdTree(long idTree) {
    this.idTree = idTree;
  }

  public long getIdTerm() {
    return idTerm;
  }

  public void setIdTerm(long idTerm) {
    this.idTerm = idTerm;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int compareTo(Object voca) {
    return this.getName().compareTo(((Synonym) voca).getName());
  }

  public String _getTableName() {
    return "SB_Thesaurus_Synonym";
  }

  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }
}