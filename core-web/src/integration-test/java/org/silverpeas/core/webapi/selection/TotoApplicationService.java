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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.selection;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.SettingBundle;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Optional;

/**
 * The service provided by the application Toto. For tests.
 * @author mmoquillon
 */
@Service
@Named("totoService")
public class TotoApplicationService implements ApplicationService {

  @Inject
  private PublicationService publicationService;

  @Override
  @SuppressWarnings("unchecked")
  public Optional<PublicationDetail> getContributionById(
      final ContributionIdentifier contributionId) {
    return Optional.ofNullable(publicationService.getDetail(
        new PublicationPK(contributionId.getLocalId(), contributionId.getComponentInstanceId())));
  }

  @Override
  public SettingBundle getComponentSettings() {
    return null;
  }

  @Override
  public LocalizationBundle getComponentMessages(final String language) {
    return null;
  }

  @Override
  public boolean isRelatedTo(final String instanceId) {
    return instanceId.startsWith("toto");
  }
}
