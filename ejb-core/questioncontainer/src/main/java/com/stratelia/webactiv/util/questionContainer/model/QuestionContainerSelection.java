package com.stratelia.webactiv.util.questionContainer.model;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.Serializable;
import java.util.Date;

import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.clipboard.SKDException;
import com.silverpeas.util.clipboard.SilverpeasKeyData;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntry;



public class QuestionContainerSelection extends ClipboardSelection implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  static public DataFlavor QuestionContainerDetailFlavor;
  
  static {
    try {
      QuestionContainerDetailFlavor = new DataFlavor (Class.forName ("com.stratelia.webactiv.util.questionContainer.model.QuestionContainerDetail"), "QuestionContainer");
    } catch (ClassNotFoundException e) {
      e.printStackTrace ();
    }
  }

  private QuestionContainerDetail m_questionContainer;

  /**--------------------------------------------------------------------------------------------------------
   * Constructor
   *
   */
  public QuestionContainerSelection (QuestionContainerDetail questionContainer) {
    super ();
    m_questionContainer = questionContainer;
    super.addFlavor (QuestionContainerDetailFlavor);
  }

    /**--------------------------------------------------------------------------------------------------------
   *
   */
  public synchronized Object getTransferData (DataFlavor parFlavor) throws UnsupportedFlavorException {
    Object transferedData;

    try {
       transferedData = super.getTransferData (parFlavor);
    } catch (UnsupportedFlavorException e) {
       if (parFlavor.equals (QuestionContainerDetailFlavor))
         transferedData = m_questionContainer;
       else
         throw e;
    }
    return transferedData;
  }

  /**--------------------------------------------------------------------------------------------------------
   *
   */
  public IndexEntry getIndexEntry () {
    IndexEntry indexEntry;
    QuestionContainerPK questionContainerPK = m_questionContainer.getHeader().getPK();
   indexEntry = new IndexEntry(questionContainerPK.getComponentName(), "QuestionContainer", questionContainerPK.getId());
     indexEntry.setTitle (m_questionContainer.getHeader().getName());
   return indexEntry;
  }

  /**--------------------------------------------------------------------------------------------------------
   * Tranformation obligatoire en SilverpeasKeyData
   */
  public SilverpeasKeyData getKeyData () {
    SilverpeasKeyData keyData = new SilverpeasKeyData ();

    keyData.setTitle ( m_questionContainer.getHeader().getName() );
    keyData.setAuthor ( m_questionContainer.getHeader().getCreatorId() );
    keyData.setCreationDate ( new Date(m_questionContainer.getHeader().getCreationDate()) );
    keyData.setDesc ( m_questionContainer.getHeader().getDescription() );
    try {
      keyData.setProperty ( "BEGINDATE" , m_questionContainer.getHeader().getBeginDate().toString () );
      keyData.setProperty ( "ENDDATE" , m_questionContainer.getHeader().getEndDate().toString () );
    } catch (SKDException e) {
            SilverTrace.error("questionContainer", "QuestionContainerSelection.getKeyData" , "questionContainer.ERROR_KEY_DATA",  e);
    }
    return keyData;
  }

}
