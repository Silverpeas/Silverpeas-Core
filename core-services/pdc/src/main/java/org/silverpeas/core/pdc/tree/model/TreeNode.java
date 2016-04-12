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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.pdc.tree.model;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderI18N;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.WAPrimaryKey;

import java.util.HashMap;
import java.util.Map;

/**
 * Class declaration
 * @author
 */
public class TreeNode extends AbstractI18NBean<TreeNodeI18N> implements java.io.Serializable {

  private static final long serialVersionUID = 4644891370102942728L;

  private WAPrimaryKey pk;

  private String treeId;
  private String creationDate;
  private String creatorId;
  private String path;
  private int levelNumber;
  private int orderNumber;
  private String fatherId;

  /**
   * Constructor declaration
   * @see
   */
  public TreeNode() {
    init("0", "", "", "", "", "", "", 0, 0, "0");
  }

  /**
   * Method declaration
   * @param id
   * @param name
   * @param description
   * @param creationDate
   * @param creatorId
   * @param path
   * @param levelNumber
   * @param fatherId
   * @see
   */
  private void init(String id, String treeId, String name, String description,
      String creationDate, String creatorId, String path, int levelNumber,
      int orderNumber, String fatherId) {
    setPK(new TreeNodePK(id));
    this.treeId = treeId;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.levelNumber = levelNumber;
    this.fatherId = fatherId;
    this.orderNumber = orderNumber;
  }

  /**
   * Constructor declaration
   * @param pk
   * @param name
   * @param description
   * @param creationDate
   * @param creatorId
   * @param path
   * @param levelNumber
   * @param fatherId
   * @see
   */
  public TreeNode(TreeNodePK pk, String treeId, String name,
      String description, String creationDate, String creatorId, String path,
      int levelNumber, int orderNumber, String fatherId) {
    setPK(pk);
    this.treeId = treeId;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.creatorId = creatorId;
    this.path = path;
    this.levelNumber = levelNumber;
    this.orderNumber = orderNumber;
    this.fatherId = fatherId;
  }

  /**
   * Constructor declaration
   * @param id
   * @param name
   * @param description
   * @param creationDate
   * @param creatorId
   * @param path
   * @param levelNumber
   * @param fatherId
   * @see
   */
  public TreeNode(String id, String treeId, String name, String description,
      String creationDate, String creatorId, String path, int levelNumber,
      int orderNumber, String fatherId) {
    init(id, treeId, name, description, creationDate, creatorId, path,
        levelNumber, orderNumber, fatherId);
  }

  public TreeNode(TreeNodePersistence persistence) {
    this.pk = persistence.getPK();
    this.treeId = persistence.getTreeId();
    setName(persistence.getName());
    setDescription(persistence.getDescription());
    this.creationDate = persistence.getCreationDate();
    this.creatorId = persistence.getCreatorId();
    this.path = persistence.getPath();
    this.levelNumber = persistence.getLevelNumber();
    this.orderNumber = persistence.getOrderNumber();
    this.fatherId = persistence.getFatherId();
    setLanguage(persistence.getLang());
  }

  public WAPrimaryKey getPK() {
    return pk;
  }

  public void setPK(WAPrimaryKey value) {
    pk = value;
  }

  public String getTreeId() {
    return this.treeId;
  }

  public void setTreeId(String treeId) {
    this.treeId = treeId;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getCreationDate() {
    return (this.creationDate);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getCreatorId() {
    return (this.creatorId);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getPath() {
    return (this.path);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getLevelNumber() {
    return (this.levelNumber);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getOrderNumber() {
    return (this.orderNumber);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getFatherId() {
    return (this.fatherId);
  }

  public boolean hasFather() {
    return StringUtil.isDefined(getFatherId()) && !getFatherId().equals("-1");
  }

  /**
   * Method declaration
   * @param date
   * @see
   */
  public void setCreationDate(String date) {
    this.creationDate = date;
  }

  /**
   * Method declaration
   * @param creatorId
   * @see
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * Method declaration
   * @param fatherId
   * @see
   */
  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  /**
   * Method declaration
   * @param path
   * @see
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Method declaration
   * @param levelNumber
   * @see
   */
  public void setLevelNumber(int levelNumber) {
    this.levelNumber = levelNumber;
  }

  /**
   * Method declaration
   * @param orderNumber
   * @see
   */
  public void setOrderNumber(int orderNumber) {
    this.orderNumber = orderNumber;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(pk = " + getPK().toString() + ", treeId = " + treeId + ", name = "
        + getName() + ", path = " + getPath() + ", levelNumber = "
        + getLevelNumber() + ", fatherId = " + getFatherId() + ")";
  }

  /**
   * determine the connection type to the database
   */
  public int _getConnectionType() {
    return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
  }

  public void setTranslationsFrom(Map<String, AxisHeaderI18N> translations) {
    Map<String, TreeNodeI18N> treeNodeTranslations = new HashMap<String, TreeNodeI18N>();
    for (Map.Entry<String, AxisHeaderI18N> entry : translations.entrySet()) {
      treeNodeTranslations.put(entry.getKey(), new TreeNodeI18N(entry.getValue()));
    }
    setTranslations(treeNodeTranslations);
  }
}
