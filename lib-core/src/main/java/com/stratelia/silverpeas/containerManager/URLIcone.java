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
package com.stratelia.silverpeas.containerManager;

/**
 * This is the data structure that represents a link links between icone and action (use in JSP
 * ActionBar)
 */
public class URLIcone {
  private String sIconePath = ""; // path on the icone to show in the JSP
  private String sAlternateText = ""; // Alternate text on the icone
  private String sActionURL = ""; // URLs on the action to link with the icones
  private boolean bPopUp = true; // Tells the JSP to open the sActionURL in a

  // new PopUp window or not

  public URLIcone() {
  }

  public void setIconePath(String sGivenIconePath) {
    sIconePath = sGivenIconePath;
  }

  public String getIconePath() {
    return sIconePath;
  }

  public void setAlternateText(String sGivenAlternateText) {
    sAlternateText = sGivenAlternateText;
  }

  public String getAlternateText() {
    return sAlternateText;
  }

  public void setActionURL(String sGivenActionURL) {
    sActionURL = sGivenActionURL;
  }

  public String getActionURL() {
    return sActionURL;
  }

  public void setPopUp(boolean bGivenPopUp) {
    bPopUp = bGivenPopUp;
  }

  public boolean getPopUp() {
    return bPopUp;
  }
}
