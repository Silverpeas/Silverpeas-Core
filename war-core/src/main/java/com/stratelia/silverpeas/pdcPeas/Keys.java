package com.stratelia.silverpeas.pdcPeas;

public enum Keys {
  RequestSortXformField("SortResXForm"), requestImplementor("Impl"), defaultImplementor(
      "defaultSort"),RequestSortImplementor("sortImp");

  private String keyword = null;

  Keys(String key) {
    this.keyword = key;
  }

  public String value() {
    return keyword;

  }

}
