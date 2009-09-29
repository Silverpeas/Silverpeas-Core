/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Collection;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILUtil {
  // private String language = "";
  private String userId = "";
  private ResourceLocator message = null;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public SILVERMAILUtil(String userId, String language) {
    // this.language = language;
    this.userId = userId;
  }

  /**
   * Method declaration
   * 
   * 
   * @param folderName
   * 
   * @return
   * 
   * @see
   */
  public Collection getFolderMessageList(String folderName)
      throws SILVERMAILException {
    return SILVERMAILPersistence.getMessageOfFolder(Integer.parseInt(userId),
        folderName);
  }

  /**
   * Method declaration
   * 
   * 
   * @param resName
   * 
   * @return
   * 
   * @see
   */
  public String getString(String resName) {
    if (message == null) {
      return resName;
    } else {
      return message.getString(resName);
    }
  }

}