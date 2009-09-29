package com.stratelia.webactiv.util.publication.model;

import java.io.Serializable;

import com.silverpeas.util.i18n.Translation;

public class PublicationI18N extends Translation implements Serializable {

  private String name = null;
  private String description = null;
  private String keywords = null;

  public PublicationI18N() {
  }

  public PublicationI18N(PublicationDetail publi) {
    if (publi.getLanguage() != null)
      super.setLanguage(publi.getLanguage());

    this.name = publi.getName();
    this.description = publi.getDescription();
    this.keywords = publi.getKeywords();

    if (publi.getTranslationId() != null)
      super.setId(Integer.parseInt(publi.getTranslationId()));
    super.setObjectId(publi.getPK().getId());
  }

  public PublicationI18N(String lang, String name, String description,
      String keywords) {
    if (lang != null)
      super.setLanguage(lang);
    this.name = name;
    this.description = description;
    this.keywords = keywords;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

}
