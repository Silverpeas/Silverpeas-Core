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

package org.silverpeas.core.cmis.model;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.silverpeas.core.ResourceIdentifier;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An object in the CMIS model that is mapped to a resource in Silverpeas. It can be a collaborative
 * space, an application, a category of a gallery, a folder of an EDM, a classification onto the
 * PdC, or simply a contribution (publication, attachment, images, ...). A {@link CmisObject}
 * class defines the properties that are expected for a given CMIS object, with some of them
 * peculiar to its type. It is a way to map the properties of a Silverpeas resource to those of a
 * CMIS object and then, from them, to generalize the build of CMIS data done by a CMIS repository.
 * Because Silverpeas objects are localized, then a CMIS object mapped to such an object is always
 * localized to the locale setting of the current user behind the CMIS request.
 * <p>
 *  All the objects in CMIS must satisfy the following properties: unique identifier, name,
 *  description, creation and last modification date, creator's name, last updater's name, ACL, and
 *  allowable actions.
 * </p>
 * @author mmoquillon
 */
public abstract class CmisObject {

  private final String id;
  private final String name;
  private String description;
  private String creator;
  private String lastModifier;
  private long creationDate;
  private long lastModificationDate;
  private Supplier<List<Ace>> aclSupplier = Collections::emptyList;
  private Supplier<Set<Action>> actionsSupplier = Collections::emptySet;
  private final String language;

  CmisObject(final ResourceIdentifier id, String name, String language) {
    this.id = id.asString();
    this.name = name;
    this.language = language;
  }

  /**
   * Gets the unique identifier of the object in the CMIS objects tree.
   * @return the unique identifier of this CMIS object.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the name, localized for the user behind the request, of this CMIS object.
   * @return the localized name of the object.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the language in which some of the object's attributes are localized.
   * @return an ISO 639-1 language code corresponding to the language of the user behind the
   * request.
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Gets the base CMIS type from which the type of the CMIS object is derived.
   * @returna a {@link BaseTypeId} enumeration value.
   */
  public abstract BaseTypeId getBaseCmisType();

  /**
   * Gets the type of this CMIS object.
   * @return a {@link TypeId} enumeration value.
   */
  public abstract TypeId getCmisType();

  /**
   * Gets a description of this CMIS object.
   * @return a short description about it.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets a description about this CMIS object.
   * @param description a short text to set.
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setDescription(final String description) {
    this.description = description;
    return self();
  }

  /**
   * Gets the full name of the user that has authored this CMIS object.
   * @return the full name of the creator.
   */
  public String getCreator() {
    return creator;
  }

  /**
   * Sets the full name of the user that has created this CMIS object.
   * @param creator the full name of a user in Silverpeas.
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setCreator(final String creator) {
    this.creator = creator;
    return self();
  }

  /**
   * Gets the full name of the user that has lastly modified this CMIS object. By default, the first
   * modifier is the creator of this object.
   * @return the full name of the last modifier.
   */
  public String getLastModifier() {
    return lastModifier;
  }

  /**
   * Sets the full name of the user that has modified this CMIS object.
   * @param lastModifier the full name of a user in Silverpeas.
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setLastModifier(final String lastModifier) {
    this.lastModifier = lastModifier;
    return self();
  }

  /**
   * Gets the date at which this CMIS object was created.
   * @return the number of milliseconds since the EPOCH January 1, 1970, 00:00:00 GMT
   */
  public long getCreationDate() {
    return creationDate;
  }

  /**
   * Sets the date at which this CMIS object was created.
   * @param creationDate a number of milliseconds since the EPOCH January 1, 1970, 00:00:00 GMT
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setCreationDate(final long creationDate) {
    this.creationDate = creationDate;
    return self();
  }

  /**
   * Gets the date at which this CMIS object was lastly modified. By default, the first modification
   * date is the date at which this object has been created.
   * @return the number of milliseconds since the EPOCH January 1, 1970, 00:00:00 GMT
   */
  public long getLastModificationDate() {
    return lastModificationDate;
  }

  public <T extends CmisObject> T setLastModificationDate(final long lastModificationDate) {
    this.lastModificationDate = lastModificationDate;
    return self();
  }

  /**
   * Gets the ACL regarding this CMIS object.
   * @return a list of {@link Ace} objects, each of them defining an entry in the ACL.
   */
  public List<Ace> getAcl() {
    return aclSupplier.get();
  }

  /**
   * Sets a supplier of an ACL to apply on this CMIS object.
   * @param supplier a function that supplies on demand a list of ACEs to apply on this object.
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setAclSupplier(final Supplier<List<Ace>> supplier) {
    this.aclSupplier = supplier;
    return self();
  }

  /**
   * Gets all the actions that can be performed by the user behind the request onto this CMIS
   * object.
   * @return a set of {@link Action} enumeration values, each of them defining a peculiar action.
   */
  public Set<Action> getAllowedActions() {
    return this.actionsSupplier.get();
  }

  /**
   * Sets a supplier of the actions that can be performed on this CMIS object.
   * @param supplier a function that supplies on demand a set of actions allowed by the current
   * user behind the request on this object.
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setAllowedActionsSupplier(final Supplier<Set<Action>> supplier) {
    this.actionsSupplier = supplier;
    return self();
  }

  @SuppressWarnings("unchecked")
  private <T extends CmisObject> T self() {
    return (T) this;
  }

}
  