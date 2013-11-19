package com.silverpeas.thumbnail.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.ActionType;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.apache.commons.io.FileUtils;
import org.silverpeas.process.annotation.AbstractDummyHandledFileConverter;
import org.silverpeas.process.io.file.DummyHandledFile;

import javax.inject.Named;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
@Named
public class ThumbnailDummyHandledFileConverter
    extends AbstractDummyHandledFileConverter<ThumbnailSimulationElement> {

  @Override
  public Class<ThumbnailSimulationElement> getSourceElementType() {
    return ThumbnailSimulationElement.class;
  }

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
