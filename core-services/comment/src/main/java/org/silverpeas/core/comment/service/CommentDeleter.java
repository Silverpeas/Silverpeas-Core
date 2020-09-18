/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.comment.service;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.contribution.ContributionDeletion;
import org.silverpeas.core.contribution.model.Contribution;

import javax.inject.Inject;

/**
 * Deleter of comments relative to a contribution that has been deleted.
 * @author mmoquillon
 */
@Service
public class CommentDeleter implements ContributionDeletion {

  @Inject
  private CommentService commentService;

  @Override
  public void delete(final Contribution contribution) {
    if (!contribution.getContributionType().equals(Comment.CONTRIBUTION_TYPE)) {
      try {
        final ResourceReference ref = ResourceReference.to(contribution.getContributionId());
        commentService.deleteAllCommentsOnPublication(contribution.getContributionType(), ref);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }
    }
  }
}
  