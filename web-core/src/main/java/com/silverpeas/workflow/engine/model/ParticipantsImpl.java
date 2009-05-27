package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Participant;
import com.silverpeas.workflow.api.model.Participants;

/**
 * Class implementing the representation of the &lt;participants&gt; element of a Process Model.
**/
public class ParticipantsImpl implements Serializable, Participants 
{
    private List participantList;

	/**
	 * Constructor
	 */
    public ParticipantsImpl() 
	{
        participantList = new ArrayList();
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participants#addParticipant(com.silverpeas.workflow.api.model.Participant)
     */
    public void addParticipant(Participant participant) 
    {
        participantList.add(participant);
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participants#createParticipant()
     */
    public Participant createParticipant() 
    {
        return new ParticipantImpl();
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participants#getParticipants()
     */
    public Participant[] getParticipants() 
    {
        if (participantList == null)
            return null;

        return (Participant[]) participantList.toArray(new ParticipantImpl[0]);
    }

    /**
     * Get the participant with given name
     * @param    name    participant name
     * @return wanted participant
     */
    public Participant getParticipant(String name)
    {
        if (participantList == null)
            return null;
        
        Participant participant = null;
        for (int r=0; r<participantList.size(); r++)
        {
            participant = (Participant) participantList.get(r);
            if (participant.getName().equals(name))
                return participant;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participants#iterateParticipant()
     */
    public Iterator iterateParticipant() 
    {
        return participantList.iterator();
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participants#removeParticipant(java.lang.String)
     */
    public void removeParticipant(String strParticipantName) throws WorkflowException 
    {
        Participant participant = createParticipant();
        
        participant.setName(strParticipantName);
        
        if ( participantList == null )
            return;
        
        if ( !participantList.remove( participant) )
            throw new WorkflowException("ParticipantsImpl.removeParticipant()", //$NON-NLS-1$
                                        "workflowEngine.EX_PARTICIPANT_NOT_FOUND",               // $NON-NLS-1$
                                        strParticipantName == null
                                            ? "<null>"  //$NON-NLS-1$
                                            : strParticipantName );
    }
}