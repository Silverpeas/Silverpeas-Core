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

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.DateUtil;

import java.sql.Connection;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Centralization of common treatments between all implementations of {@link ContentInterface}.
 * @author silveryocha
 */
public abstract class AbstractContentInterface implements ContentInterface {

  protected ContentManager getContentManager() {
    return ContentManagerProvider.getContentManager();
  }

  @Override
  public List<SilverContentInterface> getSilverContentByReference(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    List<Contribution> contributions =
        getAccessibleContributions(resourceReferences, currentUserId);
    return contributions.stream().map(this::convert).collect(Collectors.toList());
  }

  @Override
  public <T extends Contribution> int getSilverContentId(final T contribution) {
    final ContributionIdentifier contributionId = contribution.getContributionId();
    return getSilverContentId(contributionId.getLocalId(), contributionId.getComponentInstanceId());
  }

  @Override
  public <T extends Contribution> int getOrCreateSilverContentId(final T contribution) {
    int contentId = getSilverContentId(contribution);
    if (contentId == -1) {
      contentId = createSilverContent(contribution, contribution.getLastModifier().getId());
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
   * id management and not to provide the data to an other service.
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
  public int createSilverContent(final Connection connection, final Contribution contribution,
      final String userId) throws ContentManagerException {
    final ContributionIdentifier contributionId = contribution.getContributionId();
    final SilverContentVisibility scv = computeSilverContentVisibility(contribution);
    return getContentManager().addSilverContent(connection, contributionId.getLocalId(),
        contributionId.getComponentInstanceId(), userId, scv);
  }

  /**
   * Same as {@link #createSilverContent(Contribution, String)}, but here the {@link Connection}
   * is given.
   * @throws ContentManagerException on technical error.
   */
  public int createSilverContent(final Connection connection, final Contribution contribution,
      final String userId, final boolean visible) throws ContentManagerException {
    final ContributionIdentifier contributionId = contribution.getContributionId();
    final SilverContentVisibility scv = computeSilverContentVisibility(contribution);
    scv.setVisibilityAttributes(visible);
    return getContentManager().addSilverContent(connection, contributionId.getLocalId(),
        contributionId.getComponentInstanceId(), userId, scv);
  }

  /**
   * Same as {@link #createSilverContent(Contribution, String)}, but here the {@link Connection}
   * is given and instead of {@link Contribution} resource identifier and component instance
   * identifier are given.<br>
   * Internal method {@link #computeSilverContentVisibility(Contribution)} will be called with a
   * null parameter.
   * @throws ContentManagerException on technical error.
   */
  public int createSilverContent(final Connection connection, final String resourceId,
      final String componentInstanceId, final String userId) throws ContentManagerException {
    final SilverContentVisibility scv = computeSilverContentVisibility(null);
    return getContentManager()
        .addSilverContent(connection, resourceId, componentInstanceId, userId, scv);
  }

  /**
   * Updates the visibility of the given contribution.
   * @param contribution the content.
   */
  public <T extends Contribution> void updateSilverContentVisibility(T contribution)
      throws ContentManagerException {
    int silverContentId = getSilverContentId(contribution);
    if (silverContentId == -1) {
      createSilverContent(null, contribution, contribution.getLastModifier().getId());
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
  public <T extends Contribution> void updateSilverContentVisibility(T contribution,
      boolean visibility) throws ContentManagerException {
    int silverContentId = getSilverContentId(contribution);
    if (silverContentId == -1) {
      createSilverContent(null, contribution, contribution.getLastModifier().getId(), visibility);
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
      final T contribution) {
    if (contribution == null) {
      throw new IllegalArgumentException("contribution parameter must not be null");
    }
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
    final ContributionIdentifier contributionId = contribution.getContributionId();
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
   * Converts the given {@link Contribution} into a {@link SilverContentInterface} one.
   * </p>
   * @param <T> the type of the instance.
   * @param instance an instance, a {@link Contribution} for the best.
   * @return a {@link SilverContentInterface} instance.
   */
  private <T extends Contribution> SilverContentInterface convert(T instance) {
    final String componentInstanceId = instance.getContributionId().getComponentInstanceId();
    return new ContributionWrapper(instance, getContentIconFileName(componentInstanceId));
  }

  /**
   * Gets the icon url of the resource.
   * @param componentInstanceId the component instance id which could help to determine the right
   * icon.
   * @return the icon file name.
   */
  protected abstract String getContentIconFileName(String componentInstanceId);

  /**
   * Wrapper which permits to handle as a {@link SilverContentInterface} each implementation of a
   * {@link Contribution}.
   */
  public static class ContributionWrapper implements SilverContentInterface {

    private final Contribution wrappedInstance;
    private final ContributionIdentifier contributionId;
    private final String contentIconFileName;
    private final SilverContentInterface silverContentInterface;
    private LocalizedContribution contribution;

    private ContributionWrapper(final Contribution contribution, final String contentIconFileName) {
      this.contributionId = contribution.getContributionId();
      this.wrappedInstance = contribution;
      this.contribution = LocalizedContribution.from(contribution);
      this.contentIconFileName = contentIconFileName;
      if (contribution instanceof SilverContentInterface) {
        silverContentInterface = (SilverContentInterface) contribution;
      } else {
        silverContentInterface = null;
      }
    }

    private void setRightLocalizedContribution(String language) {
      if (!language.equals(contribution.getLanguage())) {
        contribution = LocalizedContribution.from(contribution, language);
      }
    }

    public Contribution getWrappedInstance() {
      return wrappedInstance;
    }

    @Override
    public String getId() {
      return contributionId.getLocalId();
    }

    @Override
    public String getName() {
      return contribution.getTitle();
    }

    @Override
    public String getName(final String language) {
      if (silverContentInterface != null) {
        return silverContentInterface.getName(language);
      }
      setRightLocalizedContribution(language);
      return contribution.getTitle();
    }

    @Override
    public String getDescription(final String language) {
      if (silverContentInterface != null) {
        return silverContentInterface.getDescription(language);
      }
      setRightLocalizedContribution(language);
      return contribution.getDescription();
    }

    @Override
    public String getURL() {
      if (silverContentInterface != null) {
        return silverContentInterface.getURL();
      }
      // Indeed, the URL into context of PDC result is not used for now...
      return null;
    }

    @Override
    public String getInstanceId() {
      return contributionId.getComponentInstanceId();
    }

    @Override
    public String getComponentInstanceId() {
      return contributionId.getComponentInstanceId();
    }

    @Override
    public String getDate() {
      if (silverContentInterface != null) {
        return silverContentInterface.getDate();
      }
      return DateUtil.date2SQLDate(contribution.getLastModificationDate());
    }

    @Override
    public String getSilverCreationDate() {
      if (silverContentInterface != null) {
        return silverContentInterface.getSilverCreationDate();
      }
      return DateUtil.date2SQLDate(contribution.getCreationDate());
    }

    @Override
    public String getIconUrl() {
      return contentIconFileName;
    }

    @Override
    public String getCreatorId() {
      if (silverContentInterface != null) {
        return silverContentInterface.getCreatorId();
      }
      return contribution.getCreator().getId();
    }

    @Override
    public User getCreator() {
      return contribution.getCreator();
    }

    @Override
    public User getLastModifier() {
      return contribution.getLastModifier();
    }

    @Override
    public Date getCreationDate() {
      return contribution.getCreationDate();
    }

    @Override
    public Date getLastModificationDate() {
      return contribution.getLastModificationDate();
    }

    @Override
    public String getSilverpeasContentId() {
      if (contribution instanceof SilverpeasContent) {
        return ((SilverpeasContent) contribution).getSilverpeasContentId();
      }
      return SilverContentInterface.super.getSilverpeasContentId();
    }

    @Override
    public ContributionIdentifier getContributionId() {
      return contributionId;
    }

    @Override
    public String getTitle() {
      return contribution.getTitle();
    }

    @Override
    public String getDescription() {
      return contribution.getDescription();
    }

    @Override
    public String getContributionType() {
      return contributionId.getType();
    }

    @Override
    public boolean isIndexable() {
      return contribution.isIndexable();
    }

    @Override
    public boolean canBeAccessedBy(final User user) {
      return contribution.canBeAccessedBy(user);
    }

    @Override
    public boolean canBeModifiedBy(final User user) {
      return contribution.canBeModifiedBy(user);
    }

    @Override
    public boolean canBeDeletedBy(final User user) {
      return contribution.canBeDeletedBy(user);
    }

    @Override
    public Collection<String> getLanguages() {
      if (silverContentInterface != null) {
        return silverContentInterface.getLanguages();
      }
      // for now, no simple implementation of Contribution handles multi-language content.
      // only some kind of SilverContentInterface handle it.
      return Collections.emptyList();
    }
  }
}
