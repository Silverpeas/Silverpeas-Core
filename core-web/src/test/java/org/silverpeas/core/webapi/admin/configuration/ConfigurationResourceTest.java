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

package org.silverpeas.core.webapi.admin.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.configuration.ConfigurationFile;
import org.silverpeas.core.admin.configuration.ConfigurationService;
import org.silverpeas.core.admin.configuration.CustomSettingsTarget;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class ConfigurationResourceTest {

  @TestManagedMock
  private ConfigurationService service;
  @TestedBean
  private TestResource resource;

  @BeforeEach
  void setUp() {
    resource.user = mock(User.class);
    when(resource.user.isAccessAdmin()).thenReturn(true);
  }

  @Test
  void generalAdministratorCanListReadAndUpdateWithoutCachingResponses() throws IOException {
    var file = new ConfigurationFile("org/general.properties", "revision", Map.of("flag", "true"));
    when(service.list()).thenReturn(List.of(file.path()));
    when(service.read(file.path())).thenReturn(file);
    when(service.update(file.path(), "revision", Map.of("flag", "true"))).thenReturn(file);
    assertEquals(List.of(file.path()), resource.list().getEntity());
    assertEquals(file, resource.read(file.path()).getEntity());
    var response = resource.update(file.path(),
        new ConfigurationResource.Update("revision", Map.of("flag", "true")));
    assertEquals(file, response.getEntity());
    assertEquals("no-store", response.getHeaderString("Cache-Control"));
  }

  @Test
  void searchesKeysWithoutCachingAndRequiresAdministrator() throws IOException {
    var matches = Map.of("nested/general.properties", List.of("home.logo"));
    when(service.search("logo")).thenReturn(matches);
    var response = resource.search("logo");
    assertEquals(matches, response.getEntity());
    assertEquals("no-store", response.getHeaderString("Cache-Control"));
    when(resource.user.isAccessAdmin()).thenReturn(false);
    assertThrows(WebApplicationException.class, () -> resource.search("logo"));
  }

  @Test
  void savesTheFileIdentifiedInTheJsonBodyWithoutAQueryParameter() throws IOException {
    var file = new ConfigurationFile("org/general.properties", "revision", Map.of("flag", "true"));
    when(service.update(file.path(), "revision", file.properties())).thenReturn(file);
    var response = resource.update(null,
        new ConfigurationResource.Update(file.path(), "revision", file.properties()));
    assertEquals(file, response.getEntity());
    verify(service).update(file.path(), "revision", file.properties());
  }

  @Test
  void rejectsMissingAndConflictingPathsBeforeCallingTheService() {
    assertStatus(400, () -> resource.update(null,
        new ConfigurationResource.Update("revision", Map.of())));
    assertStatus(400, () -> resource.update(null,
        new ConfigurationResource.Update(" ", "revision", Map.of())));
    assertStatus(400, () -> resource.update("first.properties",
        new ConfigurationResource.Update("second.properties", "revision", Map.of())));
    verifyNoInteractions(service);
  }

  @Test
  void refusesNonAdministratorsAndAnonymousUsersBeforeCallingTheService() {
    when(resource.user.isAccessAdmin()).thenReturn(false);
    assertForbidden();
    resource.user = null;
    assertForbidden();
    verifyNoInteractions(service);
  }

  @Test
  void rejectsInvalidPayloads() {
    assertStatus(400, () -> resource.update("file", null));
    assertStatus(400, () -> resource.update("file", new ConfigurationResource.Update(null, Map.of())));
    assertStatus(400, () -> resource.update("file", new ConfigurationResource.Update("", Map.of())));
    assertStatus(400, () -> resource.update("file", new ConfigurationResource.Update("revision", null)));
    verifyNoInteractions(service);
  }

  @Test
  void mapsStorageFailuresWithoutExposingServerPathsOrValues() throws IOException {
    when(service.read("missing")).thenThrow(new NoSuchFileException("/private/server/file"));
    when(service.read("invalid")).thenThrow(new IllegalArgumentException("private value"));
    when(service.read("denied")).thenThrow(new SecurityException("private value"));
    when(service.list()).thenThrow(new IOException("/private/server/root"));
    assertStatus(404, () -> resource.read("missing"));
    assertStatus(400, () -> resource.read("invalid"));
    assertStatus(403, () -> resource.read("denied"));
    assertStatus(500, () -> resource.list());
  }

  @Test
  void mapsStaleRevisionsToConflict() throws IOException {
    when(service.update("file", "old", Map.of()))
        .thenThrow(new ConcurrentModificationException("private value"));
    assertStatus(409, () -> resource.update("file", new ConfigurationResource.Update("old", Map.of())));
  }

  @Test
  void serializesPropertiesAndHelpAndDeserializesTheUpdateContract() throws IOException {
    var mapper = new ObjectMapper();
    var file = new ConfigurationFile("file.properties", "revision", Map.of("flag", "true"),
        new ConfigurationFile.Documentation("File help", Map.of("flag", "Property help")));
    var json = mapper.readTree(mapper.writeValueAsString(file));
    assertEquals("true", json.at("/properties/flag").asText());
    assertEquals("Property help", json.at("/documentation/properties/flag").asText());
    var update = mapper.readValue("{\"path\":\"file.properties\",\"revision\":\"revision\",\"properties\":{\"flag\":\"false\"}}",
        ConfigurationResource.Update.class);
    assertEquals("file.properties", update.path());
    assertEquals(Map.of("flag", "false"), update.properties());
    assertFalse(update.toString().contains("false"));
  }

  private void assertForbidden() {
    assertStatus(403, () -> resource.list());
    assertStatus(403, () -> resource.read("file"));
    assertStatus(403, () -> resource.update("file", null));
    assertStatus(403, () -> resource.validateUserAuthorization(null));
    assertStatus(403, () -> resource.persistenceTargets());
    assertStatus(403, () -> resource.persist(null));
  }

  @Test
  void discoversAndUpdatesCustomXmlWithOnlyASelectionOfKeys() throws IOException {
    var target = new CustomSettingsTarget("Company.xml", "before", true);
    var saved = new CustomSettingsTarget("Company.xml", "after", true);
    var keys = java.util.Set.of("flag");
    when(service.getPersistenceTargets()).thenReturn(List.of(target));
    when(service.persist("general.properties", "revision", keys, target)).thenReturn(saved);
    assertEquals(List.of(target), resource.persistenceTargets().getEntity());
    var response = resource.persist(new ConfigurationResource.PersistenceRequest(
        "general.properties", "revision", keys, target));
    assertEquals(saved, response.getEntity());
    assertEquals("no-store", response.getHeaderString("Cache-Control"));
    verify(service).persist("general.properties", "revision", keys, target);
  }

  @Test
  void persistenceRejectsNullPayloadAndReportsXmlConflicts() throws IOException {
    assertStatus(400, () -> resource.persist(null));
    var target = new CustomSettingsTarget("Company.xml", "old", true);
    var keys = java.util.Set.of("flag");
    when(service.persist("general.properties", "revision", keys, target))
        .thenThrow(new ConcurrentModificationException("private XML content"));
    assertStatus(409, () -> resource.persist(new ConfigurationResource.PersistenceRequest(
        "general.properties", "revision", keys, target)));
  }

  private void assertStatus(int status, org.junit.jupiter.api.function.Executable operation) {
    var error = assertThrows(WebApplicationException.class, operation);
    assertEquals(status, error.getResponse().getStatus());
    assertEquals("no-store", error.getResponse().getHeaderString("Cache-Control"));
    assertFalse(error.getResponse().getEntity().toString().contains("private"));
  }

  public static class TestResource extends ConfigurationResource {
    private User user;

    @Override
    protected User getUser() {
      return user;
    }
  }
}
