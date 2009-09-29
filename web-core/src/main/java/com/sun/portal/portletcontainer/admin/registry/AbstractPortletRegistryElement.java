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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;

/**
 * AbstractPortletRegistryElement provides partial implementation of the
 * PortletRegistryElement interface. It has implementation for the methods that
 * are common to all elements like PortletApp, PortletWindow or
 * PortletWindowPreference
 */
public abstract class AbstractPortletRegistryElement implements
    PortletRegistryTags, PortletRegistryElement {

  private String name;
  private String userName;
  private String portletName;
  private String isRemote;
  private String lang;
  private Map collectionMapTable;
  private Map collectionStringTable;

  public AbstractPortletRegistryElement() {
    isRemote = Boolean.FALSE.toString(); // By default the Portlet Window is not
    // remote
    collectionMapTable = new HashMap();
    collectionStringTable = new HashMap();
  }

  public void setCollectionProperty(String key, Map values) {
    setMap(key, values);
  }

  public Map getCollectionProperty(String key) {
    return getMapValue(key);
  }

  public void setCollectionProperty(String key, List values) {
    setList(key, values);
  }

  public void setStringProperty(String key, String value) {
    setString(key, value);
  }

  public String getStringProperty(String key) {
    return getStringValue(key);
  }

  public String getName() {
    if (this.name == null || this.name.trim().length() == 0)
      return getPortletName();
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPortletName() {
    return this.portletName;
  }

  public void setPortletName(String portletName) {
    this.portletName = portletName;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getUserName() {
    if (this.userName == null)
      return PortletRegistryContext.USER_NAME_DEFAULT;
    return this.userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getRemote() {
    return this.isRemote;
  }

  public void setRemote(String isRemote) {
    this.isRemote = isRemote;
  }

  private void setList(String key, List values) {
    Map m = new HashMap();
    if (values != null) {
      int size = values.size();
      for (int i = 0; i < size; i++) {
        String s = (String) values.get(i);
        m.put(s, s);
      }
    }
    setMap(key, m);
  }

  private void setMap(String key, Map values) {
    collectionMapTable.put(key, values);
  }

  private void setString(String key, String value) {
    collectionStringTable.put(key, value);
  }

  private Map getMapValue(String key) {
    return (Map) collectionMapTable.get(key);
  }

  private String getStringValue(String key) {
    return (String) collectionStringTable.get(key);
  }

  protected Map getMapCollectionTable() {
    return this.collectionMapTable;
  }

  protected Map getStringCollectionTable() {
    return this.collectionStringTable;
  }

  protected void populateValues(Element rootTag) {
    // Get the child element of Properties Tag and a list of Child element,
    // which will be
    // a list of Collection/String tags
    Element propertiesTag = XMLDocumentHelper.getChildElement(rootTag,
        PROPERTIES_TAG);
    List tags = XMLDocumentHelper.createElementList(propertiesTag);
    int numOfTags = tags.size();
    String tagName;
    for (int j = 0; j < numOfTags; j++) {
      Element tag = (Element) tags.get(j);
      // Check the name of the tag
      tagName = tag.getTagName();
      // Get the attributes for the Tag.
      Map attributes = XMLDocumentHelper.createAttributeTable(tag);
      String name = (String) attributes.get(NAME_KEY);
      if (tagName.equals(STRING_TAG)) {
        setStringProperty(name, (String) attributes.get(VALUE_KEY));
      } else if (tagName.equals(COLLECTION_TAG)) {
        List innerTags = XMLDocumentHelper.createElementList(tag);
        int numOfInnerTags = innerTags.size();
        Map mapValues = new HashMap();
        for (int k = 0; k < numOfInnerTags; k++) {
          Element innerTag = (Element) innerTags.get(k);
          String innerTagName = innerTag.getTagName();
          if (innerTagName.equals(STRING_TAG)) {
            // Get the attributes for String Tag.
            Map stringAttributes = XMLDocumentHelper
                .createAttributeTable(innerTag);
            String innerName = (String) stringAttributes.get(NAME_KEY);
            String innerValue = (String) stringAttributes.get(VALUE_KEY);
            if (innerName == null) // If there is no name, use the value for key
              // also
              mapValues.put(innerValue, innerValue);
            else
              mapValues.put(innerName, innerValue);
          } else if (innerTagName.equals(COLLECTION_TAG)) {
            // Get the attributes for the Tag.
            Map innerAttributes = XMLDocumentHelper
                .createAttributeTable(innerTag);
            String innerName = (String) innerAttributes.get(NAME_KEY);
            List stringTags = XMLDocumentHelper.createElementList(innerTag);
            int numOfStringTags = stringTags.size();
            List listValues = new ArrayList();
            for (int l = 0; l < numOfStringTags; l++) {
              Element stringTag = (Element) stringTags.get(l);
              // Get the attributes for String Tag.
              Map stringAttributes = XMLDocumentHelper
                  .createAttributeTable(stringTag);
              listValues.add((String) stringAttributes.get(VALUE_KEY));
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
    Map stringTags = getStringCollectionTable();
    Set mappings = stringTags.entrySet();
    Iterator itr = mappings.iterator();
    Element stringTag;
    String name, value;
    while (itr.hasNext()) {
      Map.Entry me = (Map.Entry) itr.next();
      name = (String) me.getKey();
      value = (String) me.getValue();
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
    Map collectionTags = getMapCollectionTable();
    mappings = collectionTags.entrySet();
    itr = mappings.iterator();
    Element collectionTag, innerCollectionTag;
    Map values;
    String key;
    Object obj;
    while (itr.hasNext()) {
      Map.Entry me = (Map.Entry) itr.next();
      name = (String) me.getKey();
      values = (Map) me.getValue();
      Set keys = values.entrySet();
      Iterator keysItr = keys.iterator();

      // If there are no values, do not create collections tag.
      if (keysItr.hasNext()) {
        // Create Collections tag, add String child elements and append it to
        // the document
        collectionTag = XMLDocumentHelper.createElement(document,
            COLLECTION_TAG);
        collectionTag.setAttribute(NAME_KEY, name);
        propertiesTag.appendChild(collectionTag);
        while (keysItr.hasNext()) {
          Map.Entry entry = (Map.Entry) keysItr.next();
          key = (String) entry.getKey();
          ;
          obj = entry.getValue();
          if (obj instanceof String) {
            value = (String) obj;
            createStringTag(document, collectionTag, key, value);
          } else if (obj instanceof List) { // If its a List
            List innerValuesList = (List) obj;
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
            Map innerValuesMap = (Map) obj;
            Set innerKeys = values.keySet();
            Iterator innerkeysItr = innerKeys.iterator();
            innerCollectionTag = XMLDocumentHelper.createElement(document,
                COLLECTION_TAG);
            innerCollectionTag.setAttribute(NAME_KEY, key);
            collectionTag.appendChild(innerCollectionTag);
            while (innerkeysItr.hasNext()) {
              key = (String) innerkeysItr.next();
              value = (String) innerValuesMap.get(key);
              createStringTag(document, innerCollectionTag, key, value);
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
