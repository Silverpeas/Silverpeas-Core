/*
 * Copyright (C) 2000 - 2020 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.cmis;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.silverpeas.core.admin.user.model.User;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Filtering parameters to apply on the characteristics of the objects to return.
 * @author mmoquillon
 */
public class Filtering {

  private final Set<String> properties = new HashSet<>();
  private boolean includeAllowableActions;
  private boolean includePathSegment;
  private boolean includeAcl;
  private IncludeCmisObjectTypes includeCmisObjectTypes = IncludeCmisObjectTypes.ALL;
  private IncludeRelationships includeRelationships = IncludeRelationships.NONE;
  private final User user = User.getCurrentRequester();
  private final String language = user.getUserPreferences().getLanguage();

  public User getCurrentUser() {
    return user;
  }

  public String getLanguage() {
    return language;
  }

  /**
   * Sets a filter on the properties of the object(s) to return.
   * @param filterExpression a filter expression.
   * @return itself.
   */
  public Filtering setPropertiesFilter(final String filterExpression) {
    if (filterExpression == null || filterExpression.trim().isEmpty()) {
      properties.clear();
      return this;
    }

    for (String s : filterExpression.split(",")) {
      s = s.trim();
      if (s.equals("*")) {
        this.properties.clear();
        return this;
      } else if (!s.isEmpty()) {
        properties.add(s);
      }
    }

    // set a few base properties
    // query name == id (for base type properties)
    properties.add(PropertyIds.OBJECT_ID);
    properties.add(PropertyIds.OBJECT_TYPE_ID);
    properties.add(PropertyIds.BASE_TYPE_ID);

    return this;
  }

  /**
   * Gets the different properties to include with the object(s) to return. By default all the
   * object(s)' properties are included.
   * @return a set of object's property identifiers that have to be set in the object(s) to return.
   * An empty list if all the object's properties have to be included with the object(s).
   */
  public Set<String> getPropertiesFilter() {
    return Collections.unmodifiableSet(properties);
  }

  /**
   * Are the actions allowable to the object(s) to be included? No by default.
   * @return true if the allowable actions have to be included with the object(s) to return. False
   * otherwise.
   */
  public boolean areAllowedActionsToBeIncluded() {
    return includeAllowableActions;
  }

  /**
   * Indicates whether the allowable actions have to be included with the object(s) to return.
   * @param includeAllowableActions a boolean indicating whether the allowable actions has to be
   * included or not.
   * @return itself.
   */
  public Filtering setIncludeAllowableActions(final boolean includeAllowableActions) {
    this.includeAllowableActions = includeAllowableActions;
    return this;
  }

  /**
   * <p>
   * A folder hierarchy MAY be represented in a canonical notation such as path. For CMIS, a path
   * is represented by:
   * </p>
   * <ul>
   *  <li>’/’ for the root folder.</li>
   *  <li>All paths start with the root folder.</li>
   *  <li>A set of the folder and object path segments separated by ’/’ in order of closest to the
   *    root.</li>
   *  <li>Folder and object path segments are speciﬁed by pathSegment tokens which can be
   *    retrieved by all services that take an includePathSegments parameter (for example
   *    getChildren).</li>
   *  <li>A pathSegment token MUST not include a ’/’ character.
   *    It is repository speciﬁc how a repository chooses the value for pathSegment.
   *    Repositories might choose to use cmis:name or content stream ﬁlename for pathSegment
   *    token.</li>
   *  <li>The pathSegment token for each item MUST uniquely identify the item in the folder.</li>
   * </ul>
   * <p>
   * That is, if folder A is under the root, and folder B is under A, then the path would be /A/B.
   * </p>
   * <p>
   * A path for an object may be calculated in the following way:
   * </p>
   * <ul>
   *  <li>If the object is the root folder, the path is ’/’.</li>
   *  <li>If the object is a direct child of the root folder, the path is the object’s pathSegment
   *    preﬁxed by ’/’.</li>
   *  <li>If the object is not a direct child of the root folder, the path is item’s parent folder
   *    cmis:path property appended by ’/’ and the object’s pathSegment.</li>
   * </ul>
   * @return true if the object's path segments have to be included with its description, false
   * otherwise.
   */
  public boolean isPathSegmentToBeIncluded() {
    return includePathSegment;
  }

   /**
   * <p>
   * A folder hierarchy MAY be represented in a canonical notation such as path. For CMIS, a path
   * is represented by:
   * </p>
   * <ul>
   *  <li>’/’ for the root folder.</li>
   *  <li>All paths start with the root folder.</li>
   *  <li>A set of the folder and object path segments separated by ’/’ in order of closest to the
   *    root.</li>
   *  <li>Folder and object path segments are speciﬁed by pathSegment tokens which can be
   *    retrieved by all services that take an includePathSegments parameter (for example
   *    getChildren).</li>
   *  <li>A pathSegment token MUST not include a ’/’ character.
   *    It is repository speciﬁc how a repository chooses the value for pathSegment.
   *    Repositories might choose to use cmis:name or content stream ﬁlename for pathSegment
   *    token.</li>
   *  <li>The pathSegment token for each item MUST uniquely identify the item in the folder.</li>
   * </ul>
   * <p>
   * That is, if folder A is under the root, and folder B is under A, then the path would be /A/B.
   * </p>
   * <p>
   * A path for an object may be calculated in the following way:
   * </p>
   * <ul>
   *  <li>If the object is the root folder, the path is ’/’.</li>
   *  <li>If the object is a direct child of the root folder, the path is the object’s pathSegment
   *    preﬁxed by ’/’.</li>
   *  <li>If the object is not a direct child of the root folder, the path is item’s parent folder
   *    cmis:path property appended by ’/’ and the object’s pathSegment.</li>
   * </ul>
   * @param includePathSegment a boolean indicating if the path segment of objects have to be
    * included with the data that describe them.
   * @return itself.
   */
  public Filtering setIncludePathSegment(final boolean includePathSegment) {
    this.includePathSegment = includePathSegment;
    return this;
  }

  /**
   * Gets how the relationships of an object have to be included.
   * @return an {@link IncludeRelationships} value indicating how the relationships of an object
   * have to be included within its own data describing it. By default no relationships are included
   * ({@link IncludeRelationships#NONE}
   */
  public IncludeRelationships getIncludeRelationships() {
    return includeRelationships;
  }

  /**
   * Sets how the relationships of an object have to be included with its own description.
   * @param includeRelationships an {@link IncludeRelationships} instance indicating how the
   * relationships of an object have to be included within its own data describing it.
   * @return itself
   */
  public Filtering setIncludeRelationships(final IncludeRelationships includeRelationships) {
    this.includeRelationships = includeRelationships;
    return this;
  }

  /**
   * Gets what types of CMIS objects have to be included. By default, all file-able object types.
   * This filtering rule is only taken into account with subtree navigation operations.
   * @return an {@link IncludeCmisObjectTypes} value indicating what types of file-able CMIS objects
   * have to be taken into account.
   */
  public IncludeCmisObjectTypes getIncludeCmisObjectTypes() {
    return includeCmisObjectTypes;
  }

  /**
   * Sets the types of the CMIS objects to include. By default all the file-able object types.
   * This filtering rule is only taken into account with subtree navigation operations.
   * @param includeCmisObjectTypes an {@link IncludeCmisObjectTypes} value indicating what types
   * of file-able CMIS objects have to be taken into account.
   * @return itself.
   */
  public Filtering setIncludeCmisObjectTypes(final IncludeCmisObjectTypes includeCmisObjectTypes) {
    this.includeCmisObjectTypes = includeCmisObjectTypes;
    return this;
  }

  /**
   * Is the ACL about the object(s) to be included? No by default.
   * @return true if the ACL has to be included with the object(s) to return. False
   * otherwise.
   */
  public boolean isACLToBeIncluded() {
    return includeAcl;
  }

  /**
   * Indicates whether the ACL has to be included with the object(s) to return.
   * @param includeAcl a boolean indicating whether the ACL has to be included or
   * not.
   * @return itself.
   */
  public Filtering setIncludeAcl(final boolean includeAcl) {
    this.includeAcl = includeAcl;
    return this;
  }

  public enum IncludeCmisObjectTypes {
    ALL,
    ONLY_FOLDERS;
  }
}
  