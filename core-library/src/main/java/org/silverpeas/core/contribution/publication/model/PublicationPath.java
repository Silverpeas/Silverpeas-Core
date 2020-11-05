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

package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;

import java.util.Objects;

/**
 * The path of a given publication in an application. The path is represented here by a list of
 * nodes, each of them being a path segment of the publication path. The path ended by the
 * publication itself.
 * @author mmoquillon
 */
public class PublicationPath extends NodePath {

  public static PublicationPath getPath(final ContributionIdentifier pubId) {
    PublicationPK pk = new PublicationPK(pubId.getLocalId(), pubId.getComponentInstanceId());
    PublicationService service = PublicationService.get();
    PublicationDetail pub = service.getDetail(pk);
    if (pub == null) {
      throw new NotFoundException("No such publication " + pubId.asString());
    }
    Location folder = service.getMainLocation(pk)
        .orElseGet(() -> new Location(NodePK.ROOT_NODE_ID, pub.getInstanceId()));
    return new PublicationPath(pub, folder);
  }

  private final PublicationDetail publication;
  private final Location folder;

  private PublicationPath(PublicationDetail pub, Location folder) {
    this.publication = pub;
    this.folder = folder;
    if (!this.folder.isRoot()) {
      this.addAll(NodeService.get().getPath(this.folder));
    }
  }

  @Override
  protected boolean isRoot(final NodeDetail node) {
    return this.folder.isRoot() || super.isRoot(node);
  }

  @Override
  public String format(final String language, final boolean fullSpacePath, final String pathSep) {
    if (isEmpty()) {
      return "";
    }
    return super.format(language, fullSpacePath, pathSep) + pathSep +
        publication.getTranslation(language).getName();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final PublicationPath that = (PublicationPath) o;
    return Objects.equals(publication, that.publication) && Objects.equals(folder, that.folder);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), publication, folder);
  }
}
  