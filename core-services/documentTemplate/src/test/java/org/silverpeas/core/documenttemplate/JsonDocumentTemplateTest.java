/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core.documenttemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.documenttemplate.DocumentTemplateTestUtil.DEFAULT_CREATION_INSTANT;
import static org.silverpeas.core.documenttemplate.DocumentTemplateTestUtil.DEFAULT_JSON;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class JsonDocumentTemplateTest {

  @DisplayName("Empty json should be decoded")
  @Test
  void empty() {
    Arrays.stream(new String[]{null, "", "{}"}).forEach(t -> {
      final JsonDocumentTemplate result = JsonDocumentTemplate.decode(t);
      assertThat(result, notNullValue());
      assertThat(result.getId(), nullValue());
      assertThat(result.getNameTranslations(), notNullValue());
      assertThat(result.getNameTranslations().size(), is(0));
      assertThat(result.getDescriptionTranslations(), notNullValue());
      assertThat(result.getDescriptionTranslations().size(), is(0));
      assertThat(result.getPosition(), is(-1));
    });
  }

  @DisplayName("Encode into json should work")
  @Test
  void encode() {
    JsonDocumentTemplate jsonEntity = new JsonDocumentTemplate();
    jsonEntity.setId("an identifier");
    jsonEntity.setPosition(3);
    jsonEntity.getNameTranslations().put("fr", "Ceci est un test");
    jsonEntity.getNameTranslations().put("en", "This is a test");
    jsonEntity.setCreatorId("1");
    jsonEntity.setCreationInstant(DEFAULT_CREATION_INSTANT);
    final String json = jsonEntity.toString();
    assertThat(json, is(DEFAULT_JSON));
  }

  @DisplayName("Decode from json should work")
  @Test
  void decode() {
    JsonDocumentTemplate jsonEntity = JsonDocumentTemplate.decode(DEFAULT_JSON);
    assertThat(jsonEntity.getId(), is("an identifier"));
    assertThat(jsonEntity.getPosition(), is(3));
    assertThat(jsonEntity.getNameTranslations().size(), is(2));
    assertThat(jsonEntity.getNameTranslations().get("fr"), is("Ceci est un test"));
    assertThat(jsonEntity.getNameTranslations().get("en"), is("This is a test"));
  }
}