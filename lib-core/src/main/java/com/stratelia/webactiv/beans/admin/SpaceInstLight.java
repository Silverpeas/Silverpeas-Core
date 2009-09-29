/*
 * Created on 29 nov. 2004
 *
 */
package com.stratelia.webactiv.beans.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.organization.SpaceRow;

/**
 * @author neysseri
 * 
 */
public class SpaceInstLight extends AbstractI18NBean implements Serializable,
    Comparable {

  private String id = null;
  private String name = null;
  private String description = null;
  private int orderNum = 0;
  private int level = 0;
  private String fatherId = null;
  private Date createDate = null;
  private Date updateDate = null;
  private Date removeDate = null;
  private String status = null;
  private int createdBy = -1;
  private int updatedBy = -1;
  private int removedBy = -1;

  private String creatorName = null;
  private String updaterName = null;
  private String removerName = null;

  private String look = null;

  private List path = null;

  public SpaceInstLight() {
    id = "";
    fatherId = "";
    name = "";
    description = "";
    orderNum = 0;
    level = 0;
  }

  public SpaceInstLight(SpaceRow spaceRow) {
    if (spaceRow != null) {
      setId(spaceRow.id);
      setName(spaceRow.name);
      setFatherId(spaceRow.domainFatherId);
      description = spaceRow.description;
      orderNum = spaceRow.orderNum;
      if (spaceRow.createTime != null)
        createDate = new Date(Long.parseLong(spaceRow.createTime));
      if (spaceRow.updateTime != null)
        updateDate = new Date(Long.parseLong(spaceRow.updateTime));
      if (spaceRow.removeTime != null)
        removeDate = new Date(Long.parseLong(spaceRow.removeTime));
      status = spaceRow.status;

      createdBy = spaceRow.createdBy;
      updatedBy = spaceRow.updatedBy;
      removedBy = spaceRow.removedBy;

      look = spaceRow.look;
    }
  }

  public SpaceInstLight(SpaceInst spaceInst) {
    if (spaceInst != null) {
      setId(spaceInst.getId());
      setName(spaceInst.getName());
      setFatherId(spaceInst.getDomainFatherId());
      description = spaceInst.getDescription();
      orderNum = spaceInst.getOrderNum();
      setLevel(spaceInst.getLevel());
      createDate = spaceInst.getCreateDate();
      updateDate = spaceInst.getUpdateDate();
      removeDate = spaceInst.getRemoveDate();
      status = spaceInst.getStatus();
      look = spaceInst.getLook();

      setTranslations(spaceInst.getTranslations());
    }
  }

  public String getShortId() {
    return id;
  }

  public String getFullId() {
    return "WA" + getShortId();
  }

  /**
   * @return
   */
  public String getFatherId() {
    return fatherId;
  }

  /**
   * @return
   */
  public int getLevel() {
    return level;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  public String getName(String language) {
    if (!I18NHelper.isI18N)
      return getName();

    SpaceI18N s = (SpaceI18N) getTranslations().get(language);
    if (s == null)
      s = (SpaceI18N) getNextTranslation();

    return s.getName();
  }

  /**
   * @param string
   */
  public void setFatherId(int fatherId) {
    this.fatherId = new Integer(fatherId).toString();
  }

  public void setFatherId(String fatherId) {
    this.fatherId = fatherId;
  }

  /**
   * @param string
   */
  public void setId(int id) {
    this.id = new Integer(id).toString();
  }

  public void setId(String id) {
    this.id = id;
  }

  /**
   * @param i
   */
  public void setLevel(int i) {
    level = i;
  }

  /**
   * @param string
   */
  public void setName(String string) {
    name = string;
  }

  public boolean isRoot() {
    return "0".equals(getFatherId());
  }

  /**
   * @return
   */
  public String getDescription() {
    return description;
  }

  /**
   * @return
   */
  public int getOrderNum() {
    return orderNum;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public Date getRemoveDate() {
    return removeDate;
  }

  public String getStatus() {
    return status;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public String getRemoverName() {
    return removerName;
  }

  public void setRemoverName(String removerName) {
    this.removerName = removerName;
  }

  public String getUpdaterName() {
    return updaterName;
  }

  public void setUpdaterName(String updaterName) {
    this.updaterName = updaterName;
  }

  public int getCreatedBy() {
    return createdBy;
  }

  public int getRemovedBy() {
    return removedBy;
  }

  public int getUpdatedBy() {
    return updatedBy;
  }

  public int compareTo(Object o) {
    return getOrderNum() - ((SpaceInstLight) o).getOrderNum();
  }

  public String getPath(String separator) {
    String sPath = "";
    if (path != null) {
      SpaceInstLight space = null;
      for (int i = 0; i < path.size(); i++) {
        if (i > 0)
          sPath += separator;

        space = (SpaceInstLight) path.get(i);
        sPath += space.getName();
      }
    }
    return sPath;
  }

  public void setPath(List path) {
    this.path = path;
  }

  public String getLook() {
    return look;
  }

  public void setLook(String look) {
    this.look = look;
  }

}