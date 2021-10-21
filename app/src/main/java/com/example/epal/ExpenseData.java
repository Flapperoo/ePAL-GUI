package com.example.epal;

public class ExpenseData {
    String expItem, expDate, expId, expNotes;
    int expAmount, expMonth;

    public ExpenseData(){

    }

    public ExpenseData(String expItem, String expDate, String expId, String expNotes, int expAmount, int expMonth) {
        this.expItem = expItem;
        this.expDate = expDate;
        this.expId = expId;
        this.expNotes = expNotes;
        this.expAmount = expAmount;
        this.expMonth = expMonth;
    }

    public String getExpItem() {
        return expItem;
    }

    public void setExpItem(String expItem) {
        this.expItem = expItem;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getExpId() {
        return expId;
    }

    public void setExpId(String expId) {
        this.expId = expId;
    }

    public String getExpNotes() {
        return expNotes;
    }

    public void setExpNotes(String expNotes) {
        this.expNotes = expNotes;
    }

    public int getExpAmount() {
        return expAmount;
    }

    public void setExpAmount(int expAmount) {
        this.expAmount = expAmount;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(int expMonth) {
        this.expMonth = expMonth;
    }
}
