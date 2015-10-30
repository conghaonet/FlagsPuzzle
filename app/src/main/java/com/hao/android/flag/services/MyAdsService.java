package com.hao.android.flag.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.xmlpull.v1.XmlPullParser;

import com.hao.android.flag.AppConstants;
import com.hao.android.flag.AppPrefUtil;
import com.hao.android.flag.AppTools;
import com.hao.android.flag.MyApp;
import com.hao.android.flag.MyHttpClient;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Xml;

public class MyAdsService extends IntentService {
	private static final String TAG = MyAdsService.class.getName();
	private static final String ADS_XML_FILENAME="ads.xml";
	private MyApp myApp;
	
	public MyAdsService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		boolean isSuccessful = false;
		if(this.myApp == null) this.myApp = (MyApp)this.getApplicationContext();
		isSuccessful = downloadAdsXml();
		if(isSuccessful) {
			isSuccessful = updateAdId();
		}
		if(!isSuccessful) {
			AppTools.tableUnfinishedServices.put(this.getClass().getName(), this.getClass());
		}
	}
	private boolean downloadAdsXml() {
		boolean isSuccessful = false;
		long onlineContentLength = -1;
		String onlineLastModified = null;
		File adsFile = new File(myApp.getAppFilesPath(true)+ADS_XML_FILENAME);
		String localLastModified = AppPrefUtil.getAdsXmlLastModified(this, null);
		if(ResourceServerConstants.ENABLED_SERVER.equalsIgnoreCase(ResourceServerConstants.SERVER_VPS_ALIYUN)) {
			HttpGet httpGet = new HttpGet(ResourceServerConstants.VpsAliyun.ADS_XML_URL);
			byte[] buffer = new byte[2048];
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			try {
				HttpResponse response = MyHttpClient.getInstance().execute(httpGet);
				if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					onlineContentLength = response.getEntity().getContentLength();
					try {
						onlineLastModified = response.getFirstHeader("Last-Modified").getValue();
					} catch(Exception e){}
					if(adsFile.length() != onlineContentLength || localLastModified==null || onlineLastModified==null || !localLastModified.equalsIgnoreCase(onlineLastModified)) {
						bis = new BufferedInputStream(response.getEntity().getContent());
						bos = new BufferedOutputStream(new FileOutputStream(adsFile));
						int len = -1;
						while ((len = bis.read(buffer)) != -1) {
							bos.write(buffer, 0, len);
						}
						bos.flush();
						SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
						Editor editor = pref.edit();
						AppPrefUtil.setAdsXmlLastModified(this, editor, onlineLastModified);
						editor.commit();
					}
					isSuccessful = true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(httpGet != null && !httpGet.isAborted()) httpGet.abort();
				if(bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		} else if(ResourceServerConstants.ENABLED_SERVER.equalsIgnoreCase(ResourceServerConstants.SERVER_OSS_ALIYUN)) {
			
		}
		return isSuccessful;
	}
	private boolean updateAdId() {
		boolean isSuccessful = false;
		String adUnitId = null;
		String adDefaultId = null;
		BufferedInputStream bis = null;
		try{
			File adsFile = new File(myApp.getAppFilesPath(true)+ADS_XML_FILENAME);
			if(!adsFile.exists() || adsFile.length()<=0) return isSuccessful;
			bis = new BufferedInputStream(new FileInputStream(adsFile));
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(bis, "UTF-8");
			int eventType = parser.getEventType();
			boolean blnCategoryMatched =false;
			while(eventType!=XmlPullParser.END_DOCUMENT && adUnitId == null){
				switch(eventType){
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if(AdsXml.NODE_CATEGORY.equals(parser.getName())) {
						if(AppConstants.XML_CATEGORY_NAME.equals(parser.getAttributeValue("", AdsXml.ATTR_NAME))) {
							blnCategoryMatched = true;
							adDefaultId = parser.getAttributeValue("", AdsXml.ATTR_ADID);
							String force = parser.getAttributeValue("", AdsXml.ATTR_FORCE);
							if(force != null && Boolean.parseBoolean(force)) {
								adUnitId = adDefaultId;
							}
						}
					} else if(blnCategoryMatched) {
						if(AdsXml.NODE_AD.equals(parser.getName())) {
							if(AppConstants.XML_CODE.equals(parser.getAttributeValue("", AdsXml.ATTR_NAME))) {
								adUnitId = parser.getAttributeValue("", AdsXml.ATTR_ADID);
							}
						}
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				if(adUnitId != null) break;
				eventType = parser.next();
			}
			if(adUnitId == null) adUnitId = adDefaultId;
			if(adUnitId != null) {
				String oldId = AppPrefUtil.getAdBannerId(this, null);
				if(oldId == null || !oldId.equalsIgnoreCase(adUnitId)) {
					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
					Editor editor = pref.edit();
					AppPrefUtil.setAdBannerId(this, editor, adUnitId);
					editor.commit();
					Intent tempIntent = new Intent(this.getPackageName() + MyAdsReceiver.ACTION_AD_CHANGED);
					sendBroadcast(tempIntent);
				}
			}
			isSuccessful = true;
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(bis != null) {
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSuccessful;
	}
	@Override
    public void onDestroy() {
		super.onDestroy();
	}

	private static final class AdsXml {
		private static final String NODE_CATEGORY="category";
		private static final String NODE_AD="ad";
		private static final String ATTR_NAME="name";
		private static final String ATTR_FORCE="force";
		private static final String ATTR_ADID="adid";
	}
	
}
