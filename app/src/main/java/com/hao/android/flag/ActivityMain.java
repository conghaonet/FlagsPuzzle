package com.hao.android.flag;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import org.hh.flag.R;

public class ActivityMain extends Activity {
//	public static final String TAG = ActivityMain.class.getName();
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		ImageButton freePlayBtn = (ImageButton)findViewById(R.id.mainFreePlayBtn);
		ImageButton challengeBtn = (ImageButton)findViewById(R.id.mainChallengeBtn);
		ImageButton listBtn = (ImageButton)findViewById(R.id.mainListBtn);
		ImageButton settingsBtn = (ImageButton)findViewById(R.id.mainSettingsBtn);
		ImageButton moreBtn = (ImageButton)findViewById(R.id.mainMoreBtn);

		freePlayBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ActivityMain.this, ActivityFreePlay.class);
				startActivity(intent);
				ActivityMain.this.finish();
			}
		});
		challengeBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ActivityMain.this, ActivityChallengeTabhost.class);
				startActivity(intent);
				ActivityMain.this.finish();
			}
		});
		listBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ActivityMain.this, ActivityListContinents.class);
				startActivity(intent);
				ActivityMain.this.finish();
			}
		});
		settingsBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(ActivityMain.this, ActivitySettings.class);
				startActivity(intent);
				ActivityMain.this.finish();
			}
		});
		moreBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(MyApp.MARKET_MORE_GAMES);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});

//		exitBtn.setOnClickListener(new Button.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				exitMyself();
//			}
//		});
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
//			exitMyself();
			showExitDialog();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	private void showExitDialog() {
		Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(R.string.quit_app_dialog_msg);
		dialogBuilder.setTitle(R.string.quit_app_dialog_title);
		dialogBuilder.setPositiveButton(
				R.string.dialog_btn_yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						android.os.Process.killProcess(android.os.Process.myPid());					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.dialog_btn_rating,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = Uri.parse("market://details?id="+getPackageName());
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						startActivity(intent);
					}
				}
			);
		dialogBuilder.setNegativeButton(
				R.string.dialog_btn_no,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}
			);
		dialogBuilder.show();

	}
	public void exitMyself() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}