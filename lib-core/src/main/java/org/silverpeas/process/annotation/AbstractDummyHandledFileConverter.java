package org.silverpeas.process.annotation;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * User: Yohann Chastagnier
 * Date: 25/10/13
 */
public abstract class AbstractDummyHandledFileConverter<E extends SimulationElement<?>>
    implements DummyHandledFileConverter<E> {

  /*
   * (non-Javadoc)
   * {@see org.silverpeas.process.annotation.DummyHandledFileConverter#register()}
   */
  @Override
  @PostConstruct
  public void register() {
    DummyHandledFileConverterRegistration.register(this);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.process.annotation.DummyHandledFileConverter#unregister()
   */
  @Override
  @PreDestroy
  public void unregister() {
    DummyHandledFileConverterRegistration.unregister(this);
  }
}
