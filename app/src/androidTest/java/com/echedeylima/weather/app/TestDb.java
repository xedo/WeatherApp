package com.echedeylima.weather.app;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.echedeylima.weather.app.data.WeatherDbHelper;

public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void testCreateDb () throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }
}
