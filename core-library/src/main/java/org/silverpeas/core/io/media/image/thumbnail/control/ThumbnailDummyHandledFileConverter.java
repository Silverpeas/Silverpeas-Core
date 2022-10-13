/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media.image.thumbnail.control;

import org.apache.commons.io.FileUtils;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.process.annotation.AbstractDummyHandledFileConverter;
import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.util.StringUtil;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * User: Yohann Chastagnier Date: 25/10/13
 */
@Service
public class ThumbnailDummyHandledFileConverter
    extends AbstractDummyHandledFileConverter<ThumbnailSimulationElement> {

  @Override
  public List<DummyHandledFile> convert(final List<ThumbnailSimulationElement> elements,
      final ResourceReference target, final ActionType actionType) {

    // Initializing the result
    List<DummyHandledFile> dummyHandledFiles = new LinkedList<>();

    // For now, only move and copy actions are handled
    if (actionType.isCreate() || actionType.isUpdate() || actionType.isCopy() ||
        actionType.isMove()) {

      for (ThumbnailSimulationElement thumbnail : elements) {

        String thumbnailRootPath = ThumbnailController.getImageDirectory(thumbnail.getElement()
            .getReference().getComponentInstanceId());

        for (String thumbnailName : new String[]{thumbnail.getElement().getCropFileName(),
            thumbnail.getElement().getOriginalFileName()}) {

          if (StringUtil.isNotDefined(thumbnailName)) {
            continue;
          }

          File thumbnailFile = FileUtils.getFile(thumbnailRootPath, thumbnailName);

          performTask(target, actionType, dummyHandledFiles, thumbnail, thumbnailFile);
        }
      }
    }

    // Result
    return dummyHandledFiles;
  }

  private void performTask(final ResourceReference target, final ActionType actionType,
      final List<DummyHandledFile> dummyHandledFiles, final ThumbnailSimulationElement thumbnail,
      final File thumbnailFile) {
    if (thumbnailFile.exists() && thumbnailFile.isFile()) {

      // Adding the dummy representation of the source document in case of update and if the
      // current element is an old one (deletion)
      // Adding the dummy representation of the source document in case of move and if the
      // current element is not an old one(deletion)
      if ((actionType.isUpdate() && thumbnail.isOld()) ||
          (actionType.isMove() && !thumbnail.isOld())) {
        dummyHandledFiles.add(
            new ThumbnailDummyHandledFile(thumbnail.getElement(), thumbnailFile, true));
      }

      // Adding the dummy representation of the target file, if the current element is not an
      // old one
      if (!thumbnail.isOld()) {
        dummyHandledFiles.add(
            new ThumbnailDummyHandledFile(thumbnail.getElement(), thumbnailFile, target));
      }
    }
  }
}
