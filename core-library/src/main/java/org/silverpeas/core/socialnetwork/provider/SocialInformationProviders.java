/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.socialnetwork.provider;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.kernel.annotation.Technical;

/**
 * All the available providers of social information.
 * @author mmoquillon
 */
@Technical
@Bean
public class SocialInformationProviders {

  @Inject
  private Instance<SocialEventProvider<?>> socialEventProviders;

  @Inject
  private Instance<SocialMediaProvider<?>> socialMediaProviders;

  @Inject
  private Instance<SocialMediaCommentProvider<?>> socialMediaCommentProviders;

  @Inject
  private Instance<SocialPublicationProvider<?>> socialPublicationProviders;

  @Inject
  private Instance<SocialPublicationCommentProvider<?>> socialPublicationCommentProviders;

  @Inject
  private Instance<SocialNewsCommentProvider<?>> socialNewsCommentProviders;

  @Inject
  private Instance<SocialStatusProvider<?>> socialStatusProviders;

  @Inject
  private Instance<SocialRelationShipProvider<?>> socialRelationShipProviders;

  /**
   * Gets a provider of social events.
   * @return {@link SocialEventProvider} object.
   */
  public SocialEventProvider<?> getSocialEventsProvider() {
    return socialEventProviders.get();
  }

  /**
   * Gets a provider of medias (images and videos) authored in a user social network.
   * @return {@link SocialMediaProvider} object.
   */
  public SocialMediaProvider<?> getSocialMediaProvider() {
    return socialMediaProviders.get();
  }

  /**
   * Gets a provider of comments on social medias authored in a user social network.
   * @return {@link SocialMediaCommentProvider} object.
   */
  public SocialMediaCommentProvider<?> getSocialMediaCommentProvider() {
    return socialMediaCommentProviders.get();
  }

  /**
   * Gets a provider of publications authored in a user social network.
   * @return {@link SocialPublicationProvider} object.
   */
  public SocialPublicationProvider<?> getSocialPublicationProvider() {
    return socialPublicationProviders.get();
  }

  /**
   * Gets a provider of comments on publications authored in a user social network.
   * @return {@link SocialPublicationCommentProvider} object.
   */
  public SocialPublicationCommentProvider<?> getSocialPublicationCommentProvider() {
    return socialPublicationCommentProviders.get();
  }

  /**
   * Gets a provider of comments on news published in a user social network.
   * @return {@link SocialNewsCommentProvider} object.
   */
  public SocialNewsCommentProvider<?> getSocialNewsCommentProvider() {
    return socialNewsCommentProviders.get();
  }

  /**
   * Gets a provider of status of users.
   * @return {@link SocialStatusProvider} object.
   */
  public SocialStatusProvider<?> getSocialStatusProvider() {
    return socialStatusProviders.get();
  }

  /**
   * Gets provider of user relationships.
   * @return {@link SocialRelationShipProvider} object.
   */
  public SocialRelationShipProvider<?> getSocialRelationShipProvider() {
    return socialRelationShipProviders.get();
  }
}
  