package org.silverpeas.web.variables;

import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.web.util.SelectableUIEntity;

import java.util.Set;
import java.util.function.Function;

public class VariableUIEntity extends SelectableUIEntity<Variable> {

  VariableUIEntity(final Variable data, final Set<String> selectedIds) {
    super(data, selectedIds);
  }

  @Override
  public String getId() {
    return String.valueOf(getData().getId());
  }

  public static SilverpeasList<VariableUIEntity> convertList(
      final SilverpeasList<Variable> values, final Set<String> selectedIds) {
    final Function<Variable, VariableUIEntity> converter =
        c -> new VariableUIEntity(c, selectedIds);
    return values.stream().map(converter).collect(SilverpeasList.collector(values));
  }
}