package com.word.dictionary;


import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.word.database.DbHelper;


public class MainActivity extends Activity implements OnClickListener{

	private AutoCompleteTextView searchView;
	private Dialog updateDialog;
	private TextView wordView;
	private TextView meaningView;
	private TextView wordToUpdateView;
	private TextView meaningToUpdateView;
	private DbHelper dbHelper;

	private ArrayList<String> wordList = new ArrayList<String>();
	private ArrayAdapter<String> searchAdapter;

	private static final byte LOAD_WORDS = 1;
	private static final byte LOAD_MEANING = 2;
	private static final byte UPDATE_WORD = 3;

	private static final String KEY_INSTANCE_WORD = "instance_word";
	private static final String KEY_INSTANCE_MEANING = "instance_meaning";
	private static final String KEY_INSTANCE_DIALOG = "instance_dialog";

	private String word;
	private String meaning;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbHelper = new DbHelper(this);
		findIds();
		initDialog();

		new DbLoader().execute(LOAD_WORDS);
	}



	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(KEY_INSTANCE_WORD,word);
		outState.putString(KEY_INSTANCE_MEANING, meaning);
		outState.putBoolean(KEY_INSTANCE_DIALOG, updateDialog.isShowing());
		super.onSaveInstanceState(outState);
	}




	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		word = savedInstanceState.getString(KEY_INSTANCE_WORD);
		meaning = savedInstanceState.getString(KEY_INSTANCE_MEANING);

		boolean isDialogShowing = savedInstanceState.getBoolean(KEY_INSTANCE_DIALOG);
		if(isDialogShowing){
			wordToUpdateView.setText(word);
			meaningToUpdateView.setText(meaning);
			updateDialog.show();
		}else{
			if(meaning!=null&&!meaning.trim().equals("")){
				wordView.setText(word);
				meaningView.setText(meaning);
			}
		}
	}


	private void initDialog() {
		updateDialog = new Dialog(this);
		updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		updateDialog.setContentView(R.layout.dialog);
		wordToUpdateView = (TextView) updateDialog.findViewById(R.id.wordToUpdate);
		meaningToUpdateView = (TextView) updateDialog.findViewById(R.id.meaningToUpdate);
		((Button) updateDialog.findViewById(R.id.updateBtn)).setOnClickListener(this);
	}



	private void findIds() {

		searchView = (AutoCompleteTextView)findViewById(R.id.wordToSearchView);
		wordView = (TextView)findViewById(R.id.wordTextView);
		meaningView = (TextView)findViewById(R.id.meaningTextView);

		searchAdapter = new ArrayAdapter<String>(this, R.layout.custom_spinner, R.id.custom_spinner_text, wordList);
		searchView.setAdapter(searchAdapter);
		searchView.setThreshold(1);
		((Button) findViewById(R.id.searchButton)).setOnClickListener(this);
	}



	private class DbLoader extends AsyncTask<Byte, Void, Byte>{


		@Override
		protected Byte doInBackground(Byte... params) {

			byte tag = params[0];

			if(tag == LOAD_WORDS){

				wordList.clear();
				wordList.addAll(dbHelper.fetchWord());
				searchAdapter.notifyDataSetChanged();

			}else if(tag == LOAD_MEANING){

				meaning = dbHelper.fetchMeaning(word);

			}else if(tag == UPDATE_WORD){

				String word = wordToUpdateView.getText().toString();
				String meaning = meaningToUpdateView.getText().toString();
				dbHelper.updataDictionary(word, meaning);
				wordList.clear();
				wordList.addAll(dbHelper.fetchWord());
				searchAdapter.notifyDataSetChanged();
			}

			return tag;
		}


		@Override
		protected void onPostExecute(Byte result) {

			byte tag = result;

			if(tag == LOAD_MEANING){

				if(meaning==null){
					wordToUpdateView.setText(word);
					updateDialog.show();
				}else{
					wordView.setText(word);
					meaningView.setText(meaning);
				}

			}
			super.onPostExecute(result);
		}

	}



	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.searchButton:

			word = String.valueOf(searchView.getText());

			if(word!=null&&!word.trim().equals("")){
				new DbLoader().execute(LOAD_MEANING);				 
			}else{
				Toast.makeText(this, "No word to search", Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.updateBtn:

			word = String.valueOf(wordToUpdateView.getText());
			meaning = String.valueOf(meaningToUpdateView.getText());

			if(word!=null&&!word.trim().equals("")&&meaning!=null&&!meaning.trim().equals("")){
				new DbLoader().execute(UPDATE_WORD);
				updateDialog.hide();
			}else{
				Toast.makeText(MainActivity.this, "please fill all the fields", Toast.LENGTH_SHORT).show();
			}

			break;
		}

	}



	@Override
	protected void onDestroy() {
		if(updateDialog!=null){
			updateDialog.cancel();
			updateDialog.dismiss();
			updateDialog = null;
		}

		if(dbHelper!=null){
			dbHelper.close();
			dbHelper = null;
		}
		super.onDestroy();
	}


}
