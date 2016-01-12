package com.approachingpi.store.order;

import com.approachingpi.store.cart.CartItem;
import com.approachingpi.store.catalog.ProductVariation;
import com.approachingpi.store.catalog.Size;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

/**
 * User: terrence
 * Date: Aug 24, 2004
 * Time: 8:40:17 PM
 */
public class OrderDetail {
	protected String description;
	protected int id;
	protected Order order;
    protected Payment payment;
	protected BigDecimal priceItem;
	protected BigDecimal priceTotal;
	protected ProductVariation productVariation;
    protected Shipment shipment;
	protected Size size;
	protected String sizeDesc;
	protected int qty;

    protected ArrayList detailSizes = new ArrayList();

    // inStock is not actually part of the detail, but used when using this object
    // as a bean for the web application.
    protected int inQtyInStock;


	public OrderDetail(Order order, CartItem item) {
		setOrder(order);
        setPriceItem(item.getPriceItem());
		setPriceTotal(item.getPriceTotal());
		setProductVariation(item.getProductVariation());
		setSize(item.getSize());
		setSizeDesc(item.getSize().getName());
		setQty(item.getQty());
		setDescription(productVariation.getProduct().getName() + " - " + productVariation.getStyle() + " - " + productVariation.getColor());
	}
    public OrderDetail(Order order, int id) {
        setOrder(order);
        setId(id);
    }

    public void addDetailSize(OrderDetailSize size) {
        if (size != null) {
            detailSizes.add(size);
            qty = qty + size.getQty();
        }
    }
	public String getDescription() {
		return description;
	}
    public ArrayList getDetailSizes() {
        return detailSizes;
    }
	public int getId() {
		return id;
	}
	public Order getOrder() {
		return order;
	}
    public Payment getPayment() {
        return payment;
    }
	public BigDecimal getPriceItem() {
		return priceItem.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public BigDecimal getPriceTotal() {
		return priceTotal.setScale(2,BigDecimal.ROUND_HALF_EVEN);
	}
	public ProductVariation getProductVariation() {
		return productVariation;
	}
    public int getQtyInStock() {
        return inQtyInStock;
    }
    public int getQtyForSize(Size size) {
        if (size == null) {
            return 0;
        }
        for (int i=0; i<detailSizes.size(); i++) {
            OrderDetailSize detailSize = (OrderDetailSize)detailSizes.get(i);
            if (detailSize != null && detailSize.getSize().equals(size)) {
                return detailSize.getQty();
            }
        }
        return 0;
    }
    public Shipment getShipment() {
        return shipment;
    }
	public Size getSize() {
		return size;
	}
    public String getSizeDesc() { return sizeDesc; }
	public int getQty() {
		return qty;
	}
	public void loadFromDb(Connection con) throws SQLException {
		if (getId() == 0) {
 	        return;
		}
		PreparedStatement ps = con.prepareStatement("SELECT * FROM tbOrderDetail WHERE inId=?");
		ps.setInt(1,getId());
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			loadFromRs(rs);
		}
		rs.close();
	}

	public void loadFromRs(ResultSet rs) throws SQLException {
		Order newOrder=new Order();
		newOrder.setId(rs.getString("vcOrderId"));
		if (getOrder()==null || !getOrder().getId().equalsIgnoreCase(newOrder.getId())) {
			setOrder(newOrder);
		}
		ProductVariation newProductVariation = new ProductVariation(rs.getInt("inProductVariationId"));
		if (getProductVariation()==null || getProductVariation().getId() != newProductVariation.getId()) {
			setProductVariation(newProductVariation);
		}
		Size newSize = new Size(rs.getInt("inSizeId"));
		if (getSize()==null || getSize().getId()!=newSize.getId()) {
			setSize(newSize);
		}
		setSizeDesc(rs.getString("vcSizeDesc"));
		setQty(rs.getInt("inQty"));
		setPriceItem(rs.getBigDecimal("moPriceOne"));
		setPriceTotal(rs.getBigDecimal("moPriceTotal"));
		//setCouponApplied(rs.getBigDecimal("moCoupon_Applied"));
		//setWeight(rs.getInt("inWeightOne"));
		//setWeightTotal(rs.getInt("inWeightTotal"));
        if (rs.getInt("inShipmentId") > 0) {
		    setShipment(new Shipment(rs.getInt("inShipmentId")));
        }
        /*
        if (rs.getInt("inPaymentId") > 0) {
            setPayment(new Payment(rs.getInt("inPaymentId")));
        }
         */
		//setCouponIdBreakDown(rs.getString("vcCouponIdBreakdown"));
		//setCouponBreakdown(rs.getString("vcCouponBreakdown"));
		setDescription(rs.getString("vcItemDesc"));
	}

    public void loadInStockFromDb(Connection con) throws SQLException {
        if (getProductVariation() != null && getSize() != null) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbLinkProductVariationSize WHERE inProductVariationId=? AND inSizeId=?");
            ps.setInt(1, this.getProductVariation().getId());
            ps.setInt(2, this.getSize().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setQtyInStock(rs.getInt("inQtyInStock"));
            }
            rs.close();
        }
    }

	public void saveToDb(Connection con) throws Exception {
		if (getOrder()==null || getOrder().getId().equals("")) {
			throw new Exception("You must set and save the order prior to saving the detail.");
		}

		PreparedStatement ps;
		if (getId()>0) {
			ps = con.prepareStatement("UPDATE tbOrderDetail SET vcOrderId=?, inProductVariationId=?, inSizeId=?, vcSizeDesc=?, inQty=?, moPriceOne=?, moPriceTotal=?, moCoupon_Applied=?, inWeightOne=?, inWeightTotal=?, inShipmentId=?, inPaymentId=?, vcCouponIdBreakdown=?, vcCouponBreakdown=?, vcItemDesc=? WHERE inId=?");
		} else {
			ps = con.prepareStatement("INSERT INTO tbOrderDetail ( "+
			        "vcOrderId, inProductVariationId, inSizeId, vcSizeDesc, inQty, "+
			        "moPriceOne, moPriceTotal, moCoupon_Applied, inWeightOne, inWeightTotal, "+
			        "inShipmentId, inPaymentId, vcCouponIdBreakdown, vcCouponBreakdown, vcItemDesc "+
			        ") VALUES ( "+
			        "?,?,?,?,?,"+
			        "?,?,?,?,?,"+
			        "?,?,?,?,?"+
			        ")");
		}

		int i=0;
		ps.setString(++i,getOrder().getId());
		ps.setInt(++i,getProductVariation().getId());
		ps.setInt(++i,getSize().getId());
		ps.setString(++i, getSizeDesc());
		ps.setInt(++i, getQty());

		ps.setBigDecimal(++i,getPriceItem());
		ps.setBigDecimal(++i,getPriceTotal());
		ps.setBigDecimal(++i,new BigDecimal("0.00"));
		ps.setNull(++i,Types.INTEGER);
		ps.setNull(++i,Types.INTEGER);

        if (getShipment() != null && getShipment().getId() > 0) {
		    ps.setInt(++i, getShipment().getId());
        } else {
            ps.setNull(++i,Types.INTEGER);
        }
        if (getPayment() != null && getPayment().getId() > 0) {
            ps.setNull(++i,getPayment().getId());
        } else {
            ps.setNull(++i,Types.INTEGER);
        }
		ps.setString(++i,"");
		ps.setString(++i,"");
		ps.setString(++i,this.getDescription());

        if (getId()>0) {
	        ps.setInt(++i,getId());
        }

		ps.execute();

		if (getId()==0) {
			ps = con.prepareStatement("SELECT Max(inId) as inMaxId FROM tbOrderDetail WHERE vcOrderId=? AND inProductVariationId=?");
			ps.setString(1,getOrder().getId());
			ps.setInt(2,getProductVariation().getId());
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				setId(rs.getInt("inMaxId"));
			}
			rs.close();
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}
	public void setId(int id) {
		this.id = id;
	}
    public void setPayment(Payment in) {
        this.payment = in;
    }
	public void setPriceItem(BigDecimal priceItem) {
		this.priceItem = priceItem;
	}
	public void setPriceTotal(BigDecimal priceTotal) {
		this.priceTotal = priceTotal;
	}
	public void setProductVariation(ProductVariation productVariation) {
		this.productVariation = productVariation;
	}
    public void setQtyInStock(int in) {
        this.inQtyInStock = in;
    }
    public void setShipment(Shipment in) {
        this.shipment = in;
    }
	public void setSize(Size size) {
		this.size = size;
	}
	public void setSizeDesc(String in) { sizeDesc=(in==null)?"":in; }
	public void setOrder(Order order) {
		this.order = order;
	}
	public void setQty(int qty) {
		this.qty = qty;
	}

}
