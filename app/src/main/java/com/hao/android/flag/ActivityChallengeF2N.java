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

public class ActivityChallengeF2N extends Activity {
	private Properties propFile;
	private MyApp myApp;
	private ImageView flagImg;
	private ImageView correctImg;
	private ImageView wrongImg;
	private Button nameBtn1;
	private Button nameBtn2;
	private Button nameBtn3;
	private Button nameBtn4;
	private TextView questionIndexText;
	private TextView levelIndexText;
	private TextView scoreText;
	private List<EntityNation> listAllNation;
	private List<EntityNation> listNation;
	private List<Integer> listRandomIndexForBtn;
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
	private static final String CHALLENGE_MODE = MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.challenge_f2n);
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
		this.flagImg = (ImageView)findViewById(R.id.challengeF2nFlagImg);
		this.correctImg = (ImageView)findViewById(R.id.challengeF2nCorrectImg);
		this.correctImg.setVisibility(ImageView.GONE);
		this.wrongImg = (ImageView)findViewById(R.id.challengeF2nWrongImg);
		this.wrongImg.setVisibility(ImageView.GONE);
		this.levelIndexText = (TextView)findViewById(R.id.challengeF2nLevelIndexText);
		this.questionIndexText = (TextView)findViewById(R.id.challengeF2nQuestionIndexText);
		this.scoreText = (TextView)findViewById(R.id.challengeF2nScoreText);
		this.nameBtn1 = (Button)findViewById(R.id.challengeF2nBtn1);
		this.nameBtn2 = (Button)findViewById(R.id.challengeF2nBtn2);
		this.nameBtn3 = (Button)findViewById(R.id.challengeF2nBtn3);
		this.nameBtn4 = (Button)findViewById(R.id.challengeF2nBtn4);

		loadPropFile();
		resetUI();
		setQuestion();


		this.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case ActivityChallengeF2N.HANDLE_MSG_AFTER_ANSWER:
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
			Properties scoresProp = this.myApp.getScoresPropFile(ActivityChallengeF2N.CHALLENGE_MODE);
			this.lastScore = Integer.valueOf(scoresProp.getProperty(MyApp.PROPKEY_SCORE_LEVELE+this.levelIndex, "-1"));
			if(this.myApp.convertCorrect2Score(this.correctAnswerAmout) > lastScore) {
				int score = this.myApp.convertCorrect2Score(ActivityChallengeF2N.this.correctAnswerAmout);
				scoresProp.setProperty(MyApp.PROPKEY_SCORE_LEVELE+this.levelIndex, String.valueOf(score));
				this.myApp.saveScoresPropFile(scoresProp, ActivityChallengeF2N.CHALLENGE_MODE);
			}
			initalPopupWindow();
		} else {
			this.currentQuestionIndex = this.currentQuestionIndex + 1;
			this.setQuestion();
		}
	}

	private void resetUI() {
		if(this.levelIndex == 0) {
			this.nameBtn1.setOnClickListener(new QuizButtonClickListener(0));
			this.nameBtn2.setVisibility(Button.INVISIBLE);
			this.nameBtn3.setOnClickListener(new QuizButtonClickListener(1));
			this.nameBtn4.setVisibility(Button.INVISIBLE);
		} else {
			this.nameBtn2.setVisibility(Button.VISIBLE);
			this.nameBtn4.setVisibility(Button.VISIBLE);
			this.nameBtn1.setOnClickListener(new QuizButtonClickListener(0));
			this.nameBtn2.setOnClickListener(new QuizButtonClickListener(1));
			this.nameBtn3.setOnClickListener(new QuizButtonClickListener(2));
			this.nameBtn4.setOnClickListener(new QuizButtonClickListener(3));
		}

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
		initListNation();
		this.blnSound = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_SOUND, String.valueOf(true)));
		this.blnVibrate = Boolean.parseBoolean(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_VIBRATE, String.valueOf(true)));
		if(this.blnVibrate) this.mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		if(blnSound) {
			// ?趨?????????y??????,???????????????????????????????????????????
			ActivityChallengeF2N.this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
			this.correctAnswerAmout = this.correctAnswerAmout + 1;
			this.scoreText.setText(
					getResources().getString(R.string.challenge_score)
					+" "+this.myApp.convertCorrect2Score(correctAnswerAmout));
		} else {
			this.blnAnswer = false;
			if(this.levelIndex > 0) {
				if(this.listRandomIndexForBtn.get(0) == this.currentQuestionIndex) this.nameBtn1.setPressed(true);
				else if(this.listRandomIndexForBtn.get(1) == this.currentQuestionIndex) this.nameBtn2.setPressed(true);
				else if(this.listRandomIndexForBtn.get(2) == this.currentQuestionIndex) this.nameBtn3.setPressed(true);
				else if(this.listRandomIndexForBtn.get(3) == this.currentQuestionIndex) this.nameBtn4.setPressed(true);
			}
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

		this.questionIndexText.setText((this.currentQuestionIndex+1) + "/" + MyApp.LEVELS[this.levelIndex][1]);

		this.listRandomIndexForBtn = new ArrayList<Integer>();
		EntityNation currentNation = this.listNation.get(this.currentQuestionIndex);
		this.flagImg.setImageResource(myApp.getFlagResourceId(currentNation.getA2()));
		if(this.levelIndex == 0) { //简单模式，只有两个按键
			this.listRandomIndexForBtn = myApp.randomRank(MyApp.LEVELS[this.levelIndex][1], 2);
			if(!this.listRandomIndexForBtn.contains(this.currentQuestionIndex)) {
				int intI = new Random().nextInt(2);
				this.listRandomIndexForBtn.add(intI, this.currentQuestionIndex);
				this.listRandomIndexForBtn.remove(2);
			}
			String strBtn1 = "";
			String strBtn3 = "";
			EntityNation antionForBtn1 = this.listNation.get(this.listRandomIndexForBtn.get(0));
			EntityNation antionForBtn3 = this.listNation.get(this.listRandomIndexForBtn.get(1));
			if(this.blnEngLang && !this.blnUserLang) {
				strBtn1 = antionForBtn1.getName();
				strBtn3 = antionForBtn3.getName();
			} else if(!this.blnEngLang && this.blnUserLang) {
				strBtn1 = this.propUserLang.getProperty(antionForBtn1.getA2());
				strBtn3 = this.propUserLang.getProperty(antionForBtn3.getA2());
				if(strBtn1 == null || strBtn1.trim().equalsIgnoreCase("")) strBtn1 = antionForBtn1.getName();
				if(strBtn3 == null || strBtn3.trim().equalsIgnoreCase("")) strBtn3 = antionForBtn3.getName();
			} else if(this.blnEngLang && this.blnUserLang) {
				strBtn1 = antionForBtn1.getName()+"\n"+this.propUserLang.getProperty(antionForBtn1.getA2());
				strBtn3 = antionForBtn3.getName()+"\n"+this.propUserLang.getProperty(antionForBtn3.getA2());
			}
			this.nameBtn1.setText(strBtn1);
			this.nameBtn3.setText(strBtn3);
		} else {
			this.listRandomIndexForBtn = myApp.randomRank(MyApp.LEVELS[this.levelIndex][1], 4);
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
			//for test
//			this.flagImg.setImageResource(R.drawable.n_se);
//			this.nameBtn3.setText("aaa \nDemocratic Republic of the Congo\nДевствени Острови (Велика Британи?а)");
		}

		ActivityChallengeF2N.this.setButtonsClickable(true);
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
						intent.setClass(ActivityChallengeF2N.this, ActivityChallengeTabhost.class);
						startActivity(intent);
						ActivityChallengeF2N.this.finish();
					}
				}
			);
		dialogBuilder.setNeutralButton(
				R.string.dialog_btn_retry,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityChallengeF2N.this.correctAnswerAmout=0;
						ActivityChallengeF2N.this.currentQuestionIndex = 0;
						Collections.shuffle(ActivityChallengeF2N.this.listNation);
						ActivityChallengeF2N.this.setQuestion();
						ActivityChallengeF2N.this.resetUI();
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

		if((currentStars == 0 && starsOfLastScore == 0) || (ActivityChallengeF2N.this.levelIndex + 1) >= MyApp.LEVELS.length ) {
			nextBtn.setVisibility(Button.GONE);
		} else {
			Properties scoresProp = this.myApp.getScoresPropFile(ActivityChallengeF2N.CHALLENGE_MODE);
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
				ActivityChallengeF2N.this.correctAnswerAmout=0;
				ActivityChallengeF2N.this.currentQuestionIndex = 0;
				Collections.shuffle(ActivityChallengeF2N.this.listNation);
				ActivityChallengeF2N.this.setQuestion();
				ActivityChallengeF2N.this.resetUI();
				popupWindow.dismiss();	//对话框消失
			}
		});
		nextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActivityChallengeF2N.this.correctAnswerAmout=0;
				ActivityChallengeF2N.this.currentQuestionIndex = 0;
				ActivityChallengeF2N.this.levelIndex = ActivityChallengeF2N.this.levelIndex + 1;
				ActivityChallengeF2N.this.initListNation();
				ActivityChallengeF2N.this.resetUI();
				ActivityChallengeF2N.this.setQuestion();
				popupWindow.dismiss();	//对话框消失
			}
		});
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popupWindow.dismiss();	//对话框消失
				Intent intent = new Intent();
				intent.setClass(ActivityChallengeF2N.this, ActivityChallengeTabhost.class);
				startActivity(intent);
				ActivityChallengeF2N.this.finish();
			}
		});
		popupWindow.showAtLocation(findViewById(R.id.challengeF2nRootLayout), Gravity.CENTER, 0, 0);
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
			if(ActivityChallengeF2N.this.mVibrator != null) {
				if(ActivityChallengeF2N.this.blnAnswer) ActivityChallengeF2N.this.mVibrator.vibrate(40);
				else ActivityChallengeF2N.this.mVibrator.vibrate(120);
//				long[] pattern = {1, 20, 200, 20}; // OFF/ON/OFF/ON...
//				this.mVibrator.vibrate(pattern, -1);
			}
		}
	}

	class FeedBackSoundThread extends Thread {
		public void run() {
			int soundId = 0;
			if(ActivityChallengeF2N.this.blnAnswer) soundId = 1;
			else soundId = 2;
			try {
				if(ActivityChallengeF2N.this.soundPool != null && ActivityChallengeF2N.this.soundPoolMap != null)
					ActivityChallengeF2N.this.soundPool.play(ActivityChallengeF2N.this.soundPoolMap.get(soundId), ActivityChallengeF2N.this.streamVolume, ActivityChallengeF2N.this.streamVolume, 1, 0, 1f);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	class QuizButtonClickListener implements OnClickListener {
		private int buttonIndex;
		public QuizButtonClickListener(int buttonIndex) {
			this.buttonIndex = buttonIndex;
		}
		@Override
		public void onClick(View v) {
			ActivityChallengeF2N.this.setButtonsClickable(false);
			verifyAnswer(ActivityChallengeF2N.this.listRandomIndexForBtn.get(buttonIndex));

		}
	}

	class AfterAnswerThread extends Thread {
		public void run() {
			try{
				if(ActivityChallengeF2N.this.blnAnswer) Thread.sleep(300);
				else Thread.sleep(1500);
			} catch(Exception e) {
				e.printStackTrace();
			}
			ActivityChallengeF2N.this.mHandler.sendMessage(ActivityChallengeF2N.this.mHandler.obtainMessage(ActivityChallengeF2N.HANDLE_MSG_AFTER_ANSWER));
		}
	}

}
