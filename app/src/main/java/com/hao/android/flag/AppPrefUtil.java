package com.hao.android.flag;

import org.hh.flag.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class AppPrefUtil {
	public static String getAdsXmlLastModified(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_ADS_XML_LASTMODIFIED_KEY), null);
	}
	public static void setAdsXmlLastModified(Context context, Editor editor, String lastModified) {
		editor.putString(context.getString(R.string.PREF_ADS_XML_LASTMODIFIED_KEY), lastModified);
	}

	public static String getAdBannerId(Context context, SharedPreferences pref) {
		if(pref == null) pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getString(context.getString(R.string.PREF_AD_UNIT_ID_KEY), context.getString(R.string.ad_banner_id));
	}
	public static void setAdBannerId(Context context, Editor editor, String adBannerId) {
		editor.putString(context.getString(R.string.PREF_AD_UNIT_ID_KEY), adBannerId);
	}
}
