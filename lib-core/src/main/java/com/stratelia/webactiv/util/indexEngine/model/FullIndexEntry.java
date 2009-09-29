package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.util.indexEngine.DateFormatter;

/**
 * A FullIndexEntry is an IndexEntry completed with data usefull uniquely at the
 * index creation time (mainly all the data contents which must be indexed but
 * which is useless at retrieve time). This extra-content is indexed but not
 * stored in the index.
 */
public class FullIndexEntry extends IndexEntry implements Serializable {
  /**
   * The constructor only set the key part of the IndexEntry.
   * 
   * @deprecated - parameter space is no more used
   */
  public FullIndexEntry(String space, String component, String objectType,
      String objectId) {
    super(component, objectType, objectId);
  }

  public FullIndexEntry(String component, String objectType, String objectId) {
    super(component, objectType, objectId);
  }

  /**
   * Add a text fragment to be indexed.
   * 
   * All this text fragments will be indexed but not stored in the index. They
   * may be added in any order.
   */
  public void addTextContent(String text) {
    addTextContent(text, null);
  }

  public void addTextContent(String text, String language) {
    getTextList().add(new TextDescription(text, language));
  }

  /**
   * Add a file to be indexed.
   * 
   * We need :
   * <UL>
   * <LI>the path to the file</LI>
   * <LI>the encoding of the file</LI>
   * <LI>the format of the file</LI>
   * <LI>the language of the file</LI>
   * </UL>
   * 
   * All this files will be parsed and then indexed but not stored in the index.
   * They may be added in any order.
   */
  public void addFileContent(String path, String encoding, String format,
      String lang) {
    if (path != null) {
      getFileList().add(new FileDescription(path, encoding, format, lang));
    }
  }

  /**
   * @deprecated use addField(String fieldName, String value) instead
   */
  public void addXMLField(String fieldName, String value) {
    addXMLField(fieldName, value, null);
  }

  /**
   * @deprecated use addField(String fieldName, String value, String language)
   *             instead
   */
  public void addXMLField(String fieldName, String value, String language) {
    getFields().add(new FieldDescription(fieldName, value, language));
  }

  public void addField(String fieldName, String value) {
    addField(fieldName, value, null);
  }

  public void addField(String fieldName, String value, String language) {
    getFields().add(new FieldDescription(fieldName, value, language));
  }

  public void addField(String fieldName, Date value) {
    addField(fieldName, value, null);
  }

  public void addField(String fieldName, Date value, String language) {
    getFields().add(
        new FieldDescription(fieldName, DateFormatter.date2IndexFormat(value),
            language));
  }

  /**
   * Return the List of all the added texts. The returned List is a list of
   * String.
   */
  public List getTextContentList() {
    return getTextList();
  }

  /**
   * Return the List of all the added files. The returned List is a list of
   * FileDescription.
   */
  public List getFileContentList() {
    return getFileList();
  }

  /**
   * @deprecated use getFields() instead
   */
  public List getXmlFields() {
    return getFields();
  }

  private List getTextList() {
    if (textList == null)
      textList = new ArrayList();
    return textList;
  }

  private List getFileList() {
    if (fileList == null)
      fileList = new ArrayList();
    return fileList;
  }

  public List getFields() {
    if (fields == null)
      fields = new ArrayList();
    return fields;
  }

  /**
   * All the added texts and files are collected in two lists to be later
   * retrieved by the index engine.
   * 
   * textList is a list of String. fileList is a list of FileDescription.
   */
  private List textList = null;
  private List fileList = null;
  private List fields = null;
}
