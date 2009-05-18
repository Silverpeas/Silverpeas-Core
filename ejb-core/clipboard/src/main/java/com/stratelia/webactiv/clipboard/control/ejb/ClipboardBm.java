/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.clipboard.control.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.stratelia.webactiv.clipboard.model.ClipboardSelection;

/**
 * A ClipboardBm implement the clipboard mechanisme.
 */
public interface ClipboardBm extends EJBObject
{

    /**
     * Method declaration
     * 
     * 
     * @param clipObject
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void add(ClipboardSelection clipObject) throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public ClipboardSelection getObject() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void PasteDone() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public Collection getSelectedObjects() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public Collection getObjects() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public int size() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @param index
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public ClipboardSelection getObject(int index) throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @param index
     * @param setIt
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void setSelected(int index, boolean setIt) throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @param index
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void remove(int index) throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void clear() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void setMultiClipboard() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void setSingleClipboard() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public String getName() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public Integer getCount() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public String getMessageError() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @return
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public Exception getExceptionError() throws RemoteException;

    /**
     * Method declaration
     * 
     * 
     * @param messageID
     * @param e
     * 
     * @throws RemoteException
     * 
     * @see
     */
    public void setMessageError(String messageID, Exception e) throws RemoteException;
}
