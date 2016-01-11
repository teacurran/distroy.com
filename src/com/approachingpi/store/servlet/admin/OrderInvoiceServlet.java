/*
 * OrderInvoiceServlet.java
 *
 * Created on October 6, 2004, 10:25 PM
 */
package com.approachingpi.store.servlet.admin;

import com.approachingpi.servlet.PiServlet;
import com.approachingpi.store.order.Order;
import com.approachingpi.store.order.OrderAddress;
import com.approachingpi.store.order.OrderDetail;
import com.approachingpi.store.catalog.Size;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class OrderInvoiceServlet extends PiServlet {
    public static final int TYPE_INVOICE        = 0;
    public static final int TYPE_RECEIPT        = 1;
    public static final int TYPE_GIFT_RECEIPT   = 2;
    public static final int TYPE_PACKING_LIST   = 3;

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    doPost(req,res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
	    super.initPiServlet(req, res);

		Connection con = openConnection();

        int type = PiServlet.getReqInt(req,"type",0);
        Order order = new Order(PiServlet.getReqString(req,"orderId"));
        try {
            order.loadFromDb(con);
            order.loadExtendedFromDb(con);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream baosPDF = null;

        String filePath = getServletContext().getRealPath("/");
        if (filePath != null) {
            // trim off the ".\"
            if (filePath.substring(filePath.length() - 2, filePath.length()).equals(".\\")) {
                filePath = filePath.substring(0, filePath.length() - 2);
            }
            if (!filePath.substring(filePath.length() - 1, filePath.length()).equals(File.separator)) {
                filePath += File.separator;
            }
            filePath += "WEB-INF" + File.separator;
        }
        System.out.println(filePath);
        
        try {
            baosPDF = generatePDFDocumentBytes(req, this.getServletContext(), order, filePath, type, con);

            res.setHeader("Cache-Control", "max-age=30");   // 30 seconds
            res.setContentType("application/pdf");
            res.setHeader("Content-disposition", "inline; filename=invoice-"+ order.getId() + ".pdf");

            ServletOutputStream sos;
            sos = res.getOutputStream();
            baosPDF.writeTo(sos);
            sos.flush();
        } catch (DocumentException dex)	{
            dex.printStackTrace();
			res.setContentType("text/html");
			PrintWriter writer = res.getWriter();
			writer.println(
					this.getClass().getName() 
					+ " caught an exception: " 
					+ dex.getClass().getName()
					+ "<br>");
			writer.println("<pre>");
			dex.printStackTrace(writer);
			writer.println("</pre>");
		} finally {
			if (baosPDF != null) {
				baosPDF.reset();
			}
		} 
        
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    	/**
	 *  
	 * @param req must be non-null
	 * 
	 * @return a non-null output stream. The output stream contains
	 *         the bytes for the PDF document
	 * 
	 * @throws DocumentException
	 * 
	 */
	protected ByteArrayOutputStream generatePDFDocumentBytes(final HttpServletRequest req, final ServletContext ctx, Order order, String filePath, int type, Connection con) throws DocumentException {
		Document doc = new Document();
		
		ByteArrayOutputStream baosPDF = new ByteArrayOutputStream();
		PdfWriter docWriter = null;
        
        SimpleDateFormat timeFormat = new SimpleDateFormat("M/d/yyyy HH:mm:ss z");

        String typeName = "";
        switch (type) {
            case TYPE_INVOICE:
                typeName="INVOICE";
                break;
            case TYPE_RECEIPT:
                typeName="RECEIPT";
                break;
            case TYPE_GIFT_RECEIPT:
                typeName="GIFT RECEIPT";
                break;
            case TYPE_PACKING_LIST:
                typeName="PACKING LIST";
                break;
            default:
                typeName="INVOICE";
                break;
        }

		try	{
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font stdFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font smallFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            float grayValue = (float)0.95;

            if (order.getStore().isWholesale()) {
                stdFont = smallFont;
            }

			docWriter = PdfWriter.getInstance(doc, baosPDF);
			
			doc.addAuthor(this.getClass().getName());
			doc.addCreationDate();
            
			doc.addProducer();
			doc.addCreator(this.getClass().getName());
			doc.addTitle("DISTRO.Y Order #" + order.getId());
			doc.addKeywords("pdf, itext, Java, open source, http");
			doc.setPageSize(PageSize.LETTER);
           
            
            Phrase footerPhrase = new Phrase("For questions about your order, please contact Customer Service:\n"+
                "http://www.distroy.com/content/support/contact\n"+
                "or call us toll free at: 800.627.4980",stdFont);
            HeaderFooter footer = new HeaderFooter(footerPhrase,false);
            doc.setFooter(footer);
            
			doc.open();

            try {
                BaseFont bfApi = BaseFont.createFont(filePath + "api.ttf", BaseFont.CP1252, BaseFont.EMBEDDED);
                Font fApi = new Font(bfApi, 24);

                Font fInvoice = new Font(Font.HELVETICA, 18, Font.BOLD);
                
                Table headTable = new Table(2);
                headTable.setWidth(100);
                headTable.setBorderWidth(0);
                headTable.setPadding(0);
                headTable.setSpacing(0);

                Cell logoCell = new Cell(new Phrase("J",fApi));
                logoCell.setVerticalAlignment(Cell.TOP);
                logoCell.setBorderWidth(0);
                Cell invoiceCell = new Cell(new Phrase(typeName,fInvoice));
                invoiceCell.setVerticalAlignment(Cell.ALIGN_TOP);
                invoiceCell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                invoiceCell.setBorderWidth(0);
                
                headTable.addCell(logoCell);
                headTable.addCell(invoiceCell);
                
                Table orderIdTable = new Table(3);
                //orderIdTable.setDefaultCellBorder(1);
                //orderIdTable.setBorder(0);
                //orderIdTable.setPadding(3);

                int[] widths = {5,5,1};
                orderIdTable.setWidths(widths);

                orderIdTable.setWidth(90);
                orderIdTable.setBorderWidth(0);
                orderIdTable.setDefaultCellBorder(0);
                orderIdTable.setPadding(3);
                orderIdTable.setWidth(50);
                orderIdTable.setAlignment(Table.ALIGN_RIGHT);

                if (order.getStore().isWholesale()) {
                    Cell cell = new Cell(new Phrase("CUST PO", boldFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    cell.setBorder(0);
                    orderIdTable.addCell(cell);
                    cell = new Cell(new Phrase(order.getPo(),boldFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    orderIdTable.addCell(cell);
                    orderIdTable.addCell(new Cell(new Phrase("",boldFont)));
                }

                Cell cell = new Cell(new Phrase("ORDER#", boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setGrayFill((float)0.95);
                orderIdTable.addCell(cell);
                cell = new Cell(new Phrase(order.getId(),boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setGrayFill((float)0.95);
                orderIdTable.addCell(cell);
                orderIdTable.addCell(new Cell(new Phrase("",boldFont)));

                cell = new Cell(new Phrase("DATE ", boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                orderIdTable.addCell(cell);
                cell = new Cell(new Phrase(timeFormat.format(order.getDateCreated()),boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                orderIdTable.addCell(cell);
                orderIdTable.addCell(new Cell(new Phrase("",boldFont)));

                cell = new Cell(new Phrase("STATUS ", boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setGrayFill((float)0.95);
                orderIdTable.addCell(cell);
                cell = new Cell(new Phrase(Order.getStatusById(order.getStatus()),boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setGrayFill((float)0.95);
                orderIdTable.addCell(cell);
                orderIdTable.addCell(new Cell(new Phrase("",boldFont)));

                doc.add(headTable);
                doc.add(orderIdTable);

                //headTable.insertTable(orderIdTable, 1, 1);
                
                //doc.add(headTable);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                Table contentTable = new Table(2);
                contentTable.setWidth(100);
                contentTable.setBorderWidth(0);
                
                /*
                Cell cell = new Cell(new Phrase("ORDER# " + order.getId(), boldFont));
                cell.setColspan(2);
                cell.setBorderWidth(0);
                contentTable.addCell(cell);
                */
                 
                Cell cell = new Cell(new Phrase("BILL TO:", boldFont));
                cell.setBorderWidth(0);
                contentTable.addCell(cell);

                cell = new Cell(new Phrase("SHIP TO:", boldFont));
                cell.setBorderWidth(0);
                contentTable.addCell(cell);
                
                OrderAddress billing = order.getBillAddress();
                OrderAddress shipping = order.getShipAddress();
                
                cell = new Cell();
                cell.setBorderWidth(0);
                cell.addElement(new Paragraph(billing.getNameFirst() + " " + billing.getNameLast(),stdFont));
                cell.addElement(new Paragraph(billing.getAddress1(),stdFont));
                if (billing.getAddress2().length() > 0) {
                    cell.addElement(new Paragraph(billing.getAddress2(),stdFont));
                }
                if (billing.getState().getAbbrev().length() > 0) {
                    cell.addElement(new Paragraph(billing.getCity() + ", " + billing.getState().getAbbrev() + " " + billing.getZip(),stdFont));
                } else if (billing.getZip().length() > 0) {
                    cell.addElement(new Paragraph(billing.getCity() + ", " + billing.getZip(),stdFont));
                } else {
                    cell.addElement(new Paragraph(billing.getCity(),stdFont));
                }
                cell.addElement(new Paragraph(billing.getCountry().getName(),stdFont));
                cell.addElement(new Paragraph(billing.getPhoneNumber(),stdFont));
                contentTable.addCell(cell);

                cell = new Cell();
                cell.setBorderWidth(0);
                if (shipping == null || shipping.getId() == 0 || billing.getId() == shipping.getId()) {
                    cell.addElement(new Paragraph("(same as billing info)",stdFont));
                } else {
                    cell.addElement(new Paragraph(shipping.getNameFirst() + " " + shipping.getNameLast(),stdFont));
                    cell.addElement(new Paragraph(shipping.getAddress1(),stdFont));
                    if (billing.getAddress2().length() > 0) {
                        cell.addElement(new Paragraph(shipping.getAddress2(),stdFont));
                    }
                    if (billing.getState().getAbbrev().length() > 0) {
                        cell.addElement(new Paragraph(shipping.getCity() + ", " + shipping.getState().getAbbrev() + " " + shipping.getZip(),stdFont));
                    } else if (billing.getZip().length() > 0) {
                        cell.addElement(new Paragraph(shipping.getCity() + ", " + shipping.getZip(),stdFont));
                    } else { 
                        cell.addElement(new Paragraph(shipping.getCity(),stdFont));
                    } 
                    cell.addElement(new Paragraph(shipping.getCountry().getName(),stdFont));
                    cell.addElement(new Paragraph(shipping.getPhoneNumber(),stdFont));
                } 
                contentTable.addCell(cell);
                
                // spacer cell
                cell = new Cell(" ");
                cell.setBorderWidth(0);
                contentTable.addCell(cell);
                
                doc.add(contentTable);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ArrayList sizes = new ArrayList();
            if (type == TYPE_PACKING_LIST) {
                try {
                    sizes = Size.loadAllSizes(con);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Table table = null;
            if (type == TYPE_GIFT_RECEIPT) {
                table = new Table(4);
                int[] widths = {2,5,1,1};
                table.setWidths(widths);
            } else if (type == TYPE_PACKING_LIST) {
                table = new Table(3 + sizes.size());
                int[] widths = new int[3 + sizes.size()];
                System.out.println("\n\n\n*************\n" + widths.length + "\n***********\n");
                int col = -1;
                widths[++col] = 2;
                widths[++col] = 5;
                int i=0;
                for (i=0; i<sizes.size(); i++) {
                    widths[++col] = 1;
                }
                widths[++col] = 1;
                table.setWidths(widths);
            } else {
                table = new Table(5);
                int[] widths = {2,5,1,1,1};
                table.setWidths(widths);
            }

            table.setWidth(100);
            table.setBorderWidth(0);
            table.setPadding(3);

            Cell cell = new Cell(new Phrase("SKU",boldFont));
            //cell.setColspan(2);
            table.addCell(cell);
            cell = new Cell(new Phrase("Description",boldFont));
            //cell.setColspan(3);
            table.addCell(cell);
            if (type == TYPE_PACKING_LIST) {
                for (int x=0; x<sizes.size(); x++) {
                    Size thisSize = (Size)sizes.get(x);
                    cell = new Cell(new Phrase(thisSize.getNameShort(), boldFont));
                    if (x%2==0) {
                        cell.setGrayFill((float)0.95);
                    }
                    cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
                    table.addCell(cell);
                }
            } else {
                table.addCell(new Cell(new Phrase("Size",boldFont)));
            }
            cell = new Cell(new Phrase("QTY", boldFont));
            cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
            table.addCell(cell);

            if ((type != TYPE_GIFT_RECEIPT) && (type != TYPE_PACKING_LIST)) {
                table.addCell(new Phrase("Price",boldFont));
            }

            if (type == TYPE_PACKING_LIST) {
                ArrayList items = order.getDetailsGrouped();
                for (int i=0; i<items.size(); i++) {
                    OrderDetail item = (OrderDetail)items.get(i);
                    table.addCell(new Cell(new Phrase(item.getProductVariation().getSku(),stdFont)));
                    table.addCell(new Cell(new Phrase(item.getDescription(),stdFont)));

                    for (int x=0; x<sizes.size(); x++) {
                        Size thisSize = (Size)sizes.get(x);
                        int qty = item.getQtyForSize(thisSize);

                        if (qty > 0) {
                            cell = new Cell(new Phrase(Integer.toString(qty), stdFont));
                        } else {
                            cell = new Cell(new Phrase("", stdFont));
                        }
                        if (x%2==0) {
                            cell.setGrayFill((float)0.95);
                        }
                        cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
                        table.addCell(cell);
                    }
                    cell = new Cell(new Phrase(Integer.toString(item.getQty()), stdFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_CENTER);
                    table.addCell(cell);
                }
            } else {
                ArrayList items = order.getDetails();
                for (int i=0; i<items.size(); i++) {
                    OrderDetail item = (OrderDetail)items.get(i);
                    cell = new Cell(new Phrase(item.getProductVariation().getSku(),stdFont));
                    table.addCell(cell);
                    cell = new Cell(new Phrase(item.getDescription(),stdFont));
                    table.addCell(cell);
                    table.addCell(new Cell(new Phrase(item.getSizeDesc(), stdFont)));
                    table.addCell(new Cell(new Phrase(Integer.toString(item.getQty()), stdFont)));
                    if (type != TYPE_GIFT_RECEIPT) {
                        cell = new Cell(new Phrase("$"+item.getPriceTotal().toString(), stdFont));
                        cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                        cell.setNoWrap(true);
                        table.addCell(cell);
                    }
                }
            }

            // subtotal
            cell = new Cell(new Phrase("Ship Method: " + order.getShipMethod().getName(),stdFont));
            cell.setColspan(2);
            cell.setBorderWidth(0);
            table.addCell(cell);
            if (type != TYPE_GIFT_RECEIPT && type != TYPE_PACKING_LIST) {
                cell = new Cell(new Phrase("Subtotal:",stdFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setColspan(2);
                cell.setBorderWidth(0);
                table.addCell(cell);
                cell = new Cell(new Phrase("$"+order.getAmountSubtotal().toString(),stdFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setNoWrap(true);
                table.addCell(cell);

                System.out.println("[" + table.columns() + "]");

                // shipping
                cell = new Cell(new Phrase("Shipping & Handling:",stdFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setColspan(sizes.size() + 4);
                cell.setBorderWidth(0);
                table.addCell(cell);
                cell = new Cell(new Phrase("$"+order.getAmountShipping().toString(),stdFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setNoWrap(true);
                table.addCell(cell);

                // coupon
                if (order.getAmountCouponTotal().compareTo(new BigDecimal("0.00")) > 0) {
                    cell = new Cell(new Phrase("Coupon Discount:",stdFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    cell.setColspan(sizes.size() + 4);
                    cell.setBorderWidth(0);
                    table.addCell(cell);
                    cell = new Cell(new Phrase("$"+order.getAmountShipping().toString(),stdFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    cell.setNoWrap(true);
                    table.addCell(cell);
                }

                // shipping
                cell = new Cell(new Phrase("Tax:",stdFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setColspan(sizes.size() + 4);
                cell.setBorderWidth(0);
                table.addCell(cell);
                cell = new Cell(new Phrase("$"+order.getAmountTax().toString(),stdFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setNoWrap(true);
                table.addCell(cell);

                if (order.getAmountCapturedTotal().compareTo(new BigDecimal("0.00")) > 0) {
                    cell = new Cell(new Phrase("Payment Received:",stdFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    cell.setBorderWidth(0);
                    cell.setColspan(sizes.size() + 4);
                    table.addCell(cell);
                    cell = new Cell(new Phrase("($"+order.getAmountCapturedTotal().toString()+")",stdFont));
                    cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                    cell.setNoWrap(true);
                    table.addCell(cell);
                }

                cell = new Cell(new Phrase("Total:",boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setBorderWidth(0);
                cell.setColspan(sizes.size() + 4);
                table.addCell(cell);
                cell = new Cell(new Phrase("$"+order.getAmountTotalDue().toString(),boldFont));
                cell.setHorizontalAlignment(Cell.ALIGN_RIGHT);
                cell.setNoWrap(true);
                table.addCell(cell);
            }

            doc.add(table);
			
            
			
		} catch (DocumentException dex) {
			baosPDF.reset();
			throw new DocumentException(dex); 
		} finally {
			if (doc != null) {
				doc.close();
			}
			if (docWriter != null) {
				docWriter.close();
			}
		}

		if (baosPDF.size() < 1) {
			throw new DocumentException("document has "	+ baosPDF.size() + " bytes");		
		}
		return baosPDF;
	}
}
