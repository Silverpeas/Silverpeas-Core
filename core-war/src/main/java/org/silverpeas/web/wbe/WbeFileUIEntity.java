package org.silverpeas.web.wbe;

import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.wbe.WbeFile;
import org.silverpeas.core.wbe.WbeUser;
import org.silverpeas.core.web.util.SelectableUIEntity;
import org.silverpeas.core.wbe.WbeHostManager;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WbeFileUIEntity extends SelectableUIEntity<WbeFile> {

  private static final String SEP = " > ";

  private SilverpeasList<WbeUserUIEntity> editors = null;

  private WbeFileUIEntity(final WbeFile data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  @Override
  public String getId() {
    return getData().id();
  }

  public static SilverpeasList<WbeFileUIEntity> convertList(
      final SilverpeasList<WbeFile> values, final Set<String> selectedIds) {
    final Function<WbeFile, WbeFileUIEntity> converter = c -> new WbeFileUIEntity(c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }

  public String getLocation(final String userLanguage) {
    return getData().linkedToResource()
        .flatMap(r -> getOrganizationController().getComponentInstance(r.getComponentInstanceId()))
        .map(i -> getPath(i, userLanguage) + SEP + i.getLabel(userLanguage))
        .orElse(StringUtil.EMPTY);
  }

  public SilverpeasList<WbeUserUIEntity> editors() {
    if (editors == null) {
      final List<WbeUser> list = WbeHostManager.get().getEditorsOfFile(getData());
      editors = WbeUserUIEntity.convertList(SilverpeasList.wrap(list), null);
    }
    return editors;
  }

  private String getPath(SilverpeasComponentInstance instance, final String language) {
    return getOrganizationController().getPathToComponent(instance.getId()).stream()
        .map(s -> s.getName(language))
        .collect(Collectors.joining(SEP));
  }

  private OrganizationController getOrganizationController() {
    return OrganizationController.get();
  }

}
