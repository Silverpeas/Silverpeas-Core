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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.NotFoundException;
import org.silverpeas.core.admin.component.model.ComponentInstPath;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.security.authorization.NodeAccessControl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

/**
 * The path of a given publication in an application. The path is represented here by a list of
 * nodes, each of them being a path segment of the publication path. The path ended by the
 * publication itself.
 * @author mmoquillon
 */
public class PublicationPath extends NodePath {
  private static final long serialVersionUID = -7426600130592037668L;

  /**
   * Gets the best publication path of the user represented by the given user identifier by
   * taking care about its access authorizations.
   * @param pubId the {@link ContributionIdentifier} of a publication.
   * @param userId the identifier as string of a user.
   * @return an initialized {@link PublicationPath}.
   */
  public static PublicationPath getBestPathForUser(final ContributionIdentifier pubId,
      final String userId) {
    final PublicationDetail pub = getPublication(pubId);
    final Location folder = OrganizationController.get().getComponentInstance(pub.getInstanceId())
        .filter(SilverpeasComponentInstance::isTopicTracker)
        .map(i -> {
          final PublicationPK pk = new PublicationPK(pubId.getLocalId(), pubId.toReference());
          final Map<NodePK, Location> indexedLocations = PublicationService.get().getAllLocations(pk).stream()
              .filter(l -> l.getComponentInstanceId().equals(pubId.getComponentInstanceId()))
              .sorted(comparing(Location::getId))
              .collect(toMap(l -> new NodePK(l.getId(), l.getInstanceId()), l -> l, (l, o) -> l, LinkedHashMap::new));
          return NodeAccessControl.get()
              .filterAuthorizedByUser(indexedLocations.keySet(), userId)
              .map(indexedLocations::get)
              .findFirst()
              .map(l -> {
                pub.setAuthorizedLocation(l);
                return l;
              })
              .orElseGet(() -> new Location(NodePK.UNDEFINED_NODE_ID, pubId.getComponentInstanceId()));
        })
        .orElseGet(() -> new Location(NodePK.ROOT_NODE_ID, pubId.getComponentInstanceId()));
    return new PublicationPath(pub, folder);
  }

  /**
   * Gets the best publication path of the group represented by the given group identifier by
   * taking care about its access authorizations.
   * @param pubId the {@link ContributionIdentifier} of a publication.
   * @param groupId the identifier as string of a group.
   * @return an initialized {@link PublicationPath}.
   */
  public static PublicationPath getBestPathForGroup(final ContributionIdentifier pubId,
      final String groupId) {
    final PublicationDetail pub = getPublication(pubId);
    final PublicationPK pk = new PublicationPK(pubId.getLocalId(), pubId.toReference());
    final NodeAccessControl nodeAccessControl = NodeAccessControl.get();
    final Location folder = OrganizationController.get().getComponentInstance(pub.getInstanceId())
        .filter(SilverpeasComponentInstance::isTopicTracker)
        .map(i -> PublicationService.get()
            .getAllLocations(pk)
            .stream()
            .filter(l -> l.getComponentInstanceId().equals(pubId.getComponentInstanceId()))
            .filter(l -> nodeAccessControl.isGroupAuthorized(groupId, new NodePK(l.getId(), l.getInstanceId())))
            .findFirst()
            .map(l -> {
              pub.setAuthorizedLocation(l);
              return l;
            })
            .orElseGet(() -> new Location(NodePK.UNDEFINED_NODE_ID, pubId.getComponentInstanceId())))
        .orElseGet(() -> new Location(NodePK.ROOT_NODE_ID, pubId.getComponentInstanceId()));
    return new PublicationPath(pub, folder);
  }

  /**
   * Gets a publication path without taking care about right accesses.
   * <p>
   * The main location of the publication is taken into account.
   * </p>
   * @param pubId the publication contribution identifier.
   * @return an initialized {@link PublicationPath}.
   */
  public static PublicationPath getPath(final ContributionIdentifier pubId) {
    final PublicationDetail pub = getPublication(pubId);
    return getPath(pub);
  }

  /**
   * Gets a publication path without taking care about right accesses.
   * <p>
   * The main location of the publication is taken into account.
   * </p>
   * @param pub a publication contribution instance.
   * @return an initialized {@link PublicationPath}.
   */
  public static PublicationPath getPath(final PublicationDetail pub) {
    final PublicationPK pk = pub.getPK();
    final Location folder = OrganizationController.get().getComponentInstance(pub.getInstanceId())
        .filter(SilverpeasComponentInstance::isTopicTracker)
        .map(i -> PublicationService.get()
            .getMainLocation(pk)
            .orElseGet(() -> new Location(NodePK.UNDEFINED_NODE_ID, pub.getInstanceId())))
        .orElseGet(() -> new Location(NodePK.ROOT_NODE_ID, pub.getInstanceId()));
    return new PublicationPath(pub, folder);
  }

  private static PublicationDetail getPublication(final ContributionIdentifier pubId) {
    final PublicationPK pk = new PublicationPK(pubId.getLocalId(), pubId.getComponentInstanceId());
    final PublicationDetail pub = PublicationService.get().getDetail(pk);
    if (pub == null) {
      throw new NotFoundException("No such publication " + pubId.asString());
    }
    return pub;
  }

  private final PublicationDetail publication;
  private final Location folder;

  private PublicationPath(PublicationDetail pub, Location folder) {
    this.publication = pub;
    this.folder = folder;
    if (!this.folder.isUndefined() && !this.folder.isRoot()) {
      this.addAll(NodeService.get().getPath(this.folder));
    }
  }

  /**
   * Gets the contribution aimed by the path.
   * @return a {@link PublicationDetail} instance.
   */
  public PublicationDetail getContribution() {
    return this.publication;
  }

  /**
   * Gets the location of the publication into the component instance aimed by the path.
   * <p>
   * If the component does not handle topic, a root location on the instance is returned.
   * </p>
   * @return a {@link Location} instance.
   */
  public Location getLocation() {
    return folder;
  }

  @Override
  protected boolean isRoot(final NodeDetail node) {
    return this.folder.isRoot() || super.isRoot(node);
  }

  @Override
  public String format(final String language, final boolean fullSpacePath, final String pathSep) {
    final String pathPrefix;
    if (isEmpty()) {
      final ComponentInstPath path = ComponentInstPath.getPath(folder.getComponentInstanceId());
      pathPrefix = path.format(language, fullSpacePath, pathSep);
    } else {
      pathPrefix = super.format(language, fullSpacePath, pathSep);
    }
    return pathPrefix + pathSep + publication.getTranslation(language).getName();
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
  