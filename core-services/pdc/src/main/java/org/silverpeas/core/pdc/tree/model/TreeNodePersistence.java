/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.pdc.tree.model;

import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBean;
import org.silverpeas.kernel.annotation.NonNull;

@SuppressWarnings("deprecation")
public class TreeNodePersistence extends SilverpeasBean implements java.io.Serializable {

  private static final long serialVersionUID = 4149873143955281302L;
  private String treeId;
  private String name;
  private String description;
  private String creationDate;
  private String creatorId;
  private String path;
  private int levelNumber;
  private int orderNumber;
  private String fatherId;
  private String lang = null;

  @Override
  @NonNull
  protected String getTableName() {
    return new TreeNodePK(treeId).getTableName();
  }

  public String getTreeId() {
    return this.treeId;
  }

  public void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  public String getName() {
    return (this.name);
  }

  public String getDescription() {
    return (this.description);
  }

  public String getCreationDate() {
    return (this.creationDate);
  }

  public String getCreatorId() {
    return (this.creatorId);
  }

  public String getPath() {
    return (this.path);
  }

  public int getLevelNumber() {
    return (this.levelNumber);
  }

  public int getOrderNumber() {
    return (this.orderNumber);
  }

  public String getFatherId() {
    return (this.fatherId);
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setCreationDate(String date) {
    this.creationDate = date;
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setLevelNumber(int levelNumber) {
    this.levelNumber = levelNumber;
  }

  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  public String getLang() {
    return this.lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String toString() {
    return "(pk = " + getPK().toString() + ", treeId = " + treeId + ", name = "
        + getName() + ", path = " + getPath() + ", levelNumber = "
        + getLevelNumber() + ", fatherId = " + getFatherId() + ")";
  }

}
