/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.beans.admin;

import com.silverpeas.util.ArrayUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import java.io.Serializable;

public class Group implements Serializable, Comparable<Group> {

  private static final long serialVersionUID = 4430574302630237352L;
  private String id = null;
  private String specificId = null;
  private String domainId = null;
  private String superGroupId = null;
  private String name = "";
  private String description = "";
  private String rule = null;
  private String[] m_sUserIds = ArrayUtil.EMPTY_STRING_ARRAY;

  private int nbUsers = -1;

  /**
   * Constructor
   */
  public Group() {

  }

  public Group(Group toClone) {
    id = toClone.id;
    specificId = toClone.specificId;
    domainId = toClone.domainId;
    superGroupId = toClone.superGroupId;
    name = toClone.name;
    description = toClone.description;
    m_sUserIds = toClone.m_sUserIds;
    rule = toClone.rule;
  }

  /**
   * Get the group id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the group id
   */
  public void setId(String newId) {
    this.id = newId;
  }

  /**
   * Get the group specific id
   */
  public String getSpecificId() {
    return specificId;
  }

  /**
   * Set the group specific id
   */
  public void setSpecificId(String newSpecificId) {
    this.specificId = newSpecificId;
  }

  /**
   * Get the domain id where the group is stored
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Set the domain id where the group is stored
   */
  public void setDomainId(String newDomainId) {
    this.domainId = newDomainId;
  }

  /**
   * Get the father group id
   */
  public String getSuperGroupId() {
    return superGroupId;
  }

  /**
   * Set the father group id
   */
  public void setSuperGroupId(String newSuperGroupId) {
    this.superGroupId = newSuperGroupId;
  }

  /**
   * Get the group name
   */
  public String getName() {
    return name;
  }

  /**
   * Set the group name
   */
  public void setName(String newName) {
    this.name = newName;
  }

  /**
   * Get the group description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the group description
   */
  public void setDescription(String newDescription) {
    if (newDescription != null) {
      this.description = newDescription;
    } else {
      this.description = "";
    }
  }

  /**
   * Set the list of users in the group
   */
  public void setUserIds(String[] sUserIds) {
    m_sUserIds = ArrayUtil.nullToEmpty(sUserIds);
  }

  /**
   * Get the list of users in the group
   */
  public String[] getUserIds() {
    return m_sUserIds;
  }

  /**
   * Trace the group's values
   */
  public void traceGroup() {
    int i;

    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "id : " + id);
    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "specificId : " + specificId);
    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "domainId : " + domainId);
    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "superGroupId : " + superGroupId);
    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "name : " + name);
    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "description : " + description);
    SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
        "rule : " + rule);
    for (i = 0; i < m_sUserIds.length; i++) {
      SilverTrace.info("admin", "Group.traceGroup", "admin.MSG_DUMP_GROUP",
          "userId " + Integer.toString(i) + " : " + m_sUserIds[i]);
    }
  }

  public int compareTo(Group o) {
    return (getName().toLowerCase()).compareTo(o.getName().toLowerCase());
  }

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }

  public boolean isSynchronized() {
    return (rule != null && rule.trim().length() > 0);
  }

  public int getNbUsers() {
    if (nbUsers == -1) {
      return getUserIds().length;
    }
    return nbUsers;
  }

  public void setNbUsers(int nbUsers) {
    this.nbUsers = nbUsers;
  }

}
