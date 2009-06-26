/** 
 *
 * @author  akhadrou
 * @version 
 */
package com.stratelia.webactiv.util;

/**
 * This interface stores the name of all the database tables. 
 * The String constants in this class should be used by other
 * classes instead of hardcoding the name of a database table
 * into the source code.
 */
public interface DatabaseNames {
  
    public static final String THEME_TABLE = "topics";
    public static final String THEME_TABLE_FIELD_LIST= "id, name, description, owner, keywords, path, fatherid";
    public static final String PUBLICATION_TABLE = "publication";

    public static final String JOB_TABLE = "job";
    public static final int JOB_SIZE = 12;
    
    public static final String JOB_A1 = "IDJOB";
    public static final String JOB_A2 = "IDPOST";
    public static final String JOB_A3 = "IDRESTAU";
    public static final String JOB_A4 = "TITLE";
    public static final String JOB_A5 = "PROFILE_JOB";
    public static final String JOB_A6 = "CONTACT";
    public static final String JOB_A7 = "BEGIN_DATE";
    public static final String JOB_A8 = "END_DATE";
    public static final String JOB_A9 = "TOTALLY_ASSIGNED_STATE";
    public static final String JOB_A10 = "REMARKS";
    public static final String JOB_A11 = "STATE";
    public static final String JOB_A12 = "IDSECTOR";
    
    public static final String RESTAURANT_TABLE = "restaurant";
    public static final int RESTAURANT_SIZE = 6;
    
    public static final String RESTAURANT_A1 = "IDRESTAU";
    public static final String RESTAURANT_A2 = "NAME";
    public static final String RESTAURANT_A3 = "CODE";
    public static final String RESTAURANT_A4 = "ADDRESS";
    public static final String RESTAURANT_A5 = "IDMANAGER";
    public static final String RESTAURANT_A6 = "IDSECTOR";  
    
    public static final String SECTOR_TABLE = "sector";
    public static final int SECTOR_SIZE = 3;
    
    public static final String SECTOR_A1 = "IDSECTOR";
    public static final String SECTOR_A2 = "NAME";
    public static final String SECTOR_A3 = "IDMANAGER";
    
    public static final String POST_TABLE = "post";
    public static final int POST_SIZE = 3;
    
    public static final String POST_A1 = "IDPOST";
    public static final String POST_A2 = "NAME";
    public static final String POST_A3 = "ABBREV";
    
    public static final String LRPRIMSTAFF_TABLE = "lrprimStaff";
    public static final int LRPRIMSTAFF_SIZE = 23;
    
    public static final String LRPRIMSTAFF_A1 = "IDLRPRIM";
    public static final String LRPRIMSTAFF_A2 = "LAST_NAME";
    public static final String LRPRIMSTAFF_A3 = "FIRST_NAME";
    public static final String LRPRIMSTAFF_A4 = "SEX";
    public static final String LRPRIMSTAFF_A5 = "MARITAL_STATUS";
    public static final String LRPRIMSTAFF_A6 = "ADDRESS";
    public static final String LRPRIMSTAFF_A7 = "TELEPHONE_NUMBER";
    public static final String LRPRIMSTAFF_A8 = "DATE_OF_BIRTH";
    public static final String LRPRIMSTAFF_A9 = "IDPOST";
    public static final String LRPRIMSTAFF_A10 = "PROFESSIONAL_EXPERIENCE";
    public static final String LRPRIMSTAFF_A11 = "PROFESSIONAL_ABILITY";
    public static final String LRPRIMSTAFF_A12 = "REFERENCE";
    public static final String LRPRIMSTAFF_A13 = "VOCATIONAL_TRAINING";
    public static final String LRPRIMSTAFF_A14 = "NB_THEORITICAL_HOUR";
    public static final String LRPRIMSTAFF_A15 = "WAGE_MODIFICATION";
    public static final String LRPRIMSTAFF_A16 = "INTERNAL_BONUS";
    public static final String LRPRIMSTAFF_A17 = "HAVE_CHILDREN";
    public static final String LRPRIMSTAFF_A18 = "HAVE_CAR";
    public static final String LRPRIMSTAFF_A19 = "REMARKS";
    public static final String LRPRIMSTAFF_A20 = "STATE";
    public static final String LRPRIMSTAFF_A21 = "TYPE";
    public static final String LRPRIMSTAFF_A22 = "CONTRACT";
    public static final String LRPRIMSTAFF_A23 = "END_DATE_CDD";

    public static final String AVAILABILITYLRP_TABLE = "availabilityLrp";
    public static final int AVAILABILITYLRP_SIZE = 7;
    
    public static final String AVAILABILITYLRP_A1 = "IDLRPAVAILABILITY";
    public static final String AVAILABILITYLRP_A2 = "IDLRP";
    public static final String AVAILABILITYLRP_A3 = "IDRESTAU";
    public static final String AVAILABILITYLRP_A4 = "BEGIN_DATE";    	
    public static final String AVAILABILITYLRP_A5 = "END_DATE";
    public static final String AVAILABILITYLRP_A6 = "REMARKS";
    public static final String AVAILABILITYLRP_A7 = "STATE";
    
    public static final String ASSIGNMENTLRPRIM_TABLE = "assignmentLrprim";
    public static final int ASSIGNMENTLRPRIM_SIZE = 10;
    
    public static final String ASSIGNMENTLRPRIM_A1 = "IDLRPRIMASSIGNMENT";
    public static final String ASSIGNMENTLRPRIM_A2 = "IDJOB";
    public static final String ASSIGNMENTLRPRIM_A3 = "IDLRPRIM";
    public static final String ASSIGNMENTLRPRIM_A4 = "BEGIN_DATE";
    public static final String ASSIGNMENTLRPRIM_A5 = "END_DATE";
    public static final String ASSIGNMENTLRPRIM_A6 = "TARIFF";
    public static final String ASSIGNMENTLRPRIM_A7 = "TARIFE";
    public static final String ASSIGNMENTLRPRIM_A8 = "REMARKS";
    public static final String ASSIGNMENTLRPRIM_A9 = "BEGIN_MONTH";
    public static final String ASSIGNMENTLRPRIM_A10 = "BEGIN_YEAR";
    
    public static final String BONUS_TABLE = "bonus";
    public static final int BONUS_SIZE = 7;
    
    public static final String BONUS_A1 = "IDBONUS";
    public static final String BONUS_A2 = "IDLRPRIMASSIGNMENT";
    public static final String BONUS_A3 = "AMOUNTF";
    public static final String BONUS_A4 = "AMOUNTE";
    public static final String BONUS_A5 = "DATE";
    public static final String BONUS_A6 = "MONTH";
    public static final String BONUS_A7 = "YEAR";
    
    public static final String ASSIGNMENTLRP_TABLE = "assignmentLrp";
    public static final int ASSIGNMENTLRP_SIZE = 9;
    
    public static final String ASSIGNMENTLRP_A1 = "IDLRPASSIGNMENT";
    public static final String ASSIGNMENTLRP_A2 = "IDJOB";
    public static final String ASSIGNMENTLRP_A3 = "IDLRP";
    public static final String ASSIGNMENTLRP_A4 = "BEGIN_DATE";
    public static final String ASSIGNMENTLRP_A5 = "END_DATE";
    public static final String ASSIGNMENTLRP_A6 = "NB_HOUR";
    public static final String ASSIGNMENTLRP_A7 = "REMARKS";
    public static final String ASSIGNMENTLRP_A8 = "BEGIN_MONTH";
    public static final String ASSIGNMENTLRP_A9 = "BEGIN_YEAR";
    
    public static final String ASSIGNMENTINTERIM_TABLE = "assignmentInterim";
    public static final int ASSIGNMENTINTERIM_SIZE = 10;
    
    public static final String ASSIGNMENTINTERIM_A1 = "IDINTERIMASSIGNMENT";
    public static final String ASSIGNMENTINTERIM_A2 = "IDJOB";
    public static final String ASSIGNMENTINTERIM_A3 = "NUM_REF";
    public static final String ASSIGNMENTINTERIM_A4 = "INTERIM_AGENCY";
    public static final String ASSIGNMENTINTERIM_A5 = "BEGIN_DATE";
    public static final String ASSIGNMENTINTERIM_A6 = "END_DATE";
    public static final String ASSIGNMENTINTERIM_A7 = "NB_HOUR";
    public static final String ASSIGNMENTINTERIM_A8 = "REMARKS";
    public static final String ASSIGNMENTINTERIM_A9 = "BEGIN_MONTH";
    public static final String ASSIGNMENTINTERIM_A10 = "BEGIN_YEAR";
    
    public static final String UNAVAILABILITY_TABLE = "unavailability";
    public static final int UNAVAILABILITY_SIZE = 7;
    
    public static final String UNAVAILABILITY_A1 = "IDUNAVAIL";
    public static final String UNAVAILABILITY_A2 = "IDREASON";
    public static final String UNAVAILABILITY_A3 = "IDLRPRIM";
    public static final String UNAVAILABILITY_A4 = "BEGIN_DATE";
    public static final String UNAVAILABILITY_A5 = "END_DATE";
    public static final String UNAVAILABILITY_A6 = "DURATION";
    public static final String UNAVAILABILITY_A7 = "REMARKS";
    
    public static final String REASON_TABLE = "reason";
    public static final int REASON_SIZE = 2;
    
    public static final String REASON_A1 = "IDREASON";
    public static final String REASON_A2 = "NAME";
    
    public static final String POSTLRPRIM_TABLE = "postLrprim";
    public static final int POSTLRPRIM_SIZE = 2;
    
    public static final String POSTLRPRIM_A1 = "IDLRPRIM";
    public static final String POSTLRPRIM_A2 = "IDPOST";
    
    public static final String TRAINING_TABLE = "training";
    public static final int TRAINING_SIZE = 3;
    
    public static final String TRAINING_A1 = "IDLRPRIM";
    public static final String TRAINING_A2 = "NAME";
    public static final String TRAINING_A3 = "DONE_STATE";
    
    public static final String EVALUATION_TABLE = "evaluation";
    public static final int EVALUATION_SIZE = 15;
    
    public static final String EVALUATION_A1 = "IDEVALUATION";
    public static final String EVALUATION_A2 = "IDJOB";
    public static final String EVALUATION_A3 = "IDLRPRIM";
    public static final String EVALUATION_A4 = "DATE";
    public static final String EVALUATION_A5 = "PUNCTUALITY";
    public static final String EVALUATION_A6 = "AVAILABILITY";
    public static final String EVALUATION_A7 = "ADAPTATION";
    public static final String EVALUATION_A8 = "CONTACT";
    public static final String EVALUATION_A9 = "ATTITUDE";
    public static final String EVALUATION_A10 = "HYGIENE";
    public static final String EVALUATION_A11 = "QUALITY";
    public static final String EVALUATION_A12 = "VERSATILITY";
    public static final String EVALUATION_A13 = "MOTIVATION";
    public static final String EVALUATION_A14 = "OBSERVATION";
    public static final String EVALUATION_A15 = "REMARKS";
    
    public static final String SPECIALPERIOD_TABLE = "specialPeriod";
    public static final int SPECIALPERIOD_SIZE = 3;
    
    public static final String SPECIALPERIOD_A1 = "IDPERIOD";
    public static final String SPECIALPERIOD_A2 = "BEGIN_DATE";
    public static final String SPECIALPERIOD_A3 = "END_DATE";
    
    public static final String HOUR_TABLE = "hour";
    public static final int HOUR_SIZE = 5;
    
    public static final String HOUR_A1 = "DATE";
    public static final String HOUR_A2 = "MONTH";
    public static final String HOUR_A3 = "YEAR";
    public static final String HOUR_A4 = "NBHOUR";
    public static final String HOUR_A5 = "IDLRPRIMASSIGNMENT";
    
    public static final String INTERIMAGENCY_TABLE = "interimAgency";
    public static final int INTERIMAGENCY_SIZE = 2;
    
    public static final String INTERIMAGENCY_A1 = "IDAGENCY";
    public static final String INTERIMAGENCY_A2 = "NAME";
    
    public static final String STATEMENT_TABLE = "statement";
    public static final int STATEMENT_SIZE = 3;
    
    public static final String STATEMENT_A1 = "IDRESTAU";
    public static final String STATEMENT_A2 = "BEGIN_DATE";    
    public static final String STATEMENT_A3 = "END_DATE";    
    
    public static final String FORUM_TABLE = "Forum";
    public static final String MESSAGE_TABLE = "Message";
    public static final String RIGHTS_TABLE = "Rights";
    public static final String SUBSCRIPT_TABLE = "Subscription";

    public static final String FORMULAIRE_TABLE = "Formulaire";
    public static final String FORMULAIRESFOLDER_TABLE = "Folder";
    public static final String FORMULAIRESRIGHTS_TABLE = "Rights";
    public static final String FORMULAIRESDISPLAY_TABLE = "Display";
                
    public static final String SITE_TABLE = "site";
    public static final String ICONS_TABLE = "icons";
    public static final String SITEICONS_TABLE = "siteIcons";  
    
    public static final String OBJECTS_TABLE = "SILVERPEASformDesigner0objects";  
    public static final String FORMDESIGN_TABLE = "SILVERPEASformDesigner0formDesign";
    
    public static final String MYFORMS_TABLE = "myForms";
    public static final String DROITS_TABLE = "droits";    
}