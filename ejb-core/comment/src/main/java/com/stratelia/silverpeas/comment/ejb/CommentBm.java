package com.stratelia.silverpeas.comment.ejb;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.comment.model.*;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Vector;
import com.stratelia.webactiv.util.WAPrimaryKey;

public interface CommentBm extends javax.ejb.EJBObject {

    public CommentPK createComment( Comment cmt ) throws RemoteException;

    public void deleteComment( CommentPK pk ) throws RemoteException;
       
	public void updateComment( Comment cmt ) throws RemoteException;

    public Comment getComment( CommentPK pk ) throws RemoteException;

    public int getCommentsCount( WAPrimaryKey foreign_pk ) throws RemoteException;
    
    public Vector getAllComments( WAPrimaryKey foreign_pk ) throws RemoteException;

    public Vector getAllCommentsWithUserName( WAPrimaryKey foreign_pk ) throws RemoteException;
    
    public Collection getMostCommentedAllPublications() throws RemoteException;
    
    public Collection getMostCommented(Collection pks, int notationsCount) throws RemoteException;
    
    public void deleteAllComments(ForeignPK foreign_pk) throws RemoteException;
    
    public void moveComments(ForeignPK fromPK, ForeignPK toPK) throws RemoteException;

}