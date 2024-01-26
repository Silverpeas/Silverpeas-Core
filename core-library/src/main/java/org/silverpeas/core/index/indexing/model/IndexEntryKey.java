/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.kernel.util.StringUtil;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * An IndexEntryKey uniquely identifies an entry in the indexes. An IndexEntryKey is set at the
 * index entry creation time: when a Silverpeas component adds a new document to index.
 * The document to index refers a contribution or a resource of the specified component. A
 * contribution can be, for example, a publication or a comment. A resource, on the other side, can
 * be, for example  a user or a space instance. A component can be a space management service, a
 * user management service, a Silverpeas application, or any other modular components of which
 * Silverpeas is made up.
 * <p>
 * This IndexEntryKey will be returned later when the document matches a query. A document in
 * Silverpeas is uniquely identified by:
 * </p>
 * <ul>
 * <LI>The component which handles the contribution or the resource, referred further to as the
 * object. It can be a transverse service, an administration one or a component instance (a
 * Silverpeas application).</LI>
 * <LI>The type of the object (contribution or resource) referred by the document to index. The
 * meaning of this type is uniquely determined by the component that handles the object.</LI>
 * <LI>The unique identifier of the object. For example a user
 * identifier or a contribution identifier.</LI>
 * <LI>Eventually, if any, the identifier of another object to which the object referred by the
 * key is linked. For example, a publication is the object to which a comment is linked.</LI>
 * </UL>
 */
public final class IndexEntryKey implements Serializable {

  private static final long serialVersionUID = 339617003068469338L;
  /**
   * The separator used to write all key parts in a single lucene field.
   */
  private static final String SEP = "|";

  /**
   * The four parts of an IndexEntryKey are private and fixed at construction time.
   */
  private final String component;
  private final String objectType;
  private final String objectId;
  private final String linkedObjectId;

  /**
   * Constructs a new {@link IndexEntryKey} instance.
   * @param componentId the unique identifier of a component.
   * @param objectType the type of the object related by the document to index.
   * @param objectId the unique identifier of the object related by the document to index.
   */
  public IndexEntryKey(String componentId, String objectType, String objectId) {
    this.component = normalize(componentId);
    this.objectType = normalize(objectType);
    this.objectId = normalize(objectId);
    this.linkedObjectId = "";
  }

  /**
   * Constructs a new {@link IndexEntryKey} instance.
   * @param componentId the unique identifier of a component.
   * @param objectType the type of the object related by the document to index.
   * @param objectId the unique identifier of the object related by the document to index.
   * @param linkedObjectId the unique identifier of the object to which the object referred by the
   * key is linked. For example, for a comment, the linked object can be a publication.
   */
  public IndexEntryKey(String componentId, String objectType, String objectId,
      String linkedObjectId) {
    this.component = normalize(componentId);
    this.objectType = normalize(objectType);
    this.objectId = normalize(objectId);
    this.linkedObjectId = normalize(linkedObjectId);
  }

  /**
   * Constructs a new {@link IndexEntryKey} instance from the specified another one.
   * @param other the {@link IndexEntryKey} instance to copy.
   */
  IndexEntryKey(final IndexEntryKey other) {
    this.component = other.component;
    this.objectType = other.objectType;
    this.objectId = other.objectId;
    this.linkedObjectId = other.linkedObjectId;
  }

  /**
   * Creates a new IndexEntry from the specified formatted text.
   * @param s the formatted entry key
   * @return the {@link IndexEntryKey} instance got from the decoding of the given text.
   */
  public static IndexEntryKey create(String s) {
    /*
     * The Tokenizer must return the separators SEP as a missing field.
     * Examples of format:
     * COMPO|TYPE|ID| must give (COMP,TYPE,ID, "")
     * COMPO|TYPE|ID|LINKED must give (COMP, TYPE, LINKED)
     */
    final StringTokenizer stk = new StringTokenizer(s, SEP, true);
    String[] keyParts = new String[]{"", "", "", ""};
    int i = 0;
    while (stk.hasMoreElements() && i < 4) {
      String token = stk.nextToken();
      if (!token.equals(SEP)) {
        keyParts[i] = token;
        if (stk.hasMoreElements()) {
          stk.nextToken();
        }
      }
      i++;
    }
    return new IndexEntryKey(keyParts[0], keyParts[1], keyParts[2], keyParts[3]);
  }

  /**
   * Gets the unique identifier of the component instance handling the object referred by this key.
   * @return the identifier of the component that handles the object related by the
   * indexed document.
   */
  public String getComponentId() {
    return component;
  }

  /**
   * Gets the type of the object referred by this key. The meaning of this type is uniquely
   * determined by the component handling the object.
   * @return the type of the object related by the indexed document.
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * Gets the identifier of the object referred by this key.
   * @return the unique identifier of the object related by the indexed document.
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * Gets the identifier of the object to which the object referred by this key is linked.
   * @return identifier of the linked object or an empty string if no object is linked.
   */
  public String getLinkedObjectId() {
    return linkedObjectId;
  }

  /**
   * Returns a string which can be used later to construct the key with the create method.
   */
  @Override
  public String toString() {
    return getComponentId() + SEP + getObjectType() + SEP + getObjectId() + SEP + getLinkedObjectId();
  }

  /**
   * To be equal two IndexEntryKey must have the same four parts (space, component, type, id). The
   * equals method is redefined so IndexEntryKey objects can be put in a Set or used as Map key.
   */
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof IndexEntryKey) {
      IndexEntryKey k = (IndexEntryKey) o;
      return this.toString().equals(k.toString());
    }
    return false;
  }

  /**
   * Returns the hash code of the String representation. The hashCode method is redefined so
   * IndexEntryKey objects can be put in a Set or used as Map key.
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  private String normalize(final String raw) {
    return StringUtil.defaultStringIfNotDefined(raw);
  }

}
