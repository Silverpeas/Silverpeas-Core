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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ConcurrentModificationException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class ConfigurationFileRepositoryTest {

  @TempDir
  Path root;
  private ConfigurationFileRepository repository;

  @BeforeEach
  void setUp() {
    repository = new ConfigurationFileRepository(root);
  }

  @Test
  void exposesFileAndPropertyHelpWithoutChangingTheComments() throws IOException {
    var content = "# General configuration\n\n# Enable this option\n! Restart is not needed\n"
        + "flag=false\n\n# Dur\\u00e9e en secondes\ntimeout=60\nplain=value\n";
    var path = create("settings.properties", content);
    var file = repository.read("settings.properties");
    assertEquals("General configuration", file.documentation().file());
    assertEquals("Enable this option\n Restart is not needed",
        file.documentation().properties().get("flag"));
    assertEquals("Durée en secondes", file.documentation().properties().get("timeout"));
    assertFalse(file.documentation().properties().containsKey("plain"));
    var documentation = file.documentation().properties();
    assertThrows(UnsupportedOperationException.class, () -> documentation.put("flag", "other"));
    var updated = repository.update(file.path(), file.revision(), Map.of("flag", "true"));
    assertEquals(file.documentation(), updated.documentation());
    assertEquals(content.replace("flag=false", "flag=true"), Files.readString(path));
  }

  @Test
  void associatesInitialHelpWithTheFirstKeyWhenThereIsNoHeaderSeparator() throws IOException {
    create("settings.properties", "# First key help\nflag=false\n# No following key\n");
    var file = repository.read("settings.properties");
    assertEquals("", file.documentation().file());
    assertEquals(Map.of("flag", "First key help"), file.documentation().properties());
  }

  @Test
  void listsOnlyPropertiesRecursivelyWithRelativeSortedPaths() throws IOException {
    create("org/silverpeas/general.properties", "name=Silverpeas\n");
    create("looks/aurora/skin.properties", "enabled=true\n");
    create("empty/.keep", "");
    create("readme.txt", "ignored");
    assertEquals(java.util.List.of("looks/aurora/skin.properties",
        "org/silverpeas/general.properties"), repository.list());
  }

  @Test
  void readsRawValuesWithoutResolvingVariablesAndReturnsImmutableSnapshot() throws IOException {
    create("general.properties", "name=Silverpeas\npath=${SILVERPEAS_HOME}/data\nempty=\n");
    var file = repository.read("general.properties");
    assertEquals("general.properties", file.path());
    assertEquals("${SILVERPEAS_HOME}/data", file.properties().get("path"));
    assertEquals("", file.properties().get("empty"));
    var properties = file.properties();
    assertThrows(UnsupportedOperationException.class, () -> properties.put("name", "other"));
    assertFalse(file.toString().contains("${SILVERPEAS_HOME}"));
  }

  @Test
  void updatesOnlyExistingKeysAndPreservesCommentsOrderAndUnchangedContent() throws IOException {
    var path = create("org/general.properties",
        "# Configuration\n! Documentation\n\nenabled : false\nother = untouched\nlast=no newline");
    var before = repository.read("org/general.properties");
    var after = repository.update(before.path(), before.revision(), Map.of("enabled", "true"));
    assertEquals("# Configuration\n! Documentation\n\nenabled=true\nother = untouched\nlast=no newline",
        Files.readString(path));
    assertEquals("true", after.properties().get("enabled"));
    assertNotEquals(before.revision(), after.revision());
    assertEquals(after, repository.read(before.path()));
  }

  @Test
  void handlesEscapedKeysMultilineValuesDuplicatesAndCommentBackslashes() throws IOException {
    create("settings.properties", "# comment\\\nkey\\ with\\ spaces=old\\\n  value\n"
        + "duplicate=first\nduplicate=last\n");
    var file = repository.read("settings.properties");
    assertEquals("oldvalue", file.properties().get("key with spaces"));
    var updated = repository.update(file.path(), file.revision(),
        Map.of("key with spaces", " été\nnext\\line", "duplicate", "updated"));
    assertEquals(" été\nnext\\line", updated.properties().get("key with spaces"));
    assertEquals("updated", updated.properties().get("duplicate"));
    assertTrue(Files.readString(root.resolve(file.path())).startsWith("# comment\\\n"));
  }

  @Test
  void preservesUtf8AndLegacyLatin1Files() throws IOException {
    var utf8 = create("utf8.properties", "# été\nlabel=été\nflag=false\n");
    var latin1 = root.resolve("latin1.properties");
    Files.writeString(latin1, "# été\nlabel=été\nflag=false\n", ISO_8859_1);
    for (var path : java.util.List.of(utf8, latin1)) {
      var file = repository.read(path.getFileName().toString());
      assertEquals("été", file.properties().get("label"));
      var updated = repository.update(file.path(), file.revision(), Map.of("flag", "true"));
      assertEquals("été", updated.properties().get("label"));
    }
    assertTrue(Files.readString(utf8, UTF_8).startsWith("# été\n"));
    assertTrue(Files.readString(latin1, ISO_8859_1).startsWith("# été\n"));
  }

  @Test
  void supportsCrLfAndCrLineEndingsWithoutLosingOtherEntries() throws IOException {
    for (var ending : java.util.List.of("\r\n", "\r")) {
      var path = create("settings.properties", "# comment" + ending + "flag=false" + ending
          + "other=retained" + ending);
      var file = repository.read("settings.properties");
      var updated = repository.update(file.path(), file.revision(), Map.of("flag", "true"));
      assertEquals("true", updated.properties().get("flag"));
      assertEquals("retained", updated.properties().get("other"));
      assertTrue(Files.readString(path).endsWith("other=retained" + ending));
    }
  }

  @Test
  void invalidUnicodeEscapeFailsWithoutChangingTheFile() throws IOException {
    var path = create("settings.properties", "flag=\\uZZZZ\n");
    assertThrows(IllegalArgumentException.class, () -> repository.read("settings.properties"));
    assertEquals("flag=\\uZZZZ\n", Files.readString(path));
  }

  @Test
  void rejectsStaleRevisionsWithoutOverwritingExternalEdits() throws IOException {
    var path = create("settings.properties", "flag=false\n");
    var before = repository.read("settings.properties");
    Files.writeString(path, "flag=external\n");
    var changes = Map.of("flag", "true");
    var relativePath = before.path();
    var revision = before.revision();
    assertThrows(ConcurrentModificationException.class,
        () -> repository.update(relativePath, revision, changes));
    assertEquals("flag=external\n", Files.readString(path));
  }

  @Test
  void rejectsUnknownKeysAndNullValuesWithoutChangingTheFile() throws IOException {
    var path = create("settings.properties", "flag=false\n");
    var file = repository.read("settings.properties");
    var relativePath = file.path();
    var revision = file.revision();
    var unknownKey = Map.of("newKey", "value");
    assertThrows(IllegalArgumentException.class,
        () -> repository.update(relativePath, revision, unknownKey));
    var invalid = new java.util.HashMap<String, String>();
    invalid.put("flag", null);
    assertThrows(IllegalArgumentException.class,
        () -> repository.update(relativePath, revision, invalid));
    assertEquals("flag=false\n", Files.readString(path));
  }

  @Test
  void emptyOrUnchangedUpdateDoesNotRewriteTheFile() throws IOException {
    var path = create("settings.properties", "flag : false\n");
    var file = repository.read("settings.properties");
    assertEquals(file, repository.update(file.path(), file.revision(), Map.of()));
    assertEquals(file, repository.update(file.path(), file.revision(), Map.of("flag", "false")));
    assertEquals("flag : false\n", Files.readString(path));
  }

  @Test
  void rejectsPathsOutsideTheRootAndFilesOtherThanProperties() throws IOException {
    create("readme.txt", "flag=false");
    for (var path : java.util.List.of("../escape.properties", "/tmp/escape.properties",
        "org/../../escape.properties", "org/../general.properties", "readme.txt", "")) {
      assertThrows(IllegalArgumentException.class, () -> repository.read(path), path);
    }
    assertThrows(IOException.class, () -> repository.read("missing.properties"));
  }

  @Test
  void excludesAndRejectsSymbolicLinksIncludingLinksInsideTheRoot() throws IOException {
    var original = create("original.properties", "flag=false\n");
    Files.createSymbolicLink(root.resolve("alias.properties"), original);
    Files.createSymbolicLink(root.resolve("folder"), root);
    assertEquals(java.util.List.of("original.properties"), repository.list());
    assertThrows(IllegalArgumentException.class, () -> repository.read("alias.properties"));
    assertThrows(IllegalArgumentException.class, () -> repository.read("folder/original.properties"));
    assertEquals("flag=false\n", Files.readString(original));
  }

  @Test
  void preservesFilePermissionsAndLeavesNoTemporaryFile() throws IOException {
    var path = create("settings.properties", "flag=false\n");
    var permissions = PosixFilePermissions.fromString("rw-r-----");
    Files.setPosixFilePermissions(path, permissions);
    var file = repository.read("settings.properties");
    repository.update(file.path(), file.revision(), Map.of("flag", "true"));
    assertEquals(permissions, Files.getPosixFilePermissions(path));
    try (var files = Files.list(root)) {
      assertEquals(1, files.count());
    }
  }

  private Path create(String relativePath, String content) throws IOException {
    var path = root.resolve(relativePath);
    Files.createDirectories(path.getParent());
    return Files.writeString(path, content);
  }
}
