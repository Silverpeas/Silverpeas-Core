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

package org.silverpeas.core.admin.configuration;

import jakarta.inject.Inject;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.kernel.bundle.ResourceLocator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Administrative entry point shared by the generic configuration console and future specialized
 * forms. Every operation requires an authenticated general administrator.
 *
 * The underlying ResourceBundle cache is cleared after a successful modification so even retained
 * SettingBundle instances see new values on their next lookup. Values copied into service fields
 * or system properties at startup require a specific reconfiguration mechanism.
 */
@Service
public class ConfigurationService {

  @Inject
  private ConfigurationFileRepository repository;
  @Inject
  private UserProvider users;

  /**
   * Lists configuration file paths, retaining their directory hierarchy.
   * @return sorted relative paths
   * @throws IOException if listing fails
   * @throws SecurityException if the requester is not a general administrator
   */
  public List<String> list() throws IOException {
    checkAdministrator();
    return repository.list();
  }

  /**
   * Reads raw properties, including any sensitive values, for the general administrator only.
   * Values must not be logged or included in error messages.
   * @param path relative configuration file path
   * @return immutable snapshot
   * @throws IOException if reading fails
   * @throws SecurityException if the requester is not a general administrator
   */
  public ConfigurationFile read(String path) throws IOException {
    checkAdministrator();
    return repository.read(path);
  }

  /**
   * Saves and invalidates cached configuration on this application node.
   * @param path relative configuration file path
   * @param revision revision obtained when reading the file
   * @param changes raw values indexed by existing property keys
   * @return saved snapshot
   * @throws IOException if saving fails
   * @throws SecurityException if the requester is not a general administrator
   */
  public ConfigurationFile update(String path, String revision, Map<String, String> changes)
      throws IOException {
    checkAdministrator();
    var saved = repository.update(path, revision, changes);
    if (!saved.revision().equals(revision)) {
      ResourceLocator.resetCache();
    }
    return saved;
  }

  private void checkAdministrator() {
    var requester = users.getCurrentRequester();
    if (requester == null || !requester.isAccessAdmin()) {
      throw new SecurityException("Configuration requires a general administrator");
    }
  }
}
