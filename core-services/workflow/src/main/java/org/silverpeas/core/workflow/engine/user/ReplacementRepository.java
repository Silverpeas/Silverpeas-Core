/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.workflow.engine.user;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;
import org.silverpeas.core.workflow.api.user.Replacement;
import org.silverpeas.core.workflow.api.user.User;

import javax.inject.Singleton;
import java.util.List;

/**
 * Implementation of the business replacement repository by using JPA.
 * @author mmoquillon
 */
@Singleton
@Repository
public class ReplacementRepository extends SilverpeasJpaEntityRepository<ReplacementImpl>
    implements Replacement.Repository {

  @Override
  public Replacement save(final Replacement replacement) {
    return save((ReplacementImpl) replacement);
  }

  @Override
  public void delete(final Replacement replacement) {
    super.delete((ReplacementImpl) replacement);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReplacementImpl> findAllByIncumbentAndByWorkflow(final User user,
      final String workflowInstanceId) {
    NamedParameters parameters = newNamedParameters()
        .add("incumbent", user.getUserId())
        .add("workflow", workflowInstanceId);
    return findByNamedQuery("Replacement.findAllByIncumbentAndByWorkflow", parameters);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReplacementImpl> findAllBySubstituteAndByWorkflow(final User user,
      final String workflowInstanceId) {
    NamedParameters parameters = newNamedParameters()
        .add("substitute", user.getUserId())
        .add("workflow", workflowInstanceId);
    return findByNamedQuery("Replacement.findAllBySubstituteAndByWorkflow", parameters);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReplacementImpl> findAllByWorkflow(final String workflowInstanceId) {
    NamedParameters parameters = newNamedParameters()
        .add("workflow", workflowInstanceId);
    return findByNamedQuery("Replacement.findAllByWorkflow", parameters);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReplacementImpl> findAllByUsersAndByWorkflow(final User incumbent,
      final User substitute, final String workflowInstanceId) {
    NamedParameters parameters = newNamedParameters()
        .add("incumbent", incumbent.getUserId())
        .add("substitute", substitute.getUserId())
        .add("workflow", workflowInstanceId);
    return findByNamedQuery("Replacement.findAllByUsersAndByWorkflow", parameters);
  }
}
  