/* * ProductRecordSet.java * * Created on October 22, 2004, 2:07 AM * * @author  Administrator */package com.approachingpi.store.catalog;import com.approachingpi.search.ResultPage;public class ProductRecordSet extends ResultPage {    public ProductRecordSet() {        // set default values of -1 so we know they wern't set    }    public Product nextProduct() {        if (itemsIterator == null) {            reset();        }        if (itemsIterator.hasNext()) {            return (Product)itemsIterator.next();        } else {            return null;        }    }}