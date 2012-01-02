package org.supervisor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DataStorage {
	
	private static final String TAG = DataStorage.class.getSimpleName();
	private static final String DB_NAME = "tasks.db";
	private static final int DB_VERSION = 1;
	
	private static final String TASK_TABLE = "tasks";
	static final String C_ID = BaseColumns._ID;
	static final String C_NAME = "name";
	static final String C_DESC = "description";
	static final String C_LAT = "latitude";
	static final String C_LON = "longitude";
	static final String C_STATE = "state";
	static final String C_CREATION_TIME= "creation_time";
	static final String C_LAST_MODIFIED = "last_modified";
	static final String C_FINISH_TIME = "finish_time";
	static final String C_START_TIME = "start_time";
	static final String C_VERSION = "version";
	static final String C_LAST_SYNC = "last_synced";
	
	private static final String MONITORING_TABLE = "user_data";
	static final String C_USERNAME = "username";
	static final String C_PASS = "password";
	
	final DBHelper dbHelper;
	
	private class DBHelper extends SQLiteOpenHelper {
		
		final String TAG = DBHelper.class.getSimpleName();

		public DBHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + TASK_TABLE + " ( " + C_ID + " integer primary key, " +
				C_NAME + " text, " + C_DESC + " text, " + C_LAT + " real, " + C_LON + " real, " +
				C_STATE + " integer, " + C_CREATION_TIME + " text, " + C_LAST_MODIFIED + " text, " +
				C_FINISH_TIME + " text, " + C_START_TIME + " text, " + C_VERSION + " integer, " + 
				C_LAST_SYNC + " text);";
				
			db.execSQL(sql);
			Log.d(TAG, "tasks table created");
			
			sql = "CREATE TABLE " + MONITORING_TABLE + " ( " + C_USERNAME + " text, " + 
				C_PASS + " text);";
			
			db.execSQL(sql);
			Log.d(TAG, "monitoring table created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + TASK_TABLE + ";";
			db.execSQL(sql);
			sql = "DROP TABLE " + MONITORING_TABLE + ";";
			db.execSQL(sql);
			Log.d(TAG, "tables dropped");
			
			onCreate(db);
		}
		
	}
	
	public DataStorage(Context context) {
		dbHelper = new DBHelper(context);
		Log.d(TAG, "DataStorage initialized");
	}
	
	public void close(){
		dbHelper.close();
	}
	
	public Cursor getAllTasks() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TASK_TABLE, null, null, null, null, null, C_CREATION_TIME + " DESC");		
	}
	
	public Cursor getTasks(int status) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TASK_TABLE, null, 
				"WHERE " + C_STATE + " LIKE " + status, null, null, null, C_CREATION_TIME + " DESC");
	}
	
	public void insert(ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertOrThrow(TASK_TABLE, null, values);
		} catch (SQLException e) { 
			Log.d(TAG, "exception: " + e.getMessage());
		} finally {
			db.close();
		}
	}
	
	public Task getTaskById(long id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + TASK_TABLE +
				" WHERE " + C_ID + " = " + Long.toString(id), null);
		if(c != null) {
			if(c.moveToFirst()) {
				String finish_time, start_time;
				try {
					finish_time = c.getString(7);
				} catch (SQLException e) {
					finish_time = null;
				}
				try {
					start_time = c.getString(8);
				} catch (SQLException e) {
					start_time = null;
				}
				
				Task task = new Task(c.getLong(0),
						c.getString(1),
						c.getDouble(2),
						c.getDouble(3),
						c.getInt(4),
						c.getString(5),
						c.getString(6),
						finish_time,
						start_time,
						c.getInt(9),
						c.getString(10));
				return task;
			}
		}
		return null;
	}
	
}
