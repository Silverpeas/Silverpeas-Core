package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;participants&gt; element of
 * a Process Model.
 */
public interface Participants {

  /**
   * Iterate through the Participant objects
   * 
   * @return an iterator
   */
  public Iterator iterateParticipant();

  /**
   * Create an Participant
   * 
   * @return an object implementing Participant
   */
  public Participant createParticipant();

  /**
   * Add an participant to the collection
   * 
   * @param participant
   *          to be added
   */
  public void addParticipant(Participant participant);

  /**
   * Returns all the Participant elements as an array
   */
  public Participant[] getParticipants();

  /**
   * Get the participant definition with given name
   * 
   * @param name
   *          participant name
   * @return wanted participant definition
   */
  public Participant getParticipant(String name);

  /**
   * Remove an participant from the collection
   * 
   * @param strParticipantName
   *          the name of the participant to be removed.
   * @throws WorkflowException
   *           when the participant could not be deleted.
   */
  public void removeParticipant(String strParticipantName)
      throws WorkflowException;
}
