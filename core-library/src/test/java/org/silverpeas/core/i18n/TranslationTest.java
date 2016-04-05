/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.i18n;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author Yohann Chastagnier
 */
public class TranslationTest {

  private TestI18NBean testI18NBean = new TestI18NBean();

  @SuppressWarnings("unchecked")
  @Test
  public void getClonedTranslations() {
    Map<String, TestI18N> translations = new HashMap<String, TestI18N>();
    translations.put("fr", new TestI18N("fr", "nom", "description", "valeur", new ObjectClonable(),
        new ObjectNotClonable()));
    translations.put("en",
        new TestI18N("en", "name", "desc", "value", new ObjectClonable(), new ObjectNotClonable()));

    for (Map.Entry<String, TestI18N> entry : translations.entrySet()) {
      testI18NBean.addTranslation(entry.getValue());
    }

    Map<String, TestI18N> translationsToVerify = testI18NBean.getTranslations();
    Map<String, TestI18N> clonedTranslationsToVerify = testI18NBean.getClonedTranslations();

    for (Map.Entry<String, TestI18N> entry : translations.entrySet()) {

      TestI18N translation = translationsToVerify.get(entry.getKey());
      TestI18N clonedTranslation = translationsToVerify.get(entry.getKey());

      assertThat(translation == entry.getValue(), is(true));
      assertThat(clonedTranslationsToVerify == entry.getValue(), is(false));

      assertThat(translation.getLanguage(), is(entry.getKey()));
      assertThat(clonedTranslation.getLanguage(), is(entry.getKey()));

      assertThat(translation.getName(), is(entry.getValue().getName()));
      assertThat(clonedTranslation.getName(), is(entry.getValue().getName()));
      assertThat(clonedTranslation.getName(), sameInstance(entry.getValue().getName()));

      assertThat(translation.getDescription(), is(entry.getValue().getDescription()));
      assertThat(clonedTranslation.getDescription(), is(entry.getValue().getDescription()));
      assertThat(clonedTranslation.getDescription(),
          sameInstance(entry.getValue().getDescription()));

      assertThat(translation.getValue(), is(entry.getValue().getValue()));
      assertThat(clonedTranslation.getValue(), is(entry.getValue().getValue()));
      assertThat(clonedTranslation.getValue(), sameInstance(entry.getValue().getValue()));

      assertThat(translation.getObjectClonable(), is(entry.getValue().getObjectClonable()));
      assertThat(clonedTranslation.getObjectClonable(), is(entry.getValue().getObjectClonable()));
      assertThat(clonedTranslation.getObjectClonable(),
          sameInstance(entry.getValue().getObjectClonable()));

      assertThat(translation.getObjectNotClonable(), is(entry.getValue().getObjectNotClonable()));
      assertThat(clonedTranslation.getObjectNotClonable(),
          is(entry.getValue().getObjectNotClonable()));
      assertThat(clonedTranslation.getObjectNotClonable(),
          sameInstance(entry.getValue().getObjectNotClonable()));
    }
  }

  public class TestI18N extends Translation {
    private static final long serialVersionUID = 353607407808930532L;

    private String value;
    private ObjectClonable objectClonable;
    private ObjectNotClonable objectNotClonable;

    public TestI18N(String lang, String name, String description, String value,
        ObjectClonable objectClonable, ObjectNotClonable objectNotClonable) {
      super(lang, name, description);
      this.value = value;
      this.objectClonable = objectClonable;
      this.objectNotClonable = objectNotClonable;
    }

    public String getValue() {
      return value;
    }

    public ObjectClonable getObjectClonable() {
      return objectClonable;
    }

    public ObjectNotClonable getObjectNotClonable() {
      return objectNotClonable;
    }

    @Override
    protected TestI18N clone() {
      return (TestI18N) super.clone();
    }
  }

  public class ObjectNotClonable {
    public String value = "tata";
  }

  public class ObjectClonable implements Cloneable {
    public String value = "toto";

    @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
    @Override
    protected ObjectClonable clone() {
      try {
        return (ObjectClonable) super.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public class TestI18NBean extends AbstractI18NBean<TestI18N> {
    private static final long serialVersionUID = -7696451005336074443L;
  }
}