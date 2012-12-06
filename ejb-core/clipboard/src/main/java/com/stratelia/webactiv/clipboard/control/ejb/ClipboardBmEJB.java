/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.clipboard.control.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.util.clipboard.ClipboardException;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.search.indexEngine.model.IndexEntry;

/**
 * A SearchEngincommeeEJB search the web'activ index and give access to the retrieved index entries.
 */
public class ClipboardBmEJB implements SessionBean {
  private static final long serialVersionUID = -824732581358882058L;

  private ClipboardSelection m_LastObject = null;
  private ArrayList<ClipboardSelection> m_ObjectList = null;
  private boolean m_MultiClip = true;
  private boolean m_Adding2Selection = true;
  private String m_Name = null;
  private int m_Count = 0;

  /**
   * User alert in case of error during operation
   */
  private String m_MessageError = null;
  private Exception m_ExceptionError = null;

  /**
   * Copy a node.
   * @param objectToCopy
   * @throws ClipboardException
   */
  public void add(ClipboardSelection objectToCopy) throws ClipboardException {
    try {
      SilverTrace.info("clipboard", "ClipboardBmEJB.add()",
          "root.MSG_GEN_ENTER_METHOD");
      m_Count += 1;
      boolean failed = false;
      if (objectToCopy != null) {
        if (!m_Adding2Selection) {
          // we have to deselect the object still in clipboard
          for (ClipboardSelection clipObject : m_ObjectList) {
            clipObject.setSelected(false);
          }
          // now we add...
          m_Adding2Selection = true;
        }
        if (objectToCopy.isDataFlavorSupported(ClipboardSelection.IndexFlavor)) {
          try {
            IndexEntry MainIndexEntry = (IndexEntry) objectToCopy
                .getTransferData(ClipboardSelection.IndexFlavor);
            IndexEntry indexEntry;
            for (ClipboardSelection clipObject : m_ObjectList) {
              if (clipObject
                  .isDataFlavorSupported(ClipboardSelection.IndexFlavor)) {
                indexEntry = (IndexEntry) clipObject
                    .getTransferData(ClipboardSelection.IndexFlavor);
                if (indexEntry.equals(MainIndexEntry)) {
                  clipObject.setSelected(true);
                  throw new Exception("");
                }
              }
            }
          } catch (Exception e) {
            // We dont add an other copy of this object
            // but we need to select it
            objectToCopy.setSelected(true);
            failed = true;
          }
        }
        if (objectToCopy != null && !failed) {
          m_LastObject = objectToCopy;
          if (m_MultiClip) {
            m_ObjectList.add(m_LastObject);
            m_LastObject.setSelected(true);
          }
        }
      }
      SilverTrace.info("clipboard", "ClipboardBmEJB.add()",
          "root.MSG_GEN_EXIT_METHOD");
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.add()",
          "root.MSG_GEN_ERROR", "ERROR occured in ClipboardBmEJB.add()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.add()", e);
    }
  }

  /**
   * Paste a node.
   * @return
   */
  public ClipboardSelection getObject() {
    m_Count += 1;
    return m_LastObject;
  }

  /**
   * Return al the objects.
   * @return
   */
  public Collection<ClipboardSelection> getObjects() {
    m_Count += 1;
    return Collections.unmodifiableCollection(m_ObjectList);
  }

  /**
   * Return the selected objects.
   * @return
   * @throws ClipboardException
   */
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException {
    try {
      m_Count += 1;
      ArrayList<ClipboardSelection> result = new ArrayList<ClipboardSelection>();
      for (ClipboardSelection clipObject : m_ObjectList) {
        if (clipObject.isSelected()) {
          result.add(clipObject);
        }
      }
      return result;
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.getSelectedObjects()",
          "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.getSelectedObjects()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.getSelectedObjects()", e);
    }
  }

  /**
   * Returns the number of elements in the clipboard.
   * @return
   * @throws ClipboardException
   */
  public int size() throws ClipboardException {
    try {
      return m_ObjectList.size();
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.size()",
          "root.MSG_GEN_ERROR", "ERROR occured in ClipboardBmEJB.size()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.size()", e);
    }
  }

  /**
   * Returns the element at the specified position in the clipboard.
   * @param index
   * @return
   * @throws ClipboardException
   */
  public ClipboardSelection getObject(int index) throws ClipboardException {
    try {
      ClipboardSelection clipObject;

      clipObject = m_ObjectList.get(index);
      return clipObject;
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.getObject()",
          "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.getObject() index = "
          + Integer.toString(index), e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in getObject(" + index + ")", e);
    }
  }

  /**
   * When paste is done.
   */
  public void PasteDone() {
    // As soon as one paste operation is done
    // we know that the next copy should not keep the old selection
    m_Adding2Selection = false;

    // Deselect cutted objects still in clipboard
    for (ClipboardSelection clipObject : m_ObjectList) {
      if (clipObject.isCutted()) {
        clipObject.setSelected(false);
      }
    }
  }

  /**
   * Returns the element at the specified position in the clipboard.
   * @param index
   * @param setIt
   * @throws ClipboardException
   */
  public void setSelected(int index, boolean setIt) throws ClipboardException {
    ClipboardSelection clipObject;

    try {
      clipObject = m_ObjectList.get(index);
      if (clipObject != null) {
        clipObject.setSelected(setIt);
      }
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.setSelected()",
          "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.setSelected() index = "
          + Integer.toString(index), e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in getSelectedObject(" + index + ", " + setIt + ")", e);
    }
  }

  /**
   * Removes the element at the specified position in the clipboard.
   * @param index
   * @throws ClipboardException
   */
  public void removeObject(int index) throws ClipboardException {
    try {
      m_ObjectList.remove(index);
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.remove()",
          "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.remove() index = "
          + Integer.toString(index), e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.remove(" + index + ")", e);

    }
  }

  /**
   * Removes all of the elements from the clipboard.
   * @throws ClipboardException
   */
  public void clear() throws ClipboardException {
    try {
      m_ObjectList.clear();
      m_LastObject = null;
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.clear()",
          "root.MSG_GEN_ERROR", "ERROR occured in ClipboardBmEJB.clear()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.clear()", e);
    }
  }

  /**
   * Switch the clipboard to multi mode.
   * @throws RemoteException
   */
  public void setMultiClipboard() throws RemoteException {
    try {
      m_MultiClip = true;
      if (m_LastObject != null) {
        m_ObjectList.clear();
        m_ObjectList.add(m_LastObject);
      }
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.setMultiClipboard()",
          "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.setMultiClipboard()", e);
      throw new RemoteException(
          "ERROR occured in ClipboardBmEJB.setMultiClipboard()", e);
    }
  }

  /**
   * Switch the clipboard to single mode.
   */
  public void setSingleClipboard() {
    m_MultiClip = false;
  }

  /**
   * Get the name of clipboard.
   * @return
   */
  public String getName() {
    return m_Name;
  }

  /**
   * Get the count access of clipboard.
   * @return
   */
  public Integer getCount() {
    return new Integer(m_Count);
  }

  /**
   * Method getMessageError
   * @return
   * @see
   */
  public String getMessageError() {
    String message = m_MessageError;

    m_MessageError = null;
    return message;
  }

  public Exception getExceptionError() {
    Exception valret = m_ExceptionError;

    m_ExceptionError = null;
    return valret;
  }

  /**
   * - Method setMessageError
   * @param messageID
   * @param e
   * @see
   */
  public void setMessageError(String messageID, Exception e) {
    m_MessageError = messageID;
    m_ExceptionError = e;
  }

  /**
   * Create a ClipboardBm. The results set is initialized empty.
   * @param name
   * @throws CreateException
   * @throws RemoteException
   */
  public void ejbCreate(String name) throws CreateException, RemoteException {
    m_LastObject = null;
    m_ObjectList = new ArrayList<ClipboardSelection>();
    m_Name = name;
    SilverTrace.info("clipboard", "ClipboardBmEJB.ejbCreate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * Constructor.
   */
  public ClipboardBmEJB() {
    SilverTrace.info("clipboard", "ClipboardBmEJB.constructor()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * The last results set is released.
   */
  @Override
  public void ejbRemove() {
    SilverTrace.info("clipboard", "ClipboardBmEJB.ejbRemove()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * The session context is useless.
   * @param sc
   */
  @Override
  public void setSessionContext(SessionContext sc) {
    SilverTrace.info("clipboard", "ClipboardBmEJB.setSessionContext()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * There is no ressources to be released.
   */
  @Override
  public void ejbPassivate() {
    SilverTrace.info("clipboard", "ClipboardBmEJB.ejbPassivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

  /**
   * There is no ressources to be restored.
   */
  @Override
  public void ejbActivate() {
    SilverTrace.info("clipboard", "ClipboardBmEJB.ejbActivate()",
        "root.MSG_GEN_ENTER_METHOD");
  }

}
