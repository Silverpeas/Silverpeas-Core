/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.clipboard.control.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;

/**
 * A SearchEngineEJB search the web'activ index
 * and give access to the retrieved index entries.
 */
public class ClipboardBmEJB implements SessionBean
{
    private ClipboardSelection m_LastObject = null;
    private ArrayList          m_ObjectList = null;
    private boolean            m_MultiClip = true;
    private boolean            m_Adding2Selection = true;
    private String             m_Name = null;
    private int                m_Count = 0;

    /**
     * User alert in case of error during operation
     */
    private String             m_MessageError = null;
    private Exception          m_ExceptionError = null;

    /**
     * Copy a node.
     * 
     */
    public void add(ClipboardSelection objectToCopy) throws RemoteException
    {
        try
        {
            SilverTrace.info("clipboard", "ClipboardBmEJB.add()", "root.MSG_GEN_ENTER_METHOD");
            m_Count += 1;
            if (objectToCopy != null)
            {
                if (!m_Adding2Selection)
                {
                    // we have to deselect the object still in clipboard
                    Iterator iterator = m_ObjectList.iterator();

                    while (iterator.hasNext())
                    {
                        ClipboardSelection clipObject = (ClipboardSelection) iterator.next();

                        clipObject.setSelected(false);
                    }
                    // now we add...
                    m_Adding2Selection = true;
                }
                if (objectToCopy.isDataFlavorSupported(ClipboardSelection.IndexFlavor))
                {
                    try
                    {
                        IndexEntry MainIndexEntry = (IndexEntry) objectToCopy.getTransferData(ClipboardSelection.IndexFlavor);
                        IndexEntry indexEntry;
                        Iterator   iterator = m_ObjectList.iterator();

                        while (iterator.hasNext())
                        {
                            ClipboardSelection clipObject = (ClipboardSelection) iterator.next();

                            if (clipObject.isDataFlavorSupported(ClipboardSelection.IndexFlavor))
                            {
                                indexEntry = (IndexEntry) clipObject.getTransferData(ClipboardSelection.IndexFlavor);
                                if (indexEntry.equals(MainIndexEntry))
                                {
                                	clipObject.setSelected(true);
                                    throw new Exception("");
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        // We dont add an other copy of this object
                        // but we need to select it
                        objectToCopy.setSelected(true);
                        objectToCopy = null;
                    }
                }
                if (objectToCopy != null)
                {
                    m_LastObject = objectToCopy;
                    if (m_MultiClip)
                    {
                        m_ObjectList.add(m_LastObject);
                        m_LastObject.setSelected(true);
                    }
                }
            }
            SilverTrace.info("clipboard", "ClipboardBmEJB.add()", "root.MSG_GEN_EXIT_METHOD");
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.add()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.add()",e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.add()",e);
        }
    }


    /**
     * Paste a node.
     * 
     */
    public ClipboardSelection getObject()
    {
        m_Count += 1;
        return m_LastObject;
    }

    /**
     * Return al the objects.
     * 
     */
    public Collection getObjects()
    {
        m_Count += 1;
        return m_ObjectList;
    }

    /**
     * Return the selected objects.
     * 
     */
    public Collection getSelectedObjects() throws RemoteException
    {
        try
        {
            m_Count += 1;
            ArrayList result = new ArrayList();
            Iterator  qi = m_ObjectList.iterator();

            while (qi.hasNext())
            {
                ClipboardSelection clipObject = (ClipboardSelection) qi.next();

                if (clipObject.isSelected())
                {
                    result.add(clipObject);
                }
            }
            return result;
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.getSelectedObjects()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.getSelectedObjects()",e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.getSelectedObjects()",e);
        }
    }

    /**
     * Returns the number of elements in the clipboard.
     */
    public int size() throws RemoteException
    {
        try
        {
            int Size;

            Size = m_ObjectList.size();
            return Size;
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.size()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.size()",e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.size()",e);
        }
    }

    /**
     * Returns the element at the specified position in the clipboard.
     */
    public ClipboardSelection getObject(int index) throws RemoteException
    {
        try
        {
            ClipboardSelection clipObject;

            clipObject = (ClipboardSelection) m_ObjectList.get(index);
            return clipObject;
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.getObject()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.getObject() index = " + Integer.toString(index),e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.getObject() index = " + Integer.toString(index),e);
        }
    }

    /**
     * When paste is done.
     */
    public void PasteDone()
    {
        // As soon as one paste operation is done
        // we know that the next copy should not keep the old selection
        m_Adding2Selection = false;
    }

    /**
     * Returns the element at the specified position in the clipboard.
     */
    public void setSelected(int index, boolean setIt) throws RemoteException
    {
        ClipboardSelection clipObject;

        try
        {
            clipObject = (ClipboardSelection) m_ObjectList.get(index);
            if (clipObject != null)
            {
                clipObject.setSelected(setIt);
            }
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.setSelected()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.setSelected() index = " + Integer.toString(index),e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.setSelected() index = " + Integer.toString(index),e);
        }
    }

    /**
     * Removes the element at the specified position in the clipboard.
     */
    public void remove(int index) throws RemoteException
    {
        try
        {
            m_ObjectList.remove(index);
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.remove()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.remove() index = " + Integer.toString(index),e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.remove() index = " + Integer.toString(index),e);
        }
    }

    /**
     * Removes all of the elements from the clipboard.
     */
    public void clear() throws RemoteException
    {
        try
        {
            m_ObjectList.clear();
            m_LastObject = null;
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.clear()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.clear()",e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.clear()",e);
        }
    }

    /**
     * Switch the clipboard to multi mode.
     */
    public void setMultiClipboard() throws RemoteException
    {
        try
        {
            m_MultiClip = true;
            if (m_LastObject != null)
            {
                m_ObjectList.clear();
                m_ObjectList.add(m_LastObject);
            }
        }
        catch (Exception e)
        {
            SilverTrace.warn("clipboard","ClipboardBmEJB.setMultiClipboard()","root.MSG_GEN_ERROR","ERROR occured in ClipboardBmEJB.setMultiClipboard()",e);
            throw new RemoteException("ERROR occured in ClipboardBmEJB.setMultiClipboard()",e);
        }
    }

    /**
     * Switch the clipboard to single mode.
     */
    public void setSingleClipboard()
    {
        m_MultiClip = false;
    }

    /**
     * Get the name of clipboard.
     */
    public String getName()
    {
        return m_Name;
    }

    /**
     * Get the count access of clipboard.
     */
    public Integer getCount()
    {
        return new Integer(m_Count);
    }

    /**
     * Method getMessageError
     * 
     * 
     * @return
     * 
     * @see
     */
    public String getMessageError()
    {
        String message = m_MessageError;

        m_MessageError = null;
        return message;
    }

    public Exception getExceptionError()
    {
        Exception valret = m_ExceptionError;

        m_ExceptionError = null;
        return valret;
    }

    /**
     * -
     * Method setMessageError
     * 
     * 
     * @return
     * 
     * @see
     */
    public void setMessageError(String messageID, Exception e)
    {
        m_MessageError = messageID;
        m_ExceptionError = e;
    }

    /**
     * Create a ClipboardBm.
     * 
     * The results set is initialized empty.
     */
    public void ejbCreate(String name) throws CreateException, RemoteException
    {
        m_LastObject = null;
        m_ObjectList = new ArrayList();
        m_Name = name;
		SilverTrace.info("clipboard", "ClipboardBmEJB.ejbCreate()", "root.MSG_GEN_ENTER_METHOD");
    }

    /**
     * Constructor.
     */
    public ClipboardBmEJB() 
	{
		SilverTrace.info("clipboard", "ClipboardBmEJB.constructor()", "root.MSG_GEN_ENTER_METHOD");
	}

    /**
     * The last results set is released.
     */
    public void ejbRemove() 
	{
		SilverTrace.info("clipboard", "ClipboardBmEJB.ejbRemove()", "root.MSG_GEN_ENTER_METHOD");
	}

    /**
     * The session context is useless.
     */
    public void setSessionContext(SessionContext sc) 
	{
		SilverTrace.info("clipboard", "ClipboardBmEJB.setSessionContext()", "root.MSG_GEN_ENTER_METHOD");
	}

    /**
     * There is no ressources to be released.
     */
    public void ejbPassivate() 
	{
		SilverTrace.info("clipboard", "ClipboardBmEJB.ejbPassivate()", "root.MSG_GEN_ENTER_METHOD");
	}

    /**
     * There is no ressources to be restored.
     */
    public void ejbActivate() 
	{
		SilverTrace.info("clipboard", "ClipboardBmEJB.ejbActivate()", "root.MSG_GEN_ENTER_METHOD");
	}

}
