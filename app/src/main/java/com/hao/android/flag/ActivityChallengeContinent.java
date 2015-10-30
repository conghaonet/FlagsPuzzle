package com.hao.android.flag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

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
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RatingBar;
import android.widget.TextView;

public class ActivityChallengeContinent extends Activity {
	private Properties propFile;
	private MyApp myApp;
	private ImageView flagImg;
	private ImageView correctImg;
	private ImageView wrongImg;
	private Button nationNameBtn;
	private Button nameBtn1;
	private Button nameBtn2;
	private Button nameBtn3;
	private Button nameBtn4;
	private Button nameBtn5;
	private Button nameBtn6;
	private TextView questionIndexText;
	private TextView levelIndexText;
	private TextView scoreText;
	private List<EntityNation> listAllNation;
	private List<EntityNation> listNation;
	private List<EntityContinent> listContinent;
	private boolean blnSound;
	private boolean blnVibrate;
	private int currentQuestionIndex = 0;
	private Vibrator mVibrator;
	private SoundPool soundPool;
	private HashMap<Integer, Integer> soundPoolMap;
	private AudioManager audioManager;
	private int streamVolume;
	private boolean blnAnswer;
	private Properties propUserLang;
	private boolean blnUserLang;
	private boolean blnEngLang;
	private int levelIndex;
	private Handler mHandler;
	private int correctAnswerAmout;
	private PopupWindow popupWindow;
	private int lastScore;
	private AdView adView;
	private FrameLayout adLayout;
	private static final int HANDLE_MSG_AFTER_ANSWER = 1;
	private static final String CHALLENGE_MODE = MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge_continent);
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

		Bundle bundle = this.getIntent().getExtras();
		this.levelIndex = bundle.getInt("levelIndex");
		this.flagImg = (ImageView)findViewById(R.id.challengeConFlagImg);
		this.correctImg = (ImageView)findViewById(R.id.challengeConCorrectImg);
		this.correctImg.setVisibility(ImageView.GONE);
		this.wrongImg = (ImageView)findViewById(R.id.challengeConWrongImg);
		this.wrongImg.setVisibility(ImageView.GONE);
		this.levelIndexText = (TextView)findViewById(R.id.challengeConLevelIndexText);
		this.questionIndexText = (TextView)findViewById(R.id.challengeConQuestionIndexText);
		this.scoreText = (TextView)findViewById(R.id.challengeConScoreText);
		this.nationNameBtn = (Button)findViewById(R.id.challengeConNationNameBtn);
		this.nameBtn1 = (Button)findViewById(R.id.challengeConBtn1);
		this.nameBtn2 = (Button)findViewById(R.id.challengeConBtn2);
		this.nameBtn3 = (Button)findViewById(R.id.challengeConBtn3);
		this.nameBtn4 = (Button)findViewById(R.id.challengeConBtn4);
		this.nameBtn5 = (Button)findViewById(R.id.challengeConBtn5);
		this.nameBtn6 = (Button)findViewById(R.id.challengeConBtn6);

		loadPropFile();
		resetUI();
		setQuestion();


		this.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case ActivityChallengeContinent.HANDLE_MSG_AFTER_ANSWER:
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
		if((this.currentQuestionIndex + 1) >= this.listNation.size()) {
			Properties scoresProp = this.myApp.getScoresPropFile(ActivityChallengeContinent.CHALLENGE_MODE);
			this.lastScore = Integer.valueOf(scoresProp.getProperty(MyApp.PROPKEY_SCORE_LEVELE+this.levelIndex, "-1"));
			if(this.myApp.convertCorrect2Score(this.correctAnswerAmout) > lastScore) {
				int score = this.myApp.convertCorrect2Score(ActivityChallengeContinent.this.correctAnswerAmout);
				scoresProp.setProperty(MyApp.PROPKEY_SCORE_LEVELE+this.levelIndex, String.valueOf(score));
				this.myApp.saveScoresPropFile(scoresProp, ActivityChallengeContinent.CHALLENGE_MODE);
			}
			initalPopupWindow();
		} else {
			this.currentQuestionIndex = this.currentQuestionIndex + 1;
			this.setQuestion();
		}
	}

	private void resetUI() {
			this.nameBtn1.setOnClickListener(new QuizButtonClickListener(0));
			this.nameBtn2.setOnClickListener(new QuizButtonClickListener(1));
			this.nameBtn3.setOnClickListener(new QuizButtonClickListener(2));
			this.nameBtn4.setOnClickListener(new QuizButtonClickListener(3));
			this.nameBtn5.setOnClickListener(new QuizButtonClickListener(4));
			this.nameBtn6.setOnClickListener(new QuizButtonClickListener(5));

		this.questionIndexText.setText((this.currentQuestionIndex+1) + "/" + MyApp.LEVELS[this.levelIndex][1]);
		this.levelIndexText.setText(
				getResources().getString(R.string.challenge_level_index)
				+" "+(this.levelIndex+1)+"/"+MyApp.LEVELS.length);
		this.scoreText.setText(getResources().getString(R.string.challenge_score)+" 0");
	}
	private void initListNation() {
		this.listNation = new ArrayList<EntityNation>();
		for(int i=MyApp.LEVELS[this.levelIndex][0];i<MyApp.LEVELS[this.levelIndex][0]+MyApp.LEVELS[this.levelIndex][1];i++) {
			this.listNation.add(this.listAllNation.get(i));
		}
		Collections.shuffle(this.listNation);

	}
	private void loadPropFile() {
		this.propFile = this.myApp.getSettingsPropFile();
		this.listAllNation = this.myApp.getNations();
		List<EntityContinent> tempListAllContinent = this.myApp.getContinents();
		this.listContinent = new ArrayList<EntityContinent>();
		while(!tempListAllContinent.isEmpty()) {
			EntityContinent tempContinent = tempListAllContinent.remove(0);
			if(tempContinent.getAmount() > 0) {
				this.listContinent.add(tempContinent);
			}
		}
		this.nameBtn1.setText(this.listContinent.get(0).getName());
		this.nameBtn2.setText(this.listContinent.get(1).getName());
		this.nameBtn3.setText(this.listContinent.get(2).getName());
		this.nameBtn4.setText(this.listContinent.get(3).getName());
		this.nameBtn5.setText(this.listContinent.get(4).getName());
		this.nameBtn6.setText(this.listContinent.get(5).getName());

		initListNation();
		this.blnSound = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_SOUND, String.valueOf(true)));
		this.blnVibrate = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_VIBRATE, String.valueOf(true)));
		if(this.blnVibrate) this.mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if(blnSound) {
			// ?趨?????????y??????,???????????????????????????????????????????
			ActivityChallengeContinent.this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
		this.nameBtn5.setClickable(blnEnable);
		this.nameBtn6.setClickable(blnEnable);
	}
	private void verifyAnswer(int continentIndex) {
		EntityNation currentNation = this.listNation.get(this.currentQuestionIndex);
		EntityContinent currentContinent = this.listContinent.get(continentIndex);
		if(currentContinent.getCode().equalsIgnoreCase(currentNation.getContinentCode())) {
			this.blnAnswer = true;
			this.correctAnswerAmout = this.correctAnswerAmout + 1;
			this.scoreText.setText(
					getResources().getString(R.string.challenge_score)
					+" "+this.myApp.convertCorrect2Score(correctAnswerAmout));
		} else {
			this.blnAnswer = false;
				if(this.listContinent.get(0).getCode().equalsIgnoreCase(currentNation.getContinentCode())) this.nameBtn1.setPressed(true);
				else if(this.listContinent.get(1).getCode().equalsIgnoreCase(currentNation.getContinentCode())) this.nameBtn2.setPressed(true);
				else if(this.listContinent.get(2).getCode().equalsIgnoreCase(currentNation.getContinentCode())) this.nameBtn3.setPressed(true);
				else if(this.listContinent.get(3).getCode().equalsIgnoreCase(currentNation.getContinentCode())) this.nameBtn4.setPressed(true);
				else if(this.listContinent.get(4).getCode().equalsIgnoreCase(currentNation.getContinentCode())) this.nameBtn5.setPressed(true);
				else if(this.listContinent.get(5).getCode().equalsIgnoreCase(currentNation.getContinentCode())) this.nameBtn6.setPressed(true);
		}

		if(this.blnSound) new FeedBackSoundThread().start();
		if(this.blnVibrate) new FeedBackVibrateThread().start();
		if(this.blnAnswer) {
			this.correctImg.setVisibility(ImageView.VISIBLE);
		} else {
			this.wrongImg.setVisibility(ImageView.VISIBLE);
		}
		new AfterAnswerThread().start();
	}

	private void setQuestion() {
		if(this.nameBtn1.isPressed()) this.nameBtn1.setPressed(false);
		if(this.nameBtn2.isPressed()) this.nameBtn2.setPressed(false);
		if(this.nameBtn3.isPressed()) this.nameBtn3.setPressed(false);
		if(this.nameBtn4.isPressed()) this.nameBtn4.setPressed(false);
		if(this.nameBtn5.isPressed()) this.nameBtn5.setPressed(false);
		if(this.nameBtn6.isPressed()) this.nameBtn6.setPressed(false);

		this.questionIndexText.setText((this.currentQuestionIndex+1) + "/" + MyApp.LEVELS[this.levelIndex][1]);

		EntityNation currentNation = this.listNation.get(this.currentQuestionIndex);
		this.flagImg.setImageResource(myApp.getFlagResourceId(currentNation.getA2()));
		if(this.blnEngLang && !this.blnUserLang) {
			this.nationNameBtn.setText(currentNation.getName());
		} else if(!this.blnEngLang && this.blnUserLang) {
			String str = this.propUserLang.getProperty(currentNation.getA2());
			if(str == null || str.trim().equalsIgnoreCase("")) str = currentNation.getName();
			this.nationNameBtn.setText(str);
		} else if(this.blnEngLang && this.blnUserLang) {
			this.nationNameBtn.setText(currentNation.getName()+"\n"+this.propUserLang.getProperty(currentNation.getA2()));
		}
		//for testing
//		this.nationNameBtn.setText("Democratic Republic of the Congo\nДевствени Острови (Велика Британи?а)");
//		this.flagImg.setImageResource(R.drawable.n_se);

		ActivityChallengeContinent.this.setButtonsClickable(true);
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
						intent.setClass(ActivityChallengeContinent.this, ActivityChallengeTabhost.class);
						startActivity(intent);
						ActivityChallengeContinent.this.finish();
					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.dialog_btn_retry,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityChallengeContinent.this.correctAnswerAmout=0;
						ActivityChallengeContinent.this.currentQuestionIndex = 0;
						Collections.shuffle(ActivityChallengeContinent.this.listNation);
						ActivityChallengeContinent.this.setQuestion();
						ActivityChallengeContinent.this.resetUI();
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

	/**
	 * 创建PopupWindow
	 */
	protected void initalPopupWindow() {
		// TODO Auto-generated method stub
		if(null != popupWindow) popupWindow.dismiss();
		View popupView = getLayoutInflater().inflate(R.layout.popup_finished, null,false);//获取自定义布局文件popup_finished.xml的视图
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		popupWindow = new PopupWindow(popupView, (int)(displayMetrics.widthPixels*0.9), (int)(displayMetrics.widthPixels*1.2), true);//创建PopupWindow实例
		TextView popupLevelText = (TextView)popupView.findViewById(R.id.popupFinishedLevelIndexText);
		RatingBar popupHighRating = (RatingBar)popupView.findViewById(R.id.popupFinishedHighRatingBar);
		TextView popupHighScoreText = (TextView)popupView.findViewById(R.id.popupFinishedHighScoreText);
		TextView popupAmazingText = (TextView)popupView.findViewById(R.id.popupFinishedAmazingText);
		TextView popupCurrentScoreText = (TextView)popupView.findViewById(R.id.popupFinishedCurrentScoreText);
		TextView popupNewHighScoreText = (TextView)popupView.findViewById(R.id.popupFinishedNewHighScoreText);
		RatingBar popupCurrentRating = (RatingBar)popupView.findViewById(R.id.popupFinishedCurrentRatingBar);
		TextView popupLimitNoticeText = (TextView)popupView.findViewById(R.id.popupFinishedLimitNoticeText);
		Button retryBtn = (Button)popupView.findViewById(R.id.popupFinishedRetryBtn);   //popup_finished.xml视图里面的控件
		Button nextBtn = (Button)popupView.findViewById(R.id.popupFinishedNextBtn);   //popup_finished.xml视图里面的控件
		Button backBtn = (Button)popupView.findViewById(R.id.popupFinishedBackBtn);   //popup_finished.xml视图里面的控件
		int currentScore = this.myApp.convertCorrect2Score(this.correctAnswerAmout);
		int currentStars = this.myApp.getStars(currentScore, MyApp.LEVELS[this.levelIndex][1]);
		int starsOfLastScore = this.myApp.getStars(this.lastScore, MyApp.LEVELS[this.levelIndex][1]);
		popupAmazingText.setVisibility(TextView.INVISIBLE);
		popupNewHighScoreText.setVisibility(TextView.INVISIBLE);
		popupLimitNoticeText.setVisibility(TextView.INVISIBLE);
		popupLevelText.setText(getResources().getString(R.string.popup_finished_level_index)+" "+(this.levelIndex+1)+"/"+MyApp.LEVELS.length);
		if(currentScore > this.lastScore) {
			popupHighRating.setRating(currentStars);
			popupHighScoreText.setText(getResources().getString(R.string.popup_finished_highscore)+" "+currentScore);
			if(currentStars == 3) {
				popupAmazingText.setVisibility(TextView.VISIBLE);
			}
			if(this.lastScore >= 0) {
				popupNewHighScoreText.setVisibility(TextView.VISIBLE);
			}
		} else {
			popupHighRating.setRating(this.myApp.getStars(this.lastScore, MyApp.LEVELS[this.levelIndex][1]));
			popupHighScoreText.setText(getResources().getString(R.string.popup_finished_highscore)+" "+this.lastScore);
		}

		popupCurrentScoreText.setText(getResources().getString(R.string.popup_finished_score_current)+" "+currentScore);
		popupCurrentRating.setRating(currentStars);

		if((currentStars == 0 && starsOfLastScore == 0) || (ActivityChallengeContinent.this.levelIndex + 1) >= MyApp.LEVELS.length ) {
			nextBtn.setVisibility(Button.GONE);
		} else {
			Properties scoresProp = this.myApp.getScoresPropFile(ActivityChallengeContinent.CHALLENGE_MODE);
			int totalStars = this.myApp.sumStars(scoresProp);
			if(totalStars < MyApp.LEVELS[this.levelIndex][2]) {
				popupLimitNoticeText.setVisibility(TextView.VISIBLE);
				popupLimitNoticeText.setText(getResources().getString(R.string.popup_finished_limit_notice).replaceAll(MyApp.STRING_REPLACE_TAG, ""+totalStars));
				nextBtn.setVisibility(Button.GONE);
			} else {
				nextBtn.setVisibility(Button.VISIBLE);
			}
		}
		retryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityChallengeContinent.this.correctAnswerAmout=0;
				ActivityChallengeContinent.this.currentQuestionIndex = 0;
				Collections.shuffle(ActivityChallengeContinent.this.listNation);
				ActivityChallengeContinent.this.setQuestion();
				ActivityChallengeContinent.this.resetUI();
				popupWindow.dismiss();	//对话框消失
			}
		});
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityChallengeContinent.this.correctAnswerAmout=0;
				ActivityChallengeContinent.this.currentQuestionIndex = 0;
				ActivityChallengeContinent.this.levelIndex = ActivityChallengeContinent.this.levelIndex + 1;
				ActivityChallengeContinent.this.initListNation();
				ActivityChallengeContinent.this.resetUI();
				ActivityChallengeContinent.this.setQuestion();
				popupWindow.dismiss();	//对话框消失
			}
		});
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();	//对话框消失
				Intent intent = new Intent();
				intent.setClass(ActivityChallengeContinent.this, ActivityChallengeTabhost.class);
				startActivity(intent);
				ActivityChallengeContinent.this.finish();
			}
		});
		popupWindow.showAtLocation(findViewById(R.id.challengeConRootLayout), Gravity.CENTER, 0, 0);
		//获取屏幕和对话框各自高宽
//		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();
//		screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();
//
//		dialgoWidth = popupWindow.getWidth();
//		dialgoheight = popupWindow.getHeight();

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
			if(ActivityChallengeContinent.this.mVibrator != null) {
				if(ActivityChallengeContinent.this.blnAnswer) ActivityChallengeContinent.this.mVibrator.vibrate(40);
				else ActivityChallengeContinent.this.mVibrator.vibrate(120);
//				long[] pattern = {1, 20, 200, 20}; // OFF/ON/OFF/ON...
//				this.mVibrator.vibrate(pattern, -1);
			}
		}
	}

	class FeedBackSoundThread extends Thread {
		public void run() {
			int soundId = 0;
			if(ActivityChallengeContinent.this.blnAnswer) soundId = 1;
			else soundId = 2;
			try {
				if(ActivityChallengeContinent.this.soundPool != null && ActivityChallengeContinent.this.soundPoolMap != null)
					ActivityChallengeContinent.this.soundPool.play(ActivityChallengeContinent.this.soundPoolMap.get(soundId), ActivityChallengeContinent.this.streamVolume, ActivityChallengeContinent.this.streamVolume, 1, 0, 1f);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	class QuizButtonClickListener implements OnClickListener {
		private int continentIndex;
		public QuizButtonClickListener(int continentIndex) {
			this.continentIndex = continentIndex;
		}
		@Override
		public void onClick(View v) {
			ActivityChallengeContinent.this.setButtonsClickable(false);
			verifyAnswer(continentIndex);

		}
	}

	class AfterAnswerThread extends Thread {
		public void run() {
			try{
				if(ActivityChallengeContinent.this.blnAnswer) Thread.sleep(300);
				else Thread.sleep(1500);
			} catch(Exception e) {
				e.printStackTrace();
			}
			ActivityChallengeContinent.this.mHandler.sendMessage(ActivityChallengeContinent.this.mHandler.obtainMessage(ActivityChallengeContinent.HANDLE_MSG_AFTER_ANSWER));
		}
	}

}
