package com.stratelia.webactiv.almanach;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;

import java.sql.Connection;

/**
 * A dummy instanciator for tests.
 * @author miguel
 */
public class AlmanachInstanciator implements ComponentsInstanciatorIntf {
  /**
   * Create a new instance of the component for a requested user and space.
   * @param connection - Connection to the database used to save the create information.
   * @param spaceId - Identity of the space where the component will be instancied.
   * @param componentId - Identity of the component to instanciate.
   * @param userId - Identity of the user who want the component
   * @throws InstanciationException
   * @roseuid 3B82286B0236
   */
  @Override
  public void create(final Connection connection, final String spaceId, final String componentId,
      final String userId) throws InstanciationException {

  }

  /**
   * Delete the component instance created for the user on the requested space.
   * @param connection - Connection to the database where the create information will be destroyed.
   * @param spaceId - Identity of the space where the instanced component will be deleted.
   * @param componentId - Identity of the instanced component
   * @param userId - Identity of the user who have instantiate the component.
   * @throws InstanciationException
   * @roseuid 3B8228740117
   */
  @Override
  public void delete(final Connection connection, final String spaceId, final String componentId,
      final String userId) throws InstanciationException {

  }
}
