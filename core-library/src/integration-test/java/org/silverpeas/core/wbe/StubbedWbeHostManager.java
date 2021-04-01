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

package org.silverpeas.core.wbe;

import org.silverpeas.core.security.session.SilverpeasUserSession;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * @author silveryocha
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class StubbedWbeHostManager implements WbeHostManager {

  public boolean handled = true;

  @Override
  public void clear() {

  }

  @Override
  public Optional<String> getClientAdministrationUrl() {
    return Optional.empty();
  }

  @Override
  public void notifyEditionWith(final WbeFile file, final Set<String> userIds) {

  }

  @Override
  public List<WbeFile> getEditedFilesBy(final WbeUser user) {
    return null;
  }

  @Override
  public List<WbeUser> getEditorsOfFile(final WbeFile file) {
    return null;
  }

  @Override
  public void enable(final boolean enable) {

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isHandled(final WbeFile file) {
    return handled;
  }

  @Override
  public <T extends WbeEdition> Optional<T> prepareEditionWith(final SilverpeasUserSession spUserSession, final WbeFile file) {
    return Optional.empty();
  }

  @Override
  public <R> R getEditionContextFrom(final String fileId, final String accessToken,
      final BiFunction<Optional<WbeUser>, Optional<WbeFile>, R> contextInitializer) {
    return null;
  }

  @Override
  public void revokeUser(final WbeUser user) {

  }

  @Override
  public void revokeFile(final WbeFile file) {

  }

  @Override
  public List<WbeUser> listCurrentUsers() {
    return null;
  }

  @Override
  public List<WbeFile> listCurrentFiles() {
    return null;
  }
}
