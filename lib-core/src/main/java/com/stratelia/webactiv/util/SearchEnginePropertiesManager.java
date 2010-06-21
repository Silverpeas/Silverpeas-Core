/**
 * 
 */
package com.stratelia.webactiv.util;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

/**
 * Class used to realize some treatment with SearchEngineSetting.properties file.
 * @author David Derigent
 */
public class SearchEnginePropertiesManager {

  private static ArrayList<String> fieldsNameList = null;

  static {

  }

  /**
   * 
   */
  private SearchEnginePropertiesManager() {
  }

  /**
   * gets the list of form XML fields name use to sort search result
   * @return a list of fields name
   */
  public static ArrayList<String> getFieldsNameList() {
    if (fieldsNameList == null) {
      fieldsNameList = new ArrayList<String>();
      ResourceLocator resource =
          new ResourceLocator("com.silverpeas.searchEngine.searchEngineSettings",
              "");
      String property = resource.getString("sorting.formXML.fields");
      if (StringUtils.isNotEmpty(property)) {
        StringTokenizer tokens = new StringTokenizer(property, ",");

        while (tokens.hasMoreTokens()) {
          fieldsNameList.add(tokens.nextToken());
        }
      }
    }
    return fieldsNameList;
  }

}
