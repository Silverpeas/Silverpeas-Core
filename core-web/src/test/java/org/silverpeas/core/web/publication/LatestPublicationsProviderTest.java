/*
 * Copyright (C) 2000 - 2026 Silverpeas
 * SPDX-License-Identifier: AGPL-3.0-or-later
 */
package org.silverpeas.core.web.publication;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.contribution.publication.dao.PublicationCriteria;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.util.SilverpeasList;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LatestPublicationsProviderTest {

  @Test
  void queriesPublicationsForAnAccessibleComponent() {
    OrganizationController organization = mock(OrganizationController.class);
    PublicationService publications = mock(PublicationService.class);
    LatestPublicationsProvider provider = new LatestPublicationsProvider(organization, publications);
    SilverpeasList<PublicationDetail> expected =
        SilverpeasList.wrap(List.of(mock(PublicationDetail.class)));
    when(organization.getAvailCompoIds("123", "1")).thenReturn(new String[] {"kmelia42"});
    when(publications.getAuthorizedPublicationsForUserByCriteria(
        eq("1"), any(PublicationCriteria.class))).thenReturn(expected);

    assertEquals(expected, provider.getPublications("123", "1", 10, true));
  }
}
