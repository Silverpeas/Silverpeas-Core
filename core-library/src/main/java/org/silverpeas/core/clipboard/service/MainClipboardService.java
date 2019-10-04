/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.clipboard.service;

import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;
import org.silverpeas.core.index.indexing.model.IndexEntry;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.transaction.Transactional;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.clipboard.ClipboardSelection.IndexFlavor;

/**
 * Silverpeas Service to maintain the status of the main clipboard.
 */
@SessionScoped
@Transactional
@MainClipboard
public class MainClipboardService implements Clipboard, Serializable {

  private static final long serialVersionUID = -824732581358882058L;
  private ClipboardSelection lastObject = null;
  private List<ClipboardSelection> objectsInClipboard = null;
  private boolean multipleClipboardSupported = true;
  private boolean addingToSelection = true;
  private int count = 0;
  /**
   * User alert in case of error during operation
   */
  private String errorMessage = null;
  private Exception error = null;

  @Override
  public void add(ClipboardSelection objectToCopy) throws ClipboardException {
    try {
      count += 1;
      if (objectToCopy != null) {
        if (!addingToSelection) {
          unselectAllItems();
        }
        int index = indexOfObjectThatShouldBeSelected(objectToCopy);
        lastObject = objectToCopy;
        if (index >= 0) {
          // We don't add an other copy of this object
          // but we need to merge and to select it
          objectsInClipboard.set(index, lastObject);
          lastObject.setSelected(true);
        } else if (multipleClipboardSupported) {
          objectsInClipboard.add(lastObject);
          lastObject.setSelected(true);
        }
      }

    } catch (Exception e) {
      throw new ClipboardException("Error while adding object into the clipboard", e);
    }
  }

  private int indexOfObjectThatShouldBeSelected(final ClipboardSelection objectToCopy)
      throws UnsupportedFlavorException {
    int index = -1;
    if (objectToCopy.isDataFlavorSupported(IndexFlavor)) {
      final IndexEntry mainIndexEntry = (IndexEntry) objectToCopy.getTransferData(IndexFlavor);
      for (int i = 0; i < objectsInClipboard.size(); i++) {
        final ClipboardSelection clipObject = objectsInClipboard.get(i);
        if (clipObject.isDataFlavorSupported(IndexFlavor)) {
          final IndexEntry indexEntry = (IndexEntry) clipObject.getTransferData(IndexFlavor);
          if (indexEntry.equals(mainIndexEntry)) {
            index = i;
            break;
          }
        }
      }
    }
    return index;
  }

  private void unselectAllItems() {
    // we have to deselect the object still in clipboard
    objectsInClipboard.forEach(c -> c.setSelected(false));
    // now we add...
    addingToSelection = true;
  }

  @Override
  public ClipboardSelection getObject() {
    count += 1;
    return lastObject;
  }

  @Override
  public Collection<ClipboardSelection> getObjects() {
    count += 1;
    return Collections.unmodifiableCollection(objectsInClipboard);
  }

  @Override
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException {
    try {
      count += 1;
      return objectsInClipboard.stream().filter(ClipboardSelection::isSelected).collect(Collectors.toList());
    } catch (Exception e) {
      throw new ClipboardException("Error while getting the selected object from the clipboard", e);
    }
  }

  @Override
  public int size() throws ClipboardException {
    try {
      return objectsInClipboard.size();
    } catch (Exception e) {
      throw new ClipboardException("Error while computing the clipboard size", e);
    }
  }

  @Override
  public ClipboardSelection getObject(int index) throws ClipboardException {
    try {
      return objectsInClipboard.get(index);
    } catch (Exception e) {
      throw new ClipboardException(
          "Error while getting the object at index " + index + " from the clipboard", e);
    }
  }

  @Override
  public void PasteDone() {
    // As soon as one paste operation is done
    // we know that the next copy should not keep the old selection
    addingToSelection = false;
    // Deselect cutted objects still in clipboard
    objectsInClipboard.stream().filter(ClipboardSelection::isCutted).forEach(c -> c.setSelected(false));
  }

  @Override
  public void setSelected(int index, boolean setIt) throws ClipboardException {
    try {
      ClipboardSelection clipObject = objectsInClipboard.get(index);
      if (clipObject != null) {
        clipObject.setSelected(setIt);
      }
    } catch (Exception e) {
      throw new ClipboardException(
          "Error while selecting or deselecting the object at index " + index + " in the clipboard",
          e);
    }
  }

  @Override
  public void removeObject(int index) throws ClipboardException {
    try {
      objectsInClipboard.remove(index);
    } catch (Exception e) {
      throw new ClipboardException(
          "Error while removing the object at index " + index + " from the clipboard", e);

    }
  }

  @Override
  public void clear() {
    objectsInClipboard.clear();
    lastObject = null;
  }

  @Override
  public void setMultiClipboard() throws ClipboardException {
    try {
      multipleClipboardSupported = true;
      if (lastObject != null) {
        objectsInClipboard.clear();
        objectsInClipboard.add(lastObject);
      }
    } catch (Exception e) {
      throw new ClipboardException("Error while enabling the support of multi-clipboards", e);
    }
  }

  /**
   * Switch the clipboard to single mode.
   */
  @Override
  public void setSingleClipboard() {
    multipleClipboardSupported = false;
  }

  @Override
  public int getCount() {
    return count;
  }

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

  @Override
  public void setMessageError(String messageID, Exception e) {
    errorMessage = messageID;
    error = e;
  }

  @PostConstruct
  public void setUp() {
    lastObject = null;
    objectsInClipboard = new ArrayList<>();
  }
}
