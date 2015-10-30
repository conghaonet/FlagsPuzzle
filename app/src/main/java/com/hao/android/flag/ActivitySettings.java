package com.hao.android.flag;

import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import org.hh.flag.R;

public class ActivitySettings extends Activity {
	private MyApp myApp;
	private Properties propFile;
	private CheckBox soundCheckBox;
	private CheckBox vibrateCheckBox;
	private List<EntityLanguage> listLanguage;
	private Spinner languageSpinner;
	private ArrayAdapter<String> arrLangAdapter;
	private CheckBox englishCheckBox;
	private CheckBox userLangCheckBox;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.myApp = (MyApp)getApplicationContext();
		setContentView(R.layout.settings);
		soundCheckBox = (CheckBox)findViewById(R.id.settingsSoundCheckBox);
		vibrateCheckBox = (CheckBox)findViewById(R.id.settingsVibrateCheckBox);
		Button saveBtn = (Button)findViewById(R.id.settingsSaveBtn);
		Button backBtn = (Button)findViewById(R.id.settingsBackBtn);

//		RadioGroup radioGroup = (RadioGroup)findViewById(R.id.freeplayRadioGroup);
//		f2nRadio = (RadioButton)findViewById(R.id.settingsF2NRadio);
//		n2fRadio = (RadioButton)findViewById(R.id.settingsN2FRadio);
		englishCheckBox = (CheckBox)findViewById(R.id.settingsLangEngCheckBox);
		userLangCheckBox = (CheckBox)findViewById(R.id.settingsLangYoursCheckBox);

		languageSpinner = (Spinner)findViewById(R.id.settingsSpinner);
		loadPropFile();

		saveBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				savePropFile();
				goPreviousActivity();
			}
		});
		backBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				goPreviousActivity();
			}
		});
		userLangCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					popupNoticeMsg();
					ActivitySettings.this.languageSpinner.setEnabled(true);
				} else {
					ActivitySettings.this.languageSpinner.setEnabled(false);
					if(!ActivitySettings.this.englishCheckBox.isChecked()) ActivitySettings.this.englishCheckBox.setChecked(true);
				}

			}

		});

	}

	private void savePropFile() {
		this.propFile.setProperty(MyApp.PROPKEY_SETTINGS_SOUND, String.valueOf(this.soundCheckBox.isChecked()));
		this.propFile.setProperty(MyApp.PROPKEY_SETTINGS_VIBRATE, String.valueOf(this.vibrateCheckBox.isChecked()));
//		if(this.f2nRadio.isChecked())
//			this.propFile.setProperty(MyApplication.PROPKEY_SETTINGS_QUIZMODE, MyApplication.PROPVALUE_SETTINGS_QUIZMODE_F2N);
//		else this.propFile.setProperty(MyApplication.PROPKEY_SETTINGS_QUIZMODE, MyApplication.PROPVALUE_SETTINGS_QUIZMODE_N2F);
		this.propFile.setProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_ENG, String.valueOf(this.englishCheckBox.isChecked()));
		this.propFile.setProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_USER_LANG, String.valueOf(this.userLangCheckBox.isChecked()));
		if(this.userLangCheckBox.isChecked()) {
			this.propFile.setProperty(MyApp.PROPKEY_SETTINGS_USER_LANG, this.listLanguage.get(this.languageSpinner.getSelectedItemPosition()).getCode());
		}

		this.myApp.saveSettingsPropFile(this.propFile);
	}
	private void loadPropFile() {
		this.propFile = this.myApp.getSettingsPropFile();
		this.listLanguage = this.myApp.getAvailableLanguages();
		this.arrLangAdapter = new ArrayAdapter<String>(ActivitySettings.this, android.R.layout.simple_spinner_item);
		for(int i=0;i<this.listLanguage.size();i++) {
			//For test
//			if(this.listLanguage.get(i).getCode().equalsIgnoreCase("ARABIC")) {
//				String strTemp = this.listLanguage.get(i).getLocal();
//				Log.d(this.getClass().getName(), "========="+strTemp+"==========");
//			}
			this.arrLangAdapter.add(this.listLanguage.get(i).getLocal()+" [ "+this.listLanguage.get(i).getCode().toUpperCase()+" ]");
		}
		this.arrLangAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.languageSpinner.setAdapter(this.arrLangAdapter);
		this.languageSpinner.setPrompt(getResources().getString(R.string.settings_spinner_prompt));
		String userLangCode = propFile.getProperty(MyApp.PROPKEY_SETTINGS_USER_LANG);
		if(userLangCode != null) {
			for(int i=0;i<this.listLanguage.size();i++) {
				if(this.listLanguage.get(i).getCode().equalsIgnoreCase(userLangCode)) {
					this.languageSpinner.setSelection(i);
					break;
				}
			}
		}

		this.soundCheckBox.setChecked(Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_SOUND)));
		this.vibrateCheckBox.setChecked(Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_VIBRATE)));

//		String quizMode = propFile.getProperty(MyApplication.PROPKEY_SETTINGS_QUIZMODE);
//		if(quizMode.equalsIgnoreCase(MyApplication.PROPVALUE_SETTINGS_QUIZMODE_F2N)) {
//			this.f2nRadio.setChecked(true);
//		} else {
//			this.n2fRadio.setChecked(true);
//		}
		this.englishCheckBox.setChecked(Boolean.valueOf(propFile.getProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_ENG)));
		this.userLangCheckBox.setChecked(Boolean.valueOf(propFile.getProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_USER_LANG)));
		if(!this.englishCheckBox.isChecked() && !this.userLangCheckBox.isChecked()) {
			this.englishCheckBox.setChecked(true);
		}
		if(this.userLangCheckBox.isChecked()) {
			this.languageSpinner.setEnabled(true);
		} else {
			this.languageSpinner.setEnabled(false);
		}

	}
	public void popupNoticeMsg(){
		new AlertDialog.Builder(this)
		.setTitle("Notice")
		.setMessage(getResources().getString(R.string.settings_notice_language_yours))
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();

	}

	private void goPreviousActivity() {
		Intent intent = new Intent();
		intent.setClass(ActivitySettings.this, ActivityMain.class);
		startActivity(intent);
		ActivitySettings.this.finish();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			goPreviousActivity();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
}
