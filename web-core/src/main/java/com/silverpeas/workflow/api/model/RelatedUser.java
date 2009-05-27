package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of the &lt;relatedUser&gt; element of a Process Model.
 */
public interface RelatedUser
{
    /**
     * Get the referred participant
     * @return Participant object
     */
    public Participant getParticipant();

    /**
     * Set the referred participant
     * @param Participant object
     */
    public void setParticipant( Participant participant );
    
    /**
     * Get the referred item
     */
    public Item getFolderItem();

    /**
     * Set the referred item
     */
    public void setFolderItem( Item item );      
    
    /**
     * Get the relation between user and participant
     * @return relation, if null get the participant himself instead of searching related user
     */
    public String getRelation();

    /** 
     * set the relation between user and participant 
     */
    public void setRelation( String strRelation );

    /**
     * Get the role to which the related user will be affected
     * @return the role name
     */
    public String getRole();

    /** 
     * New method: Set the role the related user will be affected to
     */
    public void setRole( String strRole );
}