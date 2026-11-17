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
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class ConfigurationServiceTest {

  @TestManagedMock
  private ConfigurationFileRepository repository;
  @TestManagedMock
  private CustomSettingsRepository customSettings;
  private UserProvider users;
  @TestedBean
  private ConfigurationService service;
  private User administrator;

  @BeforeEach
  void setUp() {
    users = UserProvider.get();
    administrator = mock(User.class);
    when(administrator.isAccessAdmin()).thenReturn(true);
    when(users.getCurrentRequester()).thenReturn(administrator);
  }

  @Test
  void anonymousAndNonAdministratorUsersCannotAccessAnyOperation() {
    when(users.getCurrentRequester()).thenReturn(null);
    assertAccessDenied();
    when(users.getCurrentRequester()).thenReturn(administrator);
    when(administrator.isAccessAdmin()).thenReturn(false);
    assertAccessDenied();
    assertThrows(SecurityException.class, () -> service.search("logo"));
    verifyNoInteractions(repository, customSettings);
  }

  @Test
  void administratorCanListAndReadConfiguration() throws IOException {
    var file = new ConfigurationFile("org/general.properties", "revision", Map.of("flag", "true"));
    when(repository.list()).thenReturn(List.of(file.path()));
    when(repository.read(file.path())).thenReturn(file);
    assertEquals(List.of(file.path()), service.list());
    assertEquals(file, service.read(file.path()));
  }

  @Test
  void searchesKeysAcrossFilesWithoutReturningValues() throws IOException {
    when(repository.list()).thenReturn(List.of("a.properties", "nested/b.properties"));
    when(repository.read("a.properties")).thenReturn(new ConfigurationFile(
        "a.properties", "a", Map.of("logo.Path", "secret", "other", "logo")));
    when(repository.read("nested/b.properties")).thenReturn(new ConfigurationFile(
        "nested/b.properties", "b", Map.of("home.logo", "value")));
    assertEquals(Map.of("a.properties", List.of("logo.Path"),
        "nested/b.properties", List.of("home.logo")), service.search(" LOGO "));
    assertTrue(service.search("missing").isEmpty());
    assertThrows(IllegalArgumentException.class, () -> service.search(" "));
    assertThrows(IllegalArgumentException.class, () -> service.search(null));
  }

  @Test
  void successfulSaveInvalidatesTheKernelCache() throws IOException {
    var saved = new ConfigurationFile("org/general.properties", "after", Map.of("flag", "true"));
    var changes = Map.of("flag", "true");
    when(repository.update(saved.path(), "before", changes)).thenReturn(saved);
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertEquals(saved, service.update(saved.path(), "before", changes));
      locator.verify(ResourceLocator::resetCache);
    }
  }

  @Test
  void noChangeDoesNotInvalidateTheKernelCache() throws IOException {
    var file = new ConfigurationFile("general.properties", "same", Map.of());
    when(repository.update(file.path(), file.revision(), Map.of())).thenReturn(file);
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertEquals(file, service.update(file.path(), file.revision(), Map.of()));
      locator.verifyNoInteractions();
    }
  }

  @Test
  void failedSaveDoesNotInvalidateTheKernelCache() throws IOException {
    when(repository.update("general.properties", "old", Map.of()))
        .thenThrow(new IOException("Not writable"));
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertThrows(IOException.class, () -> service.update("general.properties", "old", Map.of()));
      locator.verifyNoInteractions();
    }
  }

  @Test
  void conflictingSaveDoesNotInvalidateTheKernelCache() throws IOException {
    when(repository.update("general.properties", "old", Map.of()))
        .thenThrow(new ConcurrentModificationException("Stale revision"));
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertThrows(ConcurrentModificationException.class,
          () -> service.update("general.properties", "old", Map.of()));
      locator.verifyNoInteractions();
    }
  }

  @Test
  void retainedSettingBundleSeesTheSavedValueImmediately() throws Exception {
    // Use only this build's test output directory, never the running installation's properties.
    var classpathRoot = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
    var folder = Files.createTempDirectory(classpathRoot, "configuration");
    var path = Files.writeString(folder.resolve("settings.properties"), "flag=false\n");
    var relativePath = classpathRoot.relativize(path).toString().replace('\\', '/');
    var realRepository = new ConfigurationFileRepository(classpathRoot);
    try {
      var bundleName = relativePath.substring(0, relativePath.length() - ".properties".length())
          .replace('/', '.');
      var retainedBundle = ResourceLocator.getSettingBundle(bundleName);
      assertFalse(retainedBundle.getBoolean("flag"));
      var before = realRepository.read(relativePath);
      var changes = Map.of("flag", "true");
      when(repository.update(relativePath, before.revision(), changes))
          .thenAnswer(invocation -> realRepository.update(relativePath, before.revision(), changes));
      service.update(relativePath, before.revision(), changes);
      assertTrue(retainedBundle.getBoolean("flag"));
    } finally {
      ResourceLocator.resetCache();
      Files.deleteIfExists(path);
      Files.deleteIfExists(folder);
    }
  }

  private void assertAccessDenied() {
    assertThrows(SecurityException.class, () -> service.list());
    assertThrows(SecurityException.class, () -> service.read("general.properties"));
    assertThrows(SecurityException.class, () -> service.update("general.properties", "", Map.of()));
    assertThrows(SecurityException.class, () -> service.getPersistenceTargets());
    assertThrows(SecurityException.class,
        () -> service.persist("general.properties", "", java.util.Set.of("flag"), null));
  }

  @Test
  void persistsOnlySelectedSavedValuesAndDoesNotUpdateRuntimeProperties() throws IOException {
    var target = new CustomSettingsTarget("Company.xml", "xml-before", true);
    var saved = new CustomSettingsTarget("Company.xml", "xml-after", true);
    when(customSettings.list()).thenReturn(List.of(target));
    when(repository.read("general.properties")).thenReturn(new ConfigurationFile(
        "general.properties", "current", Map.of("flag", "true", "unselected", "retained")));
    when(customSettings.persist(target, "general.properties", Map.of("flag", "true"))).thenReturn(saved);
    assertEquals(List.of(target), service.getPersistenceTargets());
    assertEquals(saved, service.persist("general.properties", "current", java.util.Set.of("flag"), target));
    verify(customSettings).persist(target, "general.properties", Map.of("flag", "true"));
    verify(repository, never()).update(anyString(), anyString(), anyMap());
  }

  @Test
  void rejectsStaleOrInvalidSelectionsBeforeWritingXml() throws IOException {
    when(repository.read("general.properties")).thenReturn(new ConfigurationFile(
        "general.properties", "current", Map.of("flag", "true")));
    assertThrows(ConcurrentModificationException.class,
        () -> service.persist("general.properties", "stale", java.util.Set.of("flag"), null));
    assertThrows(IllegalArgumentException.class,
        () -> service.persist("general.properties", "current", java.util.Set.of(), null));
    assertThrows(IllegalArgumentException.class,
        () -> service.persist("general.properties", "current", java.util.Set.of("unknown"), null));
    verifyNoInteractions(customSettings);
  }
}
