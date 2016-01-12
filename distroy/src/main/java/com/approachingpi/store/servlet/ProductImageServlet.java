/**
 * User: Terrence Curran
 * Date: Feb 5, 2005
 * Time: 2:56:23 PM
 */

package com.approachingpi.store.servlet;

import com.approachingpi.servlet.PiServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductImageServlet extends PiServlet {
    private Log log = LogFactory.getLog(ProductImageServlet.class);

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        String pathInfo = (req.getPathInfo()==null) ? "" : req.getPathInfo();
        //System.out.println("Brand:" + pathInfo);
        String[] pathSplit = pathInfo.split("/");

        String imageName = "";
        try {
            imageName = pathSplit[1] + "/" + pathSplit[2] + ".jpg";
        } catch (Exception e) {}

        //System.out.println("pathInfo:" + pathInfo);
        //System.out.println("file:" + "D:\\DEV\\Approaching Pi\\Distroy\\store\\dist\\img\\product\\" + imageName);
        //File file = new File("D:\\DEV\\Approaching Pi\\Distroy\\store\\dist\\img\\product\\" + imageName);
        String imagePath = getDefines().getProperty("website.base_dir")
                    + File.separator + "img"
                    + File.separator + "product"
                    + File.separator + imageName;
        log.debug("Loading image:" + imagePath);
        File file = new File(imagePath);

        if (!file.exists() || imageName==null || imageName.equals("")) {
            //PrintWriter out = res.getWriter();
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            //out.println("File Not Found");
        } else {
            FileInputStream in = new FileInputStream(file);
            int ilength = in.available();
            byte[] filebytes = new byte[ilength];
            in.read(filebytes);
            in.close();

            res.setContentType("image/jpeg");
            res.setContentLength(filebytes.length);

            ServletOutputStream ouputStream = res.getOutputStream();
            ouputStream.write(filebytes, 0, filebytes.length);
            ouputStream.flush();
            ouputStream.close();

            // set the content type of the response
        }
        file = null;
    }
}
