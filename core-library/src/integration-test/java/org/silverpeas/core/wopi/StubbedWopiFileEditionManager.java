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

package org.silverpeas.core.wopi;

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
public class StubbedWopiFileEditionManager implements WopiFileEditionManager {

  public boolean handled = true;

  @Override
  public void notifyEditionWith(final WopiFile file, final Set<String> userIds) {

  }

  @Override
  public List<WopiFile> getEditedFilesBy(final WopiUser user) {
    return null;
  }

  @Override
  public List<WopiUser> getEditorsOfFile(final WopiFile file) {
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
  public boolean isHandled(final WopiFile file) {
    return handled;
  }

  @Override
  public Optional<WopiEdition> prepareEditionWith(final SilverpeasUserSession spUserSession, final WopiFile file) {
    return Optional.empty();
  }

  @Override
  public <R> R getEditionContextFrom(final String fileId, final String accessToken,
      final BiFunction<Optional<WopiUser>, Optional<WopiFile>, R> contextInitializer) {
    return null;
  }

  @Override
  public void revokeUser(final WopiUser user) {

  }

  @Override
  public void revokeFile(final WopiFile file) {

  }

  @Override
  public List<WopiUser> listCurrentUsers() {
    return null;
  }

  @Override
  public List<WopiFile> listCurrentFiles() {
    return null;
  }
}
