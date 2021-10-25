package com.example.epal;

public class SavingsData {

    String savDate, savId, savNotes;
    int savingsAmt, savMonth;

    public SavingsData() {

    }

    public SavingsData(String savDate, String savId, String savNotes, int savingsAmt, int savMonth) {
        this.savDate = savDate;
        this.savId = savId;
        this.savNotes = savNotes;
        this.savingsAmt = savingsAmt;
        this.savMonth = savMonth;
    }

    public String getSavDate() {
        return savDate;
    }

    public void setSavDate(String savDate) {
        this.savDate = savDate;
    }

    public String getSavId() {
        return savId;
    }

    public void setSavId(String savId) {
        this.savId = savId;
    }

    public String getSavNotes() {
        return savNotes;
    }

    public void setSavNotes(String savNotes) {
        this.savNotes = savNotes;
    }

    public int getSavingsAmt() {
        return savingsAmt;
    }

    public void setSavingsAmt(int savingsAmt) {
        this.savingsAmt = savingsAmt;
    }

    public int getSavMonth() {
        return savMonth;
    }

    public void setSavMonth(int savMonth) {
        this.savMonth = savMonth;
    }
}
