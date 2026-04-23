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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.contentcontainer.content;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.model.*;
import org.silverpeas.kernel.SilverpeasRuntimeException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.annotation.NonNull;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Centralization of common treatments between all implementations of {@link SilverpeasContentManager}.
 * @author silveryocha
 */
public abstract class AbstractSilverpeasContentManager implements SilverpeasContentManager {

  protected ContentManagementEngine getContentManager() {
    return ContentManagementEngineProvider.getContentManagementEngine();
  }

  @Override
  public List<ManagedContribution> getSilverContentByReference(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    List<Contribution> contributions =
        getAccessibleContributions(resourceReferences, currentUserId);
    return contributions.stream().map(this::convert).collect(Collectors.toList());
  }

  @Override
  public <T extends Contribution> int getSilverContentId(final T contribution) {
    final ContributionIdentifier contributionId = contribution.getIdentifier();
    return getSilverContentId(contributionId.getLocalId(), contributionId.getComponentInstanceId());
  }

  @Override
  public <T extends Contribution> int getOrCreateSilverContentId(final T contribution) {
    int contentId = getSilverContentId(contribution);
    if (contentId == -1) {
      contentId = createSilverContent(contribution, contribution.getLastUpdater().getId());
    }
    return contentId;
  }

  @Override
  public int getOrCreateSilverContentId(final String resourceId, final String componentInstanceId) {
    Contribution contribution = getContribution(resourceId, componentInstanceId).orElseThrow(
        () -> new IllegalArgumentException(MessageFormat
            .format("impossible to get contribution from id {0} into component instance {1}",
                resourceId, componentInstanceId)));
    return getOrCreateSilverContentId(contribution);
  }

  /**
   * <p>
   * Gets the contribution from its identifier and the identifier of the component instance.
   * </p>
   * <p>
   * The user rights are not verified here as this method must be used only for silverpeas content
   * id management and not to provide the data to another service.
   * </p>
   * @param resourceId a resource identifier.
   * @param componentInstanceId a component instance identifier.
   * @return an optional {@link Contribution} instance.
   */
  protected abstract Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId);

  /**
   * <p>
   * Gets list of contribution from their identifier and the identifier of the component instance.
   * </p>
   * <p>
   * Implementations of this method must handle the user rights on the resource.
   * </p>
   * @param resourceReferences a list of resource identifier.
   * @param currentUserId the identifier of the user accessing the content.
   * @return an optional {@link Contribution} instance.
   */
  protected abstract List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId);

  /**
   * Same as {@link #getSilverContentId(Contribution)}, but giving resource identifier and component
   * instance identifier instead of {@link Contribution} instance.
   * @param resourceId a resource identifier.
   * @param componentInstanceId the identifier of a component.
   * @return a silverpeas content identifier as integer.
   */
  public int getSilverContentId(String resourceId, String componentInstanceId) {
    try {
      return getContentManager().getSilverContentId(resourceId, componentInstanceId);
    } catch (ContentManagerException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public <T extends Contribution> int createSilverContent(final T contribution,
      final String userId) {
    return Transaction.performInOne(() -> {
      try (Connection connection = DBUtil.openConnection()) {
        return createSilverContent(connection, contribution, userId);
      }
    });
  }

  /**
   * Same as {@link #createSilverContent(Contribution, String)}, but here the {@link Connection}
   * is given.
   * @throws ContentManagerException on technical error.
   */
  public int createSilverContent(final Connection connection, @NonNull final Contribution contribution,
      final String userId) throws ContentManagerException {
    final ContributionIdentifier contributionId = contribution.getIdentifier();
    final SilverContentVisibility scv = computeSilverContentVisibility(contribution);
    return getContentManager().addSilverContent(connection, contributionId.getLocalId(),
        contributionId.getComponentInstanceId(), userId, scv);
  }

  /**
   * Same as {@link #createSilverContent(Contribution, String)}, but here the {@link Connection}
   * is given.
   * @throws ContentManagerException on technical error.
   */
  public int createSilverContent(final Connection connection, @NonNull final Contribution contribution,
      final String userId, final boolean visible) throws ContentManagerException {
    final ContributionIdentifier contributionId = contribution.getIdentifier();
    final SilverContentVisibility scv = computeSilverContentVisibility(contribution);
    scv.setVisibilityAttributes(visible);
    return getContentManager().addSilverContent(connection, contributionId.getLocalId(),
        contributionId.getComponentInstanceId(), userId, scv);
  }

  /**
   * Updates the visibility of the given contribution.
   * @param contribution the content.
   */
  public <T extends Contribution> void updateSilverContentVisibility(@NonNull T contribution)
      throws ContentManagerException {
    int silverContentId = getSilverContentId(contribution);
    if (silverContentId == -1) {
      createSilverContent(null, contribution, contribution.getLastUpdater().getId());
    } else {
      SilverContentVisibility scv = computeSilverContentVisibility(contribution);
      getContentManager().updateSilverContentVisibilityAttributes(scv, silverContentId);
    }
  }

  /**
   * Update the visibility of the given contribution by explicitly giving it.
   * @param contribution the content
   * @param visibility forces the visibility so that it bypasses the one set by {@link
   * #computeSilverContentVisibility(Contribution)}. Only taken into account on a real update.
   */
  public <T extends Contribution> void updateSilverContentVisibility(@NonNull T contribution,
      boolean visibility) throws ContentManagerException {
    int silverContentId = getSilverContentId(contribution);
    if (silverContentId == -1) {
      createSilverContent(null, contribution, contribution.getLastUpdater().getId(), visibility);
    } else {
      SilverContentVisibility scv = computeSilverContentVisibility(contribution);
      scv.setVisibilityAttributes(visibility);
      getContentManager().updateSilverContentVisibilityAttributes(scv, silverContentId);
    }
  }

  /**
   * Gets the default content visibility of the given contribution. Useful into creation process.
   * @param contribution a contribution.
   * @return a {@link SilverContentVisibility} instance initialized with default data.
   */
  protected <T extends Contribution> SilverContentVisibility computeSilverContentVisibility(
      @NonNull final T contribution) {
    Objects.requireNonNull(contribution);
    return new SilverContentVisibility();
  }

  @Override
  public void deleteSilverContent(final String resourceId, final String componentInstanceId) {
    Transaction.performInOne(() -> {
      try (Connection connection = DBUtil.openConnection()) {
        deleteSilverContent(connection, resourceId, componentInstanceId);
      }
      return null;
    });
  }

  /**
   * Same as {@link #deleteSilverContent(String, String)}, but here the {@link Contribution}
   * is given instead of resource identifier and component instance identifier.
   */
  public void deleteSilverContent(final Contribution contribution) {
    final ContributionIdentifier contributionId = contribution.getIdentifier();
    deleteSilverContent(contributionId.getLocalId(), contributionId.getComponentInstanceId());
  }

  /**
   * Same as {@link #deleteSilverContent(String, String)}, but here the {@link Connection} is given.
   * @throws ContentManagerException on technical error.
   */
  public void deleteSilverContent(final Connection connection, final String resourceId,
      final String componentInstanceId) throws ContentManagerException {
    int contentId = getSilverContentId(resourceId, componentInstanceId);
    if (contentId != -1) {
      getContentManager().removeSilverContent(connection, contentId);
    }
  }

  /**
   * <p>
   * Converts the given {@link Contribution} into a {@link SilverpeasContent} one.
   * </p>
   * @param <T> the type of the instance.
   * @param instance an instance, a {@link Contribution} for the best.
   * @return a {@link SilverpeasContent} instance.
   */
  private <T extends Contribution> ManagedContribution convert(T instance) {
    final String componentInstanceId = instance.getIdentifier().getComponentInstanceId();
    return new ManagedContribution(instance, getContentIconFileName(componentInstanceId));
  }

  /**
   * Gets the icon url of the resource.
   * @param componentInstanceId the component instance id which could help to determine the right
   * icon.
   * @return the icon file name.
   */
  protected abstract String getContentIconFileName(String componentInstanceId);

}
