package com.word.dictionary;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.word.database.DbHelper;

public class SplashScreen extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_screen);

		new DatabaseLoader().execute();
	}
	
	
	private class DatabaseLoader extends AsyncTask<Void, Void, Void>{
		
	    private DbHelper dbHelper;
	    
		@Override
		protected Void doInBackground(Void... params) {
			dbHelper = new DbHelper(SplashScreen.this);
			dbHelper.createDatabaseFromAsset();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			
			dbHelper.close();
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					Intent intent = new Intent(SplashScreen.this, MainActivity.class);
					startActivity(intent);	
					finish();
				}
			}, 1000);
			super.onPostExecute(result);
		}
		
	}

}
