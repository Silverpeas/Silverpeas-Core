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

import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A folder of user contributions managed within an application in Silverpeas. Such a folder is a
 * contribution of a user (with peculiar rights) dedicated to categorize or to organize some
 * contributions from others users (and from himself) according to some thematics. a contribution
 * folder can be a topic in an EDM, an album in a media gallery or simply a category of posts in a
 * blog. The folder can contain also others folders in order to refine the categorization or the
 * organization of the user contributions. The contribution folder is represented in Silverpeas by
 * the technical class {@link org.silverpeas.core.node.model.NodeDetail}.
 * @author mmoquillon
 */
public class ContributionFolder extends CmisFolder {

  /**
   * The identifier of its CMIS type.
   */
  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_FOLDER;

  public static List<TypeId> getAllAllowedChildrenTypes() {
    return Arrays.asList(TypeId.SILVERPEAS_FOLDER, TypeId.SILVERPEAS_PUBLICATION);
  }

  private final ContributionIdentifier id;

  /**
   * Gets the virtual root contribution folder of the specified application.
   * @param applicationId the unique identifier of an application.
   * @return the {@link ContributionIdentifier} of the root folder.
   */
  public static ContributionIdentifier getRootFolderId(final String applicationId) {
    return ContributionIdentifier.from(applicationId, NodePK.ROOT_NODE_ID, NodeDetail.TYPE);
  }

  /**
   * Constructs a new folder of contributions with the specified identifier, name and language.
   * @param id the {@link ContributionIdentifier} instance identifying the node or
   * the category in Silverpeas.
   * @param name the name of the folder.
   * @param language the language of the folder.
   */
  ContributionFolder(final ContributionIdentifier id, final String name, final String language) {
    super(id, name, language);
    this.id = id;
  }

  @Override
  public String getSymbol() {
    return "";
  }

  public String getApplicationId() {
    return id.getComponentInstanceId();
  }

  @Override
  public boolean isRoot() {
    return false;
  }

  @Override
  public List<TypeId> getAllowedChildrenTypes() {
    return getAllAllowedChildrenTypes();
  }

  @Override
  public BaseTypeId getBaseTypeId() {
    return CMIS_TYPE.getBaseTypeId();
  }

  @Override
  public TypeId getTypeId() {
    return CMIS_TYPE;
  }

  @Override
  protected Supplier<Set<Action>> getAllowableActionsSupplier() {
    return () -> completeWithContributionFolderActions(
        completeWithFolderActions(theCommonActions()));
  }

  private Set<Action> completeWithContributionFolderActions(final Set<Action> actions) {
    actions.add(Action.CAN_CREATE_FOLDER);
    return actions;
  }
}
  