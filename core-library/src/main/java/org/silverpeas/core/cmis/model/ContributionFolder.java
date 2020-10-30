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

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.service.NodeService;

import java.util.Arrays;
import java.util.List;

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

  public static final TypeId CMIS_TYPE = TypeId.SILVERPEAS_FOLDER;

  public static List<TypeId> getAllAllowedChildrenTypes() {
    return Arrays.asList(TypeId.SILVERPEAS_FOLDER, TypeId.SILVERPEAS_PUBLICATION);
  }

  private final ContributionIdentifier id;

  ContributionFolder(final ContributionIdentifier id, final String name, final String language) {
    super(id, name, language);
    this.id = id;
  }

  public String getApplicationId() {
    return id.getComponentInstanceId();
  }

  @Override
  public String getPath() {
    NodePK nodePK = new NodePK(id.getLocalId(), id.getComponentInstanceId());
    return PATH_SEPARATOR +
        NodeService.get().getPath(nodePK).format(getLanguage(), true, PATH_SEPARATOR);
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
  public BaseTypeId getBaseCmisType() {
    return CMIS_TYPE.getBaseTypeId();
  }

  @Override
  public TypeId getCmisType() {
    return CMIS_TYPE;
  }
}
  