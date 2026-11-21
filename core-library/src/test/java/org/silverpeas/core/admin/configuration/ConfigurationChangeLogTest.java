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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.kernel.logging.SilverLogger;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigurationChangeLogTest {

  private final ConfigurationChangeLog audit = new ConfigurationChangeLog();

  @Test
  void recordsActorAndChangedValuesWithoutFingerprintsOrLeakingSecrets() {
    var actor = mock(User.class);
    when(actor.getId()).thenReturn("42");
    when(actor.getLogin()).thenReturn("admin\nforged-entry");
    when(actor.getDomainId()).thenReturn("0");
    var before = new ConfigurationFile("nested/settings.properties", "before",
        Map.of("apiToken", "old-secret", "a11y.enable", "false", "same", "unchanged"));
    var after = new ConfigurationFile(before.path(), "after",
        Map.of("apiToken", "new-secret", "a11y.enable", "true\nsecond line", "same", "unchanged"));
    var logger = mock(SilverLogger.class);
    try (var factory = mockStatic(SilverLogger.class)) {
      factory.when(() -> SilverLogger.getLogger(ConfigurationChangeLog.NAMESPACE)).thenReturn(logger);
      audit.propertiesChanged(actor, before, after);
      var entry = ArgumentCaptor.forClass(String.class);
      verify(logger).info(eq("{0}"), entry.capture());
      String json = entry.getValue();
      assertFalse(json.contains("\n"));
      assertFalse(json.contains("old-secret"));
      assertFalse(json.contains("new-secret"));
      assertFalse(json.contains("unchanged"));
      var fields = JSONCodec.decode(json, Map.class);
      assertEquals("42", fields.get("userId"));
      assertEquals("admin\nforged-entry", fields.get("login"));
      assertEquals("0", fields.get("domainId"));
      assertEquals("UPDATE_PROPERTIES", fields.get("operation"));
      assertEquals("properties/nested/settings.properties", fields.get("file"));
      assertEquals(List.of("a11y.enable", "apiToken"), fields.get("keys"));
      assertEquals(List.of(
          Map.of("key", "a11y.enable", "before", "false", "after", "true\nsecond line"),
          Map.of("key", "apiToken", "before", "[MASKED]", "after", "[MASKED]")), fields.get("changes"));
      assertFalse(fields.containsKey("revisionBefore"));
      assertFalse(fields.containsKey("revisionAfter"));
      assertDoesNotThrow(() -> Instant.parse((String) fields.get("timestamp")));
    }
  }

  @Test
  void recordsXmlCreationAndUpdatesWithSourceAndSelectedKeysOnly() {
    var actor = mock(User.class);
    var source = new ConfigurationFile("skin.properties", "properties-revision",
        Map.of("logo", "logo.png", "enabled", "true", "db.password", "private-value"));
    var absent = new CustomSettingsTarget("Company.xml", "absent", false);
    var created = new CustomSettingsTarget("Company.xml", "first", true);
    var updated = new CustomSettingsTarget("Company.xml", "second", true);
    var logger = mock(SilverLogger.class);
    try (var factory = mockStatic(SilverLogger.class)) {
      factory.when(() -> SilverLogger.getLogger(ConfigurationChangeLog.NAMESPACE)).thenReturn(logger);
      audit.settingsPersisted(actor, source, absent, created);
      audit.settingsPersisted(actor, source, created, updated);
      var entries = ArgumentCaptor.forClass(String.class);
      verify(logger, times(2)).info(eq("{0}"), entries.capture());
      var first = JSONCodec.decode(entries.getAllValues().getFirst(), Map.class);
      assertEquals("CREATE_CUSTOM_SETTINGS", first.get("operation"));
      assertEquals("UPDATE_CUSTOM_SETTINGS", JSONCodec.decode(
          entries.getAllValues().getLast(), Map.class).get("operation"));
      assertEquals("configuration/silverpeas/Company.xml", first.get("file"));
      assertEquals("properties/skin.properties", first.get("source"));
      entries.getAllValues().forEach(entry -> {
        var fields = JSONCodec.decode(entry, Map.class);
        assertFalse(fields.containsKey("sourceRevision"));
        assertFalse(fields.containsKey("revisionBefore"));
        assertFalse(fields.containsKey("revisionAfter"));
      });
      assertEquals(List.of("db.password", "enabled", "logo"), first.get("keys"));
      assertEquals(Map.of("db.password", "[MASKED]", "enabled", "true", "logo", "logo.png"),
          first.get("values"));
      entries.getAllValues().forEach(entry -> assertFalse(entry.contains("private-value")));
    }
  }

  @Test
  void sensitiveKeysAreMaskedOnBothSides() {
    for (var key : List.of("db.password", "smtp.passwd", "db.pwd", "passphrase", "clientSecret",
        "credential", "authToken", "PRIVATE_KEY", "api.key", "access-key")) {
      var before = new ConfigurationFile("test.properties", "before", Map.of(key, "old-sensitive"));
      var after = new ConfigurationFile(before.path(), "after", Map.of(key, "new-sensitive"));
      var logger = mock(SilverLogger.class);
      try (var factory = mockStatic(SilverLogger.class)) {
        factory.when(() -> SilverLogger.getLogger(ConfigurationChangeLog.NAMESPACE)).thenReturn(logger);
        audit.propertiesChanged(mock(User.class), before, after);
        var entry = ArgumentCaptor.forClass(String.class);
        verify(logger).info(eq("{0}"), entry.capture());
        assertFalse(entry.getValue().contains("old-sensitive"));
        assertFalse(entry.getValue().contains("new-sensitive"));
        assertTrue(entry.getValue().contains("[MASKED]"));
      }
    }
  }

  @Test
  void unchangedFilesProduceNoAuditEntry() {
    var file = new ConfigurationFile("settings.properties", "same", Map.of("flag", "true"));
    var target = new CustomSettingsTarget("CustomerSettings.xml", "same", true);
    try (var factory = mockStatic(SilverLogger.class)) {
      audit.propertiesChanged(mock(User.class), file, file);
      audit.settingsPersisted(mock(User.class), file, target, target);
      factory.verifyNoInteractions();
    }
  }
}
