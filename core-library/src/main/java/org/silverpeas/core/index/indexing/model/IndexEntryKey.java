/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.index.indexing.model;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * An IndexEntryKey uniquely identifies an entry in the indexes. An IndexEntryKey is set at the
 * index entry creation time : when a Silverpeas component adds a new element or document. This
 * IndexEntryKey will be returned later when the document matches a query. A document in Silverpeas
 * is uniquely identified by:
 * <ul>
 *  <LI>the space name where the element has been created. This space name may be a user id
 *  when the space is the private working space of this user.</LI>
 * <LI>The component name which handles the element. This component name may be an instance name
 * when
 * several instances of the same component live in the same space.</LI>
 * <LI>The object type. The meaning of this type is uniquely determined by the component which
 * handles the object.</LI>
 * <LI>The object id.</LI>
 * </UL>
 */
public final class IndexEntryKey implements Serializable {

  private static final long serialVersionUID = 339617003068469338L;
  /**
   * The separator used to write all key parts in a single lucene field.
   */
  private static final String SEP = "|";

  /**
   * The three parts of an IndexEntryKey are private and fixed at construction time.
   */
  private final String component;
  private final String objectType;
  private final String objectId;

  public IndexEntryKey(String componentId, String objectType, String objectId) {
    this.component = componentId;
    this.objectType = objectType;
    this.objectId = objectId;
  }

  IndexEntryKey(final IndexEntryKey other) {
    this.component = other.component;
    this.objectType = other.objectType;
    this.objectId = other.objectId;
  }

  /**
   * Create a new IndexEntry from s. We must have :
   * <PRE>
   * create(s).toString().equals(s)
   * </PRE>
   */
  public static IndexEntryKey create(String s) {
    /*
     * The Tokenizer must return the separators SEP as a missing field must be parsed correctly :
     * COMPO|TYPE|ID must give (COMP,TYPE,ID).
     */
    final StringTokenizer stk = new StringTokenizer(s, SEP, true);
    String comp = "";
    String objType = "";
    String objId = "";
    if (stk.hasMoreTokens()) {
      comp = stk.nextToken();
    }
    if (comp.equals(SEP)) {
      comp = "";
    } else if (stk.hasMoreTokens()) {
      stk.nextToken(); // skip one SEP
    }
    if (stk.hasMoreTokens()) {
      objType = stk.nextToken();
    }
    if (objType.equals(SEP)) {
      objType = "";
    } else if (stk.hasMoreTokens()) {
      stk.nextToken(); // skip one SEP
    }
    if (stk.hasMoreTokens()) {
      objId = stk.nextToken();
    }
    return new IndexEntryKey(comp, objType, objId);
  }

  /**
   * Return the name of the component's instance which handles the object.
   */
  public String getComponent() {
    return component;
  }

  /**
   * Return the type of the indexed document. The meaning of this type is uniquely determined by the
   * component handling the object.
   */
  public String getObjectType() {
    return objectType;
  }

  /**
   * Return the object id.
   */
  public String getObjectId() {
    return objectId;
  }

  /**
   * Returns a string which can be used later to recontruct the key with the create method.
   */
  @Override
  public String toString() {
    return getComponent() + SEP + getObjectType() + SEP + getObjectId();
  }

  /**
   * To be equal two IndexEntryKey must have the same four parts (space, component, type, id). The
   * equals method is redefined so IndexEntryKey objects can be put in a Set or used as Map key.
   */
  @Override
  public boolean equals(Object o) {
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
}
