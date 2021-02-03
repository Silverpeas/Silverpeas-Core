/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.user.model;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.test.UnitTest;
import org.silverpeas.core.util.JSONCodec;

import java.util.Map;

import static org.apache.commons.lang3.reflect.FieldUtils.readDeclaredField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author silveryocha
 */
@UnitTest
class NotificationResourceDataDetailsTest {

  @Test
  void decodeNotDefined() {
    assertThrows(IllegalArgumentException.class,() -> decode(null));
    assertThrows(DecodingException.class,() -> decode(""));
  }

  @Test
  void decodeEmpty() throws Exception {
    final NotificationResourceDataDetails labels = decode("{}");
    assertThat(getFeminineGenderResource(labels), nullValue());
    assertThat(getLocalizations(labels), nullValue());
    assertThat(encode(labels), is("{}"));
  }

  @Test
  void decodeFeminineGender() throws Exception {
    final NotificationResourceDataDetails labels = decode("{\"feminineGenderResource\":\"true\"}");
    assertThat(getFeminineGenderResource(labels), nullValue());
    assertThat(labels.isFeminineGenderResource(), is(true));
    assertThat(getLocalizations(labels), nullValue());
  }

  @Test
  void decodeMasculineGender() throws Exception {
    final String initialJson = "{\"feminineGenderResource\":false}";
    final NotificationResourceDataDetails labels = decode(initialJson);
    assertThat(getFeminineGenderResource(labels), is(false));
    assertThat(labels.isFeminineGenderResource(), is(false));
    assertThat(getLocalizations(labels), nullValue());
    assertThat(encode(labels), is(initialJson));
  }

  @Test
  void decodeLocalizedData() throws Exception {
    final String initialJson = "{\"localizations\":{\"en\":{\"key\":\"value\"}}}";
    final NotificationResourceDataDetails labels = decode(initialJson);
    assertThat(getFeminineGenderResource(labels), nullValue());
    assertThat(getLocalizations(labels), notNullValue());
    assertThat(labels.getLocalizations().size(), is(1));
    assertThat(labels.getLocalized("en", "key"), is("value"));
    assertThat(encode(labels), is(initialJson));
  }

  @Test
  void decodeLocalizations() throws Exception {
    final String initialJson =
        "{\"localizations\":{\"fr\":{\"clé\":\"valeur\"},\"en\":{\"key\":\"value\"}}}";
    final NotificationResourceDataDetails labels = decode(initialJson);
    assertThat(getFeminineGenderResource(labels), nullValue());
    assertThat(getLocalizations(labels), notNullValue());
    assertThat(getLocalizations(labels).size(), is(2));
    assertThat(labels.getLocalized("fr", "clé"), is("valeur"));
    assertThat(labels.getLocalized("en", "key"), is("value"));
    assertThat(encode(labels), is(initialJson));
  }

  @Test
  void encodeLocalizationsAndThenRemovingLocalizationOneByOne() throws Exception {
    final NotificationResourceDataDetails labels = new NotificationResourceDataDetails();
    labels.putLocalized("fr", "clé", "valeur");
    labels.putLocalized("en", "key", "value");
    String expectedJson =
        "{\"localizations\":{\"fr\":{\"clé\":\"valeur\"},\"en\":{\"key\":\"value\"}}}";
    assertThat(encode(labels), is(expectedJson));
    // One localization
    labels.getLocalizations().get("fr").clear();
    expectedJson = "{\"localizations\":{\"en\":{\"key\":\"value\"}}}";
    assertThat(getLocalizations(labels).size(), is(2));
    assertThat(encode(labels), is(expectedJson));
    assertThat(getLocalizations(labels).size(), is(1));
    // No localization anymore
    labels.getLocalizations().get("en").clear();
    expectedJson = "{}";
    assertThat(getLocalizations(labels).size(), is(1));
    assertThat(encode(labels), is(expectedJson));
    assertThat(getLocalizations(labels), nullValue());
  }

  @Test
  void encodeLocalizationsAndThenRemovingLocalizedValueOneByOne() throws Exception {
    final NotificationResourceDataDetails labels = new NotificationResourceDataDetails();
    labels.putLocalized("fr", "clé", "valeur");
    labels.putLocalized("en", "key", "value");
    String expectedJson =
        "{\"localizations\":{\"fr\":{\"clé\":\"valeur\"},\"en\":{\"key\":\"value\"}}}";
    assertThat(encode(labels), is(expectedJson));
    // One localization
    labels.putLocalized("fr", "clé", null);
    expectedJson = "{\"localizations\":{\"en\":{\"key\":\"value\"}}}";
    assertThat(getLocalizations(labels).size(), is(2));
    assertThat(encode(labels), is(expectedJson));
    assertThat(getLocalizations(labels).size(), is(1));
    // No localization anymore
    labels.putLocalized("en", "key", "");
    expectedJson = "{}";
    assertThat(getLocalizations(labels).size(), is(1));
    assertThat(encode(labels), is(expectedJson));
    assertThat(getLocalizations(labels), nullValue());
  }

  @Test
  void merge() {
    NotificationResourceDataDetails labels = new NotificationResourceDataDetails();
    labels.putLocalized("fr", "clé", "valeur");
    labels.putLocalized("en", "key", "value");
    final String expectedJsonOfLabels =
        "{\"localizations\":{\"fr\":{\"clé\":\"valeur\"},\"en\":{\"key\":\"value\"}}}";
    assertThat(encode(labels), is(expectedJsonOfLabels));
    final NotificationResourceDataDetails others = new NotificationResourceDataDetails();
    others.putLocalized("fr", "clef", "valeur");
    others.putLocalized("en", "key", "v");
    others.putLocalized("de", "k", "v");
    final String expectedJsonOfOthers =
        "{\"localizations\":{\"fr\":{\"clef\":\"valeur\"},\"en\":{\"key\":\"v\"},\"de\":{\"k\":\"v\"}}}";
    assertThat(encode(others), is(expectedJsonOfOthers));
    labels.merge(null);
    assertThat(encode(labels), is(expectedJsonOfLabels));
    labels.merge(new NotificationResourceDataDetails());
    assertThat(labels.isFeminineGenderResource(), is(true));
    assertThat(encode(labels), is(expectedJsonOfLabels));
    labels.merge(new NotificationResourceDataDetails().setFeminineGenderResource(false));
    assertThat(labels.isFeminineGenderResource(), is(true));
    assertThat(encode(labels), is(expectedJsonOfLabels));
    labels = new NotificationResourceDataDetails();
    labels.putLocalized("fr", "clé", "valeur");
    labels.putLocalized("en", "key", "value");
    labels.merge(others);
    final String expectedJsonOfMergeLToO =
        "{\"localizations\":{\"fr\":{\"clé\":\"valeur\",\"clef\":\"valeur\"},\"en\":{\"key\":\"v\"},\"de\":{\"k\":\"v\"}}}";
    assertThat(encode(labels), is(expectedJsonOfMergeLToO));
    assertThat(encode(others), is(expectedJsonOfOthers));
    labels = new NotificationResourceDataDetails();
    labels.putLocalized("fr", "clé", "valeur");
    labels.putLocalized("en", "key", "value");
    others.merge(labels);
    final String expectedJsonOfMergeOtoL =
        "{\"localizations\":{\"fr\":{\"clef\":\"valeur\",\"clé\":\"valeur\"},\"en\":{\"key\":\"value\"},\"de\":{\"k\":\"v\"}}}";
    assertThat(encode(labels), is(expectedJsonOfLabels));
    assertThat(encode(others), is(expectedJsonOfMergeOtoL));
  }

  @SuppressWarnings("unchecked")
  private Map<String, Map<String, String>> getLocalizations(
      final NotificationResourceDataDetails labels) throws IllegalAccessException {
    return (Map<String, Map<String, String>>) readDeclaredField(labels, "localizations", true);
  }

  private Object getFeminineGenderResource(final NotificationResourceDataDetails labels)
      throws IllegalAccessException {
    return readDeclaredField(labels, "feminineGenderResource", true);
  }

  private NotificationResourceDataDetails decode(final String json) {
    return JSONCodec.decode(json, NotificationResourceDataDetails.class);
  }

  private String encode(final NotificationResourceDataDetails labels) {
    return JSONCodec.encode(labels);
  }
}