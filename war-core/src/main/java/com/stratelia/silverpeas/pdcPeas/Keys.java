package com.stratelia.silverpeas.pdcPeas;

/**
 * Enumerates the values that may be used by the search engine
 * @author jle
 *
 */
public enum Keys {
  // TODO a zapper
  RequestSortXformField("SortResXForm"),
  // Stores the kind of sort that will be used (defaultSort, xmlFormSort, ...)
  RequestSortImplementor("sortImp"),
  // Stores the class that will make the default sort
  defaultImplementor("defaultSort"),
  // Stores the class that will sort the XML forms
  xmlFormSortImplementor("xmlFormSort");

  private String keyword = null;

  Keys(String key) {
    this.keyword = key;
  }

  public String value() {
    return keyword;

  }

}
