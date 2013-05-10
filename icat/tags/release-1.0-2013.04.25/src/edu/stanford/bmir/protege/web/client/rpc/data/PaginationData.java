package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;

public class PaginationData<T> implements Serializable{
    
    private static final long serialVersionUID = 1094937462995580159L;
    private ArrayList<T> data;
    private int totalRecords;
    
    public PaginationData() {
        data = new ArrayList<T>();
        totalRecords = 0;
    }

    public ArrayList<T> getData() {
        return data;
    }

    public void setData(ArrayList<T> data) {
        this.data = data;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

}
