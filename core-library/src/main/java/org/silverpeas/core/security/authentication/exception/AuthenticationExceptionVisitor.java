package org.silverpeas.core.security.authentication.exception;

/**
 * A processor of authentication exceptions. It is a visitor (see pattern visitor) of such exceptions
 * in order to perform the correct treatment corresponding to the type of the authentication exception.
 */
public interface AuthenticationExceptionVisitor {

  void visit(AuthenticationBadCredentialException ex) throws AuthenticationException;
  void visit(AuthenticationHostException ex) throws AuthenticationException;
  void visit(AuthenticationException ex) throws AuthenticationException;
  void visit(AuthenticationPwdNotAvailException ex) throws AuthenticationException;
  void visit(AuthenticationPasswordAboutToExpireException ex) throws AuthenticationException;
  void visit(AuthenticationPwdChangeNotAvailException ex) throws AuthenticationException;
}
