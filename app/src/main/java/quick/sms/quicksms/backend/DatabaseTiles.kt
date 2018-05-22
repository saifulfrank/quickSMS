package quick.sms.quicksms.backend

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLException

class DatabaseTiles(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table $TABLE_NAME (recipient_id LONG,tileid INTEGER PRIMARY KEY, prefered_number INTEGER)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertData(recipient_id: Long, tileid: Int, prefered_number: Int) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            val contentValues = ContentValues()
            contentValues.put(COL_1, recipient_id)
            contentValues.put(COL_2, tileid)
            contentValues.put(COL_3, prefered_number)
            val a = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
            if (a == (-1).toLong()) {
                contentValues.put(COL_1, recipient_id)
                contentValues.put(COL_2, tileid)
                contentValues.put(COL_3, prefered_number)
                db.update(TABLE_NAME, contentValues, "tileid = ?", arrayOf(tileid.toString()))
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {

        } finally {
            db.endTransaction()
        }
    }


    fun tileDefragmentator(deletedTile: Int) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            //Code to do the deed
            db.rawQuery("select * from $TABLE_NAME", null).use {
                while (it.moveToNext()) {
                    println(">>>>>> it index" + it.getInt(it.getColumnIndex("tileid")))
                    println("true or false"+(it.getInt(it.getColumnIndex("tileid")) > deletedTile))
                    if (it.getInt(it.getColumnIndex("tileid")) > deletedTile) {
                        val contentValues = ContentValues()
                        println(">>>tileToMove" + it.getInt(it.getColumnIndex("tileid")))
                        contentValues.put(COL_1, it.getLong(it.getColumnIndex("recipient_id")))
                        contentValues.put(COL_2, it.getInt(it.getColumnIndex("tileid")) - 1)
                        contentValues.put(COL_3, it.getInt(it.getColumnIndex("prefered_number")))
                        val a = db.insertWithOnConflict(TABLE_NAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
                        if (a == (-1).toLong()) {
                            println("in if claws")
                            contentValues.put(COL_1, it.getLong(it.getColumnIndex("recipient_id")))
                            contentValues.put(COL_2, (it.getInt(it.getColumnIndex("tileid")) - 1))
                            contentValues.put(COL_3, it.getInt(it.getColumnIndex("prefered_number")))
                            println(">>>>>>>asdadfasd"+(it.getInt(it.getColumnIndex("tileid")) - 1))
                            val a = db.updateWithOnConflict(TABLE_NAME, contentValues, "tileid = ?", arrayOf((it.getInt(it.getColumnIndex("tileid")) - 1).toString()), SQLiteDatabase.CONFLICT_IGNORE)
                            println(">>>> $a")
                        }
                        db.delete(TABLE_NAME, "tileid = ?", arrayOf(it.getInt(it.getColumnIndex("tileid")).toString()))
                    }
                }
            }
            db.setTransactionSuccessful()
        } catch (e: SQLException) {

        } finally {
            db.endTransaction()
        }
    }

    private fun getRecipient(tileid: Int): Long? {
        val db = this.writableDatabase
        db.rawQuery("select * from $TABLE_NAME", null).use {
            while (it.moveToNext()) {
                if (tileid.toString() == it.getString(it.getColumnIndex("tileid"))) {
                    return it.getLong(it.getColumnIndex("recipient_id"))
                }
            }
        }
        return null
    }

    fun getAllTiles(): Map<Long, Int> {
        val db = this.writableDatabase
        db.rawQuery("select * from $TABLE_NAME", null).use {
            val tiles = linkedMapOf<Long, Int>()
            while (it.moveToNext()) {
                tiles[it.getLong(it.getColumnIndex("recipient_id"))] = it.getInt(it.getColumnIndex("tileid"))
            }
            return tiles
        }
    }

    fun getTile(recipient_id: Long): Int {
        val db = this.writableDatabase
        db.rawQuery("select * from $TABLE_NAME", null).use {
            while (it.moveToNext()) {
                if (recipient_id.toString() == it.getString(it.getColumnIndex("recipient_id"))) {
                    return it.getInt(it.getColumnIndex("tileid"))
                }
            }
        }
        return -1 //If not in database return -1
    }

    fun deleteTile(tileid: Int) {
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.delete(TABLE_NAME, "tileid = ?", arrayOf(tileid.toString()))
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            // do some error handling
        } finally {
            db.endTransaction()
        }
    }

    fun deleteEntireDB() {
        //This function will delete the entire database
        val db = this.writableDatabase
        try {
            db.beginTransaction()
            db.delete(TABLE_NAME, null, null)
            db.setTransactionSuccessful()
        } catch (e: SQLException) {
            // do some error handling
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val DATABASE_NAME = "TilesMapping.db"
        private const val TABLE_NAME = "TilesMappingTable"
        private const val COL_1 = "recipient_id"
        private const val COL_2 = "tileid"
        private const val COL_3 = "prefered_number"
    }
}