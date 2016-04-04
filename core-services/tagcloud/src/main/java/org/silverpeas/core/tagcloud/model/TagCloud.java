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

package org.silverpeas.core.tagcloud.model;

import java.io.Serializable;

public class TagCloud implements Serializable {

  private static final long serialVersionUID = -4006964363195304072L;
  /**
   * Elements types which can contains tagclouds.
   */
  public static final int TYPE_UNDEFINED = -1;
  public static final int TYPE_PUBLICATION = 0;
  public static final int TYPE_FORUM = 1;
  public static final int TYPE_MESSAGE = 2;

  /**
   * Default values.
   */
  private static final int DEFAULT_ID = -1;
  private static final int DEFAULT_COUNT = 1;

  // id : ID of the tagcloud.
  private int id;
  // tag : used as a key to retrieve tagclouds which have the same key.
  // Corresponds to the label
  // of the tagcloud which is uppercased and without accents or special
  // characters.
  private String tag;
  // label : label displayed for the tagcloud.
  private String label;
  // instanceId : id of the component (topic) which the tagcloud refers to.
  private String instanceId;
  // externalId : id of the element (publication or forum) which the tagcloud
  // refers to.
  private String externalId;
  // externalType : type of the element.
  private int externalType;
  // count : number of occurrences of the tag for the element which the tagcloud
  // describes (topic, publication).
  private int count;

  private void init(int id, String tag, String label, String instanceId,
      String externalId, int externalType, int count) {
    setId(id);
    setTag(tag);
    setLabel(label);
    setInstanceId(instanceId);
    setExternalId(externalId);
    setExternalType(externalType);
    setCount(count);
  }

  public TagCloud(int id, String tag, String label, String instanceId,
      String externalId, int externalType) {
    init(id, tag, label, instanceId, externalId, externalType, DEFAULT_COUNT);
  }

  public TagCloud(String tag, String label, String instanceId,
      String externalId, int externalType) {
    init(DEFAULT_ID, tag, label, instanceId, externalId, externalType,
        DEFAULT_COUNT);
  }

  public TagCloud(String instanceId, String externalId, int externalType) {
    init(DEFAULT_ID, null, null, instanceId, externalId, externalType,
        DEFAULT_COUNT);
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getTag() {
    return tag;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalType(int externalType) {
    this.externalType = externalType;
  }

  public int getExternalType() {
    return externalType;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getCount() {
    return count;
  }

  public void incrementCount() {
    count++;
  }

  public String toString() {
    return new StringBuffer().append("id           = ").append(getId()).append(
        ",\n").append("tag          = ").append(getTag()).append(",\n").append(
        "label        = ").append(getLabel()).append(",\n").append(
        "instanceId   = ").append(getInstanceId()).append(",\n").append(
        "externalId   = ").append(getExternalId()).append(",\n").append(
        "externalType = ").append(getExternalType()).append(",\n").append(
        "count        = ").append(getCount()).toString();
  }

}