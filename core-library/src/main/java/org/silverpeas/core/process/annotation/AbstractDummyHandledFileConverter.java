package org.silverpeas.core.process.annotation;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public abstract class AbstractDummyHandledFileConverter<E extends SimulationElement<?>>
    implements DummyHandledFileConverter<E> {

  /**
   * Just after Silverpeas server start, this method is called.
   * The content of this method consists to register the class instance into
   * <code>DummyHandledFileConverterRegistration</code>.
   */
  @Override
  public void init() {
    DummyHandledFileConverterRegistration.register(this);
  }

  /**
   * Just before Silverpeas server stop, this method is called.
   * The content of this method consists to unregister the class instance from
   * <code>DummyHandledFileConverterRegistration</code>.
   */
  @Override
  public void release() {
    DummyHandledFileConverterRegistration.unregister(this);
  }
}
