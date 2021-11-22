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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.i18n;

import org.silverpeas.core.Nameable;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.util.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractI18NBean<T extends BeanTranslation>
    implements Serializable, Nameable, I18NBean<T> {
  private static final long serialVersionUID = 756146888448232764L;

  /* Name of the bean */
  @XmlElement(namespace = "http://www.silverpeas.org/exchange")
  private String name = "";

  /* Description of the bean */
  @XmlElement(namespace = "http://www.silverpeas.org/exchange")
  private String description = "";

  private String language = I18NHelper.DEFAULT_LANGUAGE;
  private String translationId = null;
  private transient Map<String, T> translations = new HashMap<>(3);
  private boolean removeTranslation = false;

  protected AbstractI18NBean() {
  }

  protected AbstractI18NBean(final AbstractI18NBean<T> other) {
    this.name = other.name;
    this.description = other.description;
    this.language = other.language;
    this.translationId = other.translationId;
    this.translations.putAll(other.translations);
    this.removeTranslation = other.removeTranslation;
  }

  private T getDefaultTranslation() {
    String lang = getLanguage();
    Class<T> type = getTranslationType();
    try {
      Constructor<T> constructor = type.getDeclaredConstructor();
      constructor.trySetAccessible();
      T translation = constructor.newInstance();
      translation.setName(name);
      translation.setDescription(description);
      translation.setLanguage(lang);
      translation.setId(translationId);
      translations.put(lang, translation);
      return translation;
    } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  protected abstract Class<T> getTranslationType();

  /**
   * Gets the name of the bean in the language defined by the {@link AbstractI18NBean#getLanguage()}
   * property.
   *
   * @return a short description about the bean.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the bean in the language defined by the {@link AbstractI18NBean#getLanguage()}
   * property.
   *
   * @param name the name of the bean.
   */
  public void setName(String name) {
    this.name = name;
    String lang = getLanguage();
    T translation = getTranslation(lang);
    translation.setName(name);
  }

  /**
   * Gets the description about the bean in the language defined by the {@link
   * AbstractI18NBean#getLanguage()} property.
   *
   * @return a short description about the bean.
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Gets the description about the bean in the language defined by the {@link
   * AbstractI18NBean#getLanguage()} property.
   *
   * @param description a short description about the bean.
   */
  public final void setDescription(String description) {
    this.description = description;
    String lang = getLanguage();
    T translation = getTranslation(lang);
    translation.setDescription(description);
  }

  /**
   * Gets the name of the bean in the given language. This method is a shortcut of the following
   * code:
   * <blockquote><pre>
   * myBean.getTranslation(language).getName();
   * </pre></blockquote>
   *
   * @param language the ISO 631-1 code of the language
   * @return the name of the bean in the specified language.
   */
  public String getName(String language) {
    T translation = selectTranslation(language);
    return translation.getName();
  }

  /**
   * Gets the description about the bean in the given language. This method is a shortcut of the
   * following code:
   * <blockquote><pre>
   * myBean.getTranslation(language).getDescription();
   * </pre></blockquote>
   *
   * @param language the ISO 631-1 code of the language
   * @return the description about the bean in the specified language.
   */
  public String getDescription(String language) {
    T translation = selectTranslation(language);
    return translation.getDescription();
  }

  private T selectTranslation(String language) {
    final String lang = StringUtil.isDefined(language) ? language : I18NHelper.DEFAULT_LANGUAGE;
    T translation = getTranslations().get(lang);
    if (translation == null) {
      translation = getNextTranslation();
    }
    return translation;
  }

  public String getLanguage() {
    if (I18NHelper.isI18nContentActivated && StringUtil.isNotDefined(language)) {
      return I18NHelper.DEFAULT_LANGUAGE;
    }
    return language;
  }

  @Override
  public void setLanguage(String language) {
    this.language = StringUtil.isDefined(language) ? language : I18NHelper.DEFAULT_LANGUAGE;
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
    return Set.copyOf(translations.keySet());
  }

  @Override
  public Map<String, T> getTranslations() {
    return Map.copyOf(translations);
  }

  /**
   * Gets cloned translations.<br> This is useful on copy/paste operations.
   *
   * @return a clone of {@link #getTranslations()} result.
   */
  public Map<String, T> getClonedTranslations() {
    return translations.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().copy()));
  }

  public void setTranslations(Map<String, T> translations) {
    this.translations = translations;
  }

  public void setTranslations(Collection<T> translations) {
    if (translations != null && !translations.isEmpty()) {
      translations.forEach(this::addTranslation);
    }
  }

  /**
   * Gets a possible translation for the specified language. If no such translation exists in the
   * given language then browse for a language for which a translation exists. This method will
   * return always a translation; indeed in the case there is no translation in whatever supported
   * language, then a default translation with the actual bean's properties is returned.
   *
   * @param language the ISO 631-1 code of the language.
   * @return a translation. Never null.
   */
  @Override
  @SuppressWarnings("unchecked")
  public T getTranslation(String language) {
    return selectTranslation(language);
  }

  public void addTranslation(T translation) {
    String lang = translation.getLanguage();
    if (!StringUtil.isDefined(lang)) {
      lang = I18NHelper.DEFAULT_LANGUAGE;
      translation.setLanguage(lang);
    }
    translations.put(lang, translation);
  }

  @Override
  public T getNextTranslation() {
    Map<String, T> l10n = getTranslations();
    return I18NHelper.getLanguages()
        .stream()
        .map(l10n::get)
        .filter(Objects::nonNull)
        .findFirst()
        .orElseGet(this::getDefaultTranslation);
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
