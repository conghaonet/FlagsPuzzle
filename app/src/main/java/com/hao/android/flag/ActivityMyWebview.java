package com.hao.android.flag;

import org.hh.flag.R;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.HttpAuthHandler;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class ActivityMyWebview extends Activity {
	private WebView myWebView;
	private ProgressBar myProgressBar;
	private Handler mHandler;
	private static final int HANDLER_MSG_MENU_OPEN = 1;
	private static final int HANDLER_MSG_MENU_CLOSE = 2;
	private String wikiSearch;
	private MyApp myApp;
	private AdView adView;
	private FrameLayout adLayout;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mywebview);
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
		try {
			this.wikiSearch = bundle.getString("wikiSearch");
		} catch(Exception e) {
			e.printStackTrace();
		}

		this.myProgressBar = (ProgressBar)findViewById(R.id.mywebviewProgressBar);
		myProgressBar.setIndeterminate(true);

		myWebView = (WebView)findViewById(R.id.mywebviewWebView);
		//设置WebView属性，能够执行JavaScript脚本
		myWebView.getSettings().setJavaScriptEnabled(true);
		//设置可以支持缩放
		myWebView.getSettings().setSupportZoom(true);
		//设置默认缩放方式尺寸是far
		myWebView.getSettings().setDefaultZoom(ZoomDensity.FAR);
		//设置出现缩放工具
		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.setWebViewClient(new MyWebViewClient());
		//加载URL内容
		if(this.wikiSearch != null) {
			this.wikiSearch = this.wikiSearch.trim().replaceAll(" ", "%20");
			Log.d(this.getClass().getName(),"=======wikiSearch="+wikiSearch);
			myWebView.loadUrl("http://en.m.wikipedia.org/wiki?search="+this.wikiSearch);
		} else {
			myWebView.loadUrl("http://en.m.wikipedia.org");
		}
		//设置web视图客户端



		this.mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case ActivityMyWebview.HANDLER_MSG_MENU_OPEN:
					ActivityMyWebview.this.openOptionsMenu();
					break;
				case ActivityMyWebview.HANDLER_MSG_MENU_CLOSE:
					ActivityMyWebview.this.closeOptionsMenu();
					break;
				default:
					break;
				}
			}
		};
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	@Override
	public void onBackPressed() {
		if((myWebView != null) && myWebView.canGoBack()) {
			myWebView.goBack();
		} else {
			this.finish();
		}
	}
	@Override
	public void onResume() {
//		new MyMenuThread().start();
		super.onResume();
	}
	//设置回退
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mywebview_menu, menu);
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem closeItem = menu.findItem(R.id.mywebviewMenuCloseItem);
		return true;
	}
	@Override
	public void openOptionsMenu() {
		super.openOptionsMenu();
	}
	@Override
	public void closeOptionsMenu() {
		super.closeOptionsMenu();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int itemId = item.getItemId();
		switch(itemId) {
		case R.id.mywebviewMenuCloseItem:
			this.finish();
			break;
		default:
			break;
		}
		return true;
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
	//web视图客户端
	public class MyWebViewClient extends WebViewClient {
		//shouldOverviewUrlLoading
		//shouldOverrideUrlLoading
		@Override
		public boolean shouldOverrideUrlLoading(WebView view,String url) {
			view.loadUrl(url);
			return true;
		}
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
//			Toast.makeText(getApplicationContext(), "onPageStarted url="+url, Toast.LENGTH_SHORT).show();
			ActivityMyWebview.this.myProgressBar.setVisibility(ProgressBar.VISIBLE);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
//			Toast.makeText(getApplicationContext(), "onPageFinished url="+url, Toast.LENGTH_SHORT).show();
			ActivityMyWebview.this.myProgressBar.setVisibility(ProgressBar.GONE);
			super.onPageFinished(view, url);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
//			Toast.makeText(getApplicationContext(), "onLoadResource url="+url, Toast.LENGTH_SHORT).show();
			super.onLoadResource(view, url);
		}
		@Override
		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
//			Toast.makeText(getApplicationContext(), "onReceivedHttpAuthRequest host="+host+" realm="+realm, Toast.LENGTH_SHORT).show();
			return;
		}

	}
	class MyMenuThread extends Thread {
		public void run() {
			try {
				ActivityMyWebview.this.mHandler.sendMessage(ActivityMyWebview.this.mHandler.obtainMessage(ActivityMyWebview.HANDLER_MSG_MENU_OPEN));
				Thread.sleep(1000);
				ActivityMyWebview.this.mHandler.sendMessage(ActivityMyWebview.this.mHandler.obtainMessage(ActivityMyWebview.HANDLER_MSG_MENU_CLOSE));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
