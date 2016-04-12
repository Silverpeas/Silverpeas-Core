/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.clipboard.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import org.silverpeas.core.index.indexing.model.IndexEntry;

import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;

import org.silverpeas.core.silvertrace.SilverTrace;

import static org.silverpeas.core.clipboard.ClipboardSelection.IndexFlavor;

/**
 * Silverpeas Service to maintain the status of the main clipboard.
 */
@Singleton
@Transactional
@MainClipboard
public class MainClipboardService implements Clipboard, Serializable {

  private static final long serialVersionUID = -824732581358882058L;
  private ClipboardSelection lastObject = null;
  private ArrayList<ClipboardSelection> objectsInClipboard = null;
  private boolean multipleClipboardSupported = true;
  private boolean addingToSelection = true;
  private int count = 0;
  private String name = null;
  /**
   * User alert in case of error during operation
   */
  private String errorMessage = null;
  private Exception error = null;

  /**
   * Copy a node.
   *
   * @param objectToCopy
   * @throws ClipboardException
   */
  @Override
  public void add(ClipboardSelection objectToCopy) throws ClipboardException {
    try {

      count += 1;
      boolean failed = false;
      if (objectToCopy != null) {
        if (!addingToSelection) {
          // we have to deselect the object still in clipboard
          for (ClipboardSelection clipObject : objectsInClipboard) {
            clipObject.setSelected(false);
          }
          // now we add...
          addingToSelection = true;
        }
        if (objectToCopy.isDataFlavorSupported(IndexFlavor)) {
          try {
            IndexEntry MainIndexEntry = (IndexEntry) objectToCopy.getTransferData(IndexFlavor);
            for (ClipboardSelection clipObject : objectsInClipboard) {
              if (clipObject.isDataFlavorSupported(IndexFlavor)) {
                IndexEntry indexEntry = (IndexEntry) clipObject.getTransferData(IndexFlavor);
                if (indexEntry.equals(MainIndexEntry)) {
                  clipObject.setSelected(true);
                  clipObject.setCutted(objectToCopy.isCutted());
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
        if (!failed) {
          lastObject = objectToCopy;
          if (multipleClipboardSupported) {
            objectsInClipboard.add(lastObject);
            lastObject.setSelected(true);
          }
        }
      }

    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.add()", "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.add()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.add()", e);
    }
  }

  /**
   * Paste a node.
   *
   * @return
   */
  @Override
  public ClipboardSelection getObject() {
    count += 1;
    return lastObject;
  }

  /**
   * Return al the objects.
   *
   * @return
   */
  @Override
  public Collection<ClipboardSelection> getObjects() {
    count += 1;
    return Collections.unmodifiableCollection(objectsInClipboard);
  }

  /**
   * Return the selected objects.
   *
   * @return
   * @throws ClipboardException
   */
  @Override
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException {
    try {
      count += 1;
      List<ClipboardSelection> result = new ArrayList<ClipboardSelection>(objectsInClipboard.size());
      for (ClipboardSelection clipObject : objectsInClipboard) {
        if (clipObject.isSelected()) {
          result.add(clipObject);
        }
      }
      return result;
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.getSelectedObjects()", "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.getSelectedObjects()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.getSelectedObjects()", e);
    }
  }

  /**
   * Returns the number of elements in the clipboard.
   *
   * @return
   * @throws ClipboardException
   */
  @Override
  public int size() throws ClipboardException {
    try {
      return objectsInClipboard.size();
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.size()", "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.size()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.size()", e);
    }
  }

  /**
   * Returns the element at the specified position in the clipboard.
   *
   * @param index
   * @return
   * @throws ClipboardException
   */
  @Override
  public ClipboardSelection getObject(int index) throws ClipboardException {
    try {
      return objectsInClipboard.get(index);
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.getObject()", "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.getObject() index = " + index, e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in getObject(" + index + ")", e);
    }
  }

  /**
   * When paste is done.
   */
  @Override
  public void PasteDone() {
    // As soon as one paste operation is done
    // we know that the next copy should not keep the old selection
    addingToSelection = false;
    // Deselect cutted objects still in clipboard
    for (ClipboardSelection clipObject : objectsInClipboard) {
      if (clipObject.isCutted()) {
        clipObject.setSelected(false);
      }
    }
  }

  /**
   * Returns the element at the specified position in the clipboard.
   *
   * @param index
   * @param setIt
   * @throws ClipboardException
   */
  @Override
  public void setSelected(int index, boolean setIt) throws ClipboardException {
    try {
      ClipboardSelection clipObject = objectsInClipboard.get(index);
      if (clipObject != null) {
        clipObject.setSelected(setIt);
      }
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.setSelected()", "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.setSelected() index = " + index, e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in getSelectedObject(" + index + ", " + setIt + ")", e);
    }
  }

  /**
   * Removes the element at the specified position in the clipboard.
   *
   * @param index
   * @throws ClipboardException
   */
  @Override
  public void removeObject(int index) throws ClipboardException {
    try {
      objectsInClipboard.remove(index);
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.remove()", "root.MSG_GEN_ERROR",
          "ERROR occured in ClipboardBmEJB.remove() index = " + index, e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.remove(" + index + ")", e);

    }
  }

  /**
   * Removes all of the elements from the clipboard.
   *
   * @throws ClipboardException
   */
  @Override
  public void clear() {
    objectsInClipboard.clear();
    lastObject = null;
  }

  /**
   * Switch the clipboard to multi mode.
   *
   * @throws ClipboardException
   */
  @Override
  public void setMultiClipboard() throws ClipboardException {
    try {
      multipleClipboardSupported = true;
      if (lastObject != null) {
        objectsInClipboard.clear();
        objectsInClipboard.add(lastObject);
      }
    } catch (Exception e) {
      SilverTrace.warn("clipboard", "ClipboardBmEJB.setMultiClipboard()",
          "root.MSG_GEN_ERROR", "ERROR occured in ClipboardBmEJB.setMultiClipboard()", e);
      throw new ClipboardException("ClipboardBmEJB", SilverTrace.TRACE_LEVEL_ERROR,
          "ERROR occured in ClipboardBmEJB.setMultiClipboard()", e);
    }
  }

  /**
   * Switch the clipboard to single mode.
   */
  @Override
  public void setSingleClipboard() {
    multipleClipboardSupported = false;
  }

  /**
   * Get the count access of clipboard.
   *
   * @return
   */
  @Override
  public int getCount() {
    return count;
  }

  /**
   * Method getMessageError
   *
   * @return
   * @see
   */
  @Override
  public String getMessageError() {
    String message = errorMessage;
    errorMessage = null;
    return message;
  }

  @Override
  public Exception getExceptionError() {
    Exception valret = error;

    error = null;
    return valret;
  }

  /**
   * - Method setMessageError
   *
   * @param messageID
   * @param e
   * @see
   */
  @Override
  public void setMessageError(String messageID, Exception e) {
    errorMessage = messageID;
    error = e;
  }

  /**
   * Constructor.
   */
  public MainClipboardService() {

  }

  @PostConstruct
  public void setUp() {
    this.name = "MainClipboard";
    lastObject = null;
    objectsInClipboard = new ArrayList<>();
  }
}
