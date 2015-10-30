package com.hao.android.flag;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.hh.flag.R;

public class ActivityListContinents extends Activity {
	private MyApp myApp;
//	private Properties propFile;
//	private Properties propUserLang;
//	private ListView continentsListView;
	private List<EntityContinent> listContinent;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.myApp = (MyApp)getApplicationContext();
		setContentView(R.layout.list_continents);
		Button allNationsBtn = (Button)findViewById(R.id.listContinentsAllBtn);
		ListView continentsListView = (ListView)findViewById(R.id.listContinentsList);
		listContinent = this.myApp.getContinents();
		ContinentAdapter continentAdapter = new ContinentAdapter(this);
		continentsListView.setAdapter(continentAdapter);
		allNationsBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				openNationsList(null);
			}
		});

	}
	private void openNationsList(String continentCode) {
		Intent intent = new Intent();
		intent.setClass(ActivityListContinents.this, ActivityListNations.class);
		Bundle bundle = new Bundle();
		bundle.putString("continentCode", continentCode);
		intent.putExtras(bundle);
		startActivity(intent);
//		ActivityListContinents.this.finish();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.setClass(ActivityListContinents.this, ActivityMain.class);
			startActivity(intent);
			ActivityListContinents.this.finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	final class ViewHolder {
		public ImageButton continentImg;
		public TextView continentName;
		public TextView continentAmount;
	}

	private class ContinentAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		public ContinentAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ActivityListContinents.this.listContinent.size();
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
			if(convertView == null) {
				holder=new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_continents_item, null);
				holder.continentImg = (ImageButton)convertView.findViewById(R.id.listContinentsImg);
				holder.continentName = (TextView)convertView.findViewById(R.id.listContinentsNameText);
				holder.continentAmount = (TextView)convertView.findViewById(R.id.listContinentsAmountText);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			EntityContinent tempContinent = ActivityListContinents.this.listContinent.get(position);
			holder.continentImg.setImageResource(myApp.getContinentResourceId(tempContinent.getCode()));
			holder.continentName.setText(Html.fromHtml("<u>"+tempContinent.getName()+"</u>"));
			holder.continentName.getPaint().setFakeBoldText(true);

			if(tempContinent.getAmount()>0) {
				holder.continentAmount.setText(tempContinent.getAmount()+" "+ActivityListContinents.this.getResources().getString(R.string.select_cont_amout));
				holder.continentImg.setEnabled(true);
			} else {
				holder.continentAmount.setText(ActivityListContinents.this.getResources().getString(R.string.select_cont_amout_zero));
				holder.continentImg.setEnabled(false);
			}
			holder.continentImg.setOnClickListener(new ContinentImgBtnClickListener(tempContinent.getCode()));
			holder.continentName.setOnClickListener(new ContinentNameClickListener(tempContinent.getName()));
			return convertView;
		}
	}
	class ContinentNameClickListener implements OnClickListener {
		private String continentName;
		public ContinentNameClickListener(String continentName) {
			this.continentName = continentName;
		}
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(ActivityListContinents.this, ActivityMyWebview.class);
			Bundle bundle = new Bundle();
			bundle.putString("wikiSearch", continentName);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	class ContinentImgBtnClickListener implements OnClickListener {
		private String continentCode;
		public ContinentImgBtnClickListener(String continentCode) {
			this.continentCode = continentCode;
		}
		@Override
		public void onClick(View v) {
			openNationsList(this.continentCode);
		}
	}

}
