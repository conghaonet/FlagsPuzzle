package com.hao.android.flag;

import org.hh.flag.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;

import com.hao.android.flag.services.NetworkAvailableReceiver;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Xml;

public class MyApp extends Application {
	public static final int STARS_TOP=3;
	public static final String STRING_REPLACE_TAG="#!REPLACE_TAG!#";

	public static final String FILE_NAME_CONTINENTS="continents.xml";
	public static final String FILE_NAME_NATIONS="nations.xml";
	public static final String FILE_NAME_SETTINGS = "settings.properties";
	public static final String FILE_NAME_LANGUAGES="languages.xml";
	public static final String FILE_NAME_SCORES_F2N = "scores_f2n.properties";
	public static final String FILE_NAME_SCORES_N2F = "scores_n2f.properties";
	public static final String FILE_NAME_SCORES_CONTINENT = "scores_con.properties";

	public static final String FILE_PREFIX_CONTINENT="c_";
	public static final String FILE_PREFIX_FLAG="n_";
	public static final String FILE_PREFIX_FLAG_THUMBNAIL="nt_";
	public static final String FILE_PREFIX_FLAG_BUTTON="nb_";
	public static final String FILE_PREFIX_LANGUAGE="lang_";


	public static final String PROPKEY_FREEPLAY_CONTINENTS = "freeplay.continents";
	public static final String PROPKEY_FREEPLAY_ORDER = "freeplay.order";
	public static final String PROPKEY_SETTINGS_SOUND = "settings.sound";
	public static final String PROPKEY_SETTINGS_VIBRATE = "settings.vibrate";
	public static final String PROPKEY_SETTINGS_DISPLAY_ENG = "settings.language.display.english";
	public static final String PROPKEY_SETTINGS_DISPLAY_USER_LANG = "settings.language.display.user";
	public static final String PROPKEY_SETTINGS_USER_LANG = "settings.language.user";
	public static final String PROPKEY_SCORE_LEVELE = "score.level."; //score.level.{LEVELS[I]}
	public static final String PROPKEY_LAST_CHALLENGE_MODE = "last.challenge.mode";
	public static final String PROPKEY_AD_UNIT_ID="ad_unit_id";
	
	public static final String PROPVALUE_FREEPLAY_ORDER_RANDOM = "random";
	public static final String PROPVALUE_FREEPLAY_ORDER_A2Z = "a2z";
	public static final String PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N = "f2n";
	public static final String PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F = "n2f";
	public static final String PROPVALUE_SETTINGS_CHALLENGE_MODE_CON = "continent";

	public static final String MARKET_MORE_GAMES = "market://search?q=F0REVERPUZZLE";
	private static final int CORRECT_AMPLIFICATION_MULTIPLE = 24; //My son's birthday
	private Properties propFile;
	
	public static final int AD_BANNER_PHONE_HEIGHT = 50;
	public static final int AD_BANNER_PHONE_WIDTH = 320;
	public static final int AD_BANNER_TABLET_HEIGHT = 60;
	public static final int AD_BANNER_TABLET_WIDTH = 468;


	//{'全部国家的列表的索引','从当前索引开始的长度','本关需要累积的星星的总和'}
	public static final int[][] LEVELS = {
		{0,10,-1},{10,10,-1},{20,10,-1},{30,10,-1},{40,10,-1},{50,10,-1},{60,10,-1},{70,10,-1},{80,10,-1},{90,10,-1}
		,{100,10,20},{110,10,-1},{120,10,-1},{130,10,-1},{140,10,-1},{150,10,-1},{160,10,-1},{170,10,-1},{180,10,-1},{190,10,-1},{200,10,-1},{210,10,-1},{220,10,-1}
		,{0,20,46},{20,21,-1},{41,21,-1},{62,21,-1},{83,21,-1},{104,21,-1},{125,21,-1},{146,21,-1},{167,21,-1},{188,21,-1},{209,21,-1}
		,{0,32,80},{32,33,-1},{65,33,-1},{98,33,-1},{131,33,-1},{164,33,-1},{197,33,-1}
		,{0,46,-1},{46,46,-1},{92,46,-1},{138,46,-1},{184,46,-1}
		,{0,76,104},{76,77,-1},{153,77,-1}
		,{0,115,-1},{115,115,-1}
		,{0,160,-1},{70,160,-1}
		,{0,230,120}
		};

	@Override
	public void onCreate() {
		super.onCreate();
		initalSaveFile();
		IntentFilter filterNetwork = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		NetworkAvailableReceiver networkReceiver = new NetworkAvailableReceiver();
		registerReceiver(networkReceiver, filterNetwork);
	}
	private void initalSaveFile() {
		this.propFile = getSettingsPropFile();
		if(this.propFile.isEmpty()) {
			List<EntityContinent> listContinents = getContinents();
			String allContinentsCode = null;
			while(!listContinents.isEmpty()) {
				EntityContinent continent = listContinents.remove(0);
				if(continent.getAmount() > 0 ) {
					if(allContinentsCode == null) allContinentsCode = continent.getCode();
					else allContinentsCode = allContinentsCode + "," + continent.getCode();
				}
			}
			this.propFile.setProperty(PROPKEY_FREEPLAY_CONTINENTS, allContinentsCode);
			this.propFile.setProperty(PROPKEY_FREEPLAY_ORDER, PROPVALUE_FREEPLAY_ORDER_RANDOM);
			this.propFile.setProperty(PROPKEY_SETTINGS_SOUND, String.valueOf(true));
			this.propFile.setProperty(PROPKEY_SETTINGS_VIBRATE, String.valueOf(true));
			this.propFile.setProperty(PROPKEY_SETTINGS_DISPLAY_ENG, String.valueOf(true));
			this.propFile.setProperty(PROPKEY_SETTINGS_DISPLAY_USER_LANG, String.valueOf(false));
			this.propFile.setProperty(PROPKEY_LAST_CHALLENGE_MODE, PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N);
			List<EntityLanguage> listLang = getAvailableLanguages();
			String defaultISO3Lang = Locale.getDefault().getISO3Language();
			while(!listLang.isEmpty()) {
				EntityLanguage entityLanguage = listLang.remove(0);
				if(entityLanguage.getIso().equalsIgnoreCase(defaultISO3Lang)) {
					this.propFile.setProperty(PROPKEY_SETTINGS_DISPLAY_USER_LANG, String.valueOf(true));
					this.propFile.setProperty(PROPKEY_SETTINGS_USER_LANG, entityLanguage.getCode());
					listLang.clear();
					listLang = null;
					break;
				}
			}
			propFile.setProperty(MyApp.PROPKEY_AD_UNIT_ID, this.getString(R.string.ad_banner_id));
			saveSettingsPropFile(propFile);
		} else {
			if(propFile.getProperty(MyApp.PROPKEY_AD_UNIT_ID) == null) {
				propFile.setProperty(MyApp.PROPKEY_AD_UNIT_ID, this.getString(R.string.ad_banner_id));
				saveSettingsPropFile(propFile);
			}
		}
	}

	public List<EntityContinent> getContinents() {
		List<EntityContinent> listContinent = new ArrayList<EntityContinent>();
		EntityContinent continent = null;
		InputStream input = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
			input = getResources().getAssets().open(MyApp.FILE_NAME_CONTINENTS);
			parser.setInput(input, "UTF-8");
			int eventType = parser.getEventType();//???????????
			while(eventType!=XmlPullParser.END_DOCUMENT){
				switch(eventType){
					case XmlPullParser.START_DOCUMENT://?ж????????????????????
//						listContinent = new ArrayList<Continent>();
						break;
					case XmlPullParser.START_TAG://?ж?????????????????????
						if("continent".equalsIgnoreCase(parser.getName())){//?ж??????????????continent
							continent = new EntityContinent();
							continent.setId(Integer.parseInt(parser.getAttributeValue(0)));//???book??????????????????continent??id
							continent.setCode(parser.getAttributeValue(1).toLowerCase());
							continent.setName(parser.getAttributeValue(2));
							continent.setAmount(Integer.parseInt(parser.getAttributeValue(3)));
						}
						break;
					case XmlPullParser.END_TAG://?ж??????????????????????
						if("continent".equals(parser.getName())){//?ж??????????????continent
							listContinent.add(continent);//??continent????listContinent????
							continent = null;
						}
						break;
				}
				eventType = parser.next();//???????????????????????
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return listContinent;
	}

	public List<EntityNation> getNations() {
		List<EntityNation> listNation = new ArrayList<EntityNation>();
		EntityNation nation = null;
		InputStream input = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
			input = getResources().getAssets().open(MyApp.FILE_NAME_NATIONS);
			parser.setInput(input, "UTF-8");
			int eventType = parser.getEventType();//???????????
			while(eventType!=XmlPullParser.END_DOCUMENT){
				switch(eventType){
					case XmlPullParser.START_DOCUMENT://?ж????????????????????
//						listContinent = new ArrayList<Continent>();
						break;
					case XmlPullParser.START_TAG://?ж?????????????????????
						if("nation".equalsIgnoreCase(parser.getName())){//?ж??????????????continent
							nation = new EntityNation();
							nation.setId(Integer.parseInt(parser.getAttributeValue(0)));//???book??????????????????continent??id
							nation.setA2(parser.getAttributeValue(1).toLowerCase());
							nation.setName(parser.getAttributeValue(2));
							nation.setContinentCode(parser.getAttributeValue(3).toLowerCase());
						}
						break;
					case XmlPullParser.END_TAG://?ж??????????????????????
						if("nation".equals(parser.getName())){//?ж??????????????continent
							listNation.add(nation);//??continent????listContinent????
							nation = null;
						}
						break;
				}
				eventType = parser.next();//???????????????????????
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return listNation;
	}

	public List<EntityLanguage> getAvailableLanguages() {
		Locale[] arrLocale = Locale.getAvailableLocales();
		List<String> listAvailableLocaleLanguages = new ArrayList<String>();
		for(int i=0;i<arrLocale.length;i++) {
			listAvailableLocaleLanguages.add(arrLocale[i].getISO3Language().toLowerCase());
		}
		List<EntityLanguage> listLanguage = new ArrayList<EntityLanguage>();
		EntityLanguage language = null;
		InputStream input = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
			input = getResources().getAssets().open(MyApp.FILE_NAME_LANGUAGES);
			parser.setInput(input, "UTF-8");
			int eventType = parser.getEventType();//???????????
			while(eventType!=XmlPullParser.END_DOCUMENT){
				switch(eventType){
					case XmlPullParser.START_DOCUMENT://?ж????????????????????
//						listContinent = new ArrayList<EntityLanguage>();
						break;
					case XmlPullParser.START_TAG://?ж?????????????????????
						if("language".equalsIgnoreCase(parser.getName())){//?ж??????????????continent
							language = new EntityLanguage();
							language.setId(Integer.parseInt(parser.getAttributeValue(0)));//???book??????????????????continent??id
							language.setCode(parser.getAttributeValue(1).toLowerCase());
							language.setIso(parser.getAttributeValue(2).toLowerCase());
							language.setLocal(parser.getAttributeValue(3));
						}
						break;
					case XmlPullParser.END_TAG://?ж??????????????????????
						if("language".equals(parser.getName())){//?ж??????????????continent
							if(listAvailableLocaleLanguages.contains(language.getIso())) {
								listLanguage.add(language);//??continent????listContinent????
							}
							language = null;
						}
						break;
				}
				eventType = parser.next();//???????????????????????
			}

		} catch(Exception e) {
			e.printStackTrace();
		}

		return listLanguage;
	}
	private Integer getResourceId(String resourceName) {
		Integer resourceId = null;
		try {
			resourceId = R.drawable.class.getField(resourceName).getInt(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resourceId;
	}
	public Integer getContinentResourceId(String continentCode) {
		return getResourceId(FILE_PREFIX_CONTINENT+continentCode);
	}
	public Integer getFlagResourceId(String nationA2) {
		return getResourceId(FILE_PREFIX_FLAG+nationA2);
	}
	public Integer getFlagThumbnailResourceId(String nationA2) {
		return getResourceId(FILE_PREFIX_FLAG_THUMBNAIL+nationA2);
	}
	public Integer getFlagButtonResourceId(String nationA2) {
		return getResourceId(FILE_PREFIX_FLAG_BUTTON+nationA2);
	}
	

	public Properties getSettingsPropFile() {
		if(this.propFile == null) {
			this.propFile = new Properties();
			try {
				FileInputStream stream = this.openFileInput(MyApp.FILE_NAME_SETTINGS);
				this.propFile.load(stream);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return propFile;
	}

	public void saveSettingsPropFile(Properties tempPropSaveFile) {
		this.propFile = tempPropSaveFile;
		try {
			FileOutputStream stream = this.openFileOutput(MyApp.FILE_NAME_SETTINGS, Context.MODE_WORLD_READABLE);
			this.propFile.store(stream, "");
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Properties getScoresPropFile(String challengeMode) {
		Properties scoresPropFile = new Properties();
		FileInputStream stream = null;
		try {
			if(challengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N))
				stream = this.openFileInput(MyApp.FILE_NAME_SCORES_F2N);
			else if(challengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F))
				stream = this.openFileInput(MyApp.FILE_NAME_SCORES_N2F);
			else if(challengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON))
				stream = this.openFileInput(MyApp.FILE_NAME_SCORES_CONTINENT);
			if(stream != null) {
				scoresPropFile.load(stream);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scoresPropFile;
	}
	public void saveScoresPropFile(Properties scoresPropFile, String challengeMode) {
		FileOutputStream stream = null;
		try {
			if(challengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_F2N))
				stream = this.openFileOutput(MyApp.FILE_NAME_SCORES_F2N, Context.MODE_WORLD_READABLE);
			else if(challengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_N2F))
				stream = this.openFileOutput(MyApp.FILE_NAME_SCORES_N2F, Context.MODE_WORLD_READABLE);
			else if(challengeMode.equalsIgnoreCase(MyApp.PROPVALUE_SETTINGS_CHALLENGE_MODE_CON))
				stream = this.openFileOutput(MyApp.FILE_NAME_SCORES_CONTINENT, Context.MODE_WORLD_READABLE);
			if(stream != null) {
				scoresPropFile.store(stream, "");
				stream.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Properties getUserLanguage(String languageCode) {
		Properties uesrLangProp = new Properties();
		try {
			InputStream input = getResources().getAssets().open(MyApp.FILE_PREFIX_LANGUAGE+languageCode+".properties");
			BufferedReader reader = new BufferedReader(new InputStreamReader(input,"UTF-8"));
			String strProp = null;
			while((strProp = reader.readLine()) != null) {
				if(strProp.indexOf("=") > 0) {
					uesrLangProp.setProperty(strProp.split("=")[0], strProp.split("=")[1]);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return uesrLangProp;
	}

//	public List<Integer> randomRank(int listSize) {
//		return randomRank(listSize, listSize);
//	}
	public List<Integer> randomRank(int srcSize, int destSize) {
		List<Integer> destList = new ArrayList<Integer>();
		Random random = new Random();
		while (destList.size() < destSize) {
			Integer intIndex = new Integer(random.nextInt(srcSize));
			if (!destList.contains(intIndex)) {
				destList.add(intIndex);
			}
		}
		return destList;
	}

	public void sortNationName(List<EntityNation> list) {
		Collections.sort(list,new Comparator<EntityNation>(){
			public int compare(EntityNation arg0, EntityNation arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
	}

	public int convertCorrect2Score(int correct) {
		return correct * MyApp.CORRECT_AMPLIFICATION_MULTIPLE;
	}
	public int getStars(int score, int baseNumber) {
		float rate = (float)(score/MyApp.CORRECT_AMPLIFICATION_MULTIPLE)/(float)baseNumber;
		if(rate >= 0.7f && rate < 0.8f) return 1;
		else if(rate >= 0.8f && rate < 0.9f) return 2;
		else if(rate >= 0.9f) return 3;
		else return 0;
	}
	public int sumStars(Properties scoresProp) {
		Object[] arrKeys  = scoresProp.keySet().toArray();
		int totalStars = 0;
		for(int i=0;i<arrKeys.length;i++) {
			String key = (String)arrKeys[i];
			int score = Integer.parseInt(scoresProp.getProperty(key));
			int tempLevelIndex = Integer.parseInt(key.substring(key.lastIndexOf(".")+1, key.length()));
			int stars = getStars(score, MyApp.LEVELS[tempLevelIndex][1]);
			totalStars = totalStars + stars;
		}
		return totalStars;
	}
	public boolean isTablet() {
		return (getResources().getConfiguration().screenLayout & 
				Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	public int getAdViewMeasureHeight() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		if(isTablet()) {
			return (int)Math.ceil(displayMetrics.density * (float)MyApp.AD_BANNER_TABLET_HEIGHT);
		} else {
			return (int)Math.ceil(displayMetrics.density * (float)MyApp.AD_BANNER_PHONE_HEIGHT);
		}
	}
	public int getAdViewMeasureWidth() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		if(isTablet()) {
			return (int)Math.ceil(displayMetrics.density * MyApp.AD_BANNER_TABLET_WIDTH);
		} else {
			return (int)Math.ceil(displayMetrics.density * MyApp.AD_BANNER_PHONE_WIDTH);
		}
	}
	public com.google.android.gms.ads.AdSize getAdSizeForAdmob() {
		if(isTablet()) {
			return com.google.android.gms.ads.AdSize.FULL_BANNER;
		} else {
			return com.google.android.gms.ads.AdSize.BANNER;
		}
	}
	public Location getLocation() {
	    LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    Location location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
	    return location;
	}
	public String getConnectivityNetworkName() {
		ConnectivityManager connManager= (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connManager.getActiveNetworkInfo();
		if(info != null && info.isAvailable()) {
			return info.getTypeName();
		} return null;
	}
	public String getAppFilesPath(boolean isEndWithFileSeparator) {
		String strPath = null;
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			strPath =  this.getExternalFilesDir(null).getPath();
		} else {
			strPath =  this.getFilesDir().getPath();
		}
		if(isEndWithFileSeparator) return strPath+File.separator;
		else return strPath;
	}
}
