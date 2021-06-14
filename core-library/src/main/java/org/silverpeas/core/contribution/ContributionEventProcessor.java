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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.notification.system.AbstractResourceEvent;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.util.ServiceProvider;

import javax.ejb.Singleton;

/**
 * Processor listening for events on a contribution to perform additional tasks relative to the
 * contribution concerned by the received event. Such tasks are commonly for allocating, cleaning up
 * or updating the resources that allocated for the contribution (like the attachments for example).
 * @author mmoquillon
 */
@Technical
@Bean
@Singleton
public class ContributionEventProcessor
    extends CDIResourceEventListener<AbstractResourceEvent<? extends Contribution>> {

  @Override
  public void onUpdate(final AbstractResourceEvent<? extends Contribution> event)
      throws Exception {
    ServiceProvider.getAllServices(ContributionModification.class).forEach(
        s -> s.update(event.getTransition().getBefore(), event.getTransition().getAfter()));
  }

  @Override
  public void onDeletion(final AbstractResourceEvent<? extends Contribution> event)
      throws Exception {
    ServiceProvider.getAllServices(ContributionDeletion.class)
        .forEach(s -> s.delete(event.getTransition().getBefore()));
  }

  @Override
  public void onCreation(final AbstractResourceEvent<? extends Contribution> event)
      throws Exception {
    ServiceProvider.getAllServices(ContributionCreation.class)
        .forEach(s -> s.create(event.getTransition().getAfter()));
  }
}
  