/*
 * Created by IntelliJ IDEA.
 * User: terrence
 * Date: Jul 12, 2004
 * Time: 9:22:44 PM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.util.MessageBean;
import com.approachingpi.util.ImageInfo;
import com.approachingpi.store.catalog.Image;
import com.approachingpi.store.catalog.Product;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.AffineTransform;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;

public class ImageUploadServlet extends PiServlet {
    public static final int ACTION_MAIN     = 0;
    public static final int ACTION_UPLOAD   = 1;
    public static final int ACTION_SCALE    = 2;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        doPost(req,res);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        super.initPiServlet(req, res);

        Connection con = openConnection();
        PreparedStatement ps;
        ResultSet rs;

        MessageBean errorBean = this.getErrorBean(req);
        MessageBean messageBean = this.getMessageBean(req);

        int action = PiServlet.getReqInt(req,"action");

        String imagePath = getDefines().getProperty("website.base_dir") + File.separator +
                                    "img" + File.separator +
                                    "product" + File.separator;
        String imagePathOriginal    = imagePath + "original" + File.separator;
        String imagePathThumb       = imagePath + "thumb" + File.separator;
        String imagePathStandard    = imagePath + "standard" + File.separator;
        String imagePathEnlarge     = imagePath + "enlarge" + File.separator;


        // this will throw an error if we have an action in the query string when there is no
        // multipart request sent.  figure this out later and fix it.
        if (action == ACTION_UPLOAD) {
            try {
                DiskFileUpload fu = new DiskFileUpload();
                // maximum size before a FileUploadException will be thrown
                fu.setSizeMax(1000000);
                // maximum size that will be stored in memory
                fu.setSizeThreshold(4096);
                // the location for saving data that is larger than getSizeThreshold()
                fu.setRepositoryPath(getDefines().getProperty("website.tmp_dir"));

                List fileItems = fu.parseRequest(req);
                // assume we know there are two files. The first file is a small
                // text file, the second is unknown and is written to a file on
                // the server
                Image image = new Image();
                image.setProduct(new Product(PiServlet.getReqInt(req,"productId")));

                Iterator i = fileItems.iterator();
                // loop over the items twice, once to set the image propertys, once to process the upload
                while (i.hasNext()) {
                    FileItem item = (FileItem) i.next();

                    if (item.isFormField()) {
                        if (item.getFieldName().equalsIgnoreCase("desc")) {
                            image.setDesc(item.getString());
                        } else if (item.getFieldName().equalsIgnoreCase("productId")) {
                            try {
                                image.setProduct(new Product(Integer.parseInt(item.getString())));
                            } catch (Exception e) {}
                        }
                    }
                }
                if (image.getProduct().getId() == 0) {
                    errorBean.addMessage("Error uploading file, product id is 0.");
                }

                if (errorBean.getMessageCount() == 0) {
                    image.saveToDb(con);
                    image.setName(image.getProduct().getId() + "_" + image.getId() + ".jpg");
                    if (image.getId() == 0) {
                        errorBean.addMessage("Error uploading file, image id is 0.");
                    }
                }

                if (errorBean.getMessageCount() == 0) {
                    i = fileItems.iterator();
                    while (i.hasNext()) {
                        FileItem item = (FileItem) i.next();

                        if (!item.isFormField() && !item.getName().equals("")) {
                            if (!item.getName().toLowerCase().endsWith(".jpg")) {
                                errorBean.addMessage("You can only upload Jpegs.");
                            } else {
                                if (item.getFieldName().equalsIgnoreCase("uploadFileEnlarge")) {
                                    image.setNameOriginalEnlarge(item.getName());
                                    File imageFile = new File(imagePathOriginal + image.getName().replaceFirst("\\.jpg","_enlarge.jpg"));
                                    item.write(imageFile);
                                } else if (item.getFieldName().equalsIgnoreCase("uploadFileStandard")) {
                                    image.setNameOriginalStandard(item.getName());
                                    File imageFile = new File(imagePathOriginal + image.getName().replaceFirst("\\.jpg","_original.jpg"));
                                    item.write(imageFile);
                                } else if (item.getFieldName().equalsIgnoreCase("uploadFileThumb")) {
                                    image.setNameOriginalThumb(item.getName());
                                    File imageFile = new File(imagePathOriginal + image.getName().replaceFirst("\\.jpg","_thumb.jpg"));
                                    item.write(imageFile);
                                }
                            }
                        }
                    }
                }
                // save the product again since the name has changed.
                image.saveToDb(con);
                req.setAttribute("image",image);
            } catch (Exception e) {
                e.printStackTrace();
                errorBean.addMessage("Error uploading file: " + e.toString());
            }

            if (errorBean.getMessageCount() > 0) {
                action = ACTION_MAIN;
            }
        } else if (action == ACTION_SCALE) {
            Image image = new Image(PiServlet.getReqInt(req,"imageId"));
            try {
                image.loadFromDb(con);

                File fileEnlarge    =  new File(imagePathOriginal + image.getName().replaceAll("\\.jpg","_enlarge.jpg"));
                File fileStandard   =  new File(imagePathOriginal + image.getName().replaceAll("\\.jpg","_standard.jpg"));
                File fileThumb      =  new File(imagePathOriginal + image.getName().replaceAll("\\.jpg","_thumb.jpg"));

                if (!fileEnlarge.exists()) {
                    errorBean.addMessage("Error scaling image, enlarge image missing.");
                }
                if (errorBean.getMessageCount()==0 && !fileStandard.exists()) {
                    fileStandard = fileEnlarge;
                }
                if (errorBean.getMessageCount()==0 && !fileThumb.exists()) {
                    fileThumb = fileStandard;
                }

                if (errorBean.getMessageCount()==0) {
                    java.awt.Image imageFileEnlarge     = ImageIO.read(fileEnlarge);
                    java.awt.Image imageFileStandard    = ImageIO.read(fileStandard);
                    java.awt.Image imageFileThumb       = ImageIO.read(fileThumb);

                    int thumbWidth, standardWidth, enlargeWidth;

                    int width = imageFileStandard.getWidth(null);
                    int height = imageFileStandard.getHeight(null);

                    if (width > height) {
                        thumbWidth      = getDefines().getPropertyInt("product.thumb.horiz.width");
                        standardWidth   = getDefines().getPropertyInt("product.standard.horiz.width");
                        enlargeWidth    = getDefines().getPropertyInt("product.enlarge.horiz.width");
                        image.setOrientation(Image.HORIZONTAL);
                    } else if (width == height) {
                        thumbWidth      = getDefines().getPropertyInt("product.thumb.square.width");
                        standardWidth   = getDefines().getPropertyInt("product.standard.square.width");
                        enlargeWidth    = getDefines().getPropertyInt("product.enlarge.square.width");
                        image.setOrientation(Image.SQUARE);
                    } else {
                        thumbWidth      = getDefines().getPropertyInt("product.thumb.vert.width");
                        standardWidth   = getDefines().getPropertyInt("product.standard.vert.width");
                        enlargeWidth    = getDefines().getPropertyInt("product.enlarge.vert.width");
                        image.setOrientation(Image.VERTICAL);
                    }
                    image.saveToDb(con);

                    File thumb = new File(imagePathThumb + image.getName());
                    this.generateImage(imageFileThumb,thumb,thumbWidth,getDefines().getPropertyInt("product.thumb.quality"));
                    thumb = null;

                    File standard = new File(imagePathStandard + image.getName());
                    this.generateImage(imageFileStandard,standard,standardWidth,getDefines().getPropertyInt("product.standard.quality"));
                    standard = null;

                    File enlarge = new File(imagePathEnlarge + image.getName());
                    this.generateImage(imageFileEnlarge,enlarge,enlargeWidth,getDefines().getPropertyInt("product.enlarge.quality"));
                    enlarge = null;

                    imageFileEnlarge.flush();
                    imageFileStandard.flush();
                    imageFileThumb.flush();
                }
                
                fileEnlarge.delete();
                fileStandard.delete();
                fileThumb.delete();


            } catch (Exception e) {
                e.printStackTrace();
                errorBean.addMessage("Error Scaling Image: " + e.toString());
            }
            if (errorBean.getMessageCount() > 0) {
                action = ACTION_MAIN;
            }
            req.setAttribute("image",image);
        }


        try {
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (action == ACTION_MAIN) {
            req.getRequestDispatcher("/jsp/admin/upload_main.jsp").forward(req, res);
        } else if (action == ACTION_UPLOAD) {
            req.getRequestDispatcher("/jsp/admin/upload_scale.jsp").forward(req, res);
        } else if (action == ACTION_SCALE) {
            req.getRequestDispatcher("/jsp/admin/upload_done.jsp").forward(req, res);
        }
    }


    public void generateImage(java.awt.Image image, File scaledFile, int new_width, int quality) throws IOException {
        double w = image.getWidth(null);
        double h = image.getHeight(null);

        double nw = 0;
        double nh = 0;
        double scalex = 0;

        if (w < h) {
            // if no explicit width then set the width to the real image size
            nw = new_width;
            if(new_width == -1) {
                nw = w;
            }
            // calculate the scaling factor
            scalex = nw/w;

            // base the height off of the frame, if there is a frame
            nh = scalex*h;
        } else {
            // if no explicit width then set the width to the real image size
            nh = new_width;
            if(new_width == -1) {
                nh = h;
            }
            // calculate the scaling factor
            scalex = nh/h;

            // base the height off of the frame, if there is a frame
            nw = scalex*w;
        }

        BufferedImage thumbImage = new BufferedImage((int)nw, (int)nh, 1);
        Graphics2D graphics2D = thumbImage.createGraphics();
        graphics2D.setBackground(Color.WHITE);
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillRect(0, 0, (int)nw, (int)nh);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.drawImage(image, 0, 0, (int)nw, (int)nh, null);
        //ImageIO.write(thumbImage, "JPG", thumbFile);

        // this is an attempt to get the image quality better than ImageIO writes
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(scaledFile));
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
        JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);
        param.setQuality((float)quality / 100.0f,false);
        encoder.encode(thumbImage,param);
    }
}
