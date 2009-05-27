package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;

import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Participant;
import com.silverpeas.workflow.engine.AbstractReferrableObject;


/**
 * Class implementing the representation of the &lt;participant&gt; element of a Process Model.
**/
public class ParticipantImpl extends AbstractReferrableObject implements Participant, Serializable 
{
    private    String                 name;
    private    String                 resolvedState;
    private    ContextualDesignations labels;
    private    ContextualDesignations descriptions;

    /**
     * Constructor
     */
    public ParticipantImpl() 
    {
        reset();
    }

    /**
     * Constructor
     * @param    name    participant name
     */
    public ParticipantImpl(String name) 
    {
        this();
        this.name = name;
    }

    /**
     * reset attributes
     */
    private void reset()
    {
        labels       = new SpecificLabelListHelper();
        descriptions = new SpecificLabelListHelper();
    }

    /**
     * Get description in specific language for the given role
     * @param        lang        description's language
     * @param        role        role for which the description is
     * @return        wanted description as a String object. 
     *                If description is not found, search description with given role and default language,
     *                if not found again, return the default description in given language,
     *                if not found again, return the default description in default language,
     *                if not found again, return empty string.
     */
    public String getDescription(String role, String language)
    {
        return descriptions.getLabel(role, language);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#getDescriptions()
     */
    public ContextualDesignations getDescriptions() 
    {
        return descriptions;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#addDescription(com.silverpeas.workflow.api.model.ContextualDesignation)
     */
    public void addDescription(ContextualDesignation description)
    {
        descriptions.addContextualDesignation(description);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#iterateDescription()
     */
    public Iterator iterateDescription()
    {
        return descriptions.iterateContextualDesignation();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#createDesignation()
     */
    public ContextualDesignation createDesignation()
    {
        return labels.createContextualDesignation();
    }

    /**
     * Get label in specific language for the given role
     * @param        lang        label's language
     * @param        role        role for which the label is
     * @return        wanted label as a String object. 
     *                If label is not found, search label with given role and default language,
     *                if not found again, return the default label in given language,
     *                if not found again, return the default label in default language,
     *                if not found again, return empty string.
     */
    public String getLabel(String role, String language)
    {
        return labels.getLabel(role, language);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#getLabels()
     */
    public ContextualDesignations getLabels() 
    {
        return labels;
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#addLabel(com.silverpeas.workflow.api.model.ContextualDesignation)
     */
    public void addLabel(ContextualDesignation label)
    {
        labels.addContextualDesignation(label);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#iterateLabel()
     */
    public Iterator iterateLabel()
    {
        return labels.iterateContextualDesignation();
    }

    /**
     * Get the name of this participant
     * @return participant's name
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Get the state that defined participant has resolved
     * @return state that defined participant has resolved
     */
    public String getResolvedState()
    {
        return this.resolvedState;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Participant#setResolvedState(java.lang.String)
     */
    public void setResolvedState(String resolvedState)
    {
        this.resolvedState = resolvedState;
    }

    /**
     * Get the unique key, used by equals method
     * @return    unique key 
     */
    public String getKey()
    {
        return (this.name);
    }
}