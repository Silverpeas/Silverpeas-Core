package com.stratelia.webactiv.util.exception;

/**
 * SilverpeasException est la racine de la hiérarchie d'exception silverpeas.
 * Toutes les classes d'exception spécifiques aux differents modules doivent
 * dériver (directement ou non) de SilverpeasException. La page d'erreur globale
 * à l'application ne saura traiter correctement que les SilverpeasException.
 * Les autres exception (ou error ou runtime) provoqueront l'affichage d'une
 * page d'erreur imprévue. Le message que l'on donne à l'exception est très
 * important, il doit etre affiché à l'utilisateur. C'est pourquoi le label est
 * multilangue. Chaque classe heritant de SilverpeasException doit surdefinir la
 * fonction getModule qui retourne le nom du module (le meme nom que celui
 * defini dans Silvertrace)
 */
abstract public class SilverpeasTrappedException extends SilverpeasException {
  String gobackPage = "";

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message) {
    super(callingClass, errorLevel, message);
  }

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message, String extraParams) {
    super(callingClass, errorLevel, message, extraParams);
  }

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message, Exception nested) {
    super(callingClass, errorLevel, message, nested);
  }

  public SilverpeasTrappedException(String callingClass, int errorLevel,
      String message, String extraParams, Exception nested) {
    super(callingClass, errorLevel, message, extraParams, nested);
  }

  public void setGoBackPage(String gbp) {
    gobackPage = gbp;
  }

  public String getGoBackPage() {
    return gobackPage;
  }
}
