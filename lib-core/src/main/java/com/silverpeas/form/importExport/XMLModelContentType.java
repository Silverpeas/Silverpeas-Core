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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.form.importExport;

import java.util.ArrayList;


/**
 * @author neysseri 
 */
public class XMLModelContentType {

  private String name = null;
  private ArrayList<XMLField> fields;

  public XMLModelContentType() {
  }

  public XMLModelContentType(String name) {
    setName(name);
  }

  /**
   * @return the name of the XML Model used
   */
  public String getName() {
    return name;
  }

  /**
   * @param name 
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return Returns the listImageParts.
   */
  public ArrayList<XMLField> getFields() {
    return fields;
  }

  /**
   * @param listImageParts The listImageParts to set.
   */
  public void setFields(ArrayList<XMLField> fields) {
    this.fields = fields;
  }

  public void addField(XMLField field) {
    if (fields == null) {
      fields = new ArrayList<XMLField>();
    }

    fields.add(field);
  }
}