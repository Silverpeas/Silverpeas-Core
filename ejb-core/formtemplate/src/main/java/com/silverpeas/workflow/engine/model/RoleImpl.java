package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;

import com.silverpeas.workflow.api.model.AbstractDescriptor;
import com.silverpeas.workflow.api.model.ContextualDesignation;
import com.silverpeas.workflow.api.model.ContextualDesignations;
import com.silverpeas.workflow.api.model.Role;
import com.silverpeas.workflow.engine.AbstractReferrableObject;


/**
 * Class implementing the representation of the &lt;role&gt; element of a Process Model.
**/
public class RoleImpl extends AbstractReferrableObject implements AbstractDescriptor, Role, Serializable 
{
    private String                 name;
    private ContextualDesignations labels;       // collection of labels
    private ContextualDesignations descriptions; // collection of descriptions

    // ~ Instance fields related to AbstractDescriptor ////////////////////////////////////////////////////////

    private AbstractDescriptor     parent;
    private boolean                hasId = false;
    private int                    id;


    /**
     * Constructor
     */
    public RoleImpl() 
    {
        reset();
    }

    /**
     * Constructor
     * @param    name    role nama
     */
    public RoleImpl(String name) 
    {
        this();
        this.name = name;
    }

    /**
     * reset attributes
     */
    private void reset()
    {
        labels        = new SpecificLabelListHelper();
        descriptions  = new SpecificLabelListHelper();
    }

    /**
     * Get the name of the Role
     * @return role's name
     */
    public String getName()
    {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#setName(java.lang.String)
     */
    public void setName(String name)
    {
        this.name = name;
    }

    ////////////////////
    // labels
    ////////////////////

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#getLabels()
     */
    public ContextualDesignations getLabels() 
    {
        return labels;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#getLabel(java.lang.String, java.lang.String)
     */
    public String getLabel(String role, String language)
    {
        return labels.getLabel(role, language);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#addLabel(com.silverpeas.workflow.api.model.ContextualDesignation)
     */
    public void addLabel(ContextualDesignation label)
    {
        labels.addContextualDesignation(label);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#iterateLabel()
     */
    public Iterator iterateLabel()
    {
        return labels.iterateContextualDesignation();
    }

    ////////////////////
    // descriptions
    ////////////////////

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#getDescriptions()
     */
    public ContextualDesignations getDescriptions() 
    {
        return descriptions;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#getDescription(java.lang.String, java.lang.String)
     */
    public String getDescription(String role, String language)
    {
        return descriptions.getLabel(role, language);
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#addDescription(com.silverpeas.workflow.api.model.ContextualDesignation)
     */
    public void addDescription(ContextualDesignation description)
    {
        descriptions.addContextualDesignation(description);
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#iterateDescription()
     */
    public Iterator iterateDescription()
    {
        return descriptions.iterateContextualDesignation();
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.Role#createDesignation()
     */
    public ContextualDesignation createDesignation()
    {
        return labels.createContextualDesignation();
    }

    /**
     * Get the unique key, used by equals method
     * @return    unique key 
     */
    public String getKey()
    {
        return name;
    }

    /************* Implemented methods *****************************************/
    //~ Methods ////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setId(int)
     */
    public void setId(int id) 
    {
        this.id = id;
        hasId = true;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getId()
     */
    public int getId() 
    {
        return id;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#setParent(com.silverpeas.workflow.api.model.AbstractDescriptor)
     */
    public void setParent(AbstractDescriptor parent) 
    {
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#getParent()
     */
    public AbstractDescriptor getParent() 
    {
        return parent;
    }

    /*
     * (non-Javadoc)
     * @see com.silverpeas.workflow.api.model.AbstractDescriptor#hasId()
     */
    public boolean hasId() 
    {
        return hasId;
    }
}