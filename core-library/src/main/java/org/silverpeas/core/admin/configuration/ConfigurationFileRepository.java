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

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.kernel.SilverpeasResourcesLocation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ConcurrentModificationException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Storage of existing properties under the Silverpeas configuration root.
 * Callers must perform authorization before accessing this internal repository.
 * Concurrent updates through this singleton are serialized; revisions also detect external edits
 * made since the file was read. Symbolic links are intentionally not supported.
 */
@Repository
public class ConfigurationFileRepository {

  private final Path root;

  public ConfigurationFileRepository() {
    this(SilverpeasResourcesLocation.getInstance().getConfigurationFilesRootPath());
  }

  ConfigurationFileRepository(Path root) {
    this.root = root.toAbsolutePath().normalize();
  }

  /**
   * Lists properties files recursively, without exposing absolute server paths or values.
   * @return sorted relative paths retaining the directory hierarchy
   * @throws IOException if the directory cannot be read
   */
  public List<String> list() throws IOException {
    try (var files = Files.walk(root)) {
      return files.filter(path -> Files.isRegularFile(path, NOFOLLOW_LINKS))
          .filter(path -> path.toString().endsWith(".properties"))
          .map(root::relativize)
          .map(path -> path.toString().replace('\\', '/'))
          .sorted().toList();
    }
  }

  /**
   * Reads an existing file without expanding variables in its property values.
   * @param relativePath the path relative to the properties root
   * @return immutable snapshot with its content revision
   * @throws IOException if the file cannot be read
   * @throws IllegalArgumentException if the path is not an allowed configuration file
   */
  public ConfigurationFile read(String relativePath) throws IOException {
    var path = resolve(relativePath);
    return snapshot(relativePath, Files.readAllBytes(path));
  }

  /**
   * Atomically patches existing keys. Comments and unchanged entries are retained.
   * No file is created and no property is removed. A filesystem supporting atomic replacement is
   * required; failure leaves the original file intact.
   * @param relativePath relative configuration file path
   * @param expectedRevision revision obtained when reading the file
   * @param changes raw values for the existing keys to update, including empty values
   * @return the saved snapshot
   * @throws IOException if reading or atomic replacement fails
   * @throws ConcurrentModificationException if the expected revision is stale
   * @throws IllegalArgumentException if a path, key or value is invalid
   */
  public synchronized ConfigurationFile update(String relativePath, String expectedRevision,
      Map<String, String> changes) throws IOException {
    var path = resolve(relativePath);
    var original = Files.readAllBytes(path);
    checkRevision(original, expectedRevision);
    var document = new PropertiesDocument(original);
    var updates = validateChanges(document.values(), changes);
    if (updates.isEmpty()) {
      return snapshot(relativePath, original);
    }
    var updated = document.update(updates);
    var result = snapshot(relativePath, updated);
    var temporary = Files.createTempFile(path.getParent(), ".configuration-", ".tmp");
    try {
      preservePermissions(path, temporary);
      Files.write(temporary, updated);
      // Detect external edits occurring while the replacement is being prepared.
      checkRevision(Files.readAllBytes(resolve(relativePath)), expectedRevision);
      Files.move(temporary, path, ATOMIC_MOVE, REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
    return result;
  }

  private Path resolve(String relativePath) throws IOException {
    if (relativePath == null || relativePath.isBlank() || relativePath.contains("\\")) {
      throw new IllegalArgumentException("A relative properties file path is required");
    }
    var relative = Path.of(relativePath);
    if (relative.isAbsolute() || !relative.normalize().equals(relative) ||
        !relativePath.endsWith(".properties")) {
      throw new IllegalArgumentException("Invalid configuration file path");
    }
    var path = root.resolve(relative).normalize();
    if (!path.startsWith(root) || path.equals(root)) {
      throw new IllegalArgumentException("Configuration file is outside the properties root");
    }
    var current = root;
    for (var part : relative) {
      current = current.resolve(part);
      if (Files.isSymbolicLink(current)) {
        throw new IllegalArgumentException("Symbolic links are not allowed in configuration paths");
      }
    }
    if (!Files.isRegularFile(path, NOFOLLOW_LINKS)) {
      throw new java.nio.file.NoSuchFileException(relativePath);
    }
    return path;
  }

  private static Map<String, String> validateChanges(Map<String, String> values,
      Map<String, String> changes) {
    if (changes == null || changes.entrySet().stream().anyMatch(entry -> entry.getKey() == null ||
        entry.getValue() == null || !values.containsKey(entry.getKey()))) {
      throw new IllegalArgumentException("Only existing properties with non-null values can be updated");
    }
    var updates = new TreeMap<String, String>();
    changes.forEach((key, value) -> {
      if (!value.equals(values.get(key))) {
        updates.put(key, value);
      }
    });
    return updates;
  }

  private static ConfigurationFile snapshot(String relativePath, byte[] content) throws IOException {
    var document = new PropertiesDocument(content);
    return new ConfigurationFile(relativePath, revision(content), document.values(),
        document.documentation());
  }

  private static void checkRevision(byte[] content, String expectedRevision) {
    if (!revision(content).equals(expectedRevision)) {
      throw new ConcurrentModificationException("Configuration changed; reload before saving");
    }
  }

  private static String revision(byte[] content) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 is required by the Java platform", e);
    }
  }

  private static void preservePermissions(Path source, Path destination) throws IOException {
    var view = Files.getFileAttributeView(source, PosixFileAttributeView.class, NOFOLLOW_LINKS);
    if (view != null) {
      Files.setPosixFilePermissions(destination, view.readAttributes().permissions());
    }
  }
}
