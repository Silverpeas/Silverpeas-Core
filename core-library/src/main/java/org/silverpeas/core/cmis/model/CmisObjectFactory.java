/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.cmis.SilverpeasCmisSettings;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A factory of objects in the CMIS model by using as source object a Silverpeas resource. It is
 * responsible to map a Silverpeas resource (spaces, applications, nodes, contributions, ...) to a
 * CMIS object.
 * @author mmoquillon
 */
@Provider
public class CmisObjectFactory {

  /**
   * Gets an instance of the {@link CmisObjectFactory} class. This method invokes the underlying IoC
   * system to get such an instance.
   * @return a {@link CmisObjectFactory} instance.
   */
  public static CmisObjectFactory getInstance() {
    return ServiceProvider.getSingleton(CmisObjectFactory.class);
  }

  /**
   * Creates the root space in the CMIS model. It is the root of the CMIS organizational tree of
   * CMIS objects. It corresponds in Silverpeas to the container of all root workspaces.
   * @return the root space.
   */
  public Space createRootSpace(final String language) {
    final User admin = User.getMainAdministrator();
    final Date spawningDate =
        admin.getCreationDate() != null ? admin.getCreationDate() : admin.getStateSaveDate();
    String name = SilverpeasCmisSettings.get().getRepositoryName();
    return new Space(Space.ROOT_ID, name, language)
        .setParentId(null)
        .setDescription("The Collaborative's root space")
        .setCreator(admin.getDisplayedName())
        .setCreationDate(spawningDate.getTime())
        .setLastModifier(admin.getDisplayedName())
        .setLastModificationDate(spawningDate.getTime())
        .setAcesSupplier(this::theCommonsACE);
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
      fatherId = Space.ROOT_ID.asString();
    } else {
      final String idPrefix = SpaceInst.SPACE_KEY_PREFIX;
      fatherId = (space.getFatherId().startsWith(idPrefix) ? "" : idPrefix) + space.getFatherId();
    }
    final User creator = space.getCreator();
    final long creationDate = inMilliseconds(space.getCreationDate(), space.getLastUpdateDate());
    final User lastModifier;
    final long lastModificationDate;
    if (space.getUpdatedBy() < 0) {
      lastModifier = creator;
      lastModificationDate = creationDate;
    } else {
      lastModifier = space.getLastUpdater();
      lastModificationDate = space.getLastUpdateDate().getTime();
    }
    return new Space(space.getIdentifier(), space.getName(language), language)
        .setParentId(fatherId)
        .setDescription(space.getDescription(language))
        .setCreator(creator.getDisplayedName())
        .setCreationDate(creationDate)
        .setLastModifier(lastModifier.getDisplayedName())
        .setLastModificationDate(lastModificationDate)
        .setAcesSupplier(this::theCommonsACE);
  }

  /**
   * Creates a CMIS representation of the specified Silverpeas application.
   * @param component a component instance in Silverpeas.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS application as a CMIS folder.
   */
  public Application createApplication(final ComponentInstLight component, final String language) {
    final User creator = User.getById(String.valueOf(component.getCreatedBy()));
    final long creationDate =
        inMilliseconds(component.getCreationDate(), component.getLastUpdateDate());
    final User lastModifier;
    final long lastModificationDate;
    if (component.getUpdatedBy() < 0) {
      lastModifier = creator;
      lastModificationDate = creationDate;
    } else {
      lastModifier = User.getById(String.valueOf(component.getUpdatedBy()));
      lastModificationDate = component.getLastUpdateDate().getTime();
    }
    return new Application(component.getIdentifier(), component.getName(language), language)
        .setParentId(component.getSpaceId())
        .setDescription(component.getDescription(language))
        .setCreator(creator.getDisplayedName())
        .setCreationDate(creationDate)
        .setLastModifier(lastModifier.getDisplayedName())
        .setLastModificationDate(lastModificationDate)
        .setAcesSupplier(this::theCommonsACE);
  }

  /**
   * Creates a CMIS representation of the specified node descriptor in a given Silverpeas
   * application. A node is a technical object used in Silverpeas to categorize or organize in a
   * generic way any user contributions. Because a node can also contain others nodes, the nodes can
   * be linked to each of them within a tree of nodes (hence the name {@code node}).
   * @param node a descriptor about a node.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS contribution folder as a CMIS folder.
   */
  public ContributionFolder createContributionFolder(final NodeDetail node, final String language) {
    String parentId;
    if (node.getFatherPK().isRoot()) {
      parentId = node.getIdentifier().getComponentInstanceId();
    } else {
      parentId = ContributionIdentifier.from(node.getFatherPK(), NodeDetail.TYPE).asString();
    }
    return new ContributionFolder(node.getIdentifier(), node.getName(language), language)
        .setParentId(parentId)
        .setDescription(node.getDescription(language))
        .setCreator(node.getCreator().getDisplayedName())
        .setCreationDate(inMilliseconds(node.getCreationDate(), node.getLastUpdateDate()))
        .setLastModifier(node.getLastUpdater().getDisplayedName())
        .setLastModificationDate(node.getLastUpdateDate().getTime())
        .setAcesSupplier(this::theCommonsACE);
  }

  /**
   * Creates a CMIS representation of the specified publication in a given folder of a Silverpeas
   * application. A publication in our CMIS implementation is always contained into a folder. If the
   * application in Silverpeas doesn't organize them in folders, then the folder here should be a
   * virtual one (a root folder).
   * @param pub detail about the publication.
   * @param folder the folder in which is contained this publication.
   * @param language the language to use in the localization of the CMIS object to construct.
   * @return a CMIS publication as a CMIS folder.
   */
  public Publication createPublication(final PublicationDetail pub,
      final ContributionIdentifier folder, final String language) {
    String parentId = folder.getLocalId().equals(NodePK.ROOT_NODE_ID) ?
        folder.getComponentInstanceId() : folder.asString();
    //noinspection removal
    return new Publication(pub.getIdentifier(), pub.getName(language), language)
        .setParentId(parentId)
        .setDescription(pub.getDescription(language))
        .setCreator(pub.getCreator().getDisplayedName())
        .setCreationDate(inMilliseconds(pub.getCreationDate(), pub.getLastUpdateDate()))
        .setLastModifier(pub.getLastUpdater().getDisplayedName())
        .setLastModificationDate(pub.getLastUpdateDate().getTime())
        .setAcesSupplier(this::theCommonsACE);
  }

  /**
   * Creates a CMIS representation of the specified localized document attached to the specified
   * contribution.
   * @param document a {@link SimpleDocument} instance.
   * @param parentId the unique identifier of a contribution.
   * @return a CMIS document attached to a contribution.
   */
  public DocumentFile createDocument(final SimpleDocument document, final ContributionIdentifier
      parentId) {
    Date updateDate;
    User updater;
    if (document.getLastUpdater() == null) {
      updateDate = document.getCreationDate();
      updater = document.getCreator();
    } else {
      updateDate = document.getLastUpdateDate();
      updater = document.getLastUpdater();
    }
    boolean readOnly = document.isReadOnly() || (document.isVersioned() && !document.isEdited());
    return new DocumentFile(document.getIdentifier(), document.getFilename(),
        document.getLanguage())
        .setTitle(document.getTitle())
        .setLastComment(document.getComment())
        .setMimeType(document.getContentType())
        .setSize(document.getSize())
        .setReadOnly(readOnly)
        .setParentId(parentId.asString())
        .setDescription(document.getDescription())
        .setCreator(document.getCreator().getDisplayedName())
        .setCreationDate(inMilliseconds(document.getCreationDate(), document.getLastUpdateDate()))
        .setLastModifier(updater.getDisplayedName())
        .setLastModificationDate(updateDate.getTime())
        .setAcesSupplier(this::theCommonsACE);
  }

  private List<Ace> theCommonsACE(final User user) {
    final AccessControlEntryImpl entry = new AccessControlEntryImpl();
    entry.setPrincipal(new AccessControlPrincipalDataImpl(user.getLogin()));
    entry.setPermissions(new ArrayList<>());
    entry.getPermissions().add(BasicPermissions.READ);
    entry.setDirect(true);
    return Collections.singletonList(entry);
  }

  /**
   * Converts in milliseconds the specified date by taking into account it can be null. If null, the
   * given default date will be taken. if the default date is also null, then the creation date or
   * the last state change date of the platform administrator is taken. This method is for resources
   * having no creation date which shouldn't be occurred unless the resource had been created with a
   * very old version of Silverpeas.
   * @param date a date to convert in milliseconds.
   * @param defaultDate a default date to use if the date above is null. If null, the default date
   * will be either the creation date or the last state change date of the Silverpeas
   * administrator.
   * @return the number of milliseconds from EPOCH represented by the specified date or default date
   */
  private long inMilliseconds(final Date date, final Date defaultDate) {
    Date dateToUse = date == null ? defaultDate : date;
    if (dateToUse == null) {
      User admin = User.getMainAdministrator();
      dateToUse =
          admin.getCreationDate() != null ? admin.getCreationDate() : admin.getStateSaveDate();
    }
    return dateToUse.getTime();
  }

}
  