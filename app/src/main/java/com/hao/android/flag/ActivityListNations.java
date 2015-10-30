package com.hao.android.flag;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.hh.flag.R;

public class ActivityListNations extends ListActivity {
	private MyApp myApp;
	private Properties propFile;
	private Properties propUserLang;
	private boolean blnUserLang;
	private List<EntityNation> listNation;
	private String continentCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.myApp = (MyApp)getApplicationContext();
		this.getListView().setBackgroundResource(R.drawable.res_bg_settings);
		this.getListView().setCacheColorHint(Color.TRANSPARENT);
		this.propFile = this.myApp.getSettingsPropFile();
		this.blnUserLang = Boolean.valueOf(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_DISPLAY_USER_LANG));
		if(this.blnUserLang) {
			this.propUserLang = this.myApp.getUserLanguage(this.propFile.getProperty(MyApp.PROPKEY_SETTINGS_USER_LANG));
		}
		this.getListView().setDividerHeight(2);
		Bundle bundle = this.getIntent().getExtras();
		try {
			this.continentCode = bundle.getString("continentCode");
		} catch(Exception e) {
			e.printStackTrace();
		}
		initListNation();
		MyNationAdapter adapter = new MyNationAdapter(this);
		setListAdapter(adapter);
	}

	private void initListNation() {
		this.listNation = new ArrayList<EntityNation>();
		List<EntityNation> tempListNation = myApp.getNations();
		if(this.continentCode == null) {
			this.listNation = tempListNation;
			return;
		} else {
			while(!tempListNation.isEmpty()) {
				EntityNation tempNation = tempListNation.remove(0);
				if(this.continentCode.equalsIgnoreCase(tempNation.getContinentCode())) {
					this.listNation.add(tempNation);
				}
			}
		}
	}


	// ListView 中某项被选中后的逻辑
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setClass(ActivityListNations.this, ActivityMyWebview.class);
		Bundle bundle = new Bundle();
		bundle.putString("wikiSearch", this.listNation.get(position).getName());
		intent.putExtras(bundle);
		startActivity(intent);
	}


	private final static class ViewHolder {
		public ImageView flagImg;
		public TextView nameText;
	}

	public class MyNationAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		public MyNationAdapter(Context context){
			this.mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ActivityListNations.this.listNation.size();
		}
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null) {
				holder=new ViewHolder();
				convertView = mInflater.inflate(R.layout.list_nations, null);
				holder.flagImg = (ImageView)convertView.findViewById(R.id.selectContNationImageView);
				holder.nameText = (TextView)convertView.findViewById(R.id.selectContNationNameTextView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			EntityNation tempNation = ActivityListNations.this.listNation.get(position);
			holder.flagImg.setImageResource(ActivityListNations.this.myApp.getFlagThumbnailResourceId(tempNation.getA2()));
			holder.nameText.setText(tempNation.getName());
			if(ActivityListNations.this.blnUserLang) {
				holder.nameText.setText(holder.nameText.getText()+"\n\n"
						+ActivityListNations.this.propUserLang.getProperty(tempNation.getA2(),""));
			}
			return convertView;
		}
	}
}
