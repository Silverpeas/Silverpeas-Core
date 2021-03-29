/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.contribution.ContributionDeletion;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import java.sql.Connection;

/**
 * Deleter of silverpeas content relative to a contribution that has been deleted.
 * @author silveryocha
 */
public class SilverpeasContentDeleter implements ContributionDeletion {

  @Inject
  private ContentManagementEngine contentMgtEngine;

  @Override
  public void delete(final Contribution contribution) {
    final ContributionIdentifier contributionId = contribution.getIdentifier();
    try {
      int contentId = contentMgtEngine
          .getSilverContentId(contributionId.getLocalId(), contributionId.getComponentInstanceId());
      if (contentId != -1) {
        Transaction.performInOne(() -> {
          try (Connection connection = DBUtil.openConnection()) {
            contentMgtEngine.removeSilverContent(connection, contentId);
          }
          return null;
        });
      }
    } catch (ContentManagerException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }
}
  