/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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
package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.List;
import java.util.Optional;

/**
 * The interface for all the content (filebox+, ..) Every container have to implement this
 * interface.
 */
public interface ContentInterface {

  static Optional<ContentInterface> getByInstanceId(final String componentInstanceId) {
    try {
      final ContentInterface contentInterface =
          ContentManagerProvider.getContentManager().getContentPeas(componentInstanceId)
              .getContentInterface();
      return Optional.ofNullable(contentInterface);
    } catch (SilverpeasException e) {
      SilverLogger.getLogger(ContentInterface.class).silent(e);
    }
    return Optional.empty();
  }

  /**
   * Find all the SilverContents with the given silverpeas content identifiers.
   * @param silverpeasContentIds list of silverpeas content identifier as integer
   * @param instanceId the identifier of a component instance.
   * @param currentUserId the identifier of the user accessing the content.
   * @return the content as {@link SilverpeasComponentInstance}
   */
  List<SilverContentInterface> getSilverContentById(List<Integer> silverpeasContentIds,
      String instanceId, String currentUserId);

  /**
   * Gets the silverpeas content identifier about a contribution.
   * @param contribution a contribution.
   * @param <T> a contribution implementation.
   * @return a silverpeas content identifier as integer.
   */
  <T extends Contribution> int getSilverContentId(T contribution);

  /**
   * Gets the silverpeas content identifier about a contribution.<br>
   * If no content identifier exists, then it is created.
   * @param <T> a contribution implementation.
   * @param contribution a contribution.
   * @return a silverpeas content identifier as integer.
   */
  <T extends Contribution> int getOrCreateSilverContentId(T contribution);

  /**
   * <p>
   * Gets the silverpeas content identifier for given resource (represented by an id) in the given
   * component instance (represented by an id).
   * </p>
   * <p>
   * In a first time, the {@link Contribution} from the given identifiers is retrieved.<br>
   * An error is thrown if it does not exists.<br>
   * Then the getting or creating of the linked silverpeas content id is performed.<br>
   * For performances, and if it knows his context, the caller can performed {@link
   * ContentManager#getSilverContentId(String, String)} before calling this method.
   * </p>
   * <p>
   * If no silverpeas content identifier exists, then it is created.
   * </p>
   * @param resourceId a resource identifier.
   * @param componentInstanceId a component instance identifier.
   * @return a silverpeas content identifier as integer.
   * @throws IllegalArgumentException if no {@link Contribution} can be retrieved from the given
   * identifier.
   */
  int getOrCreateSilverContentId(String resourceId, String componentInstanceId);

  /**
   * Creates a silverpeas content from a contribution identifier and the instance of the component
   * instance the resource belong to.
   * @param contribution a contribution.
   * @param userId the identifier of the user responsible of the creation.
   * @param <T> a contribution implementation.
   * @return the identifier of the silverpeas content of the contribution as integer.
   */
  <T extends Contribution> int createSilverContent(T contribution, String userId);

  /**
   * Deletes a silverpeas content from a resource identifier and the instance of the component
   * instance the resource belong to.
   * @param resourceId the identifier of a resource.
   * @param componentInstanceId the identifier of the component instance the resource belong to.
   */
  void deleteSilverContent(String resourceId, String componentInstanceId);
}
