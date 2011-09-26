/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.jndi;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * A JNDI context implementation that uses the memory as a dictionary of objects.
 */
public class SimpleMemoryContext implements Context {
  
  private Map<String, Object> dictionary = new ConcurrentHashMap<String, Object>();

  @Override
  public Object lookup(Name name) throws NamingException {
    return lookup(name.toString());
  }

  @Override
  public Object lookup(String name) throws NamingException {
    return dictionary.get(name);
  }

  @Override
  public void bind(Name name, Object o) throws NamingException {
    bind(name.toString(), o);
  }

  @Override
  public void bind(String name, Object o) throws NamingException {
    if (dictionary.containsKey(name)) {
      throw new NameAlreadyBoundException("Name " + name + " already bound!");
    }
    dictionary.put(name, o);
  }

  @Override
  public void rebind(Name name, Object o) throws NamingException {
    rebind(name.toString(), o);
  }

  @Override
  public void rebind(String name, Object o) throws NamingException {
    dictionary.put(name, o);
  }

  @Override
  public void unbind(Name name) throws NamingException {
    unbind(name.toString());
  }

  @Override
  public void unbind(String name) throws NamingException {
    if (!dictionary.containsKey(name)) {
      throw new NameNotFoundException("No such name " + name + " is bound!");
    }
    dictionary.remove(name);
  }

  @Override
  public void rename(Name oldName, Name newName) throws NamingException {
    rename(oldName.toString(), newName.toString());
  }

  @Override
  public void rename(String oldName, String newName) throws NamingException {
    Object object = lookup(oldName);
    bind(newName, object);
    unbind(oldName);
  }

  @Override
  public NamingEnumeration<NameClassPair> list(Name name) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NamingEnumeration<NameClassPair> list(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NamingEnumeration<Binding> listBindings(Name name) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NamingEnumeration<Binding> listBindings(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void destroySubcontext(Name name) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void destroySubcontext(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Context createSubcontext(Name name) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Context createSubcontext(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object lookupLink(Name name) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object lookupLink(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NameParser getNameParser(Name name) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public NameParser getNameParser(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Name composeName(Name name, Name name1) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String composeName(String string, String string1) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object addToEnvironment(String string, Object o) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object removeFromEnvironment(String string) throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Hashtable<?, ?> getEnvironment() throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void close() throws NamingException {
    dictionary.clear();
  }

  @Override
  public String getNameInNamespace() throws NamingException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
