package com.silverpeas.myLinks.ejb;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;
import org.mockito.Mockito;

import com.silverpeas.myLinks.model.LinkDetail;

public class MyLinksBmEJBTest {
  
  @Test
  public void testSetLinksOrderIfNeededNoPosition() {
    Collection<LinkDetail> links = new ArrayList<LinkDetail>();
    LinkDetail e = new LinkDetail();
    e.setLinkId(0);
    links.add(e);
    e = new LinkDetail();
    e.setLinkId(1);
    links.add(e);
    e = new LinkDetail();
    e.setLinkId(2);
    links.add(e);

    MyLinksBmEJB ejb = new MyLinksBmEJB();
    ejb = Mockito.spy(ejb);
    Mockito.doNothing().when(ejb).setLinksOrder(links);
    
    ejb.setLinksOrderIfNeeded(links);
    
    Mockito.verify(ejb, Mockito.times(1)).setLinksOrder(Mockito.anyCollection());
  }
  
  @Test
  public void testSetLinksOrderIfNeeded() {
    Collection<LinkDetail> links = new ArrayList<LinkDetail>();
    LinkDetail e = new LinkDetail();
    e.setLinkId(0);
    e.setPosition(0);
    e.setHasPosition(true);
    links.add(e);
    e = new LinkDetail();
    e.setLinkId(1);
    e.setPosition(1);
    e.setHasPosition(true);
    links.add(e);
    e = new LinkDetail();
    e.setLinkId(2);
    e.setPosition(2);
    e.setHasPosition(true);
    links.add(e);

    MyLinksBmEJB ejb = new MyLinksBmEJB();
    ejb = Mockito.spy(ejb);
    Mockito.doNothing().when(ejb).setLinksOrder(links);
    
    ejb.setLinksOrderIfNeeded(links);
    
    Mockito.verify(ejb, Mockito.times(0)).setLinksOrder(Mockito.anyCollection());
  }
  
  @Test
  public void testSetLinksOrderIfNeededOneMissing() {
    Collection<LinkDetail> links = new ArrayList<LinkDetail>();
    LinkDetail e = new LinkDetail();
    e.setLinkId(0);
    e.setPosition(0);
    e.setHasPosition(true);
    links.add(e);
    e = new LinkDetail();
    e.setLinkId(2);
    e.setPosition(2);
    e.setHasPosition(true);
    links.add(e);

    MyLinksBmEJB ejb = new MyLinksBmEJB();
    ejb = Mockito.spy(ejb);
    Mockito.doNothing().when(ejb).setLinksOrder(links);
    
    ejb.setLinksOrderIfNeeded(links);
    
    Mockito.verify(ejb, Mockito.times(1)).setLinksOrder(Mockito.anyCollection());
  }
}
