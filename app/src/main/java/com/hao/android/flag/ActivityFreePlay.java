package com.hao.android.flag;

import java.util.List;
import java.util.Properties;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import org.hh.flag.R;

public class ActivityFreePlay extends Activity {
	private Properties propFile;
	private MyApp myApp;
	private RadioButton radioRandom;
	private RadioButton radioName;
	private LinearLayout contsLayout;
	private TextView totalNationsText;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.freeplay);
		this.myApp = (MyApp)getApplicationContext();
		Button continentsBtn = (Button)findViewById(R.id.freeplaySelectContBtn);
//		RadioGroup radioGroup = (RadioGroup)findViewById(R.id.freeplayRadioGroup);
		radioRandom = (RadioButton)findViewById(R.id.freeplayRandomRadio);
		radioName = (RadioButton)findViewById(R.id.freeplayNameRadio);
		Button f2nBtn = (Button)findViewById(R.id.freeplayF2nBtn);
		Button n2fBtn = (Button)findViewById(R.id.freeplayN2fBtn);
		totalNationsText = (TextView)findViewById(R.id.freeplayTotalNationsText);
		contsLayout = (LinearLayout)findViewById(R.id.freeplayContsLayout);

		loadPropFile();

		continentsBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ActivityFreePlay.this, ActivitySelectContinents.class);
				startActivity(intent);
				ActivityFreePlay.this.finish();
			}
		});
		f2nBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				savePropFile();
				Intent intent = new Intent();
				intent.setClass(ActivityFreePlay.this, ActivityFreeQuizF2N.class);
				startActivity(intent);
				ActivityFreePlay.this.finish();
			}
		});
		n2fBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				savePropFile();
				Intent intent = new Intent();
				intent.setClass(ActivityFreePlay.this, ActivityFreeQuizN2F.class);
				startActivity(intent);
				ActivityFreePlay.this.finish();
			}
		});
	}

	private void savePropFile() {
		if(radioRandom.isChecked()) {
			propFile.setProperty(MyApp.PROPKEY_FREEPLAY_ORDER, MyApp.PROPVALUE_FREEPLAY_ORDER_RANDOM);
		} else {
			propFile.setProperty(MyApp.PROPKEY_FREEPLAY_ORDER, MyApp.PROPVALUE_FREEPLAY_ORDER_A2Z);
		}
		myApp.saveSettingsPropFile(propFile);
	}

	private void loadPropFile() {
		List<EntityContinent> listContinent = myApp.getContinents();
		propFile = myApp.getSettingsPropFile();
		String selectedContsCode = propFile.getProperty(MyApp.PROPKEY_FREEPLAY_CONTINENTS);
		String[] arrSelectedContsCode = selectedContsCode.split(",");
		int intLayoutLoaction = 0;
		int intIncludingNations = 0;
		for(int i=0;i<listContinent.size();i++) {
			EntityContinent continent = listContinent.get(i);
			if(continent.getAmount() > 0) {
				LayoutInflater mInflater = LayoutInflater.from(this);
				View selectedContLayout = mInflater.inflate(R.layout.freeplay_selected_conts, null);
				TextView contNameText = (TextView)selectedContLayout.findViewById(R.id.freeplaySelectedContNameText);
				TextView contAmountText = (TextView)selectedContLayout.findViewById(R.id.freeplaySelectedContAmountText);
				contNameText.setText(continent.getName());
				contAmountText.setText(R.string.freeplay_selected_conts_unselected);
				for(int j=0;j<arrSelectedContsCode.length;j++) {
					if(arrSelectedContsCode[j].equalsIgnoreCase(continent.getCode())) {
						contAmountText.setText(continent.getAmount()+" "+ getResources().getString(R.string.freeplay_selected_conts_amount));
						intIncludingNations = intIncludingNations + continent.getAmount();
						break;
					}
				}
				intLayoutLoaction = intLayoutLoaction + 1;
				this.contsLayout.addView(selectedContLayout, intLayoutLoaction);
			}
		}
		this.totalNationsText.setText(getResources().getString(
				R.string.freeplay_label_total_nations)
				.replaceAll(MyApp.STRING_REPLACE_TAG, ""+intIncludingNations));
		String orderType = propFile.getProperty(MyApp.PROPKEY_FREEPLAY_ORDER);
		if(orderType.equalsIgnoreCase(MyApp.PROPVALUE_FREEPLAY_ORDER_RANDOM)) {
			radioRandom.setChecked(true);
		} else {
			radioName.setChecked(true);
		}
	}
	private void goPreviousActivity() {
		Intent intent = new Intent();
		intent.setClass(ActivityFreePlay.this, ActivityMain.class);
		startActivity(intent);
		ActivityFreePlay.this.finish();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			goPreviousActivity();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public void onDestroy() {
		savePropFile();
		super.onDestroy();
	}

}
