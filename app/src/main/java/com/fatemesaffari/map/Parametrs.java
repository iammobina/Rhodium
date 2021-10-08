package com.fatemesaffari.map;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "parametrs")
public class Parametrs {


    public Parametrs(String plmnname, String plmn, int lac, int cellSig, int cellID, int cellMcc, int cellMnc, int cellPci, int cellTac, int lte_int_rsrq, int lte_int_rsrp, int lte_int_cinr, int lte_int_rssi, int umts_int_ec, int umts_int_rscp, int gsm_int_rxlev, int gsm_int_dbm) {
        this.plmnname = plmnname;
        this.plmn=plmn;
        this.lac=lac;
        this.cellSig = cellSig;
        this.cellID = cellID;
        this.cellMcc = cellMcc;
        this.cellMnc = cellMnc;
        this.cellPci = cellPci;
        this.cellTac = cellTac;
        this.lte_int_rsrq = lte_int_rsrq;
        this.lte_int_rsrp = lte_int_rsrp;
        this.lte_int_cinr = lte_int_cinr;
        this.lte_int_rssi = lte_int_rssi;
        this.umts_int_ec = umts_int_ec;
        this.umts_int_rscp = umts_int_rscp;
        this.gsm_int_rxlev = gsm_int_rxlev;
        this.gsm_int_dbm = gsm_int_dbm;
    }


    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "plmn")
    public String plmn;

    @ColumnInfo(name = "plmnname")
    public String plmnname;

    @ColumnInfo(name = "lac")
    public int lac;


    @ColumnInfo(name = "cellSignal")
    public int cellSig;

    @ColumnInfo(name = "cellID")
    public int cellID;
    @ColumnInfo(name = "cellMcc")
    public int cellMcc;
    @ColumnInfo(name = "cellMnc")
    public int cellMnc;
    @ColumnInfo(name = "cellPci")
    public int cellPci;
    @ColumnInfo(name = "cellTac")
    public int cellTac;


    @ColumnInfo(name = "lte_int_rsrq")
    public int lte_int_rsrq;
    @ColumnInfo(name = "lte_int_rsrp")
    public int lte_int_rsrp;
    @ColumnInfo(name = "lte_int_cinr")
    public int lte_int_cinr;
    @ColumnInfo(name = "lte_int_rssi")
    public int lte_int_rssi;


    @ColumnInfo(name = "umts_int_ec")
    public int umts_int_ec;
    @ColumnInfo(name = "umts_int_rscp")
    public int umts_int_rscp;


    @ColumnInfo(name = "gsm_int_rxlev")
    public int gsm_int_rxlev;
    @ColumnInfo(name = "gsm_int_dbm")
    public int gsm_int_dbm;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCellSig() {
        return cellSig;
    }

    public int getCellID() {
        return cellID;
    }

    public int getCellMcc() {
        return cellMcc;
    }

    public int getCellMnc() {
        return cellMnc;
    }

    public int getCellPci() {
        return cellPci;
    }

    public int getCellTac() {
        return cellTac;
    }

    public int getLte_int_rsrq() {
        return lte_int_rsrq;
    }

    public int getLte_int_rsrp() {
        return lte_int_rsrp;
    }

    public int getLte_int_cinr() {
        return lte_int_cinr;
    }

    public int getLte_int_rssi() {
        return lte_int_rssi;
    }

    public int getUmts_int_ec() {
        return umts_int_ec;
    }

    public int getUmts_int_rscp() {
        return umts_int_rscp;
    }

    public int getGsm_int_rxlev() {
        return gsm_int_rxlev;
    }

    public int getGsm_int_dbm() {
        return gsm_int_dbm;
    }
}