/*
 * Copyright (C) 2000 - 2026 Silverpeas
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package org.silverpeas.core.web.publication;

import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds the latest publications in a space for the current reader, independently of any look.
 */
public class LatestPublicationsProvider {

  private final OrganizationController organization;
  private final PublicationService publications;

  public LatestPublicationsProvider() {
    this(OrganizationControllerProvider.getOrganisationController(), PublicationService.get());
  }

  LatestPublicationsProvider(OrganizationController organization, PublicationService publications) {
    this.organization = organization;
    this.publications = publications;
  }

  public List<PublicationDetail> getPublications(String spaceId, String userId, int limit,
      boolean includeSubspaces) {
    if (spaceId == null || spaceId.isBlank() || userId == null || userId.isBlank() ||
        limit < 1) {
      throw new IllegalArgumentException("Invalid latest publications query");
    }

    // getAvailCompoIds performs the space-scoped access check for this user.
    Set<String> accessible = new HashSet<>(
        Arrays.asList(organization.getAvailCompoIds(spaceId, userId)));
    if (!includeSubspaces) {
      accessible.retainAll(Arrays.asList(organization.getAllComponentIds(spaceId)));
    }

    // Match the types supported by the existing KmeliaTransversal implementation.
    List<String> componentIds = accessible.stream()
        .filter(id -> id.startsWith("kmelia") || id.startsWith("toolbox") ||
            id.startsWith("kmax"))
        .sorted()
        .collect(Collectors.toList());
    if (componentIds.isEmpty()) {
      return List.of();
    }

    return publications.getAuthorizedPublicationsForUserByCriteria(userId,
        PublicationCriteria.excludingTrashNodeOnComponentInstanceIds(componentIds)
            .ofStatus(PublicationDetail.VALID_STATUS)
            .visibleAt(OffsetDateTime.now())
            .takingAliasesIntoAccount()
            .orderByDescendingBeginDate()
            .limitTo(limit));
  }
}
