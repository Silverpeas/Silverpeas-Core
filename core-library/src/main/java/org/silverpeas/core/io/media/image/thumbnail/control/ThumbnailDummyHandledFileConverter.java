package org.silverpeas.core.io.media.image.thumbnail.control;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.process.annotation.AbstractDummyHandledFileConverter;
import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.WAPrimaryKey;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public class ThumbnailDummyHandledFileConverter
    extends AbstractDummyHandledFileConverter<ThumbnailSimulationElement> {

  @Override
  public List<DummyHandledFile> convert(final List<ThumbnailSimulationElement> elements,
      final WAPrimaryKey targetPK, final ActionType actionType) {

    // Initializing the result
    List<DummyHandledFile> dummyHandledFiles = new LinkedList<DummyHandledFile>();

    // For now, only move and copy actions are handled
    if (actionType.isCreate() || actionType.isUpdate() || actionType.isCopy() ||
        actionType.isMove()) {

      for (ThumbnailSimulationElement thumbnail : elements) {

        String thumbnailRootPath =
            ThumbnailController.getImageDirectory(thumbnail.getElement().getInstanceId());

        for (String thumbnailName : new String[]{thumbnail.getElement().getCropFileName(),
            thumbnail.getElement().getOriginalFileName()}) {

          if (StringUtil.isNotDefined(thumbnailName)) {
            continue;
          }

          File thumbnailFile = FileUtils.getFile(thumbnailRootPath, thumbnailName);

          if (thumbnailFile.exists() && thumbnailFile.isFile()) {

            // Adding the dummy representation of the source document in case of update and if the
            // current element is an old one (deletion)
            // Adding the dummy representation of the source document in case of move and if the
            // current element is not an old one(deletion)
            if ((actionType.isUpdate() && thumbnail.isOld()) ||
                (actionType.isMove() && !thumbnail.isOld())) {
              dummyHandledFiles
                  .add(new ThumbnailDummyHandledFile(thumbnail.getElement(), thumbnailFile, true));
            }

            // Adding the dummy representation of the target file, if the current element is not an
            // old one
            if (!thumbnail.isOld()) {
              dummyHandledFiles.add(
                  new ThumbnailDummyHandledFile(thumbnail.getElement(), thumbnailFile, targetPK));
            }
          }
        }
      }
    }

    // Result
    return dummyHandledFiles;
  }
}
