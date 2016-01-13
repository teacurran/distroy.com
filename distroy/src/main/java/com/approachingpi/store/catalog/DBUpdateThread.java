
package com.approachingpi.store.catalog;
import com.approachingpi.store.catalog.Royalties.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import com.approachingpi.servlet.PiServlet;



public class DBUpdateThread extends Thread {

    boolean m_exit = false;
    private static DBUpdateThread m_instance = null;

   // Constructor is private, cannot initialize
   private DBUpdateThread()
   {

   }

    public void run()
   {
       // Thread runs forever unless forceExit is called
       while (!m_exit)
       {
           Connection con = PiServlet.openConnection("java:/datasources/distroy-ds");

           try {
                PreparedStatement ps = con.prepareStatement("begin " +
                "drop table tbOrderSummary " +
                "end " +
                "begin " +
                "select D.inId, D.vcOrderId, sum(D.moPriceTotal) 'moPriceTotalSum', O.inStatus, S.vcAbbrev, S.btWholesale, A.inId 'inArtistId', D.inQty, D.vcItemDesc, O.dtShipComplete " +
//                        "select D.inId, D.vcOrderId, D.moPriceTotal, sum(D.moPriceTotal) 'moPriceTotalSum', O.inStatus, S.vcAbbrev, S.btWholesale, A.inId 'inArtistId', D.inQty, D.vcItemDesc, O.dtShipComplete " +
                "into tbOrderSummary " +
                "FROM tbOrderDetail D, tbOrder O, tbStore S, tbProductVariation V, tbArtist A, tbLinkProductArtist LNK " +
                "WHERE D.vcOrderId = O.vcId  " +
                "AND S.inId = O.inStoreId " +
                "AND LNK.inArtistId = A.inId " +
                "AND D.inProductVariationId = V.inId " +
                "AND V.inProductId = LNK.inProductId " +
                "GROUP BY D.inId, D.vcOrderId, D.moPriceTotal, O.inStatus, S.vcAbbrev, S.btWholesale, A.inId, D.inQty, D.vcItemDesc, O.dtShipComplete " +
                "end"
                );
        ps.execute();
                        try {
                        con.close();
                        } catch (Exception e) { e.printStackTrace(); }
           }
           catch (SQLException sqlex){}

           try{
//sleep for 24 hours (in milliseconds)
           Thread.sleep(86400000) ;
//sleep for 3 minutes (in milliseconds)
//           Thread.sleep(180000) ;
           } catch (InterruptedException ie){};
       }
   }

   public synchronized void forceExit()
   {
       m_exit = true;
   }

   public static synchronized DBUpdateThread getInstance()
   {
       if (m_instance == null)
       {
           m_instance = new DBUpdateThread();
       }
       return m_instance;
   }

}
