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

import jakarta.inject.Inject;
import org.silverpeas.core.annotation.Provider;
import org.silverpeas.core.socialnetwork.SocialNetworkException;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.model.SocialInformationType;
import org.silverpeas.kernel.util.Pair;

import jakarta.annotation.PostConstruct;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Bensalem Nabil
 */
@Provider
public class DefaultSocialInformationProviderSwitcher implements SocialInformationProviderSwitcher {

  private final Map<SocialInformationType, Pair<SocialInfoListSupplier, SocialInfoListSupplier>>
      providers = new EnumMap<>(SocialInformationType.class);

  private final List<SocialInformationType> exclusion =
      Arrays.asList(SocialInformationType.ALL, SocialInformationType.EVENT,
          SocialInformationType.COMMENT, SocialInformationType.COMMENTPUBLICATION,
          SocialInformationType.COMMENTNEWS, SocialInformationType.COMMENTMEDIA);

  private static final Pair<SocialInfoListSupplier, SocialInfoListSupplier> NOTHING =
      Pair.of(c -> Collections.emptyList(), c -> Collections.emptyList());


  @Inject
  private SocialInformationProviders siProviders;

  @PostConstruct
  private void initSocialInfoProviders() {
    register(SocialInformationType.EVENT,
        c -> siProviders.getSocialEventsProvider().getSocialInformationsList(c.getUserId(),
            c.getClassification(), c.getBeginDate(), c.getEndDate()),
        c -> siProviders.getSocialEventsProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate()));

    register(SocialInformationType.MEDIA,
        c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(siProviders.getSocialMediaProvider().getSocialInformationList(c.getUserId(), c.getBeginDate(),
              c.getEndDate()));
          //MEDIA filter displays also comments on medias
          results.addAll(getSocialInformationsList(SocialInformationType.COMMENTMEDIA, c));
          return results;
        },
        c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(siProviders.getSocialMediaProvider().getSocialInformationListOfMyContacts(c.getUserId(),
              c.getContactIds(), c.getBeginDate(), c.getEndDate()));
          //MEDIA filter displays also comments on medias
          results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTMEDIA
              , c));
          return results;
        });

    register(SocialInformationType.COMMENTMEDIA,
        c -> siProviders.getSocialMediaCommentProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> siProviders.getSocialMediaCommentProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate()));

    register(SocialInformationType.PUBLICATION,
        c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(siProviders.getSocialPublicationProvider().getSocialInformationList(c.getUserId(),
              c.getBeginDate(),
              c.getEndDate()));
          //PUBLICATION filter displays also comments on publications
          results.addAll(getSocialInformationsList(SocialInformationType.COMMENTPUBLICATION, c));
          return results;
        }, c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(siProviders.getSocialPublicationProvider().getSocialInformationListOfMyContacts(c.getUserId(),
              c.getContactIds(), c.getBeginDate(), c.getEndDate()));
          //PUBLICATION filter displays also comments on publications
          results.addAll(
              getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTPUBLICATION, c));
          return results;
        });

    register(SocialInformationType.COMMENTPUBLICATION,
        c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(siProviders.getSocialPublicationCommentProvider().getSocialInformationList(c.getUserId(),
              c.getBeginDate(), c.getEndDate()));
          results.addAll(getSocialInformationsList(SocialInformationType.COMMENTNEWS, c));
          return results;
        }, c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(siProviders.getSocialPublicationCommentProvider().getSocialInformationListOfMyContacts(c.getUserId(),
              c.getContactIds(), c.getBeginDate(), c.getEndDate()));
          results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTNEWS,
              c));
          return results;
        });

    register(SocialInformationType.COMMENTNEWS,
        c -> siProviders.getSocialNewsCommentProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> siProviders.getSocialNewsCommentProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate()));

    register(SocialInformationType.COMMENT,
        c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(getSocialInformationsList(SocialInformationType.COMMENTPUBLICATION, c));
          results.addAll(getSocialInformationsList(SocialInformationType.COMMENTMEDIA, c));
          return results;
        }, c -> {
          List<SocialInformation> results = new ArrayList<>();
          results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTPUBLICATION,
              c));
          results.addAll(getSocialInformationsListOfMyContacts(SocialInformationType.COMMENTMEDIA
              , c));
          return results;
        });

    register(SocialInformationType.STATUS,
        c -> siProviders.getSocialStatusProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(),
            c.getEndDate()),
        c -> siProviders.getSocialStatusProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate()));

    register(SocialInformationType.RELATIONSHIP,
        c -> siProviders.getSocialRelationShipProvider().getSocialInformationList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> siProviders.getSocialRelationShipProvider().getSocialInformationListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate()));

    register(SocialInformationType.LASTEVENT,
        c -> siProviders.getSocialEventsProvider().getMyLastSocialInformationsList(c.getUserId(),
            c.getBeginDate(), c.getEndDate()),
        c -> siProviders.getSocialEventsProvider().getLastSocialInformationsListOfMyContacts(c.getUserId(),
            c.getContactIds(), c.getBeginDate(), c.getEndDate()));

    register(SocialInformationType.ALL,
        c -> Stream.of(SocialInformationType.values())
            .filter(t -> !exclusion.contains(t))
            .flatMap(t -> getSocialInformationsList(t, c).stream())
            .collect(Collectors.toList()), c -> Stream.of(SocialInformationType.values())
            .filter(t -> !exclusion.contains(t))
            .flatMap(t -> getSocialInformationsListOfMyContacts(t, c).stream())
            .collect(Collectors.toList()));
  }

  @Override
  public List<? extends SocialInformation> getSocialInformationsList(
      SocialInformationType socialInformationType, SocialInfoContext context) {
    try {
      return providers.getOrDefault(socialInformationType, NOTHING).getFirst().apply(context);
    } catch (Exception ex) {
      throw new SocialNetworkException(ex);
    }
  }

  @Override
  public List<? extends SocialInformation> getSocialInformationsListOfMyContacts(
      SocialInformationType socialInformationType, SocialInfoContext context) {
    try {
      return providers.getOrDefault(socialInformationType, NOTHING).getSecond().apply(context);
    } catch (Exception ex) {
      throw new SocialNetworkException(ex);
    }
  }

  private void register(SocialInformationType type,
      SocialInfoListSupplier supplierOfMyInfos, SocialInfoListSupplier supplierOfMyContactsInfo) {
    providers.put(type, Pair.of(supplierOfMyInfos, supplierOfMyContactsInfo));
  }

  @FunctionalInterface
  private interface SocialInfoListSupplier
      extends Function<SocialInfoContext, List<? extends SocialInformation>> {

  }
}
