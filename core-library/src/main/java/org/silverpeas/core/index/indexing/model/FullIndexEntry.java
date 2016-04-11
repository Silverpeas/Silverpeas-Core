/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.index.indexing.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.silverpeas.core.index.indexing.DateFormatter;

/**
 * A FullIndexEntry is an IndexEntry completed with data usefull uniquely at the index creation time
 * (mainly all the data contents which must be indexed but which is useless at retrieve time). This
 * extra-content is indexed but not stored in the index.
 */
public class FullIndexEntry extends IndexEntry implements Serializable, Cloneable {

  private static final long serialVersionUID = -4955524385769457730L;

  /**
   * The constructor only set the key part of the IndexEntry.
   * @deprecated - parameter space is no more used
   */
  public FullIndexEntry(String space, String component, String objectType,
      String objectId) {
    super(component, objectType, objectId);
  }

  public FullIndexEntry(String component, String objectType, String objectId) {
    super(component, objectType, objectId);
  }

  public FullIndexEntry(IndexEntryKey pk) {
    super(pk);
  }

  /**
   * Add a text fragment to be indexed. All this text fragments will be indexed but not stored in
   * the index. They may be added in any order.
   */
  public void addTextContent(String text) {
    addTextContent(text, null);
  }

  public void addTextContent(String text, String language) {
    getTextList().add(new TextDescription(text, language));
  }

  /**
   * Add a file to be indexed. We need :
   * <UL>
   * <LI>the path to the file</LI>
   * <LI>the encoding of the file</LI>
   * <LI>the format of the file</LI>
   * <LI>the language of the file</LI>
   * </UL>
   * All this files will be parsed and then indexed but not stored in the index. They may be added
   * in any order.
   */
  public void addFileContent(String path, String encoding, String format, String lang) {
    if (path != null) {
      FileDescription fd = new FileDescription(path, encoding, format, lang);
      if (!getFileList().contains(fd)) {
        getFileList().add(fd);
      }
    }
  }

  /**
   * Add a linked file id to be indexed. We need :
   * <UL>
   * <LI>the file id</LI>
   * </UL>
   */
  public void addLinkedFileId(String fileId) {
    getLinkedFileIdsSet().add(fileId);
  }

  /**
   * Add a linked file to be indexed. We need :
   * <UL>
   * <LI>the path to the file</LI>
   * <LI>the encoding of the file</LI>
   * <LI>the format of the file</LI>
   * <LI>the language of the file</LI>
   * </UL>
   * All this files will be parsed and then indexed but not stored in the index. They may be added
   * in any order.
   */
  public void addLinkedFileContent(String path, String encoding, String format,
      String lang) {
    if (path != null) {
      getLinkedFileList().add(new FileDescription(path, encoding, format, lang));
    }
  }

  /**
   * @deprecated use addField(String fieldName, String value) instead
   */
  public void addXMLField(String fieldName, String value) {
    addXMLField(fieldName, value, null);
  }

  /**
   * @deprecated use addField(String fieldName, String value, String language) instead
   */
  public void addXMLField(String fieldName, String value, String language) {
    getFields().add(new FieldDescription(fieldName, value, language, false));
  }

  public void addField(String fieldName, String value) {
    addField(fieldName, value, null, false);
  }

  public void addField(String fieldName, String value, String language, boolean stored) {
    getFields().add(new FieldDescription(fieldName, value, language, stored));
  }

  public void addField(String fieldName, Date value) {
    addField(fieldName, value, null);
  }

  public void addField(String fieldName, Date value, String language) {
    getFields().add(
        new FieldDescription(fieldName, DateFormatter.date2IndexFormat(value),
        language, false));
  }

  /**
   * Return the List of all the added texts. The returned List is a list of String.
   */
  public List<TextDescription> getTextContentList() {
    return getTextList();
  }

  /**
   * Return the List of all the added files. The returned List is a list of FileDescription.
   */
  public List<FileDescription> getFileContentList() {
    return getFileList();
  }

  /**
   * Return the List of all the linked files. The returned List is a list of FileDescription.
   */
  public List<FileDescription> getLinkedFileContentList() {
    return getLinkedFileList();
  }

  /**
   * @deprecated use getFields() instead
   */
  public List<FieldDescription> getXmlFields() {
    return getFields();
  }

  private List<TextDescription> getTextList() {
    if (textList == null)
      textList = new ArrayList<>();
    return textList;
  }

  private List<FileDescription> getFileList() {
    if (fileList == null)
      fileList = new ArrayList<>();
    return fileList;
  }

  private List<FileDescription> getLinkedFileList() {
    if (linkedFileList == null)
      linkedFileList = new ArrayList<>();
    return linkedFileList;
  }

  public Set<String> getLinkedFileIdsSet() {
    if (linkedFileIdsList == null)
      linkedFileIdsList = new HashSet<>();
    return linkedFileIdsList;
  }

  public List<FieldDescription> getFields() {
    if (fields == null)
      fields = new ArrayList<>();
    return fields;
  }

  @Override
  public FullIndexEntry clone() {
    return (FullIndexEntry) super.clone();
  }

  /**
   * All the added texts and files are collected in two lists to be later retrieved by the index
   * engine. textList is a list of String. fileList is a list of FileDescription.
   */
  private List<TextDescription> textList = null;
  private List<FileDescription> fileList = null;
  private List<FileDescription> linkedFileList = null;
  private List<FieldDescription> fields = null;
  private Set<String> linkedFileIdsList = null;
}
