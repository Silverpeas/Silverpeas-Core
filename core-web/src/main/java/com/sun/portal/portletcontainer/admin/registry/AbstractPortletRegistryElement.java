/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.admin.registry;

import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AbstractPortletRegistryElement provides partial implementation of the PortletRegistryElement
 * interface. It has implementation for the methods that are common to all elements like PortletApp,
 * PortletWindow or PortletWindowPreference
 */
public abstract class AbstractPortletRegistryElement implements PortletRegistryTags,
    PortletRegistryElement {

  private String name;
  private String userName;
  private String portletName;
  private String isRemote;
  private String lang;
  private Map<String, Map<String, Object>> collectionMapTable;
  private Map<String, String> collectionStringTable;

  public AbstractPortletRegistryElement() {
    isRemote = Boolean.FALSE.toString(); // By default the Portlet Window is not
    // remote
    collectionMapTable = new HashMap<>();
    collectionStringTable = new HashMap<>();
  }

  @Override
  public void setCollectionProperty(String key, Map<String, Object> values) {
    setMap(key, values);
  }

  @Override
  public Map<String, Object> getCollectionProperty(String key) {
    return getMapValue(key);
  }

  @Override
  public void setCollectionProperty(String key, List<String> values) {
    setList(key, values);
  }

  @Override
  public void setStringProperty(String key, String value) {
    setString(key, value);
  }

  @Override
  public String getStringProperty(String key) {
    return getStringValue(key);
  }

  @Override
  public String getName() {
    if (this.name == null || this.name.trim().length() == 0)
      return getPortletName();
    return this.name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getPortletName() {
    return this.portletName;
  }

  @Override
  public void setPortletName(String portletName) {
    this.portletName = portletName;
  }

  @Override
  public String getLang() {
    return lang;
  }

  @Override
  public void setLang(String lang) {
    this.lang = lang;
  }

  @Override
  public String getUserName() {
    if (this.userName == null)
      return PortletRegistryContext.USER_NAME_DEFAULT;
    return this.userName;
  }

  @Override
  public void setUserName(String userName) {
    this.userName = userName;
  }

  @Override
  public String getRemote() {
    return this.isRemote;
  }

  @Override
  public void setRemote(String isRemote) {
    this.isRemote = isRemote;
  }

  private void setList(String key, List<String> values) {
    Map<String, Object> m = new HashMap<>();
    if (values != null) {
      for(String s: values) {
        m.put(s, s);
      }
    }
    setMap(key, m);
  }

  private void setMap(String key, Map<String, Object> values) {
    collectionMapTable.put(key, values);
  }

  private void setString(String key, String value) {
    collectionStringTable.put(key, value);
  }

  private Map<String, Object> getMapValue(String key) {
    return collectionMapTable.get(key);
  }

  private String getStringValue(String key) {
    return (String) collectionStringTable.get(key);
  }

  protected Map<String, Map<String, Object>> getMapCollectionTable() {
    return this.collectionMapTable;
  }

  protected Map<String, String> getStringCollectionTable() {
    return this.collectionStringTable;
  }

  protected void populateValues(Element rootTag) {
    // Get the child element of Properties Tag and a list of Child element,
    // which will be
    // a list of Collection/String tags
    Element propertiesTag = XMLDocumentHelper.getChildElement(rootTag, PROPERTIES_TAG);
    List<Element> tags = XMLDocumentHelper.createElementList(propertiesTag);
    String tagName;
    for (Element tag: tags) {
      // Check the name of the tag
      tagName = tag.getTagName();
      // Get the attributes for the Tag.
      Map<String, String> attributes = XMLDocumentHelper.createAttributeTable(tag);
      String name = attributes.get(NAME_KEY);
      if (tagName.equals(STRING_TAG)) {
        setStringProperty(name, attributes.get(VALUE_KEY));
      } else if (tagName.equals(COLLECTION_TAG)) {
        List<Element> innerTags = XMLDocumentHelper.createElementList(tag);
        Map<String, Object> mapValues = new HashMap<>();
        for (Element innerTag: innerTags) {
          String innerTagName = innerTag.getTagName();
          if (innerTagName.equals(STRING_TAG)) {
            // Get the attributes for String Tag.
            Map<String, String> stringAttributes = XMLDocumentHelper
                .createAttributeTable(innerTag);
            String innerName = stringAttributes.get(NAME_KEY);
            String innerValue = stringAttributes.get(VALUE_KEY);
            if (innerName == null) // If there is no name, use the value for key
              // also
              mapValues.put(innerValue, innerValue);
            else
              mapValues.put(innerName, innerValue);
          } else if (innerTagName.equals(COLLECTION_TAG)) {
            // Get the attributes for the Tag.
            Map<String, String> innerAttributes = XMLDocumentHelper
                .createAttributeTable(innerTag);
            String innerName = innerAttributes.get(NAME_KEY);
            List<Element> stringTags = XMLDocumentHelper.createElementList(innerTag);
            List<String> listValues = new ArrayList<>();
            for (Element stringTag: stringTags) {
              // Get the attributes for String Tag.
              Map<String, String> stringAttributes = XMLDocumentHelper
                  .createAttributeTable(stringTag);
              listValues.add(stringAttributes.get(VALUE_KEY));
            }
            mapValues.put(innerName, listValues);
          }
        }
        setCollectionProperty(name, mapValues);
      }
    }
  }

  protected void create(Document document, Element propertiesTag) {

    // Create String Tags under Properties, with the attribute "name", whose
    // value is the key of the map.
    // and with attribute "value" whose value is the value for the key in the
    // map.
    Map<String, String> stringTags = getStringCollectionTable();
    Set<Map.Entry<String, String>> mappings1 = stringTags.entrySet();
    Element stringTag;
    String name, value;
    for(Map.Entry<String, String> me: mappings1) {
      name = me.getKey();
      value = me.getValue();
      // If there is no value, continue
      if (value == null || value.trim().length() == 0)
        continue;
      // Create String tag, add attributes
      stringTag = XMLDocumentHelper.createElement(document, STRING_TAG);
      stringTag.setAttribute(NAME_KEY, name);
      stringTag.setAttribute(VALUE_KEY, value);
      propertiesTag.appendChild(stringTag);
    }

    // Create collection Tags under Properties, with the attribute "name", whose
    // value is the key of the map.
    // The value of the Map is a HashMap.
    // 1. If the value of the HashMap is a HashMap, need to create a Collection
    // Tag, with "name"
    // attribute having the key of the HashMap, value ..#2
    // 2. If the value of the HashMap is a String and
    // a. if key and value of the HashMap are same, create "String" Tag with
    // "value" attribute
    // b. if key and value of the HashMap are different, create "String" Tag
    // with both "name"
    // and "value" attribute.
    Map<String, Map<String, Object>> collectionTags = getMapCollectionTable();
    Set<Map.Entry<String, Map<String, Object>>> mappings2 = collectionTags.entrySet();
    Element collectionTag, innerCollectionTag;
    Map<String, Object> values;
    String key;
    Object obj;
    for(Map.Entry<String, Map<String, Object>> me: mappings2) {
      name = me.getKey();
      values = me.getValue();
      Set<Map.Entry<String, Object>> keys = values.entrySet();

      // If there are no values, do not create collections tag.
      if (!keys.isEmpty()) {
        // Create Collections tag, add String child elements and append it to
        // the document
        collectionTag = XMLDocumentHelper.createElement(document,
            COLLECTION_TAG);
        collectionTag.setAttribute(NAME_KEY, name);
        propertiesTag.appendChild(collectionTag);
        for(Map.Entry<String, Object> entry: keys) {
          key = entry.getKey();
          obj = entry.getValue();
          if (obj instanceof String) {
            value = (String) obj;
            createStringTag(document, collectionTag, key, value);
          } else if (obj instanceof List) { // If its a List
            List<String> innerValuesList = (List<String>) obj;
            innerCollectionTag = XMLDocumentHelper.createElement(document,
                COLLECTION_TAG);
            innerCollectionTag.setAttribute(NAME_KEY, key);
            collectionTag.appendChild(innerCollectionTag);
            int innerValuesListSize = innerValuesList.size();
            for (int i = 0; i < innerValuesListSize; i++) {
              value = (String) innerValuesList.get(i);
              createStringTag(document, innerCollectionTag, value, value);
            }
          } else if (obj instanceof Map) { // If its a Map
            Map<String, String> innerValuesMap = (Map<String, String>) obj;
            Set<String> innerKeys = values.keySet();
            innerCollectionTag = XMLDocumentHelper.createElement(document,
                COLLECTION_TAG);
            innerCollectionTag.setAttribute(NAME_KEY, key);
            collectionTag.appendChild(innerCollectionTag);
            for(String innerKey: innerKeys) {
              value = innerValuesMap.get(innerKey);
              createStringTag(document, innerCollectionTag, innerKey, value);
            }
          }
        }
      }
    }
  }

  private void createStringTag(Document document, Element collectionTag,
      String key, String value) {
    // If key and value are same then add only value
    Element stringTag = XMLDocumentHelper.createElement(document, STRING_TAG);
    if (key.equals(value)) {
      stringTag.setAttribute(VALUE_KEY, value);
    } else {
      stringTag.setAttribute(NAME_KEY, key);
      stringTag.setAttribute(VALUE_KEY, value);
    }
    collectionTag.appendChild(stringTag);

  }
}
