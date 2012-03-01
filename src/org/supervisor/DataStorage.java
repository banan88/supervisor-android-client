package org.supervisor;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.format.Time;
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
	static final String C_SUPERVISOR = "supervisor";
	static final String C_PENDING_SYNC = "state_changed"; //flaga okreslajaca czy wyslac stan/czas pracy na serwer w trakcie synchronizacji 0/1
	private static final String WORK_TIME_TABLE = "work_time";
	static final String C_WORK_START = "work_start";
	static final String C_WORK_FINISH = "work_finish";
	static final String C_WORK_DATE = "work_date";
	private static final String FTS_TABLE = "text_search";
	
	final DBHelper dbHelper;
	
	private class DBHelper extends SQLiteOpenHelper {
		
		final String TAG = DBHelper.class.getSimpleName();

		
		public DBHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE " + TASK_TABLE + " ( " + C_ID + " integer primary key, " +
				C_NAME + " text, " + C_DESC + " text, " + C_LAT + " real, " + C_LON + " real, " +
				C_STATE + " integer, " + C_CREATION_TIME + " integer, " + C_LAST_MODIFIED + " integer, " +
				C_FINISH_TIME + " integer, " + C_START_TIME + " integer, " + C_VERSION + " integer, " + 
				C_LAST_SYNC + " integer, " + C_PENDING_SYNC + " integer, " + C_SUPERVISOR + " text);";
			db.execSQL(sql);
			Log.d(TAG, "tasks table created");
			
			sql = "CREATE TABLE " + WORK_TIME_TABLE + " ( " + C_WORK_DATE + " integer primary key, " + C_WORK_START + " integer, " + C_PENDING_SYNC + " integer, " +
				C_WORK_FINISH + " integer);";
			db.execSQL(sql);
			Log.d(TAG, "work time table created");
			
			db.execSQL("CREATE VIRTUAL TABLE " + FTS_TABLE + " USING fts3(" + 
					C_ID + ", " + C_STATE + ", " + C_DESC + ", " + C_NAME + ", "  + C_LAST_MODIFIED + ", " + C_SUPERVISOR +");");
			Log.d(TAG, "full text search virtual table created");
		}

		
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			String sql = "DROP TABLE IF EXISTS " + TASK_TABLE + ";";
			db.execSQL(sql);
			sql = "DROP TABLE IF EXISTS " + WORK_TIME_TABLE+ ";";
			db.execSQL(sql);
			sql = "DROP TABLE IF EXISTS " + FTS_TABLE+ ";";
			db.execSQL(sql);
			Log.d(TAG, "tables dropped");
			
			onCreate(db);
		}
		
	}
	
	
	public boolean isEmpty() {
		String sql = "SELECT Count(" + C_ID + ") from " + TASK_TABLE + ";";
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery(sql, null);
		boolean result = false;
		if (c != null){
		    c.moveToFirst();
		    if (c.getInt(0) == 0) 
		      result = true;
		    c.close();
		}
		return result;
	}
	
	
	public DataStorage(Context context) {
		dbHelper = new DBHelper(context);
		Log.d(TAG, "DataStorage initialized");
	}
	
	
	public void close(){
		dbHelper.close();
	}
	
	
	public void insert(ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues ftsVals = new ContentValues(); //stripped values for ftstable
		ftsVals.put(C_ID, values.getAsLong(C_ID));
		ftsVals.put(C_STATE, values.getAsInteger(C_STATE));
		ftsVals.put(C_DESC, values.getAsString(C_DESC));
		ftsVals.put(C_NAME, values.getAsString(C_NAME));
		ftsVals.put(C_LAST_MODIFIED, values.getAsLong(C_LAST_MODIFIED));
		ftsVals.put(C_SUPERVISOR, values.getAsString(C_SUPERVISOR));
		try {
			db.insertOrThrow(TASK_TABLE, null, values);
			db.insert(FTS_TABLE, null, ftsVals);
		} catch (SQLException e) {
			Log.d(TAG, "exception: " + e.getLocalizedMessage());
			Long id = values.getAsLong(C_ID);
			Cursor cursor = db.rawQuery("SELECT version FROM " + TASK_TABLE +
					" WHERE " + C_ID + " = " + Long.toString(id), null);
			if( cursor != null ) {
				Long remote_ver = values.getAsLong(C_VERSION);
				cursor.moveToFirst(); 
				if(cursor.getLong(0) != remote_ver) {
					db.update(TASK_TABLE, values, C_ID + " = " + id, null);
					db.update(FTS_TABLE, ftsVals, C_ID + " = " + id, null);
					Log.d(TAG, "duplicate id, task was updated");
				}
			}
			cursor.close();
		}
	}
	
	
	public Cursor getActiveTasks() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TASK_TABLE, null, C_STATE + " NOT IN (3, 0)", null, null, null, C_STATE + " DESC, " + C_LAST_MODIFIED + " DESC");		
	}
	
	
	public Cursor getTasks(int status) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TASK_TABLE, null, C_STATE + " LIKE " + status, null, null, null, C_LAST_MODIFIED + " DESC");
	}
	
	
	public Task getCurrentTask() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + TASK_TABLE +
				" WHERE " + C_STATE + " = 2", null);
		if(c != null) {
			if(c.moveToFirst()) {
				Log.d(TAG, "move to first true");
				String finish_time, start_time;
				try {
					finish_time = c.getString(8);
				} catch (SQLException e) {
					finish_time = null;
				}
				try {
					start_time = c.getString(9);
				} catch (SQLException e) {
					start_time = null;
				}
				
				Task task = new Task(//
						c.getLong(0),
						c.getString(1),
						c.getString(2),
						Double.parseDouble(c.getString(3)),
						Double.parseDouble(c.getString(4)),
						Integer.parseInt(c.getString(5)),
						c.getString(6),
						c.getString(7),
						finish_time,
						start_time,
						Integer.parseInt(c.getString(10)),
						c.getString(11),
						c.getString(13)
					);
				Log.d(TAG, c.getString(13));
				c.close();
				return task;
			}
		}
		c.close();
		return null;
	}
	
	public Cursor getDoneAndCancelledTasks() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TASK_TABLE, null, C_STATE + " IN (3, 0)", null, null, null, C_LAST_MODIFIED + " DESC");
	}
	
	public Task getTaskById(long id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + TASK_TABLE +
				" WHERE " + C_ID + " = " + Long.toString(id), null);
		if(c != null) {
			if(c.moveToFirst()) {
				String finish_time, start_time;
				try {
					finish_time = c.getString(8);
				} catch (SQLException e) {
					finish_time = null;
				}
				try {
					start_time = c.getString(9);
				} catch (SQLException e) {
					start_time = null;
				}
				
				Task task = new Task(
						c.getLong(0),
						c.getString(1),
						c.getString(2),
						Double.parseDouble(c.getString(3)),
						Double.parseDouble(c.getString(4)),
						Integer.parseInt(c.getString(5)),
						c.getString(6),
						c.getString(7),
						finish_time,
						start_time,
						Integer.parseInt(c.getString(10)),
						c.getString(11),
						c.getString(13)
					);
				Log.d(TAG, c.getString(13));
				c.close();
				return task;
			}
		}
		c.close();
		return null;
	}
	
	
	public Cursor searchArchivedTasks(String keyword) {
		Log.d(TAG, "szukam w archiwum");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String query = "SELECT DISTINCT * FROM " + FTS_TABLE + " WHERE " + 
			C_STATE + " IN (3,0) AND " + FTS_TABLE + " MATCH ? ORDER BY " + C_LAST_MODIFIED + ";";
		return db.rawQuery(query, new String[] { keyword+"*" });
	}
	
	
	public Cursor searchListOfTasks(String keyword) {
		Log.d(TAG, "szukam w liscie zadan");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String query = "SELECT DISTINCT * FROM " + FTS_TABLE + " WHERE " + 
			C_STATE + " NOT IN (3,0) AND " + FTS_TABLE + " MATCH ? ORDER BY " + C_LAST_MODIFIED + ";";
		return db.rawQuery(query, new String[] { keyword+"*" });
	}
	
	
	public Cursor searchAllTasks(String keyword) {
		Log.d(TAG, "szukam wszedzie");
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String query = "SELECT DISTINCT * FROM " + FTS_TABLE + " WHERE " +
			FTS_TABLE + " MATCH ? ORDER BY " + C_LAST_MODIFIED + ";";
		return db.rawQuery(query, new String[] { keyword+"*" });
	}
	
	
	public void taskStarted(long id, Long dateMillis) {
		Log.d("DATASTORAGE START TIME: " , Long.toString(dateMillis));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "UPDATE " + TASK_TABLE + " SET " + C_STATE + " = 2, " + C_START_TIME + " = " + dateMillis + 
			", "+ C_LAST_MODIFIED + " = " + dateMillis + ", " + C_PENDING_SYNC + " = 1 WHERE " + C_ID + " = " + id + ";";
		db.execSQL(sql);
		sql = "UPDATE " + FTS_TABLE + " SET " + C_STATE + " = 2 WHERE " + C_ID + " = " + id + ";";
		db.execSQL(sql);
	}
	
	
	public void taskFinished(long id, Long dateMillis) {
		Log.d("DATASTORAGE FINISH TIME: " , Long.toString(dateMillis));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "UPDATE " + TASK_TABLE + " SET " + C_STATE + " = 3, " + C_FINISH_TIME + " = " + dateMillis + 
			", "+ C_LAST_MODIFIED + " = " + dateMillis + ", " + C_PENDING_SYNC + " = 1 WHERE " + C_ID + " = " + id + ";";
		db.execSQL(sql);
		sql = "UPDATE " + FTS_TABLE + " SET " + C_STATE + " = 3 WHERE " + C_ID + " = " + id + ";";
		db.execSQL(sql);
	}
	
	public Cursor getNonSyncedTasks() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TASK_TABLE, new String[]{C_ID, C_STATE, C_START_TIME, C_FINISH_TIME},
				C_PENDING_SYNC + " = " + 1, null, null, null, null);
	}
	
	public void clearNonSyncedTasks() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Time t = new Time();
		t.setToNow();
		String sql = "UPDATE " + TASK_TABLE + " SET " + C_PENDING_SYNC + " = 0, " + C_LAST_SYNC + " = " + t.toMillis(true) + ";";
		db.execSQL(sql);
	}
	
	public Cursor getNonSyncedWorkTimes() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(WORK_TIME_TABLE, null, C_PENDING_SYNC + " = 1", null, null, null, null);
	}
	
	public void clearNonSyncedWorkTimes() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "UPDATE " + WORK_TIME_TABLE + " SET " + C_PENDING_SYNC + " = 0;";
		db.execSQL(sql);
	}
	
	public void startWork(Integer yyyymmdd, Long dateMillis) { //tworzy dzien i zaczyna prace
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Log.d(TAG, Integer.toString(yyyymmdd));
		String sql = "INSERT INTO " + WORK_TIME_TABLE + " ( " + C_WORK_DATE + ", " + C_PENDING_SYNC + ", "+ C_WORK_START + ") " +
				"VALUES (" + yyyymmdd + ", 1, " + dateMillis + ");";
		Log.d(TAG, sql);
		try {
			db.execSQL(sql);
		} catch (SQLException e) {
			Log.d(TAG, "day duplicate");
		}
		Log.d(TAG, "startWork ");
	}
	
	public void finishWork(Integer yyyymmdd, Long dateMillis) { //konczy prace
		Log.d(TAG, Integer.toString(yyyymmdd));
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String sql = "UPDATE " + WORK_TIME_TABLE + " SET " + C_WORK_FINISH + " = " + dateMillis + 
			", " + C_PENDING_SYNC + " = 1 WHERE " + C_WORK_DATE + " = " + yyyymmdd + ";";
		db.execSQL(sql);
		Log.d(TAG, "finishWork called");
		Log.d(TAG, sql);
	}
	
	public int dayState(int yyyymmdd) { //0 - brak dnia, 1 - przycisk start, 2 - przycisk zablokowany 3- przycisk zakoncz
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		String day = "SELECT * FROM " + WORK_TIME_TABLE + " WHERE " + C_WORK_DATE + " = " + yyyymmdd + ";";
		Cursor c = db.rawQuery(day, null);
		int state = 0;
		if (c.getCount()==0) { //dla tego dnia nie bylo jeszcze czasu pracy
			c.close();
			return state;
		}
		Long start, finish;
		c.moveToFirst();
		start = c.getLong(c.getColumnIndex(C_WORK_START));
		finish = c.getLong(c.getColumnIndex(C_WORK_FINISH));
		Log.d(TAG, "start: " + Long.toString(start) + " finish: " + Long.toString(finish));
		c.close();
		if(start == 0 && finish == 0)
			state = 1;
		if(start != 0 && finish != 0)
			state = 2;
		if(start != 0 && finish == 0)
			state = 3;
		return state;
	}
	
	public Cursor getDay(int yyyymmdd) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(WORK_TIME_TABLE, null, C_WORK_DATE + " = " + yyyymmdd, null, null, null, null);
	}
	
}
