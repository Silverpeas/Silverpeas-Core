package org.silverpeas.core.process.annotation;

import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.process.io.file.DummyHandledFile;
import org.silverpeas.core.ActionType;
import org.silverpeas.core.WAPrimaryKey;

import java.util.List;

/**
 * This interface provides services to convert an element from any type to a dummy handled file.
 * <p/>
 * @author Yohann Chastagnier
 */
public interface DummyHandledFileConverter<S extends SimulationElement<?>> extends Initialization {

  /**
   * Method that contains the treatment of the conversion.
   * @param elements elements to convert
   * @param targetPK the targets
   * @param actionType the action type used for the conversion
   * @return the list of converted elements.
   */
  List<DummyHandledFile> convert(final List<S> elements, final WAPrimaryKey targetPK,
      final ActionType actionType);
}
