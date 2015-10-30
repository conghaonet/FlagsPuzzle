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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityFreeQuizN2F extends Activity {
	private MyApp myApp;
	private Properties propFile;
	private Properties propUserLang;
	private Button nationNameBtn;
	private ImageView correctImg;
	private ImageView wrongImg;
	private ImageButton nameBtn1;
	private ImageButton nameBtn2;
	private ImageButton nameBtn3;
	private ImageButton nameBtn4;
	private TextView questionNoText;
	private List<String> listContinentCode = new ArrayList<String>();
	private List<EntityNation> listNation = new ArrayList<EntityNation>();
	private List<Integer> listRandomIndexForBtn;
//	private List<Integer> listQuestionIndex = new ArrayList<Integer>();
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
//	private static final String TAG = ActivityFreeQuizN2F.class.getName();
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.free_quiz_n2f);
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

		this.nationNameBtn = (Button)findViewById(R.id.freeQuizN2fNationNameBtn);
		this.correctImg = (ImageView)findViewById(R.id.freeQuizN2fCorrectImg);
		this.correctImg.setVisibility(ImageView.GONE);
		this.wrongImg = (ImageView)findViewById(R.id.freeQuizN2fWrongImg);
		this.wrongImg.setVisibility(ImageView.GONE);
		this.questionNoText = (TextView)findViewById(R.id.freeQuizN2fQuestionIndexText);
		this.nameBtn1 = (ImageButton)findViewById(R.id.freeQuizN2fBtn1);
		this.nameBtn2 = (ImageButton)findViewById(R.id.freeQuizN2fBtn2);
		this.nameBtn3 = (ImageButton)findViewById(R.id.freeQuizN2fBtn3);
		this.nameBtn4 = (ImageButton)findViewById(R.id.freeQuizN2fBtn4);
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
				case ActivityFreeQuizN2F.HANDLE_MSG_AFTER_ANSWER:
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
		if(ActivityFreeQuizN2F.this.blnAnswer) {
			if((ActivityFreeQuizN2F.this.currentQuestionIndex + 1) >= ActivityFreeQuizN2F.this.listNation.size()) {
				showFinishedDialog();
			} else {
				ActivityFreeQuizN2F.this.currentQuestionIndex = ActivityFreeQuizN2F.this.currentQuestionIndex + 1;
				ActivityFreeQuizN2F.this.setQuestion();
			}
		}
		ActivityFreeQuizN2F.this.setButtonsClickable(true);

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
		}
		this.blnSound = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_SOUND, String.valueOf(true)));
		this.blnVibrate = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_VIBRATE, String.valueOf(true)));
		if(this.blnVibrate) this.mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if(blnSound) {
			// ?趨?????????y??????,???????????????????????????????????????????
			ActivityFreeQuizN2F.this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
//		this.flagImg.setImageResource(myApp.getFlagResourceId(currentNation.getA2()));
		if(this.blnEngLang && !this.blnUserLang) {
			this.nationNameBtn.setText(currentNation.getName());
		} else if(!this.blnEngLang && this.blnUserLang) {
			String str = this.propUserLang.getProperty(currentNation.getA2());
			if(str == null || str.trim().equalsIgnoreCase("")) str = currentNation.getName();
			this.nationNameBtn.setText(str);
		} else if(this.blnEngLang && this.blnUserLang) {
			this.nationNameBtn.setText(currentNation.getName()+"\n"+this.propUserLang.getProperty(currentNation.getA2()));
		}

		this.listRandomIndexForBtn = this.myApp.randomRank(this.listNation.size(), 4);
		/*
		 * ?ж???????????????????????(??????????????й?????ж????????????????????????????й??????)
		 * ????????????????????????????????List,?????List???????????item??
		 */
		if(!this.listRandomIndexForBtn.contains(this.currentQuestionIndex)) {
			int intI = new Random().nextInt(4);
			this.listRandomIndexForBtn.add(intI, this.currentQuestionIndex);
			this.listRandomIndexForBtn.remove(4);
		}
		this.nameBtn1.setImageResource(this.myApp.getFlagButtonResourceId(this.listNation.get(this.listRandomIndexForBtn.get(0)).getA2()));
		this.nameBtn2.setImageResource(this.myApp.getFlagButtonResourceId(this.listNation.get(this.listRandomIndexForBtn.get(1)).getA2()));
		this.nameBtn3.setImageResource(this.myApp.getFlagButtonResourceId(this.listNation.get(this.listRandomIndexForBtn.get(2)).getA2()));
		this.nameBtn4.setImageResource(this.myApp.getFlagButtonResourceId(this.listNation.get(this.listRandomIndexForBtn.get(3)).getA2()));
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
						ActivityFreeQuizN2F.this.currentQuestionIndex = 0;
						ActivityFreeQuizN2F.this.wrongAnswerAmount = 0;
						ActivityFreeQuizN2F.this.setQuestion();
					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.freeplay_quiz_finished_dialog_btn_challenge,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(ActivityFreeQuizN2F.this, ActivityChallengeTabhost.class);
						startActivity(intent);
						ActivityFreeQuizN2F.this.finish();
					}
				}
			);
		dialogBuilder.setNegativeButton(
				R.string.freeplay_quiz_finished_dialog_btn_back,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent();
						intent.setClass(ActivityFreeQuizN2F.this, ActivityFreePlay.class);
						startActivity(intent);
						ActivityFreeQuizN2F.this.finish();
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
						intent.setClass(ActivityFreeQuizN2F.this, ActivityFreePlay.class);
						startActivity(intent);
						ActivityFreeQuizN2F.this.finish();
					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.dialog_btn_retry,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityFreeQuizN2F.this.currentQuestionIndex = 0;
						ActivityFreeQuizN2F.this.wrongAnswerAmount = 0;
						ActivityFreeQuizN2F.this.setQuestion();
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
			if(ActivityFreeQuizN2F.this.mVibrator != null) {
				if(ActivityFreeQuizN2F.this.blnAnswer) ActivityFreeQuizN2F.this.mVibrator.vibrate(40);
				else ActivityFreeQuizN2F.this.mVibrator.vibrate(120);
//				long[] pattern = {1, 20, 200, 20}; // OFF/ON/OFF/ON...
//				this.mVibrator.vibrate(pattern, -1);
			}
		}
	}

	class FeedBackSoundThread extends Thread {
		public void run() {
			int soundId = 0;
			if(ActivityFreeQuizN2F.this.blnAnswer) soundId = 1;
			else soundId = 2;
			try {
				if(ActivityFreeQuizN2F.this.soundPool != null && ActivityFreeQuizN2F.this.soundPoolMap != null)
					ActivityFreeQuizN2F.this.soundPool.play(ActivityFreeQuizN2F.this.soundPoolMap.get(soundId), ActivityFreeQuizN2F.this.streamVolume, ActivityFreeQuizN2F.this.streamVolume, 1, 0, 1f);
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
			ActivityFreeQuizN2F.this.setButtonsClickable(false);
			verifyAnswer(ActivityFreeQuizN2F.this.listRandomIndexForBtn.get(buttonIndex));

		}
	}
	class AfterAnswerThread extends Thread {
		public void run() {
			try{
				Thread.sleep(300);
			} catch(Exception e) {
				e.printStackTrace();
			}
			ActivityFreeQuizN2F.this.mHandler.sendMessage(ActivityFreeQuizN2F.this.mHandler.obtainMessage(ActivityFreeQuizN2F.HANDLE_MSG_AFTER_ANSWER));
		}
	}
}
