package gal.cifpacarballeira.unidad4_tarea7gestordeberes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class HomeworkDB extends SQLiteOpenHelper {
    private static final String DBname = "HomeworkDB";
    private static int DBversion = 1;

    public HomeworkDB(@Nullable Context context) {
        super(context, DBname, null, DBversion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE homework (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "subject TEXT, " +
                "description TEXT, " +
                "duedate TEXT, " +
                "completed INTEGER)";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP table if exists homework");
        onCreate(sqLiteDatabase);
    }
}
