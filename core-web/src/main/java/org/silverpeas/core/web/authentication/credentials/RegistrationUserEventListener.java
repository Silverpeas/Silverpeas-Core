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
package org.silverpeas.core.web.authentication.credentials;

import org.silverpeas.core.admin.domain.synchro.SynchroGroupManager;
import org.silverpeas.core.admin.user.notification.UserEvent;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.notification.system.CDIResourceEventListener;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A listener of events about a given user account in Silverpeas.
 * <p>
 *   It is in charge of performing some operations linked to user registration on user
 *   administration events.
 * </p>
 * @author silveryocha
 */
@Bean
@Singleton
public class RegistrationUserEventListener extends CDIResourceEventListener<UserEvent> {

  @Inject
  private SynchroGroupManager synchroGroupManager;

  @Override
  public void onCreation(final UserEvent event) {
    synchronize();
  }

  private void synchronize() {
    if (RegistrationSettings.getSettings().isGroupSynchronizationEnabled()) {
      synchroGroupManager.synchronize();
    }
  }
}
