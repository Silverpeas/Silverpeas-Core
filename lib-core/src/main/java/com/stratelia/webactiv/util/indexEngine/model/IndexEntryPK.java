/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.indexEngine.model;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * An IndexEntryPK uniquely identify an entry in the web'activ index. An IndexEntryPK is set at the
 * index entry creation time : when a web'activ's component adds a new element or document. This
 * IndexEntryPK will be return later when the document matchs a query. A web'activ document is
 * uniquely identified by :
 * <UL>
 * <LI>the space name where the element has been created. This space name may be a user id : when
 * the space is the private working space of this user.</LI>
 * <LI>The component name which handles the element. This component name may be an instance name
 * when several instances of the same component live in the same space.</LI>
 * <LI>The object type. The meaning of this type is uniquely determined by the component which
 * handles the object.</LI>
 * <LI>The object id.</LI>
 * </UL>
 */
public final class IndexEntryPK implements Serializable {
  /**
   * The constructor set in a row all the parts of the key.
   * @deprecated - parameter space is no more used
   */
  public IndexEntryPK(String space, String component, String objectType,
      String objectId) {
    // this.space = space;
    this.component = component;
    this.objectType = objectType;
    this.objectId = objectId;
  }

  public IndexEntryPK(String componentId, String objectType, String objectId) {
    this.component = componentId;
    this.objectType = objectType;
    this.objectId = objectId;
  }

  /**
   * Return the space of the indexed document or the userId if the space is a private working space.
   * @deprecated - to use this method is forbidden
   */
  public String getSpace() {
    return null;
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
  public String toString() {
    // return space +SEP+ component +SEP+ objectType +SEP+ objectId;
    return getComponent() + SEP + getObjectType() + SEP + getObjectId();
  }

  /**
   * To be equal two IndexEntryPK must have the same four parts (space, component, type, id). The
   * equals method is redefined so IndexEntryPK objects can be put in a Set or used as Map key.
   */
  public boolean equals(Object o) {
    if (o instanceof IndexEntryPK) {
      IndexEntryPK k = (IndexEntryPK) o;
      return this.toString().equals(k.toString());
    } else
      return false;
  }

  /**
   * Returns the hash code of the String representation. The hashCode method is redefined so
   * IndexEntryPK objects can be put in a Set or used as Map key.
   */
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Create a new IndexEntry from s. We must have :
   * 
   * <PRE>
   * create(s).toString().equals(s)
   * </PRE>
   */
  static public IndexEntryPK create(String s) {
    /*
     * The Tokenizer must return the separators SEP as a missing field must be parsed correctly :
     * SPACE|COMPO||ID must give (SPACE, COMP, "" , ID).
     */
    StringTokenizer Stk = new StringTokenizer(s, SEP, true);
    // String spa = "";
    String comp = "";
    String objType = "";
    String objId = "";

    /*
     * if (Stk.hasMoreTokens()) spa = Stk.nextToken(); if (spa.equals(SEP)) spa=""; else if
     * (Stk.hasMoreTokens()) Stk.nextToken(); // skip one SEP
     */

    if (Stk.hasMoreTokens())
      comp = Stk.nextToken();
    if (comp.equals(SEP))
      comp = "";
    else if (Stk.hasMoreTokens())
      Stk.nextToken(); // skip one SEP

    if (Stk.hasMoreTokens())
      objType = Stk.nextToken();
    if (objType.equals(SEP))
      objType = "";
    else if (Stk.hasMoreTokens())
      Stk.nextToken(); // skip one SEP

    if (Stk.hasMoreTokens())
      objId = Stk.nextToken();

    // return new IndexEntryPK(spa, comp, objType, objId);
    return new IndexEntryPK(comp, objType, objId);
  }

  /**
   * The four parts of an IndexEntryPK are private and fixed at construction time.
   */

  // private final String space;
  private final String component;
  private final String objectType;
  private final String objectId;

  /**
   * The separator used to write all key parts in a single lucene field.
   */
  private static final String SEP = "|";
}
