package com.fatemesaffari.map;
import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Parametrs.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ApplicationDao globalDao();

    private static volatile AppDatabase parameterInstance;

    static AppDatabase getDatabase(final Context context)
    {
        if (parameterInstance == null){
            synchronized ( (AppDatabase.class)){
                if(parameterInstance ==null){
                parameterInstance = Room.databaseBuilder(context.getApplicationContext(),
                AppDatabase.class,"parametrs").build();
                }
            }
        }
        return parameterInstance;
    }
}