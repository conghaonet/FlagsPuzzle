package com.hao.android.flag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import org.hh.flag.R;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityFreeQuizF2N extends Activity {
	private MyApp myApp;
	private Properties propFile;
	private Properties propUserLang;
	private ImageView flagImg;
	private ImageView correctImg;
	private ImageView wrongImg;
	private Button nameBtn1;
	private Button nameBtn2;
	private Button nameBtn3;
	private Button nameBtn4;
	private TextView questionNoText;
	private List<String> listContinentCode = new ArrayList<String>();
	private List<EntityNation> listNation = new ArrayList<EntityNation>();
	private List<Integer> listRandomIndexForBtn;
	private String orderType;
	private Vibrator mVibrator;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	private AudioManager audioManager;
	private int streamVolume;
	private int currentQuestionIndex;
	private int wrongAnswerAmount;
	private boolean blnAnswer;
	private boolean blnUserLang;
	private boolean blnEngLang;
	private boolean blnSound;
	private boolean blnVibrate;
	private Handler mHandler;
	private AdView adView;
	private FrameLayout adLayout;
	private static final int HANDLE_MSG_AFTER_ANSWER = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.free_quiz_f2n);
		this.myApp = (MyApp)getApplicationContext();

		adLayout = (FrameLayout)findViewById(R.id.adLayout);
		setBanner();
//		adLayout.setMinimumHeight(myApp.getAdViewMeasureHeight());
//		AdView adBanner = new AdView(this, myApp.getAdSizeForAdmob(), getString(R.string.ad_banner_id));
//		adLayout.addView(adBanner);
//		AdRequest adRequest = new AdRequest();
//		adRequest.setLocation(myApp.getLocation());
//		adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
//		adBanner.loadAd(adRequest);

		this.flagImg = (ImageView)findViewById(R.id.freeQuizF2nFlagImg);
		this.correctImg = (ImageView)findViewById(R.id.freeQuizF2nCorrectImg);
		this.correctImg.setVisibility(ImageView.GONE);
		this.wrongImg = (ImageView)findViewById(R.id.freeQuizF2nWrongImg);
		this.wrongImg.setVisibility(ImageView.GONE);
		this.questionNoText = (TextView)findViewById(R.id.freeQuizF2nQuestionIndexText);
		this.nameBtn1 = (Button)findViewById(R.id.freeQuizF2nBtn1);
		this.nameBtn2 = (Button)findViewById(R.id.freeQuizF2nBtn2);
		this.nameBtn3 = (Button)findViewById(R.id.freeQuizF2nBtn3);
		this.nameBtn4 = (Button)findViewById(R.id.freeQuizF2nBtn4);
		loadPropFile();
		setQuestion();

		this.nameBtn1.setOnClickListener(new MyButtonClickListener(0));
		this.nameBtn2.setOnClickListener(new MyButtonClickListener(1));
		this.nameBtn3.setOnClickListener(new MyButtonClickListener(2));
		this.nameBtn4.setOnClickListener(new MyButtonClickListener(3));

		this.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case ActivityFreeQuizF2N.HANDLE_MSG_AFTER_ANSWER:
					processAfterAnswer();
					break;
				default:
					break;
				}
			}

		};
	}
	public void processAfterAnswer() {
		this.correctImg.setVisibility(ImageView.GONE);
		this.wrongImg.setVisibility(ImageView.GONE);
		if(ActivityFreeQuizF2N.this.blnAnswer) {
			if((ActivityFreeQuizF2N.this.currentQuestionIndex + 1) >= ActivityFreeQuizF2N.this.listNation.size()) {
				showFinishedDialog();
			} else {
				ActivityFreeQuizF2N.this.currentQuestionIndex = ActivityFreeQuizF2N.this.currentQuestionIndex + 1;
				ActivityFreeQuizF2N.this.setQuestion();
			}
		}
		ActivityFreeQuizF2N.this.setButtonsClickable(true);

	}

	private void loadPropFile() {
		propFile = this.myApp.getSettingsPropFile();
		String choseContinentsCode = this.propFile.getProperty(MyApp.PROPKEY_FREEPLAY_CONTINENTS);
		String[] arrChoseContinentsCode = choseContinentsCode.split(",");
		for(int i=0;i<arrChoseContinentsCode.length;i++) {
			this.listContinentCode.add(arrChoseContinentsCode[i]);
		}
		this.orderType = this.propFile.getProperty(MyApp.PROPKEY_FREEPLAY_ORDER, MyApp.PROPVALUE_FREEPLAY_ORDER_RANDOM);

		List<EntityNation> listAllNation = this.myApp.getNations();
		while(!listAllNation.isEmpty()) {
			EntityNation tempNation = listAllNation.remove(0);
			if(this.listContinentCode.contains(tempNation.getContinentCode())) {
				this.listNation.add(tempNation);
			}
		}
		if(this.orderType.equalsIgnoreCase(MyApp.PROPVALUE_FREEPLAY_ORDER_RANDOM)) {
			Collections.shuffle(this.listNation);
		} else {
			this.myApp.sortNationName(listNation);
//			for(int i=0;i<this.listNation.size();i++) {
//				this.listQuestionIndex.add(i);
//			}
		}
		this.blnSound = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_SOUND, String.valueOf(true)));
		this.blnVibrate = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_VIBRATE, String.valueOf(true)));
		if(this.blnVibrate) this.mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if(blnSound) {
			// ?趨?????????y??????,???????????????????????????????????????????
			ActivityFreeQuizF2N.this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			this.audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
			this.streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			this.soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,100);
			this.soundPoolMap = new HashMap<Integer, Integer>();
			this.soundPoolMap.put(1, soundPool.load(this, R.raw.right, 1));
			this.soundPoolMap.put(2, soundPool.load(this, R.raw.wrong, 1));

		}
		this.blnEngLang = Boolean.valueOf(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_ENG));
		this.blnUserLang = Boolean.valueOf(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_USER_LANG));
		if(this.blnUserLang) {
			this.propUserLang = this.myApp.getUserLanguage(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_USER_LANG));
		}

	}
	private void setButtonsClickable(boolean blnEnable) {
		this.nameBtn1.setClickable(blnEnable);
		this.nameBtn2.setClickable(blnEnable);
		this.nameBtn3.setClickable(blnEnable);
		this.nameBtn4.setClickable(blnEnable);
	}
	private void verifyAnswer(int nationIndex) {
		if(nationIndex == this.currentQuestionIndex) {
			this.blnAnswer = true;
		} else {
			this.blnAnswer = false;
			this.wrongAnswerAmount = this.wrongAnswerAmount + 1;
		}
		if(this.blnSound) new FeedBackSoundThread().start();
		if(this.blnVibrate) new FeedBackVibrateThread().start();
		if(this.blnAnswer) {
			this.correctImg.setVisibility(ImageView.VISIBLE);
		} else {
			this.wrongImg.setVisibility(ImageView.VISIBLE);
		}
//		this.flagImg.startAnimation(this.animation);
		new AfterAnswerThread().start();
	}

	private void setQuestion() {
		this.questionNoText.setText((this.currentQuestionIndex+1) + "/" + this.listNation.size());

		listRandomIndexForBtn = new ArrayList<Integer>();
		EntityNation currentNation = this.listNation.get(this.currentQuestionIndex);
		this.flagImg.setImageResource(myApp.getFlagResourceId(currentNation.getA2()));

		this.listRandomIndexForBtn = myApp.randomRank(this.listNation.size(), 4);
		/*
		 * ?ж???????????????????????(??????????????й?????ж????????????????????????????й??????)
		 * ????????????????????????????????List,?????List???????????item??
		 */
		if(!this.listRandomIndexForBtn.contains(this.currentQuestionIndex)) {
			int intI = new Random().nextInt(4);
			this.listRandomIndexForBtn.add(intI, this.currentQuestionIndex);
			this.listRandomIndexForBtn.remove(4);
		}
		String strBtn1 = "";
		String strBtn2 = "";
		String strBtn3 = "";
		String strBtn4 = "";
		EntityNation antionForBtn1 = this.listNation.get(this.listRandomIndexForBtn.get(0));
		EntityNation antionForBtn2 = this.listNation.get(this.listRandomIndexForBtn.get(1));
		EntityNation antionForBtn3 = this.listNation.get(this.listRandomIndexForBtn.get(2));
		EntityNation antionForBtn4 = this.listNation.get(this.listRandomIndexForBtn.get(3));
		if(this.blnEngLang && !this.blnUserLang) {
			strBtn1 = antionForBtn1.getName();
			strBtn2 = antionForBtn2.getName();
			strBtn3 = antionForBtn3.getName();
			strBtn4 = antionForBtn4.getName();
		} else if(!this.blnEngLang && this.blnUserLang) {
			strBtn1 = this.propUserLang.getProperty(antionForBtn1.getA2());
			strBtn2 = this.propUserLang.getProperty(antionForBtn2.getA2());
			strBtn3 = this.propUserLang.getProperty(antionForBtn3.getA2());
			strBtn4 = this.propUserLang.getProperty(antionForBtn4.getA2());
			if(strBtn1 == null || strBtn1.trim().equalsIgnoreCase("")) strBtn1 = antionForBtn1.getName();
			if(strBtn2 == null || strBtn2.trim().equalsIgnoreCase("")) strBtn2 = antionForBtn2.getName();
			if(strBtn3 == null || strBtn3.trim().equalsIgnoreCase("")) strBtn3 = antionForBtn3.getName();
			if(strBtn4 == null || strBtn4.trim().equalsIgnoreCase("")) strBtn4 = antionForBtn4.getName();
		} else if(this.blnEngLang && this.blnUserLang) {
			strBtn1 = antionForBtn1.getName()+"\n"+this.propUserLang.getProperty(antionForBtn1.getA2());
			strBtn2 = antionForBtn2.getName()+"\n"+this.propUserLang.getProperty(antionForBtn2.getA2());
			strBtn3 = antionForBtn3.getName()+"\n"+this.propUserLang.getProperty(antionForBtn3.getA2());
			strBtn4 = antionForBtn4.getName()+"\n"+this.propUserLang.getProperty(antionForBtn4.getA2());
		}
		this.nameBtn1.setText(strBtn1);
		this.nameBtn2.setText(strBtn2);
		this.nameBtn3.setText(strBtn3);
		this.nameBtn4.setText(strBtn4);

	}
	private void showFinishedDialog() {
		Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setCancelable(false);
		String dialogMsg = getResources().getString(R.string.freeplay_quiz_finished_dialog_msg_score);
		int correctPercent = (this.listNation.size() * 100) / (this.wrongAnswerAmount+this.listNation.size());
		dialogMsg = dialogMsg.replaceAll(MyApp.STRING_REPLACE_TAG, String.valueOf(correctPercent));
		if(correctPercent < 70) dialogMsg = dialogMsg +"\n"+ getResources().getString(R.string.freeplay_quiz_finished_dialog_msg_60)+"\n";
		else if(correctPercent >= 70 && correctPercent < 80) dialogMsg = dialogMsg +"\n"+ getResources().getString(R.string.freeplay_quiz_finished_dialog_msg_70)+"\n";
		else if(correctPercent >= 80 && correctPercent < 90) dialogMsg = dialogMsg +"\n"+ getResources().getString(R.string.freeplay_quiz_finished_dialog_msg_80)+"\n";
		else if(correctPercent >=90) dialogMsg = dialogMsg +"\n"+ getResources().getString(R.string.freeplay_quiz_finished_dialog_msg_90)+"\n";

		dialogBuilder.setMessage(dialogMsg);
		dialogBuilder.setPositiveButton(
				R.string.freeplay_quiz_finished_dialog_btn_retry,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityFreeQuizF2N.this.currentQuestionIndex = 0;
						ActivityFreeQuizF2N.this.wrongAnswerAmount = 0;
						ActivityFreeQuizF2N.this.setQuestion();
					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.freeplay_quiz_finished_dialog_btn_challenge,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(ActivityFreeQuizF2N.this, ActivityChallengeTabhost.class);
						startActivity(intent);
						ActivityFreeQuizF2N.this.finish();
					}
				}
			);
		dialogBuilder.setNegativeButton(
				R.string.freeplay_quiz_finished_dialog_btn_back,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(ActivityFreeQuizF2N.this, ActivityFreePlay.class);
						startActivity(intent);
						ActivityFreeQuizF2N.this.finish();
					}
				}
			);
		dialogBuilder.show();
	}

	private void showExitDialog() {
		Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(R.string.quit_game_dialog_msg);
		dialogBuilder.setTitle(R.string.quit_game_dialog_title);
		dialogBuilder.setPositiveButton(
				R.string.dialog_btn_yes,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(ActivityFreeQuizF2N.this, ActivityFreePlay.class);
						startActivity(intent);
						ActivityFreeQuizF2N.this.finish();
					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.dialog_btn_retry,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityFreeQuizF2N.this.currentQuestionIndex = 0;
						ActivityFreeQuizF2N.this.wrongAnswerAmount = 0;
						ActivityFreeQuizF2N.this.setQuestion();
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
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override
	public void onBackPressed() {
		showExitDialog();
	}
	private void setBanner() {
		if(adView != null) adLayout.removeView(adView);
		try {
			adView = new AdView(this);
			adView.setAdSize(myApp.getAdSizeForAdmob());
			adView.setAdUnitId(AppPrefUtil.getAdBannerId(this, null));
//			adView.setBackgroundResource(R.drawable.bg_ad_bmp);
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(myApp.getAdViewMeasureWidth(), myApp.getAdViewMeasureHeight());
			params.gravity = Gravity.CENTER;
			adLayout.addView(adView, params);
			AdRequest.Builder builder = new AdRequest.Builder();
//			builder.addTestDevice("96EDE742C567059B15AEE8871B8A9B21");//nexus5
			if(AppTools.isLoadAds()) adView.loadAd(builder.build());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	class FeedBackVibrateThread extends Thread {
		public void run() {
			if(ActivityFreeQuizF2N.this.mVibrator != null) {
				if(ActivityFreeQuizF2N.this.blnAnswer) ActivityFreeQuizF2N.this.mVibrator.vibrate(40);
				else ActivityFreeQuizF2N.this.mVibrator.vibrate(120);
//				long[] pattern = {1, 20, 200, 20}; // OFF/ON/OFF/ON...
//				this.mVibrator.vibrate(pattern, -1);
			}
		}
	}

	class FeedBackSoundThread extends Thread {
		public void run() {
			int soundId = 0;
			if(ActivityFreeQuizF2N.this.blnAnswer) soundId = 1;
			else soundId = 2;
			try {
				if(ActivityFreeQuizF2N.this.soundPool != null && ActivityFreeQuizF2N.this.soundPoolMap != null)
					ActivityFreeQuizF2N.this.soundPool.play(ActivityFreeQuizF2N.this.soundPoolMap.get(soundId), ActivityFreeQuizF2N.this.streamVolume, ActivityFreeQuizF2N.this.streamVolume, 1, 0, 1f);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	class MyButtonClickListener implements OnClickListener {
		private int buttonIndex;
		public MyButtonClickListener(int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}
		@Override
		public void onClick(View v) {
//			Log.d(TAG, "**************Button.height="+v.getHeight()+" weight="+v.getWidth());
			ActivityFreeQuizF2N.this.setButtonsClickable(false);
			verifyAnswer(ActivityFreeQuizF2N.this.listRandomIndexForBtn.get(buttonIndex));

		}
	}
	class AfterAnswerThread extends Thread {
		public void run() {
			try{
				Thread.sleep(300);
			} catch(Exception e) {
				e.printStackTrace();
			}
			ActivityFreeQuizF2N.this.mHandler.sendMessage(ActivityFreeQuizF2N.this.mHandler.obtainMessage(ActivityFreeQuizF2N.HANDLE_MSG_AFTER_ANSWER));
		}
	}
}
