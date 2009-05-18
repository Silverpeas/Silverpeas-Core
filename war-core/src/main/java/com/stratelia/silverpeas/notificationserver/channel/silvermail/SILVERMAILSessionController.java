/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationserver.channel.silvermail;

import java.util.Collection;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
/**
 * Class declaration
 *
 *
 * @author
 * @version %I%, %G%
 */
public class SILVERMAILSessionController extends AbstractComponentSessionController
{
	protected String currentFunction;
	protected long currentMessageId = -1;

	/**
	 * Constructor declaration
	 *
	 *
	 * @see
	 */
	public SILVERMAILSessionController(MainSessionController mainSessionCtrl,
                ComponentContext context)
    {
        super(mainSessionCtrl, context, "com.stratelia.silverpeas.notificationserver.channel.silvermail.multilang.silvermail", "com.stratelia.silverpeas.notificationserver.channel.silvermail.settings.silvermailIcons");
        setComponentRootName(URLManager.CMP_SILVERMAIL);
    }

    protected String getComponentInstName()
    {
        return URLManager.CMP_SILVERMAIL;
    }



	/**
	 * Method declaration
	 *
	 *
	 * @param currentFunction
	 *
	 * @see
	 */
	public void setCurrentFunction(String currentFunction)
	{
		this.currentFunction = currentFunction;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public String getCurrentFunction()
	{
		return currentFunction;
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
	public Collection getFolderMessageList(String folderName) throws SILVERMAILException
	{
		return SILVERMAILPersistence.getMessageOfFolder(Integer.parseInt(getUserId()), folderName);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param messageId
	 *
	 * @return
	 *
	 * @see
	 */
	public SILVERMAILMessage getMessage(long messageId) throws SILVERMAILException
	{
		return SILVERMAILPersistence.getMessage(messageId);
	}

	/**
	 * Method declaration
	 *
	 *
	 * @return
	 *
	 * @see
	 */
	public long getCurrentMessageId()
	{
		return currentMessageId;
	}

	/**
	 * Method declaration
	 *
	 *
	 * @param value
	 *
	 * @see
	 */
	public void setCurrentMessageId(long value)
	{
		currentMessageId = value;
	}

}
