package com.example.scanner.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class QrHistoryItem(
    val id: Int,
    val value: String,
    val qrType: String,
    val qrImage: String,
    val timestamp: String
)


class QrAllHistoryDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "qr_scanner.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "qr_history"
        const val COLUMN_ID = "id"
        const val COLUMN_VALUE = "qr_value"
        const val COLUMN_TYPE = "qr_type"
        const val COLUMN_IMAGE = "qr_image"
        const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_VALUE TEXT,
                $COLUMN_TYPE TEXT,
                $COLUMN_IMAGE TEXT,
                $COLUMN_TIMESTAMP TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // ✅ Insert QR value with current time
    fun insertQrValue(value: String,type: String,image : String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_VALUE, value)
            put(COLUMN_TYPE, type)
            put(COLUMN_IMAGE, image)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis().toString()) // store as millis
        }
        val result = db.insert(TABLE_NAME, null, contentValues)
        db.close()
        return result != -1L
    }

    // ✅ Fetch all data
    fun getAllQrData(): List<QrHistoryItem> {
        val list = mutableListOf<QrHistoryItem>()
        val db = readableDatabase
        val cursor =
            db.rawQuery("SELECT $COLUMN_ID, $COLUMN_VALUE,$COLUMN_TYPE,$COLUMN_IMAGE, $COLUMN_TIMESTAMP FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val value = cursor.getString(1)
                val type = cursor.getString(2)
                val image = cursor.getString(3)
                val timestamp = cursor.getString(4)
                list.add(QrHistoryItem(id, value, type,image,timestamp))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // ✅ Delete single item
    fun deleteItem(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        db.close()
    }

    // ✅ Clear all data
    fun clearAll() {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        db.close()
    }
}