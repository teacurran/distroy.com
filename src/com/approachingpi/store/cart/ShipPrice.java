/*
 * ShipPrice.java
 *
 * Created on August 23, 2004, 7:07 PM
 */

package com.approachingpi.store.cart;

import java.sql.*;
import java.math.BigDecimal;

import com.cms.common.util.MathExpression;

public class ShipPrice {
    String      calculation = "";
    float       dollarMin;
    float       dollarMax;
    int         id;
    int         itemMin;
    int         itemMax;
    ShipMethod  shipMethod;
    int         weightMin;
    int         weightMax;
    
    public ShipPrice() {
    }
    public ShipPrice(int id) {
        setId(id);
    }
    
    public BigDecimal calculate(BigDecimal subtotal, int items, int weight) {
        BigDecimal retVal = new BigDecimal("0.00");
        
        if (calculation == null || calculation.equalsIgnoreCase("")) {
            return retVal;
        }
        calculation = calculation.replaceAll("subtotal", subtotal.toString());
        calculation = calculation.replaceAll("items", Integer.toString(items));
        calculation = calculation.replaceAll("weight", Integer.toString(weight));
        
        try {
            MathExpression me = new MathExpression(calculation);
            retVal = new BigDecimal(Double.toString(me.value(0)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("Calculation:" + calculation +"="+ retVal.toString());
        
        return retVal;
    }
    
    public String getCalculation() {
        return calculation;
    }
    public float getDollarMin() {
        return dollarMin;
    }
    public float getDollarMax() {
        return dollarMax;
    }
    public int getId() { return id; }
    public int getItemMin() { return this.itemMin; }
    public int getItemMax() { return this.itemMax; }
    public ShipMethod getShipMethod() { return this.shipMethod; }
    public int getWeightMin() { return this.weightMin; }
    public int getWeightMax() { return this.weightMax; }
    
    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setDollarMin(rs.getFloat("moDollarMin"));
        this.setDollarMax(rs.getFloat("moDollarMax"));
        this.setWeightMin(rs.getInt("inWeightMin"));
        this.setWeightMax(rs.getInt("inWeightMax"));
        this.setItemMin(rs.getInt("inItemMin"));
        this.setItemMax(rs.getInt("inItemMax"));
        this.setCalculation(rs.getString("vcCalculation"));
    }
    
    public void setCalculation(String in) { 
        this.calculation = (in==null) ? "" : in;
    }
    public void setDollarMin(float in) { dollarMin = in; } 
    public void setDollarMax(float in) { dollarMax = in; } 
    public void setId(int in) { this.id = in; }
    public void setItemMin(int in) { this.itemMin = in; }
    public void setItemMax(int in) { this.itemMax = in; }
    public void setWeightMin(int in) { this.weightMin = in; }
    public void setWeightMax(int in) { this.weightMax = in; }
}
