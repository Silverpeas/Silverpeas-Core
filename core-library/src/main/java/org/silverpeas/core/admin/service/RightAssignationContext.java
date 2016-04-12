/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.admin.service;

import org.silverpeas.core.util.StringUtil;

/**
 * This class permits to specify the context of a right assignation operations (copy or replace for
 * now).<br/>
 * It simplifies significantly all the method definition that perform right assignation operations.
 * @author Yohann Chastagnier
 */
public class RightAssignationContext {

  public enum MODE {
    COPY, REPLACE
  }

  enum RESOURCE_TYPE {
    USER, GROUP
  }

  private MODE mode = MODE.COPY;
  private String sourceId;
  private RESOURCE_TYPE sourceType;
  private String targetId;
  private RESOURCE_TYPE targetType;

  private boolean assignObjectRights = true;
  private String author = null;

  /**
   * Initializes a right assignation context by specifying directly a copy operation.
   * @return the instance of the right assignation context.
   */
  public static RightAssignationContext copy() {
    RightAssignationContext instance = new RightAssignationContext();
    instance.setMode(MODE.COPY);
    return instance;
  }

  /**
   * Initializes a right assignation context by specifying directly a copy operation.
   * @return the instance of the right assignation context.
   */
  public static RightAssignationContext replace() {
    RightAssignationContext instance = new RightAssignationContext();
    instance.setMode(MODE.REPLACE);
    return instance;
  }

  private RightAssignationContext() {
    // Hidden constructor
  }

  /**
   * Verifies that source and target resources are defined.
   * If not, {@link IllegalArgumentException} is thrown.
   */
  public void verifySourceAndTargetAreDefined() {
    if (StringUtil.isNotDefined(sourceId) || StringUtil.isNotDefined(targetId)) {
      throw new IllegalArgumentException("source or target is not defined");
    }
  }

  /**
   * Indicates if the source and target are equal.
   * This method calls firstly {@link #verifySourceAndTargetAreDefined()} method in order to be
   * sure
   * working on defined data.
   * @return true if source and target are equal, false otherwise.
   */
  public boolean areSourceAndTargetEqual() {
    verifySourceAndTargetAreDefined();
    String sourceVerify = sourceId + "@" + sourceType;
    String targetVerify = targetId + "@" + targetType;
    return sourceVerify.equals(targetVerify);
  }

  private void setMode(final MODE mode) {
    this.mode = mode;
  }

  /**
   * Indicates the source identifier of a user.
   * @param userId the identifier of a user that represents the source of rights.
   * @return the instance of the right assignation context.
   */
  public RightAssignationContext fromUserId(final String userId) {
    setSourceId(userId, RESOURCE_TYPE.USER);
    return this;
  }

  /**
   * Indicates the source identifier of a group.
   * @param groupId the identifier of a group that represents the source of rights.
   * @return the instance of the right assignation context.
   */
  public RightAssignationContext fromGroupId(final String groupId) {
    setSourceId(groupId, RESOURCE_TYPE.GROUP);
    return this;
  }

  private void setSourceId(final String sourceId, final RESOURCE_TYPE sourceType) {
    this.sourceId = sourceId;
    this.sourceType = sourceType;
  }

  /**
   * Indicates the target identifier of a user.
   * @param userId the identifier of a user that represents the target of rights.
   * @return the instance of the right assignation context.
   */
  public RightAssignationContext toUserId(final String userId) {
    setTargetId(userId, RESOURCE_TYPE.USER);
    return this;
  }

  /**
   * Indicates the target identifier of a group.
   * @param groupId the identifier of a group that represents the target of rights.
   * @return the instance of the right assignation context.
   */
  public RightAssignationContext toGroupId(final String groupId) {
    setTargetId(groupId, RESOURCE_TYPE.GROUP);
    return this;
  }

  private void setTargetId(final String targetId, final RESOURCE_TYPE targetType) {
    this.targetId = targetId;
    this.targetType = targetType;
  }

  /**
   * Indicates that component object rights must not be assigned.
   * @return the instance of the right assignation context.
   */
  public RightAssignationContext withoutAssigningComponentObjectRights() {
    this.assignObjectRights = false;
    return this;
  }

  /**
   * Sets the author behind the action.
   * @param author the identifier of a user.
   * @return the instance of the right assignation context.
   */
  public RightAssignationContext setAuthor(final String author) {
    this.author = author;
    return this;
  }

  /**
   * Gets the operation mode (copy or replace for now)
   * @return the mode.
   */
  public MODE getMode() {
    return mode;
  }

  /**
   * Gets the identifier of the resource source.
   * @return the identifier of the resource source.
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * Gets the {@link RightAssignationContext.RESOURCE_TYPE} of the resource source.
   * @return the {@link RightAssignationContext.RESOURCE_TYPE} of the resource source.
   */
  public RESOURCE_TYPE getSourceType() {
    return sourceType;
  }

  /**
   * Gets the identifier of the resource target.
   * @return the identifier of the resource target.
   */
  public String getTargetId() {
    return targetId;
  }

  /**
   * Gets the {@link RightAssignationContext.RESOURCE_TYPE} of the resource target.
   * @return the {@link RightAssignationContext.RESOURCE_TYPE} of the resource target.
   */
  public RESOURCE_TYPE getTargetType() {
    return targetType;
  }

  /**
   * Indicates if component object rights must also be assigned.
   * @return true if the component object rights must be also assigned, false otherwise.
   */
  public boolean isAssignObjectRights() {
    return assignObjectRights;
  }

  /**
   * Gets the identifier of the user behind the assignation operation.
   * @return the identifier of the user behind the assignation operation.
   */
  public String getAuthor() {
    return author;
  }
}
