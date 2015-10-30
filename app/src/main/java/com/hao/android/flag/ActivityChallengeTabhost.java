package com.hao.android.flag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import org.hh.flag.R;

public class ActivityChallengeTabhost extends Activity implements OnTabChangeListener  {
	private TabHost myTabhost;
	private MyApp myApp;
	private Properties propFile;
	private static final String KEY_LEVEL_INDEX = "INDEX";
//	private static final String KEY_LEVEL_RATING = "RATING";
//	private List<Map<String, Integer>> mData;
//	private static final String TAG = ActivityChallengeTabhost.class.getName();
	private GridView f2nGird;
	private GridView n2fGird;
	private GridView conGird;
	private Properties scoresF2nProp;
	private Properties scoresN2fProp;
	private Properties scoresConProp;
	private LevelsAdapter f2nAdapter;
	private LevelsAdapter n2fAdapter;
	private LevelsAdapter conAdapter;
	private int totalStarsF2N;
	private int totalStarsN2F;
	private int totalStarsCON;
	private Handler mHandler;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.myApp = (MyApp)getApplicationContext();
		setContentView(R.layout.challenge_tabhost);
		loadPropFile();
		RelativeLayout f2nTab = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.challenge_tabhost_tab, null);
		TextView f2nTabTitleText = (TextView)f2nTab.findViewById(R.id.challengeTabhostTabTitleText);
		f2nTabTitleText.setText(R.string.challenge_tabhost_tab_f2n_title);
		TextView f2nTabStarsText = (TextView)f2nTab.findViewById(R.id.challengeTabhostTabStarsText);
		f2nTabStarsText.setText(this.totalStarsF2N+"");
		ImageView f2nTabImg = (ImageView)f2nTab.findViewById(R.id.challengeTabhostTabImgView);
		f2nTabImg.setImageResource(R.drawable.res_tab_falg);

		RelativeLayout n2fTab = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.challenge_tabhost_tab, null);
		TextView n2fTabTitleText = (TextView)n2fTab.findViewById(R.id.challengeTabhostTabTitleText);
		n2fTabTitleText.setText(R.string.challenge_tabhost_tab_n2f_title);
		TextView n2fTabStarsText = (TextView)n2fTab.findViewById(R.id.challengeTabhostTabStarsText);
		n2fTabStarsText.setText(this.totalStarsN2F+"");
		ImageView n2fTabImg = (ImageView)n2fTab.findViewById(R.id.challengeTabhostTabImgView);
		n2fTabImg.setImageResource(R.drawable.res_tab_name);

		RelativeLayout conTab = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.challenge_tabhost_tab, null);
		TextView conTabTitleText = (TextView)conTab.findViewById(R.id.challengeTabhostTabTitleText);
		conTabTitleText.setText(R.string.challenge_tabhost_tab_con_title);
		TextView conTabStarsText = (TextView)conTab.findViewById(R.id.challengeTabhostTabStarsText);
		conTabStarsText.setText(this.totalStarsCON+"");
		ImageView conTabImg = (ImageView)conTab.findViewById(R.id.challengeTabhostTabImgView);
		conTabImg.setImageResource(R.drawable.res_tab_con);

		this.f2nGird = (GridView)findViewById(R.id.levelsGridF2N);
		this.n2fGird = (GridView)findViewById(R.id.levelsGridN2F);
		this.conGird = (GridView)findViewById(R.id.levelsGridContinent);
//		this.mData = getData();
		new InitalF2NGrid().start();
		new InitalN2FGrid().start();
		new InitalCONGrid().start();

		myTabhost = (TabHost) findViewById(R.id.challengeTabhost);
		myTabhost.setup(); //TabHost实例化后，必须立刻执行此方法
		myTabhost.addTab(myTabhost.newTabSpec(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N).setIndicator(f2nTab).setContent(R.id.levelsGridF2N));
		myTabhost.addTab(myTabhost.newTabSpec(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F).setIndicator(n2fTab).setContent(R.id.levelsGridN2F));
		myTabhost.addTab(myTabhost.newTabSpec(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON).setIndicator(conTab).setContent(R.id.levelsGridContinent));
		this.myTabhost.setCurrentTabByTag(this.propFile.getProperty(MyApp.PROPKEY_LAST_CHALLENGE_MODE));
		myTabhost.setOnTabChangedListener(this);

		this.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				default:
					break;
				}
			}

		};

	}
	private void loadPropFile() {
		this.propFile = this.myApp.getSettingsPropFile();
		this.scoresF2nProp = this.myApp.getScoresPropFile(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N);
//		for(int i=0;i<10;i++) {
//			this.scoresF2nProp.setProperty(MyApplication.PROPKEY_SCORE_LEVELE+i, "168");
//		}
		this.scoresN2fProp = this.myApp.getScoresPropFile(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F);
		this.scoresConProp = this.myApp.getScoresPropFile(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON);

		this.totalStarsF2N = this.myApp.sumStars(this.scoresF2nProp);
		this.totalStarsN2F = this.myApp.sumStars(this.scoresN2fProp);
		this.totalStarsCON = this.myApp.sumStars(this.scoresConProp);

	}
	private List<Map<String, Integer>> getData() {
		List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> map = null;
		for(int i=0;i<MyApp.LEVELS.length;i++) {
			map = new HashMap<String, Integer>();
			map.put(KEY_LEVEL_INDEX, i);
			list.add(map);
		}
		return list;
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.setClass(ActivityChallengeTabhost.this, ActivityMain.class);
			startActivity(intent);
			ActivityChallengeTabhost.this.finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	@Override
	public void onTabChanged(String tagString) {
	}
	public void popupNoticeMsg(int starsLimit){
		String strMsg = getResources().getString(R.string.challenge_tabhost_notice);
		strMsg = strMsg.replaceAll(MyApp.STRING_REPLACE_TAG, starsLimit+"");
		new AlertDialog.Builder(this)
		.setTitle("Notice")
		.setMessage(strMsg)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();
	}
	@Override
	public void onPause() {
		this.propFile.setProperty(MyApp.PROPKEY_LAST_CHALLENGE_MODE, this.myTabhost.getCurrentTabTag());
		this.myApp.saveSettingsPropFile(this.propFile);
		super.onPause();
	}

	class InitalF2NGrid extends Thread {
		public void run() {
			ActivityChallengeTabhost.this.f2nAdapter = new LevelsAdapter(ActivityChallengeTabhost.this, ActivityChallengeTabhost.this.scoresF2nProp);
			ActivityChallengeTabhost.this.f2nGird.setAdapter(ActivityChallengeTabhost.this.f2nAdapter);
			ActivityChallengeTabhost.this.f2nGird.setOnItemClickListener(new MyItemClickListener(ActivityChallengeTabhost.this.scoresF2nProp, ActivityChallengeTabhost.this.totalStarsF2N));
			String lastChallengeMode = ActivityChallengeTabhost.this.propFile.getProperty(MyApp.PROPKEY_LAST_CHALLENGE_MODE);
			if(lastChallengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N)) {
				ActivityChallengeTabhost.this.f2nGird.setVisibility(GridView.VISIBLE);
			}
		}
	}
	class InitalN2FGrid extends Thread {
		public void run() {
			ActivityChallengeTabhost.this.n2fAdapter = new LevelsAdapter(ActivityChallengeTabhost.this, ActivityChallengeTabhost.this.scoresN2fProp);
			ActivityChallengeTabhost.this.n2fGird.setAdapter(ActivityChallengeTabhost.this.n2fAdapter);
			ActivityChallengeTabhost.this.n2fGird.setOnItemClickListener(new MyItemClickListener(ActivityChallengeTabhost.this.scoresN2fProp, ActivityChallengeTabhost.this.totalStarsN2F));
			String lastChallengeMode = ActivityChallengeTabhost.this.propFile.getProperty(MyApp.PROPKEY_LAST_CHALLENGE_MODE);
			if(lastChallengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F)) {
				ActivityChallengeTabhost.this.n2fGird.setVisibility(GridView.VISIBLE);
			}
		}
	}
	class InitalCONGrid extends Thread {
		public void run() {
			ActivityChallengeTabhost.this.conAdapter = new LevelsAdapter(ActivityChallengeTabhost.this, ActivityChallengeTabhost.this.scoresConProp);
			ActivityChallengeTabhost.this.conGird.setAdapter(ActivityChallengeTabhost.this.conAdapter);
			ActivityChallengeTabhost.this.conGird.setOnItemClickListener(new MyItemClickListener(ActivityChallengeTabhost.this.scoresConProp, ActivityChallengeTabhost.this.totalStarsCON));
			String lastChallengeMode = ActivityChallengeTabhost.this.propFile.getProperty(MyApp.PROPKEY_LAST_CHALLENGE_MODE);
			if(lastChallengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON)) {
				ActivityChallengeTabhost.this.conGird.setVisibility(GridView.VISIBLE);
			}
		}
	}
	final class ViewHolder {
		public TextView levelIndex;
		public RatingBar levelRating;
		public ImageView lockImg;
	}

	class LevelsAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private Properties scoresProp;
		public LevelsAdapter(Context context, Properties scoresProp) {
//			this.mContext = context;
			this.scoresProp = scoresProp;
			this.mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return MyApp.LEVELS.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			int score = Integer.parseInt(this.scoresProp.getProperty(MyApp.PROPKEY_SCORE_LEVELE+position, "-1"));
			int stars = ActivityChallengeTabhost.this.myApp.getStars(score, MyApp.LEVELS[position][1]);
			if(convertView == null) {
				holder=new ViewHolder();
				convertView = mInflater.inflate(R.layout.challenge_tabhost_griditem, null);
				holder.levelIndex = (TextView)convertView.findViewById(R.id.challengeTabhostGridItemLevelText);
				holder.levelRating = (RatingBar)convertView.findViewById(R.id.challengeTabhostGridItemRatingBar);
				holder.lockImg = (ImageView)convertView.findViewById(R.id.challengeTabhostGridItemLockImg);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			holder.lockImg.setAlpha(128);
			if(position == 0) {
				holder.lockImg.setVisibility(ImageView.INVISIBLE);
			} else {
				if(stars>0) {
					holder.lockImg.setVisibility(ImageView.INVISIBLE);
				} else {
					int lastLevelscore = Integer.parseInt(this.scoresProp.getProperty(MyApp.PROPKEY_SCORE_LEVELE+(position-1), "-1"));
					int lastLevelstars = ActivityChallengeTabhost.this.myApp.getStars(lastLevelscore, MyApp.LEVELS[position-1][1]);
					if(lastLevelstars > 0) {
						holder.lockImg.setVisibility(ImageView.INVISIBLE);
					} else {
						holder.lockImg.setVisibility(ImageView.VISIBLE);
					}
				}
			}

			holder.levelIndex.setText((position+1)+"");
			holder.levelRating.setRating(stars);
			if(holder.lockImg.getVisibility() == View.VISIBLE) {
				holder.levelRating.setVisibility(View.INVISIBLE);
			} else {
				holder.levelRating.setVisibility(View.VISIBLE);
			}
			return convertView;
		}

	}
	class MyItemClickListener implements OnItemClickListener {
		private Properties scoresProp;
		private int totalStars;
		public MyItemClickListener(Properties scoresProp, int totalStars) {
			this.scoresProp = scoresProp;
			this.totalStars = totalStars;
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			if(arg2 == 0) {
				Intent intent = new Intent();
				if(ActivityChallengeTabhost.this.myTabhost.getCurrentTabTag().equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N))
					intent.setClass(ActivityChallengeTabhost.this, ActivityChallengeF2N.class);
				else if(ActivityChallengeTabhost.this.myTabhost.getCurrentTabTag().equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F))
					intent.setClass(ActivityChallengeTabhost.this, ActivityChallengeN2F.class);
				else if(ActivityChallengeTabhost.this.myTabhost.getCurrentTabTag().equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON))
					intent.setClass(ActivityChallengeTabhost.this, ActivityChallengeContinent.class);
				Bundle bundle = new Bundle();
				bundle.putInt("levelIndex", arg2);
				intent.putExtras(bundle);
				startActivity(intent);
				ActivityChallengeTabhost.this.finish();
			} else {
				int lastLevelScore = Integer.parseInt(this.scoresProp.getProperty(MyApp.PROPKEY_SCORE_LEVELE+(arg2-1), "0"));
				int lastLevelRateStars =ActivityChallengeTabhost.this.myApp.getStars(lastLevelScore, MyApp.LEVELS[arg2-1][1]);
				if(lastLevelRateStars>0) {
					if(this.totalStars>=MyApp.LEVELS[arg2][2]) {
						Intent intent = new Intent();
						if(ActivityChallengeTabhost.this.myTabhost.getCurrentTabTag().equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N))
							intent.setClass(ActivityChallengeTabhost.this, ActivityChallengeF2N.class);
						else if(ActivityChallengeTabhost.this.myTabhost.getCurrentTabTag().equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F))
							intent.setClass(ActivityChallengeTabhost.this, ActivityChallengeN2F.class);
						else if(ActivityChallengeTabhost.this.myTabhost.getCurrentTabTag().equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON))
							intent.setClass(ActivityChallengeTabhost.this, ActivityChallengeContinent.class);
						Bundle bundle = new Bundle();
						bundle.putInt("levelIndex", arg2);
						intent.putExtras(bundle);
						startActivity(intent);
						ActivityChallengeTabhost.this.finish();
					} else {
						popupNoticeMsg(MyApp.LEVELS[arg2][2]);
					}
				}
			}
		}

	}


}
