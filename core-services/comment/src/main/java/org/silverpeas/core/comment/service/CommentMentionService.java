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
package org.silverpeas.core.comment.service;

import jakarta.inject.Inject;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.text.RichText;

/** Contextual mention candidates and recipients; no inaccessible contribution is disclosed. */
@Service
public class CommentMentionService {
  @Inject private OrganizationController organization;
  @Inject private ApplicationServiceProvider applications;

  public record Candidate(String id, String name) { }

  public List<Candidate> candidates(ContributionIdentifier target, User requester, String query) {
    var resource = applications.getApplicationServiceById(target.getComponentInstanceId())
        .flatMap(service -> service.<Contribution>getContributionById(target))
        .orElseThrow(() -> new SecurityException("Unknown comment resource"));
    if (!active(requester) || !resource.canBeAccessedBy(requester)) {
      throw new SecurityException("Comment mentions access denied");
    }
    String search = fold(query == null ? "" : query.substring(0, Math.min(query.length(), 80)));
    User[] users = organization.getAllUsers(target.getComponentInstanceId());
    return Arrays.stream(users == null ? new User[0] : users)
        .filter(this::active).filter(user -> fold(user.getDisplayedName()).contains(search))
        .sorted(Comparator.comparing(User::getDisplayedName, String.CASE_INSENSITIVE_ORDER))
        .filter(resource::canBeAccessedBy)
        .map(user -> new Candidate(user.getId(), user.getDisplayedName())).distinct()
        .limit(50).toList();
  }

  public Set<String> recipients(Comment comment, Contribution resource) {
    if (comment.getRichText() == null) { return Set.of(); }
    return RichText.mentionIds(comment.getRichText()).stream()
        .filter(id -> !id.equals(comment.getCreatorId()))
        .map(id -> organization.<User>getUserDetail(id)).filter(this::active)
        .filter(resource::canBeAccessedBy).map(User::getId).collect(Collectors.toSet());
  }

  private boolean active(User user) {
    return user != null && !user.isAnonymous() && !user.isAccessGuest()
        && !user.isRemovedState() && !user.isDeletedState() && !user.isDeactivatedState();
  }

  private String fold(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
  }
}
