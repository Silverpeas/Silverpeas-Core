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

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Collection;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;

/**
 * Class declaration
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILSessionController extends AbstractComponentSessionController {
  protected String currentFunction;
  protected long currentMessageId = -1;

  /**
   * Constructor declaration
   * @see
   */
  public SILVERMAILSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(
        mainSessionCtrl,
        context,
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail",
        "com.stratelia.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons");
    setComponentRootName(URLManager.CMP_SILVERMAIL);
  }

  protected String getComponentInstName() {
    return URLManager.CMP_SILVERMAIL;
  }

  /**
   * Method declaration
   * @param currentFunction
   * @see
   */
  public void setCurrentFunction(String currentFunction) {
    this.currentFunction = currentFunction;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getCurrentFunction() {
    return currentFunction;
  }

  /**
   * Method declaration
   * @param folderName
   * @return
   * @see
   */
  public Collection getFolderMessageList(String folderName)
      throws SILVERMAILException {
    return SILVERMAILPersistence.getMessageOfFolder(Integer
        .parseInt(getUserId()), folderName);
  }

  /**
   * Method declaration
   * @param messageId
   * @return
   * @see
   */
  public SILVERMAILMessage getMessage(long messageId)
      throws SILVERMAILException {
    return SILVERMAILPersistence.getMessage(messageId);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public long getCurrentMessageId() {
    return currentMessageId;
  }

  /**
   * Method declaration
   * @param value
   * @see
   */
  public void setCurrentMessageId(long value) {
    currentMessageId = value;
  }

}
