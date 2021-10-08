package com.fatemesaffari.map;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApplicationDao {

    @Query("SELECT * FROM Parametrs")
    List<Parametrs> getAll();

    @Query("DELETE FROM Parametrs")
    void deleteAll();

    @Insert
    void insertAll(Parametrs... parametrs);
}