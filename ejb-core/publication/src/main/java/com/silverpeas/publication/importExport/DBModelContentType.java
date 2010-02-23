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
/*
 * Created on 24 janv. 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.silverpeas.publication.importExport;

import java.util.ArrayList;

/**
 * @author tleroi To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DBModelContentType {

  private int id = -1;
  private ArrayList listTextParts;
  private ArrayList listImageParts;

  /**
   * @return
   */
  public int getId() {
    return id;
  }

  /**
   * @param i
   */
  public void setId(int i) {
    id = i;
  }

  /**
   * @return Returns the listImageParts.
   */
  public ArrayList getListImageParts() {
    return listImageParts;
  }

  /**
   * @param listImageParts The listImageParts to set.
   */
  public void setListImageParts(ArrayList listImageParts) {
    this.listImageParts = listImageParts;
  }

  /**
   * @return Returns the listTextParts.
   */
  public ArrayList getListTextParts() {
    return listTextParts;
  }

  /**
   * @param listTextParts The listTextParts to set.
   */
  public void setListTextParts(ArrayList listTextParts) {
    this.listTextParts = listTextParts;
  }
}
