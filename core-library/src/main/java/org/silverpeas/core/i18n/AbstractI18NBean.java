/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.i18n;

import org.silverpeas.core.util.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractI18NBean<T extends BeanTranslation>
    implements Serializable, I18NBean<T> {
  private static final long serialVersionUID = 756146888448232764L;

  /* Name of the bean */
  @XmlElement(namespace = "http://www.silverpeas.org/exchange")
  private String name = "";

  /* Description of the bean */
  @XmlElement(namespace = "http://www.silverpeas.org/exchange")
  private String description = "";

  private String language = null;
  private String translationId = null;
  private Map<String, T> translations = new HashMap<>(3);
  private boolean removeTranslation = false;

  protected AbstractI18NBean() {
  }

  protected AbstractI18NBean(final AbstractI18NBean other) {
    this.name = other.name;
    this.description = other.description;
    this.language = other.language;
    this.translationId = other.translationId;
    this.translations.putAll(other.translations);
    this.removeTranslation = other.removeTranslation;
  }

  /**
   * Gets the name of the bean (default plat-form language)
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Set the bean name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the description of the bean (default plat-form language)
   * @return
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Set the bean description
   */
  public final void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the name of the bean from the given language
   * @param language
   * @return
   */
  public String getName(String language) {
    if (!I18NHelper.isI18nContentActivated) {
      return name;
    }
    T translation = selectTranslation(language);
    if (translation != null) {
      return translation.getName();
    } else {
      return name;
    }
  }

  /**
   * Gets the description of the bean from the given language
   * @param language
   * @return
   */
  public String getDescription(String language) {
    if (!I18NHelper.isI18nContentActivated) {
      return description;
    }
    T translation = selectTranslation(language);
    if (translation != null) {
      return translation.getDescription();
    } else {
      return description;
    }
  }

  /**
   * Centralization.
   * @param language
   * @return
   */
  private T selectTranslation(String language) {
    T translation = getTranslations()
        .get(StringUtil.isDefined(language) ? language : I18NHelper.defaultLanguage);
    if (translation == null) {
      translation = getNextTranslation();
    }
    return translation;
  }

  public String getLanguage() {
    if (I18NHelper.isI18nContentActivated && StringUtil.isNotDefined(language)) {
      return I18NHelper.defaultLanguage;
    }
    return language;
  }

  @Override
  public void setLanguage(String language) {
    this.language = language;
  }

  public boolean isRemoveTranslation() {
    return removeTranslation;
  }

  @Override
  public void setRemoveTranslation(boolean removeTranslation) {
    this.removeTranslation = removeTranslation;
  }

  public String getTranslationId() {
    return translationId;
  }

  @Override
  public void setTranslationId(String translationId) {
    this.translationId = translationId;
  }

  public Collection<String> getLanguages() {
    return translations.keySet();
  }

  @Override
  public Map<String, T> getTranslations() {
    return translations;
  }

  /**
   * Gets cloned translations.<br>
   * This is useful on copy/paste operations.
   * @return a clone of {@link #getTranslations()} result.
   */
  @SuppressWarnings("unchecked")
  public Map<String, T> getClonedTranslations() {
    Map<String, T> clonedTranslations = new HashMap<>(3);
    for (Map.Entry<String, T> entry : translations.entrySet()) {
      clonedTranslations.put(entry.getKey(), (T) entry.getValue().clone());
    }
    return clonedTranslations;
  }

  public void setTranslations(Map<String, T> translations) {
    this.translations = translations;
  }

  public void setTranslations(Collection<T> translations) {
    if (translations != null && !translations.isEmpty()) {
      for (T translation : translations) {
        addTranslation(translation);
      }
    }
  }

  public void setTranslations(List<T> translations) {
    if (translations != null && !translations.isEmpty()) {
      for (T translation : translations) {
        addTranslation(translation);
      }
    }
  }

  @Override
  public T getTranslation(String language) {
    return translations.get(language);
  }

  public void addTranslation(T translation) {
    String lang = translation.getLanguage();
    if (!StringUtil.isDefined(lang)) {
      lang = I18NHelper.defaultLanguage;
      translation.setLanguage(lang);
    }
    translations.put(lang, translation);
  }

  @Override
  public T getNextTranslation() {
    Iterator<String> languages = I18NHelper.getLanguages().iterator();
    T translation = null;
    while (translation == null && languages.hasNext()) {
      translation = getTranslations().get(languages.next());
    }
    return translation;
  }

  public String getLanguageToDisplay(String language) {
    String languageToDisplay = language;

    T translation = getTranslation(language);
    if (translation == null) {
      translation = getNextTranslation();
      if (translation != null) {
        languageToDisplay = translation.getLanguage();
      }
    }

    return languageToDisplay;
  }
}
