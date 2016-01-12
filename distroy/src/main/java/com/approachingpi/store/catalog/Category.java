/*
 * User: terrence
 * Date: Jun 25, 2004
 * Time: 3:42:05 AM
 */
package com.approachingpi.store.catalog;

import com.approachingpi.store.catalog.Product;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Category implements Serializable {
    public static final int TYPE_GENERAL = 0;

    private boolean active              = false;
    private ArrayList children          = new ArrayList();
    private int id;
    private String name                 = "";
    private Category parentCategory;
    private ArrayList path              = new ArrayList();
    private int productCountTotal;
    private ArrayList products          = new ArrayList();
    private int productStart;
    private int rank;
    private int typeId;

    public Category() {
        this.setId(0);
    }
    public Category(int inCategoryId) {
        this.setId(inCategoryId);
    }
    public Category(int inCategoryId, String inName) {
        this.name = inName;
        id = inCategoryId;
    }
    public Category(Category inCategory) {
        this.id = inCategory.getId();
        this.name = inCategory.getName();
        this.parentCategory = new Category(inCategory.getParent());
        //this.productCountTotal = inCategory.getProductCountTotal();
    }

    public void deleteFromDb(Connection con) throws Exception {
        if (this.getId() > 0) {
            this.loadChildrenFromDb(con);
            
            for (int i=0; i<children.size(); i++) {
                Category child = (Category)children.get(i);
                child.deleteFromDb(con);
            }
                        
            PreparedStatement ps = con.prepareStatement("DELETE FROM tbCategory WHERE inId = ?");
            ps.setInt(1,this.getId());
            ps.execute();
            
            ps = con.prepareStatement("DELETE FROM tbLinkProductVariationCategory WHERE inCategoryId = ?");
            ps.setInt(1,this.getId());
            ps.execute();
        }
    }
    public boolean getActive() { return this.active; }
    public ArrayList getChildren() { return this.children; }
    public Product getFirstProduct() {
        if (products.size() == 0) {
            return new Product();
        } else {
            return (Product)products.get(1);
        }
    }
    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public Category getParent() {
        if (parentCategory == null) {
            parentCategory = new Category();
        }
        return this.parentCategory;
    }
    public ArrayList getParents() {
        ArrayList parents = new ArrayList();

        getParentsRecrusive(parents, this);

        return parents;
    }
    private void getParentsRecrusive(ArrayList parents,Category current) {
        parents.add(0,current.getParent());
        if (current.getParent().getId() > 0) {
            getParentsRecrusive(parents,current.getParent());
        }
    }
    public String getPathAsString() {
        if (this.getParent().getId() == 0) {
            return this.getName();
        } else {
            return this.getParent().getPathAsString() + " > " + this.getName();
        }
    }
    public String getPath() {
        if (this.getParent().getId() == 0) {
            return this.getPathCode();
        } else {
            return this.getParent().getPath() + this.getPathCode();
        }
    }
    public String getPathCode() {
        // this will only work with ids up to 46656 then it will break and we will need to fix it.
        String pathCode = Integer.toString(this.getRank(), 36);
        if (pathCode.length() == 1) {
            pathCode = "00" + pathCode;
        } else if (pathCode.length() == 2) {
            pathCode = "0" + pathCode;
        }
        return pathCode;
    }
    public ArrayList getProducts() { return this.products; }
    public int getProductCountTotal() { return this.productCountTotal; }
    public int getProductStart() { return this.productStart; }
    public int getRank() { return this.rank; }
    public int getType() { return this.typeId; }

    public static ArrayList loadAllCategories(Connection con) throws SQLException {
        ArrayList categories = new ArrayList();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCategory ORDER BY vcPath");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Category newCat = new Category();
            newCat.loadFromRs(rs);
            newCat.loadParentFromDb(con);
            categories.add(newCat);
        }
        rs.close();
        return categories;
    }

    public void loadFromDb(Connection con) throws SQLException {
        if (this.getId() > 0) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM tbCategory WHERE inId = ?");
            ps.setInt(1,this.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loadFromRs(rs);
            }
            rs.close();
        }
    }
    public void loadFromRs(ResultSet rs) throws SQLException {
        this.setId(rs.getInt("inId"));
        //this.setType(rs.getInt("inTypeId"));
        this.setRank(rs.getInt("inRank"));
        this.setName(rs.getString("vcName"));
        this.setParent(new Category(rs.getInt("inParent")));
        this.setActive(rs.getBoolean("btActive"));
    }

    public void loadProductsFromDb(Connection con) throws SQLException {
        loadProductsFromDb(con,-1);
    }
    public void loadProductsFromDb(Connection con, int max) throws SQLException {
        // set up the search engine
        SearchEngine se = new SearchEngine();
        se.addCategory(this);

        products = se.executeReturnProducts(con);
    }
    public void loadParentFromDb(Connection con) throws SQLException {
        this.getParent().loadFromDb(con);
        if (this.getParent().getId() > 0) {
            this.getParent().loadParentFromDb(con);
        }
    }
    public void loadChildrenFromDb(Connection con) throws SQLException {
        loadChildrenFromDb(con,false);
    }
    public void loadChildrenFromDb(Connection con, boolean activeOnly) throws SQLException {
        PreparedStatement ps;
        if (activeOnly) {
            ps = con.prepareStatement("SELECT * FROM tbCategory WHERE inParent = ? AND btActive = 1 ORDER BY vcPath");
        } else { 
            ps = con.prepareStatement("SELECT * FROM tbCategory WHERE inParent = ? ORDER BY vcPath");
        }
        ps.setInt(1,this.getId());
        ResultSet rs = ps.executeQuery();
        children = new ArrayList();
        while (rs.next()) {
            Category newCat = new Category();
            newCat.loadFromRs(rs);
            newCat.loadParentFromDb(con);
            children.add(newCat);
        }
        rs.close();

	    for (int i=0; i<children.size(); i++) {
		    Category theCat = (Category)children.get(i);
		    theCat.loadChildrenFromDb(con);
	    }
    }

    public void saveToDb(Connection con) throws SQLException {
        this.loadParentFromDb(con);

        PreparedStatement ps;

        if (this.getId() == 0 && this.getParent().getId() > 0) {
            ps = con.prepareStatement("SELECT Count(*) AS inCount FROM tbCategory WHERE inParent = ?");
            ps.setInt(1, this.getParent().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setRank((rs.getInt("inCount")*2+2));
            }
            rs.close();
        }

        if (this.getId() > 0) {
            ps = con.prepareStatement("UPDATE tbCategory SET inCategoryTypeId = ?, vcName = ?, inParent = ?, inRank = ?, btActive = ?, vcPath = ? WHERE inId = ?");
        } else {
            ps = con.prepareStatement("INSERT INTO tbCategory (inCategoryTypeId, vcName, inParent, inRank, btActive, vcPath) VALUES(?,?,?,?,?,?)");
        }
        int i=0;
        ps.setInt(++i,this.getType());
        ps.setString(++i,this.getName());
        ps.setInt(++i,this.getParent().getId());
        ps.setInt(++i,this.getRank());
        ps.setBoolean(++i,this.getActive());
        ps.setString(++i,this.getPath());
        if (this.getId() > 0) {
            ps.setInt(++i,this.getId());
        }
        ps.execute();

        if (this.getId() == 0) {
            ps = con.prepareStatement("SELECT Max(inId) AS inId FROM tbCategory WHERE vcName = ? AND inParent = ?");
            i=0;
            ps.setString(++i,this.getName());
            ps.setInt(++i,this.getParent().getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                this.setId(rs.getInt("inId"));
            }
            rs.close();
        }
    }

    public void setActive(boolean in) { this.active = in; }
    public void setId(int inCategoryId) { this.id = inCategoryId; }
    public void setName(String inName) { this.name = inName; }
    public void setParent(Category inCategory) { this.parentCategory = inCategory; }
    public void setProductCountTotal(int inCount) { this.productCountTotal = inCount; }
    //public void setProductCountCurrent(int inCount) { this.productCountCurrent = inCount; }
    public void setProductStart(int inStart) { this.productStart = inStart; }
    public void setRank(int in) { this.rank = in; }
    public void setType(int in) { this.typeId = in; }

    // Methods for products in category
    public void addProduct(Product inProduct) {
        this.products.add(inProduct);
    }

    // methods for sub-categories
    public void addSubCategory(Category inCategory) {
        this.children.add(inCategory);
    }

    // methods for the path to this category
    public void addPathCategory(Category inCategory) {
        this.path.add(0,inCategory);
    }
}

