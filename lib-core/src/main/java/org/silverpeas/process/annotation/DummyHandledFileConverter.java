package org.silverpeas.process.annotation;

import org.silverpeas.util.ActionType;
import org.silverpeas.util.WAPrimaryKey;
import org.silverpeas.process.io.file.DummyHandledFile;

import java.util.List;

/**
 * This interface provides services to convert an element from any type to a dummy handled file.
 * <p/>
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public interface DummyHandledFileConverter<S extends SimulationElement<?>> {

  /**
   * This method have to be annoted by @PostConstruct.
   * Just after Silverpeas server start, this method is called.
   * The content of this method consists to register the class instance into
   * <code>DummyHandledFileConverterRegistration</code>.
   */
  void register();

  /**
   * This method have to be annoted by @PreDestroy.
   * Just before Silverpeas server stop, this method is called.
   * The content of this method consists to unregister the class instance from
   * <code>DummyHandledFileConverterRegistration</code>.
   */
  void unregister();

  /**
   * Gets the class of the element that has to be converted to a dummy handled file.
   * @return
   */
  Class<S> getSourceElementType();

  /**
   * Method that contains the treatment of the conversion.
   *
   *
   * @param elements elements to convert
   * @param targetPK the targets
   * @param actionType the action type used for the conversion
   * @return
   */
  List<DummyHandledFile> convert(final List<S> elements, final WAPrimaryKey targetPK,
      final ActionType actionType);
}
