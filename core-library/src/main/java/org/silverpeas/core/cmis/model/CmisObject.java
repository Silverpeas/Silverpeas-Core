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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.cmis.model;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.silverpeas.core.ResourceIdentifier;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
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
public abstract class CmisObject extends ObjectDataImpl {

  private final String id;
  private final String name;
  private String description;
  private String creator;
  private String lastModifier;
  private long creationDate;
  private long lastModificationDate;
  private transient Function<User, List<Ace>> aclSupplier = u -> Collections.emptyList();
  private final String language;

  /**
   * Constructs a new CMIS object with the specified identifier, name and language in which the
   * object is expressed.
   * @param id a {@link ResourceIdentifier} instance identifying a resource in Silverpeas.
   * @param name the name of the resource.
   * @param language the language in which the properties of the resource are written.
   */
  CmisObject(final ResourceIdentifier id, String name, String language) {
    this.id = id.asString();
    this.name = name;
    this.language = language;
  }

  /**
   * Gets the unique identifier of the object in the CMIS objects tree.
   * @return the unique identifier of this CMIS object.
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Gets the possible UTF-8 symbol representing the type of this CMIS object. It acts as an icon
   * but instead of being a regular image the icon is encoded in UTF-8. Its goal is to mark the
   * concrete type of the CMIS object upon its basic CMIS one (that is folder, document, ...)
   * @return the UTF-8 symbol representing the type of this object or an empty String if no symbol
   * is defined.
   */
  public abstract String getSymbol();

  /**
   * Is this CMIS object file-able into the CMIS objects tree?
   * @return true if the object can be put in the CMIS objects tree as a node or a leaf of that
   * tree. False otherwise
   */
  public abstract boolean isFileable();

  /**
   * Is this CMIS object a document in the CMIS objects tree? A document is a file-able object
   * that doesn't have any children (it is a leaf in the CMIS objects tree) and that has a
   * content stream (beside any rendition content streams). Such objects must have as base type
   * identifier {@link BaseTypeId#CMIS_DOCUMENT}.
   * @return true if this object is a document. False otherwise.
   */
  public boolean isDocument() {
    return getBaseTypeId().equals(BaseTypeId.CMIS_DOCUMENT);
  }

  /**
   * Gets the name, localized for the user behind the request, of this CMIS object.
   * @return the localized name of the object.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the label, localized for the user behind the request, to display to the user as name of
   * this CMIS object.
   * @return the concatenation of the symbol and the name of this object. If no symbol is defined
   * for this CMIS object, then the name is returned.
   */
  public String getLabel() {
    return StringUtil.isDefined(getSymbol()) ? getSymbol() + " " + getName() : getName();
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
   * Gets the identifier of the base type from which the type of the CMIS object is derived.
   * @returna a {@link BaseTypeId} enumeration value.
   */
  @Override
  public abstract BaseTypeId getBaseTypeId();

  /**
   * Gets the identifier of the type of this CMIS object.
   * @return a {@link TypeId} enumeration value.
   */
  public abstract TypeId getTypeId();

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
   * Adds the ACEs relative to the specified user and regarding to this CMIS object in the ACL of
   * the objet. The ACEs are supplied by the function that was passed with the
   * {@link CmisObject#setAcesSupplier(Function)} function.
   * @return itself.
   */
  public <T extends CmisObject> T addACEs(final User user) {
    final List<Ace> aces = aclSupplier.apply(user);
    if (getAcl() == null) {
      AccessControlListImpl acl = new AccessControlListImpl(new ArrayList<>());
      acl.setExact(false);
      setAcl(acl);
      // currently all the permissions set in the ACL are those as defined in the CMIS repository
      // (no custom ones)
      super.setIsExactAcl(true);
    }
    getAcl().getAces().addAll(aces);
    return self();
  }

  /**
   * Overrides the ACL of this CMIS object with this new one. All the previous ACE that were set
   * are then lost.
   * @param acl the new ACL to set explicitly.
   */
  @Override
  public void setAcl(final Acl acl) {
    // to add javadoc explaining what this method does.
    super.setAcl(acl);
  }

  /**
   * This method does nothing. The exactitude of the ACL regarding the permissions defined in the
   * CMIS repository is centralized and controlled.
   * @param isExactACL a boolean.
   */
  @Override
  public void setIsExactAcl(final Boolean isExactACL) {
    // does nothing
  }

  /**
   * Sets a supplier of an ACL to apply on this CMIS object.
   * @param supplier a function that supplies on demand a list of ACEs to apply on this object.
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setAcesSupplier(final Function<User, List<Ace>> supplier) {
    this.aclSupplier = supplier;
    return self();
  }

  /**
   * Gets the ACL of this CMIS object. The ACL contains a list of ACEs that define for one or more
   * users in Silverpeas the permissions they have regarding this object. Each permission authorizes
   * or forbids the related user to perform one or more of the actions that are allowable on this
   * CMIS object.
   * For more information about the ACL and the allowable actions on CMIS objects, see
   * <a href="http://docs.oasis-open.org/cmis/CMIS/v1.1/os/CMIS-v1.1-os.html#x1-7700012">CMIS
   * specification</a>
   * @see CmisObject#getAllowableActions()
   * @return an {@link Acl} object.
   */
  @Override
  public Acl getAcl() {
    // to add javadoc explaining what this method does.
    return super.getAcl();
  }

  /**
   * Gets all the actions that are allowable on this CMIS object, whatever the permissions a user
   * in Silverpeas can have in performing those actions. The actions are set only and only if the
   * {@link CmisObject#setAllowableActions()} was previously invoked. Indeed, in CMIS the allowable
   * actions are requested on demand.
   * For more information about the ACL and the allowable actions on CMIS objects, see
   * <a href="http://docs.oasis-open.org/cmis/CMIS/v1.1/os/CMIS-v1.1-os.html#x1-7700012">CMIS
   * specification</a>
   * @return a {@link AllowableActions} object.
   */
  @Override
  public AllowableActions getAllowableActions() {
    // to add javadoc explaining what this method does.
    return super.getAllowableActions();
  }

  /**
   * Sets all the actions allowable on this CMIS object, whatever the permissions a user
   * in Silverpeas can have in performing those actions, by using the supplier of the actions
   * predefined for this object. The goal of the supplier is to set the
   * allowable actions in this object on the demand. In some circumstances, the allowable actions
   * aren't requested and therefore don't need to be set.
   * For more information about the ACL and the allowable actions on CMIS objects, see
   * <a href="http://docs.oasis-open.org/cmis/CMIS/v1.1/os/CMIS-v1.1-os.html#x1-7700012">CMIS
   * specification</a>
   * @param <T> the concrete Java type of this CMIS object.
   * @return itself.
   */
  public <T extends CmisObject> T setAllowableActions() {
    Supplier<Set<Action>> supplier = getAllowableActionsSupplier();
    AllowableActionsImpl allowableActions = new AllowableActionsImpl();
    allowableActions.setAllowableActions(supplier.get());
    setAllowableActions(allowableActions);
    return self();
  }

  /**
   * Gets a supplier of the actions allowable on this CMIS object. The supplier is only invoked
   * when the {@link CmisObject#setAllowableActions()} is invoked.
   * @return
   */
  protected abstract Supplier<Set<Action>> getAllowableActionsSupplier();

  protected Set<Action> theCommonActions() {
    final Set<Action> actions = EnumSet.noneOf(Action.class);
    actions.add(Action.CAN_GET_PROPERTIES);
    actions.add(Action.CAN_GET_ACL);
    return actions;
  }

  @SuppressWarnings("unchecked")
  private <T extends CmisObject> T self() {
    return (T) this;
  }

}
  