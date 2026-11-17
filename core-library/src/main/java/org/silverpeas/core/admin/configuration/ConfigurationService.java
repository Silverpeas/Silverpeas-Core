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
import java.util.Set;
import java.util.TreeMap;
import java.util.ConcurrentModificationException;

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
  private CustomSettingsRepository customSettings;
  @Inject
  private UserProvider users;
  @Inject
  private ConfigurationChangeLog audit;

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
   * Reads properties available to the console, excluding automatically detected sensitive keys.
   * @param path relative configuration file path
   * @return immutable snapshot
   * @throws IOException if reading fails
   * @throws SecurityException if the requester is not a general administrator
   */
  public ConfigurationFile read(String path) throws IOException {
    checkAdministrator();
    return visible(repository.read(path));
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
    rejectSensitiveKeys(changes.keySet());
    var before = repository.read(path);
    if (!before.revision().equals(revision)) {
      throw new ConcurrentModificationException("Runtime configuration changed; reload before saving");
    }
    var saved = repository.update(path, revision, changes);
    audit.propertiesChanged(users.getCurrentRequester(), before, saved);
    if (!saved.revision().equals(revision)) {
      ResourceLocator.resetCache();
    }
    return visible(saved);
  }

  /**
   * Searches property keys across every installed properties file, without returning values.
   * @param query case insensitive fragment of a property key
   * @return matching keys grouped by relative file path
   * @throws IOException if a configuration file cannot be read
   */
  public Map<String, List<String>> search(String query) throws IOException {
    checkAdministrator();
    if (query == null || query.isBlank()) {
      throw new IllegalArgumentException("A property key fragment is required");
    }
    var fragment = query.strip().toLowerCase(java.util.Locale.ROOT);
    var matches = new TreeMap<String, List<String>>();
    for (var path : repository.list()) {
      var keys = repository.read(path).properties().keySet().stream()
          .filter(key -> !SensitiveConfiguration.isSensitive(key))
          .filter(key -> key.toLowerCase(java.util.Locale.ROOT).contains(fragment))
          .sorted().toList();
      if (!keys.isEmpty()) {
        matches.put(path, keys);
      }
    }
    return matches;
  }

  private static void rejectSensitiveKeys(Set<String> keys) {
    if (keys.stream().anyMatch(SensitiveConfiguration::isSensitive)) {
      throw new SecurityException("Sensitive configuration cannot be changed through the console");
    }
  }

  private static ConfigurationFile visible(ConfigurationFile file) {
    var properties = new TreeMap<>(file.properties());
    properties.keySet().removeIf(SensitiveConfiguration::isSensitive);
    var comments = new TreeMap<>(file.documentation().properties());
    comments.keySet().retainAll(properties.keySet());
    return new ConfigurationFile(file.path(), file.revision(), properties,
        new ConfigurationFile.Documentation(file.documentation().file(), comments));
  }

  private void checkAdministrator() {
    var requester = users.getCurrentRequester();
    if (requester == null || !requester.isAccessAdmin()) {
      throw new SecurityException("Configuration requires a general administrator");
    }
  }

  /**
   * Lists custom XML installation scripts that can retain settings across a clean installation.
   * @return targets in installation execution order, or the proposed default filename
   * @throws IOException if discovery fails
   */
  public List<CustomSettingsTarget> getPersistenceTargets() throws IOException {
    checkAdministrator();
    return customSettings.list();
  }

  /**
   * Retains selected saved settings in an installation XML script. Values are read on the server,
   * never supplied by the caller, and the runtime properties file is not modified.
   * @param path relative properties file path
   * @param revision revision of the saved file shown in the editor
   * @param keys existing keys explicitly selected by the administrator
   * @param target XML destination and expected revision
   * @return updated XML destination and revision
   * @throws IOException if reading or writing fails
   */
  public CustomSettingsTarget persist(String path, String revision, Set<String> keys,
      CustomSettingsTarget target) throws IOException {
    checkAdministrator();
    var file = repository.read(path);
    if (!file.revision().equals(revision)) {
      throw new ConcurrentModificationException("Runtime configuration changed; reload before persisting");
    }
    if (keys == null || keys.isEmpty() || keys.stream().anyMatch(java.util.Objects::isNull) ||
        !file.properties().keySet().containsAll(keys)) {
      throw new IllegalArgumentException("Select existing saved properties");
    }
    rejectSensitiveKeys(keys);
    var selected = new TreeMap<String, String>();
    keys.forEach(key -> selected.put(key, file.properties().get(key)));
    var saved = customSettings.persist(target, path, selected);
    audit.settingsPersisted(users.getCurrentRequester(),
        new ConfigurationFile(path, file.revision(), selected), target, saved);
    return saved;
  }
}
