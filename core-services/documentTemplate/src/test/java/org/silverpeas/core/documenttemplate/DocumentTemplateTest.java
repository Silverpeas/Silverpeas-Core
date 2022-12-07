/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.documenttemplate.DocumentTemplateTestUtil.DEFAULT_JSON;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class DocumentTemplateTest {

  @DisplayName("Initializing from empty json should initialize empty instance")
  @Test
  void fromEmptyJson() {
    DocumentTemplate template = new DocumentTemplate(new JsonDocumentTemplate(), null);
    assertThat(template, notNullValue());
    assertThat(template.getId(), nullValue());
    assertThat(template.getPosition(), is(-1));
    assertThat(template.getExtension(), nullValue());
    assertThat(template.getName("fr"), emptyString());
    assertThat(template.getName("en"), emptyString());
    assertThat(template.getName("de"), emptyString());
    assertThat(template.getDescription("fr"), emptyString());
    assertThat(template.getDescription("en"), emptyString());
    assertThat(template.getDescription("de"), emptyString());
  }

  @DisplayName("Initializing from json should initialize document template instance")
  @Test
  void fromJson() {
    DocumentTemplate template = new DocumentTemplate(JsonDocumentTemplate.decode(DEFAULT_JSON), "txt");
    assertThat(template, notNullValue());
    assertThat(template.getId(), is("an identifier"));
    assertThat(template.getPosition(), is(3));
    assertThat(template.getExtension(), is("txt"));
    assertThat(template.existNameTranslationIn("fr"), is(true));
    assertThat(template.getName("fr"), is("Ceci est un test"));
    assertThat(template.existNameTranslationIn("en"), is(true));
    assertThat(template.getName("en"), is("This is a test"));
    assertThat(template.existNameTranslationIn("de"), is(false));
    assertThat(template.getName("de"), is("Ceci est un test"));
    assertThat(template.existDescriptionTranslationIn("fr"), is(false));
    assertThat(template.existDescriptionTranslationIn("en"), is(false));
    assertThat(template.existDescriptionTranslationIn("de"), is(false));
  }

  @DisplayName("Json instance MUST be modified when document template is")
  @Test
  void toJson() {
    DocumentTemplate template = new DocumentTemplate();
    template.setId("an id");
    template.setPosition(4);
    template.setName("un libellé", "fr");
    template.setName("a label", "en");
    template.setDescription("une description", "fr");
    template.setDescription("a description", "en");
    JsonDocumentTemplate json = template.getJson();
    assertThat(json, notNullValue());
    assertThat(json.getId(), is("an id"));
    assertThat(json.getPosition(), is(4));
    Map<String, String> translations = json.getNameTranslations();
    assertThat(translations, notNullValue());
    assertThat(translations.size(), is(2));
    assertThat(translations.get("fr"), is("un libellé"));
    assertThat(translations.get("en"), is("a label"));
    translations = json.getDescriptionTranslations();
    assertThat(translations, notNullValue());
    assertThat(translations.size(), is(2));
    assertThat(translations.get("fr"), is("une description"));
    assertThat(translations.get("en"), is("a description"));
  }
}