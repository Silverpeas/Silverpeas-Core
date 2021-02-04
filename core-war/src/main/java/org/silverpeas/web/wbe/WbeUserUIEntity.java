package org.silverpeas.web.wbe;

import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.wbe.WbeUser;
import org.silverpeas.core.web.util.SelectableUIEntity;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeHostManager;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class WbeUserUIEntity extends SelectableUIEntity<WbeUser> {

  private SilverpeasList<WbeFileUIEntity> editedFiles = null;

  private WbeUserUIEntity(final WbeUser data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  @Override
  public String getId() {
    return getData().getId();
  }

  public static SilverpeasList<WbeUserUIEntity> convertList(
      final SilverpeasList<WbeUser> values, final Set<String> selectedIds) {
    final Function<WbeUser, WbeUserUIEntity> converter = c -> new WbeUserUIEntity(c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }

  public SilverpeasList<WbeFileUIEntity> editedFiles() {
    if (editedFiles == null) {
      final List<WbeFile> list = WbeHostManager.get().getEditedFilesBy(getData());
      editedFiles = WbeFileUIEntity.convertList(SilverpeasList.wrap(list), null);
    }
    return editedFiles;
  }
}
