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

package org.silverpeas.core.pdc.tree.model;

import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.pdc.pdc.model.AxisHeaderI18N;
import org.silverpeas.kernel.util.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class TreeNode extends AbstractI18NBean<TreeNodeI18N> implements java.io.Serializable {

  private static final long serialVersionUID = 4644891370102942728L;

  private TreeNodePK pk = new TreeNodePK("0");

  private String treeId = "0";
  private String creationDate = "";
  private String creatorId = "";
  @XmlAttribute(name = "pdcValuePath")
  private String path = "";
  private int levelNumber = 0;
  private int orderNumber = 0;
  private String fatherId = "0";

  @Override
  protected Class<TreeNodeI18N> getTranslationType() {
    return TreeNodeI18N.class;
  }

  public TreeNode() {
    setName("");
    setDescription("");
  }

  public TreeNode(String id, String treeId) {
    setPK(new TreeNodePK(id));
    this.treeId = treeId;
  }

  public TreeNode(TreeNodePersistence persistence) {
    this.pk = new TreeNodePK(persistence.getPK());
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

  public TreeNodePK getPK() {
    return pk;
  }

  public void setPK(TreeNodePK value) {
    pk = value;
  }

  public String getTreeId() {
    return this.treeId;
  }

  public void setTreeId(String treeId) {
    this.treeId = treeId;
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

  public boolean hasFather() {
    return StringUtil.isDefined(getFatherId()) && !"-1".equals(getFatherId());
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

  public void setTranslationsFrom(Map<String, AxisHeaderI18N> translations) {
    Map<String, TreeNodeI18N> treeNodeTranslations = new HashMap<>();
    for (Map.Entry<String, AxisHeaderI18N> entry : translations.entrySet()) {
      treeNodeTranslations.put(entry.getKey(), new TreeNodeI18N(entry.getValue()));
    }
    setTranslations(treeNodeTranslations);
  }
}
