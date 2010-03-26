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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * IconWA.java
 * 
 * @author  neysseri
 * Created on 12 decembre 2000, 11:37
 */

package com.stratelia.webactiv.util.viewGenerator.html.icons;

/*
 * CVS Informations
 * 
 * $Id: IconWA.java,v 1.2 2003/12/03 19:18:37 neysseri Exp $
 * 
 * $Log: IconWA.java,v $
 * Revision 1.2  2003/12/03 19:18:37  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.7  2002/05/29 09:32:16  groccia
 * portage netscape
 *
 * Revision 1.6.12.1  2002/05/07 15:24:06  fsauvand
 * no message
 *
 * Revision 1.6  2002/01/04 14:04:24  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * @author
 */
public class IconWA extends AbstractIcon {

  /**
   * Creates new IconWA
   */
  public IconWA() {
    super();
  }

  public IconWA(String iconName) {
    super(iconName);
  }

  /**
   * Constructor declaration
   * @param iconName
   * @param altText
   * @see
   */
  public IconWA(String iconName, String altText) {
    super(iconName, altText);
  }

  /**
   * Constructor declaration
   * @param iconName
   * @param altText
   * @param action
   * @see
   */
  public IconWA(String iconName, String altText, String action) {
    super(iconName, altText, action);
  }

  /**
   * Constructor declaration
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
   * @see
   */
  public IconWA(String iconName, String altText, String action, String imagePath) {
    super(iconName, altText, action, imagePath);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String path = getRootImagePath() + getImagePath();
    String action = getAction();
    String iconName = getIconName();
    String altText = getAltText();
    StringBuffer str = new StringBuffer();

    if (!action.equals(""))
      str.append("<A HREF=\"").append(action).append("\">");

    str.append("<IMG SRC=\"").append(path).append(iconName).append(
        "\" border=\"0\"");

    if (altText != null && altText.length() > 0)
      str.append("alt=\"").append(altText).append("\" title=\"")
          .append(altText);

    str.append("\">");

    if (!action.equals(""))
      str.append("</A>");

    return str.toString();
  }

}
