package org.silverpeas.web.wopi;

import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.SelectableUIEntity;
import org.silverpeas.core.wopi.WopiFile;
import org.silverpeas.core.wopi.WopiFileEditionManager;
import org.silverpeas.core.wopi.WopiUser;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class WopiUserUIEntity extends SelectableUIEntity<WopiUser> {

  private SilverpeasList<WopiFileUIEntity> editedFiles = null;

  private WopiUserUIEntity(final WopiUser data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  @Override
  public String getId() {
    return getData().getId();
  }

  public static SilverpeasList<WopiUserUIEntity> convertList(
      final SilverpeasList<WopiUser> values, final Set<String> selectedIds) {
    final Function<WopiUser, WopiUserUIEntity> converter = c -> new WopiUserUIEntity(c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }

  public SilverpeasList<WopiFileUIEntity> editedFiles() {
    if (editedFiles == null) {
      final List<WopiFile> list = WopiFileEditionManager.get().getEditedFilesBy(getData());
      editedFiles = WopiFileUIEntity.convertList(SilverpeasList.wrap(list), null);
    }
    return editedFiles;
  }
}