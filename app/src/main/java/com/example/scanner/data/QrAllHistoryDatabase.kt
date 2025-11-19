package com.example.scanner.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class QrAllHistoryDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "qr_scanner.db"
        private const val DATABASE_VERSION = 5
        const val TABLE_NAME = "qr_history"
        const val COLUMN_ID = "id"
        const val COLUMN_VALUE = "qr_value"
        const val COLUMN_TYPE = "qr_type"
        const val COLUMN_IMAGE = "qr_image"
        const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_JSON = "qr_data_json"

        const val COLUMN_FAVORITE = "is_favorite"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VALUE TEXT,
                $COLUMN_TYPE TEXT,
                $COLUMN_IMAGE TEXT,
                $COLUMN_TIMESTAMP TEXT,
                $COLUMN_JSON TEXT,
                $COLUMN_FAVORITE INTEGER DEFAULT 0
                
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_FAVORITE INTEGER DEFAULT 0")
        }
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_JSON TEXT")
        }
    }

    // ✅ Insert QR value with current time
    fun insertQrValue(value: String,type: String,image : String,detailsJson: String): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_VALUE, value)
            put(COLUMN_TYPE, type)
            put(COLUMN_IMAGE, image)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis().toString())
            put(COLUMN_JSON, detailsJson)
            put(COLUMN_FAVORITE, 0)
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result
    }

    fun deleteItem(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
    }

    // ✅ Fetch all data
    fun getAllQrData(): List<QrHistoryItem> {
        val list = mutableListOf<QrHistoryItem>()
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT $COLUMN_ID, $COLUMN_VALUE,$COLUMN_TYPE,$COLUMN_IMAGE, $COLUMN_TIMESTAMP, $COLUMN_JSON,$COLUMN_FAVORITE FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getLong(0) // <- Long
                val value = cursor.getString(1)
                val type = cursor.getString(2)
                val image = cursor.getString(3)
                val timestamp = cursor.getString(4)
                val jsonData = cursor.getString(5)
                val isFavorite = cursor.getInt(6) == 1
                list.add(QrHistoryItem(id, value, type, image, timestamp, jsonData, isFavorite))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COLUMN_FAVORITE, if (isFavorite) 1 else 0) }
        val result = db.update(TABLE_NAME, cv, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
        return result > 0
    }

    fun getItemById(id: Long): QrHistoryItem? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COLUMN_ID, $COLUMN_VALUE, $COLUMN_TYPE, $COLUMN_IMAGE, $COLUMN_TIMESTAMP, $COLUMN_JSON, $COLUMN_FAVORITE FROM $TABLE_NAME WHERE $COLUMN_ID = ?",
            arrayOf(id.toString())
        )

        var item: QrHistoryItem? = null
        if (cursor.moveToFirst()) {
            item = QrHistoryItem(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6) == 1
            )
        }
        cursor.close()
        db.close()
        return item
    }



    // ✅ Clear all data
    fun clearAll() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}