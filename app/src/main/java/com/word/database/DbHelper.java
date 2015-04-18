package com.word.database;

import static com.word.util.ConstantValues.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper{

	private Context context;


	public DbHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
		this.context = context;
	}


	public void createDatabaseFromAsset(){
		File dbFile = context.getDatabasePath(DB_NAME);

		if(!dbFile.exists()){
			getWritableDatabase();
			copyDatabaseFromAsset(dbFile);
		}
	}



	private void copyDatabaseFromAsset(File dbFile) {
		try{
			InputStream inputStream = context.getAssets().open(DB_NAME);
			OutputStream outputStream = new FileOutputStream(dbFile);

			byte[] buffer = new byte[1024];
			while(inputStream.read(buffer)>0){
				outputStream.write(buffer);
			}

			inputStream.close();
			outputStream.flush();
			outputStream.close();

		}catch(IOException e){
			Log.e("","Error while copying database from asset");
		}
	}




	public synchronized ArrayList<String> fetchWord(){
		String query = "SELECT "+KEY_WORD+" FROM "+TABLE_NAME;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();

		ArrayList<String> wordList = new ArrayList<String>();
		while(!cursor.isAfterLast()){
			wordList.add(cursor.getString(0));
			cursor.moveToNext();
		}

		cursor.close();
		db.close();
		return wordList;
	}



	public synchronized String fetchMeaning(String word){
		String meaning = null;

		String query = "SELECT "+KEY_MEANING+" FROM "+TABLE_NAME+" WHERE "+KEY_WORD +" = '"+word+"'";
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.rawQuery(query, null);
		cursor.moveToFirst();
		if(cursor.getCount()>0){
			meaning = cursor.getString(0);
		}
		
		cursor.close();
		db.close();
		return meaning;
	}

	
	public synchronized void updataDictionary(String word, String meaning){
		ContentValues values = new ContentValues();
		values.put(KEY_WORD, word);
		values.put(KEY_MEANING, meaning);
		SQLiteDatabase db = getWritableDatabase();
		db.insert(TABLE_NAME, null, values);
		db.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {}


	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

}
