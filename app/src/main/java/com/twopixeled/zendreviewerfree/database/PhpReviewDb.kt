package com.twopixeled.zendreviewerfree.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException
import android.database.sqlite.SQLiteException
import android.widget.Toast
import java.io.File


const val DB_NAME = "php_review"

abstract class PhpReviewDb(ctx: Context) : SQLiteOpenHelper(ctx, DB_NAME, null, 1) {
    protected lateinit var database: SQLiteDatabase
    private val dbFile = ctx.getDatabasePath(DB_NAME).absolutePath
    private val context = ctx

    init {
        if (!databaseExists()) copyDataBase()
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private fun databaseExists(): Boolean {

        var checkDB: SQLiteDatabase? = null

        try {
            val myPath = dbFile
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY)

        } catch (e: SQLiteException) {
            Toast.makeText(
                    context,
                    "First time launch detected. Questions database installed.",
                    Toast.LENGTH_SHORT
            ).show()
        }

        if (checkDB != null) {
            checkDB.close()
        }

        return checkDB != null
    }

    /**
     * This is executed when running the app for first time. This will import the sqlite
     * database to the android device
     */
    @Throws(IOException::class)
    private fun copyDataBase() {
        val myInput = context.assets.open("$DB_NAME.db")
        val outFileName = dbFile
        val buffer = ByteArray(1024)
        var length = myInput.read(buffer)
        val outputDB: FileOutputStream
        val dbDir = dbFile.replace("/$DB_NAME", "")

        File(dbDir).mkdir()
        outputDB = FileOutputStream(outFileName)

        while (length > 0) {
            outputDB.write(buffer, 0, length)
            length = myInput.read(buffer)
        }

        outputDB.flush()
        outputDB.close()
        myInput.close()
    }

    fun open() {
        database = SQLiteDatabase.openDatabase(dbFile, null, SQLiteDatabase.OPEN_READONLY)
    }
}