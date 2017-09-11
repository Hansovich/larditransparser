package com.yh.lt;

import java.util.Date;

/**
 * Created by @yhankovich on 9/11/17.
 */
public class Offer {
    private final String rowHtml;
    private final String id;
    private final Date sent;

    public Offer(String rowHtml, String id, Date sent) {
        this.rowHtml = rowHtml;
        this.id = id;
        this.sent = sent;
    }

    public String getRowHtml() {
        return rowHtml;
    }

    public String getId() {
        return id;
    }

    public Date getSent() {
        return sent;
    }
}