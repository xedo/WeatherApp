/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.echedeylima.weather.app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import com.echedeylima.weather.app.data.WeatherContract.LocationEntry;
import com.echedeylima.weather.app.data.WeatherContract.WeatherEntry;
import com.echedeylima.weather.app.data.WeatherDbHelper;

import java.util.Map;
import java.util.Set;

public class TestProvider extends AndroidTestCase {

    public static final String TAG = TestProvider.class.getSimpleName();

    public static String TEST_CITY_NAME = "North Pole";
    public static String TEST_LOCATION = "99705";
    public static String TEST_DATE = "20141205";

    public static ContentValues getLocationContentValues() {
        ContentValues contentValues = new ContentValues();
        String testLocationSetting = TEST_LOCATION;
        double testLatitude = 64.772;
        double testLongitude = -147.355;
        contentValues.put(LocationEntry.COLUMN_CITY_NAME, TEST_CITY_NAME);
        contentValues.put(LocationEntry.COLUMN_LOCATION_SETTING, testLocationSetting);
        contentValues.put(LocationEntry.COLUMN_COORD_LAT, testLatitude);
        contentValues.put(LocationEntry.COLUMN_COORD_LONG, testLongitude);
        return contentValues;
    }

    public static ContentValues getWeatherContentValues(long locationRowId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        contentValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        contentValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        contentValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        contentValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        contentValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        contentValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        contentValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        contentValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        contentValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return contentValues;
    }

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public static void validateCursor(ContentValues contentValues, Cursor cursor) {
        Set<Map.Entry<String, Object>> valueSet = contentValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = cursor.getColumnIndex(columnName);
            assertFalse(-1 == idx);
            String expectateValue = entry.getValue().toString();
            assertEquals(expectateValue, cursor.getString(idx));
        }
    }

    public void testInsertReadProvider() {

        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestDb.createNorthPoleLocationValues();

        long locationRowId;
        locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue(locationRowId != -1);
        Log.d(TAG, "New row id: " + locationRowId);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(LocationEntry.TABLE_NAME,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        if (cursor.moveToFirst()) {
            TestDb.validateCursor(cursor, testValues);
        }

        // A cursor is your primary interface to the query results.
        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (cursor.moveToFirst()) {
            TestDb.validateCursor(cursor, testValues);
        }

        // Now see if we can successfully query if we include the row id
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        if (cursor.moveToFirst()) {
            TestDb.validateCursor(cursor, testValues);
        }

        // Fantastic.  Now that we have a location, add some weather!
        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);

        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        // A cursor is your primary interface to the query results.
        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI,
                null, null, null, null);

        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather data for a specific date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(TEST_LOCATION, TEST_DATE),
                null,
                null,
                null,
                null
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        dbHelper.close();
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocation
                (testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(WeatherEntry.buildWeatherLocationWithDate
                (testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }
}