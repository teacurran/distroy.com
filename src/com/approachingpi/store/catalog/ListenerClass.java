/*
 * ListenerClass.java
 *
 */

package com.approachingpi.store.catalog;

import javax.servlet.ServletContextEvent;
import javax.servlet.*;
import com.approachingpi.store.catalog.DBUpdateThread;

public class ListenerClass implements ServletContextListener{
    
    public ListenerClass() {
    }
    
      public void contextInitialized(ServletContextEvent event)
   {
       DBUpdateThread.getInstance().start();
   }

   public void contextDestroyed(ServletContextEvent event)
   {
   try
   {
       DBUpdateThread.getInstance().forceExit();
       DBUpdateThread.getInstance().join();
   }
   catch (InterruptedException ie){}
   }
}
