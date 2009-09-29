package com.stratelia.silverpeas.contentManager;

/**
 * This class allows the result jsp page of the global search to show all
 * features (name, description, location)
 */
public class GlobalSilverContentI18N extends SilverContentI18N implements
    java.io.Serializable {
  private String name = "";
  private String description = "";

  public GlobalSilverContentI18N(String language, String name,
      String description) {
    super.setLanguage(language);
    setName(name);
    setDescription(description);
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

}