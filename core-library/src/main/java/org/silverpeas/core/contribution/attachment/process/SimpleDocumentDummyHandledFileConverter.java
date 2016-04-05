package org.silverpeas.core.contribution.attachment.process;

import org.silverpeas.core.process.annotation.AbstractDummyHandledFileConverter;
import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.WAPrimaryKey;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.LinkedList;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public class SimpleDocumentDummyHandledFileConverter
    extends AbstractDummyHandledFileConverter<SimpleDocumentSimulationElement> {

  @Override
  public List<DummyHandledFile> convert(final List<SimpleDocumentSimulationElement> elements,
      final WAPrimaryKey targetPK, final ActionType actionType) {

    // Initializing the result
    List<DummyHandledFile> dummyHandledFiles = new LinkedList<DummyHandledFile>();

    // For now, only move and copy actions are handled
    if (actionType.isCreate() || actionType.isUpdate() || actionType.isCopy() ||
        actionType.isMove()) {

      for (SimpleDocumentSimulationElement document : elements) {

        // Adding the dummy representation of the source document in case of update and if the
        // current element is an old one (deletion)
        // Adding the dummy representation of the source document in case of move and if the
        // current element is not an old one(deletion)
        if ((actionType.isUpdate() && document.isOld()) ||
            (actionType.isMove() && !document.isOld())) {
          dummyHandledFiles.add(new SimpleDocumentDummyHandledFile(document.getElement(), true));
        }

        // Adding the dummy representation of the target file, if the current element is not an
        // old one
        if (!document.isOld()) {
          dummyHandledFiles
              .add(new SimpleDocumentDummyHandledFile(document.getElement(), targetPK));
        }
      }
    } else {
      throw new NotImplementedException();
    }

    // Result
    return dummyHandledFiles;
  }
}
