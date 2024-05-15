package or.nevet.cloudMess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import or.nevet.cloudMess.MesSqLite.*;
import androidx.annotation.Nullable;

public class MesSqLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Messes.db";
    public static final int DATABASE_VERSION = 1;

    public MesSqLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MESSES_TABLE = "CREATE TABLE " +
                MesSqLitEntry.TABLE_NAME + " (" +
                MesSqLitEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MesSqLitEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MesSqLitEntry.COLUMN_MESS + " TEXT NOT NULL, " +
                MesSqLitEntry.COLUMN_TIME + " INTEGER NOT NULL, " +
                MesSqLitEntry.COLUMN_REPLY + " INTEGER NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_MESSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MesSqLitEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
