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
import java.util.ConcurrentModificationException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class CustomSettingsRepositoryTest {
  @TempDir
  Path home;
  private Path directory;
  private CustomSettingsRepository repository;

  @BeforeEach
  void setUp() throws IOException {
    Files.createDirectories(home.resolve("properties"));
    directory = Files.createDirectories(home.resolve("configuration/silverpeas"));
    repository = new CustomSettingsRepository(home.resolve("properties"));
  }

  @Test
  void createsMissingDirectoriesAndRetainsWhitespaceValues() throws Exception {
    Files.delete(directory);
    Files.delete(directory.getParent());
    var target = repository.list().getFirst();
    assertFalse(Files.exists(directory));
    repository.persist(target, "org/test.properties", Map.of("whitespace", "  one\ntwo\t  "));
    assertEquals("  one\ntwo\t  ", value(parse(target.name()), "whitespace"));
  }

  @Test
  void rejectsCharactersThatCannotBeRepresentedInXmlWithoutCreatingAFile() throws IOException {
    var target = repository.list().getFirst();
    var changes = Map.of("key", "invalid\u0000value");
    assertThrows(IllegalArgumentException.class,
        () -> repository.persist(target, "test.properties", changes));
    assertFalse(Files.exists(directory.resolve(target.name())));
  }

  @Test
  void offersCustomerSettingsWhenNoCustomXmlExistsAndIgnoresBuiltInSettings() throws IOException {
    xml("00-SilverpeasSettings.xml", "Silverpeas", "");
    var targets = repository.list();
    assertEquals(java.util.List.of(new CustomSettingsTarget("CustomerSettings.xml", "absent", false)), targets);
    assertFalse(Files.exists(directory.resolve("CustomerSettings.xml")));
  }

  @Test
  void createsAnInstallCompatibleFileWithOnlyTheSelectedValuesAndEscapesXml() throws Exception {
    var created = repository.persist(repository.list().getFirst(), "org/silverpeas/general.properties",
        Map.of("title", "été < & > \"", "blank", "", "path", "${SILVERPEAS_HOME}/data"));
    assertTrue(created.exists());
    var doc = parse("CustomerSettings.xml");
    assertEquals("Custom", doc.getDocumentElement().getAttribute("product"));
    var fileset = (org.w3c.dom.Element) doc.getElementsByTagName("fileset").item(0);
    assertEquals("${SILVERPEAS_HOME}/properties", fileset.getAttribute("root"));
    var file = (org.w3c.dom.Element) doc.getElementsByTagName("configfile").item(0);
    assertEquals("org/silverpeas/general.properties", file.getAttribute("name"));
    assertEquals(3, doc.getElementsByTagName("parameter").getLength());
    assertEquals("été < & > \"", value(doc, "title"));
    assertEquals("", value(doc, "blank"));
    assertEquals("${SILVERPEAS_HOME}/data", value(doc, "path"));
    assertEquals(created, repository.list().getFirst());
  }

  @Test
  void findsOtherNamesAndMergesRelativeRootsWithoutLosingUnrelatedRules() throws Exception {
    var path = xml("CompanyOverrides.xml", "Custom", """
        <!-- Keep this comment -->
        <fileset root="${SILVERPEAS_HOME}/properties/org/silverpeas/">
          <configfile name="general.properties">
            <parameter key="flag">old</parameter><parameter key="untouched">keep</parameter>
          </configfile>
          <xmlfile name="other.xml"><parameter key="/root/@attribute">keep</parameter></xmlfile>
        </fileset>
        """);
    var target = repository.list().getFirst();
    assertEquals("CompanyOverrides.xml", target.name());
    repository.persist(target, "org/silverpeas/general.properties", Map.of("flag", "new", "extra", "yes"));
    var doc = parse(target.name());
    assertEquals("new", value(doc, "flag"));
    assertEquals("keep", value(doc, "untouched"));
    assertEquals("yes", value(doc, "extra"));
    assertEquals(1, doc.getElementsByTagName("configfile").getLength());
    assertEquals(1, doc.getElementsByTagName("xmlfile").getLength());
    assertTrue(Files.readString(path).contains("Keep this comment"));
  }

  @Test
  void preservesDeclaredEncodingAndDoesNotDuplicateParametersOnRepeatedSaves() throws Exception {
    var path = directory.resolve("CustomerSettings.xml");
    Files.writeString(path, "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
        + "<silverpeas-settings product=\"Custom\"><!-- été --></silverpeas-settings>",
        java.nio.charset.StandardCharsets.ISO_8859_1);
    var saved = repository.persist(repository.list().getFirst(), "org/test.properties", Map.of("label", "été"));
    repository.persist(saved, "org/test.properties", Map.of("label", "été"));
    var doc = parse("CustomerSettings.xml");
    assertEquals("ISO-8859-1", doc.getXmlEncoding());
    assertEquals("été", value(doc, "label"));
    assertEquals(1, doc.getElementsByTagName("parameter").getLength());
  }

  @Test
  void updatesEveryDuplicateDefinitionInTheChosenXml() throws Exception {
    xml("CustomerSettings.xml", "Custom", """
        <fileset root="${SILVERPEAS_HOME}/properties">
          <configfile name="org/test.properties"><parameter key="flag">one</parameter>
            <parameter key="flag">two</parameter></configfile>
          <configfile name="org/test.properties"><parameter key="flag">three</parameter></configfile>
        </fileset>
        """);
    repository.persist(repository.list().getFirst(), "org/test.properties", Map.of("flag", "updated"));
    var parameters = parse("CustomerSettings.xml").getElementsByTagName("parameter");
    assertEquals(3, parameters.getLength());
    for (var i = 0; i < parameters.getLength(); i++) {
      assertEquals("updated", parameters.item(i).getTextContent());
    }
  }

  @Test
  void rejectsStaleRevisionsAndNewFilesAppearingSinceDiscovery() throws IOException {
    var absent = repository.list().getFirst();
    var path = xml("CustomerSettings.xml", "Custom", "");
    var before = Files.readString(path);
    var changes = Map.of("flag", "true");
    assertThrows(ConcurrentModificationException.class,
        () -> repository.persist(absent, "org/test.properties", changes));
    var stale = repository.list().getFirst();
    Files.writeString(path, before.replace("Custom", "Company"));
    assertThrows(ConcurrentModificationException.class,
        () -> repository.persist(stale, "org/test.properties", changes));
    assertEquals(before.replace("Custom", "Company"), Files.readString(path));
  }

  @Test
  void refusesEarlierTargetsOverriddenByLaterXmlFiles() throws IOException {
    xml("A.xml", "Custom", "");
    xml("Z.xml", "Custom", """
        <fileset root="${SILVERPEAS_HOME}/properties">
          <configfile name="org/test.properties"><parameter key="flag">later</parameter></configfile>
        </fileset>
        """);
    var earlierTarget = repository.list().getFirst();
    var changes = Map.of("flag", "true");
    assertThrows(ConcurrentModificationException.class,
        () -> repository.persist(earlierTarget, "org/test.properties", changes));
    assertEquals(2, repository.list().size());
    var saved = repository.persist(repository.list().getLast(), "org/test.properties", Map.of("flag", "true"));
    assertEquals("Z.xml", saved.name());
  }

  @Test
  void rejectsInvalidPathsBuiltInTargetsAndSymbolicLinks() throws IOException {
    var target = repository.list().getFirst();
    var changes = Map.of("x", "y");
    for (var path : java.util.List.of("../test.properties", "/tmp/test.properties", "org/../test.properties")) {
      assertThrows(IllegalArgumentException.class, () -> repository.persist(target, path, changes));
    }
    var outsideTarget = new CustomSettingsTarget("../escape.xml", "absent", false);
    assertThrows(IllegalArgumentException.class,
        () -> repository.persist(outsideTarget, "test.properties", changes));
    xml("00-SilverpeasSettings.xml", "Silverpeas", "");
    var builtInTarget = new CustomSettingsTarget("00-SilverpeasSettings.xml", "absent", false);
    assertThrows(IllegalArgumentException.class,
        () -> repository.persist(builtInTarget, "test.properties", changes));
    Files.createSymbolicLink(directory.resolve("CustomerSettings.xml"), directory.resolve("00-SilverpeasSettings.xml"));
    assertThrows(IllegalArgumentException.class,
        () -> repository.persist(target, "test.properties", changes));
  }

  @Test
  void refusesMalformedXmlAndExternalEntitiesWithoutRewritingAnything() throws IOException {
    var path = directory.resolve("CustomerSettings.xml");
    for (var content : java.util.List.of("<broken", """
        <!DOCTYPE silverpeas-settings [<!ENTITY external SYSTEM "file:///not-to-be-read">]>
        <silverpeas-settings product="Custom">&external;</silverpeas-settings>
        """)) {
      Files.writeString(path, content);
      assertThrows(IOException.class, () -> repository.list());
      assertEquals(content, Files.readString(path));
    }
  }

  private Path xml(String name, String product, String body) throws IOException {
    return Files.writeString(directory.resolve(name), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<silverpeas-settings product=\"" + product + "\">" + body + "</silverpeas-settings>");
  }

  private org.w3c.dom.Document parse(String name) throws Exception {
    var factory = javax.xml.parsers.DocumentBuilderFactory.newDefaultInstance();
    factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
    factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
    factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
    return factory.newDocumentBuilder().parse(directory.resolve(name).toFile());
  }

  private String value(org.w3c.dom.Document doc, String key) {
    var parameters = doc.getElementsByTagName("parameter");
    for (var i = 0; i < parameters.getLength(); i++) {
      var parameter = (org.w3c.dom.Element) parameters.item(i);
      if (key.equals(parameter.getAttribute("key"))) {
        return parameter.getTextContent();
      }
    }
    fail("Missing key: " + key);
    return null;
  }
}
