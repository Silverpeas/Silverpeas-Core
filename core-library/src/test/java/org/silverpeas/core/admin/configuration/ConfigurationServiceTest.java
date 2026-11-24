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
  @TestManagedMock
  private ConfigurationChangeLog audit;
  private UserProvider users;
  @TestedBean
  private ConfigurationService service;
  private User administrator;

  @BeforeEach
  void setUp() throws IOException {
    users = UserProvider.get();
    administrator = mock(User.class);
    when(administrator.isAccessAdmin()).thenReturn(true);
    when(users.getCurrentRequester()).thenReturn(administrator);
    var target = new CustomSettingsTarget("CustomerSettings.xml", "absent", false);
    when(customSettings.list()).thenReturn(List.of(target));
    when(customSettings.persistAndApply(any(), anyString(), anyMap(), any()))
        .thenAnswer(invocation -> new CustomSettingsRepository.PersistedConfiguration(
            invocation.getArgument(3, CustomSettingsRepository.ConfigurationWrite.class).save(),
            invocation.getArgument(0), new CustomSettingsTarget("CustomerSettings.xml", "saved", true)));
  }

  @Test
  void anonymousAndNonAdministratorUsersCannotAccessAnyOperation() {
    when(users.getCurrentRequester()).thenReturn(null);
    assertAccessDenied();
    when(users.getCurrentRequester()).thenReturn(administrator);
    when(administrator.isAccessAdmin()).thenReturn(false);
    assertAccessDenied();
    assertThrows(SecurityException.class, () -> service.search("logo"));
    verifyNoInteractions(repository, customSettings, audit);
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
    var before = new ConfigurationFile(saved.path(), "before", Map.of("flag", "false"));
    when(repository.read(saved.path())).thenReturn(before);
    when(repository.update(saved.path(), "before", changes)).thenReturn(saved);
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertEquals(saved, service.update(saved.path(), "before", changes));
      locator.verify(ResourceLocator::resetCache);
      verify(audit).propertiesChanged(administrator, before, saved);
    }
  }

  @Test
  void noChangeDoesNotInvalidateTheKernelCache() throws IOException {
    var file = new ConfigurationFile("general.properties", "same", Map.of());
    when(repository.read(file.path())).thenReturn(file);
    when(repository.update(file.path(), file.revision(), Map.of())).thenReturn(file);
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertEquals(file, service.update(file.path(), file.revision(), Map.of()));
      locator.verifyNoInteractions();
    }
  }

  @Test
  void failedSaveDoesNotInvalidateTheKernelCache() throws IOException {
    when(repository.read("general.properties")).thenReturn(
        new ConfigurationFile("general.properties", "old", Map.of("flag", "false")));
    when(repository.update("general.properties", "old", Map.of("flag", "true")))
        .thenThrow(new IOException("Not writable"));
    var changes = Map.of("flag", "true");
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertThrows(IOException.class, () -> service.update("general.properties", "old", changes));
      locator.verifyNoInteractions();
      verifyNoInteractions(audit);
    }
  }

  @Test
  void conflictingSaveDoesNotInvalidateTheKernelCache() throws IOException {
    when(repository.read("general.properties")).thenReturn(
        new ConfigurationFile("general.properties", "old", Map.of("flag", "false")));
    when(repository.update("general.properties", "old", Map.of("flag", "true")))
        .thenThrow(new ConcurrentModificationException("Stale revision"));
    var changes = Map.of("flag", "true");
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertThrows(ConcurrentModificationException.class,
          () -> service.update("general.properties", "old", changes));
      locator.verifyNoInteractions();
    }
  }

  @Test
  void rejectsAnAlreadyStaleSnapshotBeforeWritingOrAuditing() throws IOException {
    when(repository.read("general.properties")).thenReturn(
        new ConfigurationFile("general.properties", "newer", Map.of("flag", "false")));
    var changes = Map.of("flag", "true");
    assertThrows(ConcurrentModificationException.class,
        () -> service.update("general.properties", "old", changes));
    verify(repository, never()).update(anyString(), anyString(), anyMap());
    verifyNoInteractions(audit);
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
      when(repository.read(relativePath)).thenReturn(before);
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

  @Test
  void sensitivePropertiesAreHiddenAndCannotBeChangedOrPersisted() throws IOException {
    var file = new ConfigurationFile("general.properties", "current",
        Map.of("smtp.password", "hidden", "logo", "visible"),
        new ConfigurationFile.Documentation("Help", Map.of("smtp.password", "Secret help",
            "logo", "Logo help")));
    when(repository.list()).thenReturn(List.of(file.path()));
    when(repository.read(file.path())).thenReturn(file);
    var visible = service.read(file.path());
    assertEquals(Map.of("logo", "visible"), visible.properties());
    assertEquals(Map.of("logo", "Logo help"), visible.documentation().properties());
    assertEquals("current", visible.revision());
    assertTrue(service.search("password").isEmpty());
    var path = file.path();
    var changes = Map.of("logo", "new", "smtp.password", "changed");
    var selectedKeys = java.util.Set.of("smtp.password");
    assertThrows(SecurityException.class, () -> service.update(path, "current", changes));
    assertThrows(SecurityException.class, () -> service.persist(path, "current", selectedKeys, null));
    verify(repository, never()).update(anyString(), anyString(), anyMap());
    verifyNoInteractions(customSettings, audit);
  }

  @Test
  void savingAnOrdinaryPropertyDoesNotExposeOrOverwriteHiddenProperties() throws IOException {
    var before = new ConfigurationFile("general.properties", "before",
        Map.of("token", "hidden", "logo", "old"));
    var after = new ConfigurationFile(before.path(), "after",
        Map.of("token", "hidden", "logo", "new"));
    var changes = Map.of("logo", "new");
    when(repository.read(before.path())).thenReturn(before);
    when(repository.update(before.path(), "before", changes)).thenReturn(after);
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertEquals(Map.of("logo", "new"),
          service.update(before.path(), "before", changes).properties());
      verify(repository).update(before.path(), "before", changes);
      locator.verify(ResourceLocator::resetCache);
    }
  }

  @Test
  void automaticPersistenceUsesTheLastCustomXmlAndOnlyActualChanges() throws IOException {
    var first = new CustomSettingsTarget("Company.xml", "first", true);
    var last = new CustomSettingsTarget("Z-Overrides.xml", "last", true);
    var before = new ConfigurationFile("general.properties", "before",
        Map.of("flag", "false", "unchanged", "keep"));
    var after = new ConfigurationFile(before.path(), "after",
        Map.of("flag", "true", "unchanged", "keep"));
    when(customSettings.list()).thenReturn(List.of(first, last));
    when(repository.read(before.path())).thenReturn(before);
    when(repository.update(before.path(), "before", Map.of("flag", "true"))).thenReturn(after);
    assertEquals(after, service.update(before.path(), "before",
        Map.of("flag", "true", "unchanged", "keep")));
    verify(customSettings).persistAndApply(eq(last), eq(before.path()),
        eq(Map.of("flag", "true")), any());
    verify(audit).settingsPersisted(eq(administrator),
        eq(new ConfigurationFile(before.path(), "after", Map.of("flag", "true"))), eq(last), any());
  }

  @Test
  void failedAutomaticPersistenceDoesNotWritePropertiesOrAuditSuccess() throws IOException {
    var before = new ConfigurationFile("general.properties", "before", Map.of("flag", "false"));
    when(repository.read(before.path())).thenReturn(before);
    doThrow(new IOException("XML not writable")).when(customSettings)
        .persistAndApply(any(), anyString(), anyMap(), any());
    var changes = Map.of("flag", "true");
    try (var locator = mockStatic(ResourceLocator.class)) {
      assertThrows(IOException.class, () -> service.update("general.properties", "before", changes));
      locator.verifyNoInteractions();
    }
    verify(repository, never()).update(anyString(), anyString(), anyMap());
    verifyNoInteractions(audit);
  }

  @Test
  void savingAndRestoringAValueUpdatesBothRealFiles(@org.junit.jupiter.api.io.TempDir Path home)
      throws IOException {
    var root = Files.createDirectories(home.resolve("properties"));
    var path = Files.writeString(root.resolve("settings.properties"), "# Keep this help\nflag=false\nother=keep\n");
    var realProperties = new ConfigurationFileRepository(root);
    var realSettings = new CustomSettingsRepository(root);
    when(repository.read("settings.properties"))
        .thenAnswer(invocation -> realProperties.read("settings.properties"));
    when(repository.update(eq("settings.properties"), anyString(), anyMap()))
        .thenAnswer(invocation -> realProperties.update("settings.properties", invocation.getArgument(1),
            invocation.getArgument(2)));
    when(customSettings.list()).thenAnswer(invocation -> realSettings.list());
    doAnswer(invocation -> realSettings.persistAndApply(invocation.getArgument(0),
        invocation.getArgument(1), invocation.getArgument(2), invocation.getArgument(3)))
        .when(customSettings).persistAndApply(any(), anyString(), anyMap(), any());
    var saved = service.update("settings.properties", realProperties.read("settings.properties").revision(),
        Map.of("flag", "true"));
    var xml = home.resolve("configuration/silverpeas/CustomerSettings.xml");
    assertTrue(Files.readString(xml).contains(">true</parameter>"));
    assertFalse(Files.readString(xml).contains("other"));
    assertTrue(Files.readString(path).contains("flag=true"));
    service.update("settings.properties", saved.revision(), Map.of("flag", "false"));
    assertTrue(Files.readString(xml).contains(">false</parameter>"));
    assertEquals("# Keep this help\nflag=false\nother=keep\n", Files.readString(path));
  }

  private void assertAccessDenied() {
    assertThrows(SecurityException.class, () -> service.list());
    assertThrows(SecurityException.class, () -> service.read("general.properties"));
    var changes = Map.<String, String>of();
    assertThrows(SecurityException.class, () -> service.update("general.properties", "", changes));
    assertThrows(SecurityException.class, () -> service.getPersistenceTargets());
    var selectedKeys = java.util.Set.of("flag");
    assertThrows(SecurityException.class,
        () -> service.persist("general.properties", "", selectedKeys, null));
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
    verify(audit).settingsPersisted(administrator,
        new ConfigurationFile("general.properties", "current", Map.of("flag", "true")), target, saved);
  }

  @Test
  void failedXmlWriteIsNotAuditedAsSuccessful() throws IOException {
    var target = new CustomSettingsTarget("CustomerSettings.xml", "old", true);
    when(repository.read("general.properties")).thenReturn(new ConfigurationFile(
        "general.properties", "current", Map.of("flag", "true")));
    when(customSettings.persist(target, "general.properties", Map.of("flag", "true")))
        .thenThrow(new IOException("Not writable"));
    var selectedKeys = java.util.Set.of("flag");
    assertThrows(IOException.class,
        () -> service.persist("general.properties", "current", selectedKeys, target));
    verifyNoInteractions(audit);
  }

  @Test
  void rejectsStaleOrInvalidSelectionsBeforeWritingXml() throws IOException {
    when(repository.read("general.properties")).thenReturn(new ConfigurationFile(
        "general.properties", "current", Map.of("flag", "true")));
    var selectedKeys = java.util.Set.of("flag");
    var noKeys = java.util.Set.<String>of();
    var unknownKeys = java.util.Set.of("unknown");
    assertThrows(ConcurrentModificationException.class,
        () -> service.persist("general.properties", "stale", selectedKeys, null));
    assertThrows(IllegalArgumentException.class,
        () -> service.persist("general.properties", "current", noKeys, null));
    assertThrows(IllegalArgumentException.class,
        () -> service.persist("general.properties", "current", unknownKeys, null));
    verifyNoInteractions(customSettings, audit);
  }
}
