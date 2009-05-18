/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.model;

import java.util.ArrayList;

public class PDCSubscription implements java.io.Serializable, Cloneable {

    public static final int NULL_ID = -1;

    private int id = NULL_ID;
    private String name;
    private ArrayList pdcContext;
    private int ownerId = NULL_ID;


    protected PDCSubscription() {
    }

    public PDCSubscription(int id, String name, ArrayList pdcContext, int ownerId) {
        this.id = id;
        this.name = name;
        this.pdcContext = pdcContext;
        this.ownerId = ownerId;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList getPdcContext() {
        return pdcContext;
    }

    public void setPdcContext(ArrayList pdcContext) {
        this.pdcContext = pdcContext;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }


    /**
     * Overriden toString method for debug/trace purposes
     */
    public String toString() {
        return "PDCSubscription object : [ id = " + id +
                ", name = " + name +
                ", ownerId = " + ownerId +
                ", pdcContext = " + pdcContext +
                " ];";
    }


    /**
     * Support Cloneable Interface
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // this should never happened
        }
    }

}
