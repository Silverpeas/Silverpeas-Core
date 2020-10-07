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

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.silverpeas.core.Identifiable;
import org.silverpeas.core.NotSupportedException;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * A factory of objects in the CMIS model by using as source object a Silverpeas resource. It is
 * responsible to map a Silverpeas resource (spaces, applications, nodes, contributions, ...) to
 * a CMIS object.
 * @author mmoquillon
 */
@Provider
public class CmisObjectFactory {

  /**
   * Gets an instance of the {@link CmisObjectFactory} class. This method invokes the underlying
   * IoC system to get such an instance.
   * @return a {@link CmisObjectFactory} instance.
   */
  public static CmisObjectFactory getInstance() {
    return ServiceProvider.getSingleton(CmisObjectFactory.class);
  }

  /**
   * A generic way to creates a CMIS object corresponding to a specified Silverpeas object without
   * having to known the concrete type of the Silverpeas object and hence the concrete type of
   * the corresponding CMIS object.
   * @param silverpeasObject the object to represent in the CMIS model.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS object corresponding to the given Silverpeas object.
   * @throws NotSupportedException if the type of the Silverpeas object has no any correspondence
   * in the CMIS model.
   */
  public CmisObject createCmisObject(final Identifiable silverpeasObject, final String language) {
    if (silverpeasObject.getClass().equals(SpaceInstLight.class)) {
      return createSpace((SpaceInstLight) silverpeasObject, language);
    } else if (silverpeasObject.getClass().equals(ComponentInstLight.class)) {
      return createApplication((ComponentInstLight) silverpeasObject, language);
    } else if (silverpeasObject.getClass().equals(NodeDetail.class)) {
      return createContributionFolder((NodeDetail) silverpeasObject, language);
    } else {
      throw new NotSupportedException(
          silverpeasObject.getClass() + " isn't supported by our CMIS implementation");
    }
  }

  /**
   * Creates the root space in the CMIS model. It is the root of the CMIS organizational tree of
   * CMIS objects. It corresponds in Silverpeas to the container of all root workspaces.
   * @return the root space.
   */
  public Space createRootSpace() {
    final User admin = User.getMainAdministrator();
    final Date spawningDate =
        admin.getCreationDate() != null ? admin.getCreationDate() : admin.getStateSaveDate();
    return new Space(Space.ROOT_ID, "Root Space", "en")
        .setParentId(null)
        .setDescription("The Collaborative's root space")
        .setCreator(admin.getDisplayedName())
        .setCreationDate(spawningDate.getTime())
        .setLastModifier(admin.getDisplayedName())
        .setLastModificationDate(spawningDate.getTime())
        .setAclSupplier(this::theCommonsACE)
        .setAllowedActionsSupplier(() -> completeWithCommonFolderActions(theCommonActions()));
  }

  /**
   * Creates a CMIS representation of the specified Silverpeas workspace.
   * @param space a Silverpeas space.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS space as a CMIS folder.
   */
  public Space createSpace(final SpaceInstLight space, final String language) {
    final String fatherId;
    if (space.getFatherId() == null) {
      fatherId = Space.ROOT_ID;
    } else {
      final String idPrefix = SpaceInst.SPACE_KEY_PREFIX;
      fatherId = (space.getFatherId().startsWith(idPrefix) ? "" : idPrefix) + space.getFatherId();
    }
    final User creator = User.getById(String.valueOf(space.getCreatedBy()));
    final User lastModifier;
    final long lastModificationDate;
    if (space.getUpdatedBy() < 0) {
      lastModifier = creator;
      lastModificationDate = space.getCreateDate().getTime();
    } else {
      lastModifier = User.getById(String.valueOf(space.getUpdatedBy()));
      lastModificationDate = space.getUpdateDate().getTime();
    }
    return new Space(space.getId(), space.getName(language), language)
        .setParentId(fatherId)
        .setDescription(space.getDescription(language))
        .setCreator(creator.getDisplayedName())
        .setCreationDate(space.getCreateDate().getTime())
        .setLastModifier(lastModifier.getDisplayedName())
        .setLastModificationDate(lastModificationDate)
        .setAclSupplier(this::theCommonsACE)
        .setAllowedActionsSupplier(() -> completeWithFolderActions(theCommonActions()));
  }

  /**
   * Creates a CMIS representation of the specified Silverpeas application.
   * @param component a component instance in Silverpeas.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS application as a CMIS folder.
   */
  public Application createApplication(final ComponentInstLight component, final String language) {
    final User creator = User.getById(String.valueOf(component.getCreatedBy()));
    final User lastModifier;
    final long lastModificationDate;
    if (component.getUpdatedBy() < 0) {
      lastModifier = creator;
      lastModificationDate = component.getCreateDate().getTime();
    } else {
      lastModifier = User.getById(String.valueOf(component.getUpdatedBy()));
      lastModificationDate = component.getUpdateDate().getTime();
    }
    return new Application(component.getId(), component.getName(language), language).setParentId(
        component.getSpaceId())
        .setDescription(component.getDescription(language))
        .setCreator(creator.getDisplayedName())
        .setCreationDate(component.getCreateDate().getTime())
        .setLastModifier(lastModifier.getDisplayedName())
        .setLastModificationDate(lastModificationDate)
        .setAclSupplier(this::theCommonsACE)
        .setAllowedActionsSupplier(() -> completeWithFolderActions(theCommonActions()));
  }

  /**
   * Creates a CMIS representation of the specified node descriptor in a given Silverpeas
   * application. A node is a technical object used in Silverpeas to categorize or organize in a
   * generic way any user contributions. Because a node can also contain others nodes, the nodes
   * can be linked to each of them within a tree of nodes (hence the name {@code node}).
   * @param node a descriptor about a node.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS contribution folder as a CMIS folder.
   */
  public ContributionFolder createContributionFolder(final NodeDetail node, final String language) {
    String parentId;
    if (node.getFatherPK().isRoot()) {
      parentId = node.getContributionId().getComponentInstanceId();
    } else {
      parentId = ContributionIdentifier.from(node.getFatherPK(), NodeDetail.TYPE).asString();
    }
    return new ContributionFolder(node.getContributionId(), node.getName(language), language)
        .setParentId(parentId)
        .setDescription(node.getDescription(language))
        .setCreator(node.getCreator().getDisplayedName())
        .setCreationDate(node.getCreationDate().getTime())
        .setLastModifier(node.getLastModifier().getDisplayedName())
        .setLastModificationDate(node.getLastModificationDate().getTime())
        .setAclSupplier(this::theCommonsACE)
        .setAllowedActionsSupplier(() -> completeWithFolderActions(theCommonActions()));
  }

  /**
   * Creates a CMIS representation of the specified publication in a given folder of a Silverpeas
   * application. A publication in our CMIS implementation is always contained into a folder. If
   * the application in Silverpeas doesn't organize them in folders, then the folder here should be
   * a virtual one (a root folder).
   * @param pub detail about the publication.
   * @param folder the folder in which is contained this publication.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS publication as a CMIS folder.
   */
  public Publication createPublication(final PublicationDetail pub,
      final ContributionIdentifier folder, final String language) {
    String parentId = folder.getLocalId().equals(NodePK.ROOT_NODE_ID) ?
        folder.getComponentInstanceId() : folder.asString();
    return new Publication(pub.getContributionId(), pub.getName(language), language)
        .setParentId(parentId)
        .setDescription(pub.getDescription(language))
        .setCreator(pub.getCreator().getDisplayedName())
        .setCreationDate(pub.getCreationDate().getTime())
        .setLastModifier(pub.getLastModifier().getDisplayedName())
        .setLastModificationDate(pub.getLastModificationDate().getTime())
        .setAclSupplier(this::theCommonsACE)
        .setAllowedActionsSupplier(() -> completeWithFolderActions(theCommonActions()));
  }

  private List<Ace> theCommonsACE() {
    final User admin = User.getMainAdministrator();
    final AccessControlEntryImpl entry = new AccessControlEntryImpl();
    entry.setPrincipal(new AccessControlPrincipalDataImpl(admin.getLogin()));
    entry.setPermissions(new ArrayList<>());
    entry.getPermissions().add(BasicPermissions.READ);
    entry.setDirect(true);
    return Collections.singletonList(entry);
  }

  private Set<Action> theCommonActions() {
    final Set<Action> actions = EnumSet.noneOf(Action.class);
    actions.add(Action.CAN_GET_PROPERTIES);
    actions.add(Action.CAN_GET_ACL);
    return actions;
  }

  private Set<Action> completeWithCommonFolderActions(final Set<Action> actions) {
    actions.add(Action.CAN_GET_DESCENDANTS);
    actions.add(Action.CAN_GET_CHILDREN);
    actions.add(Action.CAN_GET_FOLDER_TREE);

    return actions;
  }

  private Set<Action> completeWithFolderActions(final Set<Action> actions) {
    actions.add(Action.CAN_GET_OBJECT_PARENTS);
    actions.add(Action.CAN_GET_FOLDER_PARENT);
    return completeWithCommonFolderActions(actions);
  }

}
  