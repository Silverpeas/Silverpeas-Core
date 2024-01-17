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
package org.silverpeas.core.socialnetwork.provider;

import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.socialnetwork.SocialNetworkException;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.core.util.Pair;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bensalem Nabil
 */
@Provider
public class DefaultSocialInformationProviderSwitcher implements SocialInformationProviderSwitcher {

  private Map<SocialInformationType, Pair<SocialInfoListSupplier, SocialInfoListSupplier>>
      providers = new EnumMap(SocialInformationType.class);

  private List<SocialInformationType> exclusion =
      Arrays.asList(SocialInformationType.ALL, SocialInformationType.EVENT,
          SocialInformationType.COMMENT, SocialInformationType.COMMENTPUBLICATION,
          SocialInformationType.COMMENTNEWS, SocialInformationType.COMMENTMEDIA);

  private static final Pair<SocialInfoListSupplier, SocialInfoListSupplier> NOTHING =
      Pair.of(c -> Collections.emptyList(), c -> Collections.emptyList());

  @PostConstruct
  private void initSocialInfoProviders() {
    providers.put(SocialInformationType.EVENT, Pair.of(
        c -> getSocialEventsProvider().getSocialInformationsList(c.getUserId(),
            c.getClassification(), c.getBeginDate(), c.getEndDate()),
        c -> getSocialEventsProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate())));

    providers.put(SocialInformationType.MEDIA, Pair.of(c -> {
      final List<SocialInformation> results =
          getSocialMediaProvider().getSocialInformationList(c.getUserId(), c.getBeginDate(),
              c.getEndDate());
      //MEDIA filter displays also comments on medias
      results.addAll(getSocialInformationsList(SocialInformationType.COMMENTMEDIA, c));
      return results;
    }, c -> {
      final List<SocialInformation> results =
          getSocialMediaProvider().getSocialInformationListOfMyContacts(c.getUserId(),
              c.getContactIds(), c.getBeginDate(), c.getEndDate());
      //MEDIA filter displays also comments on medias
      results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTMEDIA, c));
      return results;
    }));

    providers.put(SocialInformationType.COMMENTMEDIA, Pair.of(
        c -> getSocialMediaCommentProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> getSocialMediaCommentProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate())));

    providers.put(SocialInformationType.PUBLICATION, Pair.of(c -> {
      final List<SocialInformation> results =
          getSocialPublicationProvider().getSocialInformationList(c.getUserId(), c.getBeginDate(),
              c.getEndDate());
      //PUBLICATION filter displays also comments on publications
      results.addAll(getSocialInformationsList(SocialInformationType.COMMENTPUBLICATION, c));
      return results;
    }, c -> {
      final List<SocialInformation> results =
          getSocialPublicationProvider().getSocialInformationListOfMyContacts(c.getUserId(),
              c.getContactIds(), c.getBeginDate(), c.getEndDate());
      //PUBLICATION filter displays also comments on publications
      results.addAll(
          getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTPUBLICATION, c));
      return results;
    }));

    providers.put(SocialInformationType.COMMENTPUBLICATION, Pair.of(c -> {
      final List<SocialInformation> results =
          getSocialPublicationCommentProvider().getSocialInformationList(c.getUserId(),
              c.getBeginDate(), c.getEndDate());
      results.addAll(getSocialInformationsList(SocialInformationType.COMMENTNEWS, c));
      return results;
    }, c -> {
      final List<SocialInformation> results =
          getSocialPublicationCommentProvider().getSocialInformationListOfMyContacts(c.getUserId(),
              c.getContactIds(), c.getBeginDate(), c.getEndDate());
      results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTNEWS, c));
      return results;
    }));

    providers.put(SocialInformationType.COMMENTNEWS, Pair.of(
        c -> getSocialNewsCommentProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> getSocialNewsCommentProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate())));

    providers.put(SocialInformationType.COMMENT, Pair.of(c -> {
      final List<SocialInformation> results =
          getSocialInformationsList(SocialInformationType.COMMENTPUBLICATION, c);
      results.addAll(getSocialInformationsList(SocialInformationType.COMMENTMEDIA, c));
      return results;
    }, c -> {
      final List<SocialInformation> results =
          getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTPUBLICATION, c);
      results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTMEDIA, c));
      return results;
    }));

    providers.put(SocialInformationType.STATUS, Pair.of(
        c -> getSocialStatusProvider().getSocialInformationList(c.getUserId(), c.getBeginDate(),
            c.getEndDate()),
        c -> getSocialStatusProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate())));

    providers.put(SocialInformationType.RELATIONSHIP, Pair.of(
        c -> getSocialRelationShipProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> getSocialRelationShipProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate())));

    providers.put(SocialInformationType.LASTEVENT, Pair.of(
        c -> getSocialEventsProvider().getMyLastSocialInformationsList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> getSocialEventsProvider().getLastSocialInformationsListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate())));

    providers.put(SocialInformationType.ALL, Pair.of(c -> Stream.of(SocialInformationType.values())
        .filter(t -> !exclusion.contains(t))
        .flatMap(t -> getSocialInformationsList(t, c).stream())
        .collect(Collectors.toList()), c -> Stream.of(SocialInformationType.values())
        .filter(t -> !exclusion.contains(t))
        .flatMap(t -> getSocialInformationsListOfMyContacts(t, c).stream())
        .collect(Collectors.toList())));
  }

  @Override
  public List<SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, SocialInfoContext context) {
    try {
      return providers.getOrDefault(socialInformationType, NOTHING).getFirst().apply(context);
    } catch (Exception ex) {
      throw new SocialNetworkException(ex);
    }
  }

  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(
      SocialInformationType socialInformationType, SocialInfoContext context) {
    try {
      return providers.getOrDefault(socialInformationType, NOTHING).getSecond().apply(context);
    } catch (Exception ex) {
      throw new SocialNetworkException(ex);
    }
  }

  /**
   * return the SocialEvent providor (by using Inversion of Control Containers )
   * @return SocialEventsInterface
   */
  public SocialEventProvider getSocialEventsProvider() {
    return SocialEventProvider.get();
  }

  /**
   * return the SocialGallery providor (by using Inversion of Control Containers )
   * @return SocialGalleryInterface
   */
  public SocialMediaProvider getSocialMediaProvider() {
    return SocialMediaProvider.get();
  }

  /**
   * return the SocialCommentGallery providor (by using Inversion of Control Containers )
   * @return SocialCommentGalleryInterface
   */
  public SocialMediaCommentProvider getSocialMediaCommentProvider() {
    return SocialMediaCommentProvider.get();
  }


  /**
   * return the SocialPublications providor (by using Inversion of Control Containers )
   * @return SocialPublicationsInterface
   */
  public SocialPublicationProvider getSocialPublicationProvider() {
    return SocialPublicationProvider.get();
  }

  /**
   * return the SocialCommentPublications providor (by using Inversion of Control Containers )
   * @return SocialCommentPublicationsInterface
   */
  public SocialPublicationCommentProvider getSocialPublicationCommentProvider() {
    return SocialPublicationCommentProvider.get();
  }

  /**
   * return the SocialCommentQuickInfos providor (by using Inversion of Control Containers )
   * @return SocialCommentQuickInfosInterface
   */
  public SocialNewsCommentProvider getSocialNewsCommentProvider() {
    return SocialNewsCommentProvider.get();
  }

  /**
   * return SocialStatus providor (by using Inversion of Control Containers )
   * @return SocialStatusInterface
   */
  public SocialStatusProvider getSocialStatusProvider() {
    return SocialStatusProvider.get();
  }

  /**
   * return the SocialRelationShips providor (by using Inversion of Control Containers )
   * @return SocialRelationShipsInterface
   */
  public SocialRelationShipProvider getSocialRelationShipProvider() {
    return SocialRelationShipProvider.get();
  }

  @FunctionalInterface
  private interface SocialInfoListSupplier
      extends Function<SocialInfoContext, List<SocialInformation>> {

  }
}
