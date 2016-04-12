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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.web.silverstatistics.vo;

import java.io.Serializable;

import org.silverpeas.core.ForeignPK;

/**
 * Class declaration
 * @author
 */
public class AccessPublicationVO implements Serializable {

  private static final long serialVersionUID = 1L;

  private ForeignPK foreignPK;
  private int nbAccess;

  /**
   * Constructor declaration
   * @param nbAccess
   */
  public AccessPublicationVO(ForeignPK foreignPK, int nbAccess) {
    super();
    this.foreignPK = foreignPK;
    this.nbAccess = nbAccess;
  }

  /**
   * @return the foreignPK
   */
  public ForeignPK getForeignPK() {
    return foreignPK;
  }

  /**
   * @param foreignPK the foreignPK to set
   */
  public void setForeignPK(ForeignPK foreignPK) {
    this.foreignPK = foreignPK;
  }

  /**
   * @return the nbAccess
   */
  public int getNbAccess() {
    return nbAccess;
  }

  /**
   * @param nbAccess the nbAccess to set
   */
  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }
}
