/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * TodoUserException.java
 * 
 * Created on 18/12/2001
 */

package com.stratelia.webactiv.todo.control;

import java.lang.Exception;

/**
 * 
 * @author mmarengo
 * @version 1.0
 */
public class TodoUserException extends Exception {

  /**
   * Constructor declaration
   * 
   * @param message
   * 
   * @see
   */
  public TodoUserException(String message) {
    super(message);
  }

  /**
   * getModule
   */
  public String getModule() {
    return "todo";
  }

}
