package com.offcn.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {

    private long totalRecode;//总记录数
    private List rows;//当前页列表


    public PageResult() {
        super();
    }

    public PageResult(long totalRecode, List rows) {
        super();
        this.totalRecode = totalRecode;
        this.rows = rows;
    }

    public long getTotalRecode() {
        return totalRecode;
    }

    public void setTotalRecode(long totalRecode) {
        this.totalRecode = totalRecode;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
