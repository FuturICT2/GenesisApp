package com.example.mcb.genesisapp.Repository.SQLite;

import android.content.Context;
import android.database.SQLException;

/**
 * Android native SQLite functions for Genesis App
 * Created by mcb on 11.02.18.
 */
public interface SQLiteRepo {

    /**
     * Context of Android applciation
     * @return
     */
    public Context getContext();

    /**
     * Open SQLite Database
     * @throws SQLException
     */
    void open() throws SQLException;

    /**
     * Close databaseb
     */
    void close();

}
