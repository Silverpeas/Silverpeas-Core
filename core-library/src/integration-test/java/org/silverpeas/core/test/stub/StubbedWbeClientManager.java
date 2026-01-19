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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.test.stub;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.wbe.WbeClientManager;
import org.silverpeas.core.wbe.WbeEdition;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeUser;

import java.util.Optional;

/**
 * Implementation of the {@link WbeClientManager} interface to be used in the integration tests. Its
 * aim is to validate the correctly work of {@link org.silverpeas.core.wbe.WbeHostManager} service
 * with managed {@link WbeClientManager} objects as well as the transitive use of the
 * {@link org.silverpeas.core.wbe.WbeHostManager} service in some integration tests implying the JCR
 * or the attachments.
 *
 * @author mmoquillon
 */
@Service
public class StubbedWbeClientManager implements WbeClientManager {

  @Override
  public void clear() {
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled(WbeFile file) {
    return true;
  }

  @Override
  public <T extends WbeEdition> Optional<T> prepareEditionWith(WbeUser user, WbeFile file) {
    return Optional.empty();
  }

  @Override
  public Optional<String> getAdministrationUrl() {
    return Optional.empty();
  }

  @Override
  public String getName(String language) {
    return "StubbedWbeClientManager";
  }
}
  