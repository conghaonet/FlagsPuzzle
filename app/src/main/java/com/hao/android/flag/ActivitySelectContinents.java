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
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.hh.flag.R;

public class ActivitySelectContinents extends Activity{
	private MyApp myApp;
	private Properties propFile;
	private List<String> listChoseContinents;
	private ListView continentsListView;
	List<EntityContinent> listAllContinents;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.myApp = (MyApp)getApplicationContext();
		setContentView(R.layout.select_continents);
		this.continentsListView = (ListView)findViewById(R.id.selectContinentsList);
		loadPropFile();
		MyAdapter adapter = new MyAdapter(this);
		this.continentsListView.setAdapter(adapter);
	}
	/**
	 * listview?е????????????
	 */
	public void popupNoticeMsg(){
		new AlertDialog.Builder(this)
		.setTitle("Notice")
		.setMessage(getResources().getString(R.string.select_cont_notice_msg))
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		})
		.show();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(this.listChoseContinents.size() < 1) {
				popupNoticeMsg();
			} else {
				savePropFile();
				Intent intent = new Intent();
				intent.setClass(ActivitySelectContinents.this, ActivityFreePlay.class);
				startActivity(intent);
				ActivitySelectContinents.this.finish();
			}
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	private void savePropFile() {
		String choseContinents = null;
		if(listChoseContinents != null && listChoseContinents.size()>0) {
			for (int i=0;i<listChoseContinents.size();i++) {
				if(i==0) choseContinents = listChoseContinents.get(i);
				else choseContinents = choseContinents+","+listChoseContinents.get(i);
			}
		}
		if(choseContinents !=null) propFile.setProperty(MyApp.PROPKEY_FREEPLAY_CONTINENTS, choseContinents);
		myApp.saveSettingsPropFile(propFile);
	}
	private void loadPropFile() {
		this.listAllContinents = myApp.getContinents();
		listChoseContinents = new ArrayList<String>();
		propFile = myApp.getSettingsPropFile();
		String choseContinents = propFile.getProperty(MyApp.PROPKEY_FREEPLAY_CONTINENTS);
		if(choseContinents != null && choseContinents.length()>0) {
			String[] arrChoseContinents = choseContinents.split(",");
			for(int i=0;i<arrChoseContinents.length;i++) {
				listChoseContinents.add(arrChoseContinents[i]);
			}
		}
	}


	public final class ViewHolder {
		public ImageButton imgBtn;
		public CheckBox checkBox;
		public TextView amout;
	}

	public class MyAdapter extends BaseAdapter{
		private LayoutInflater mInflater;
		public MyAdapter(Context context){
			this.mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ActivitySelectContinents.this.listAllContinents.size();
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
				convertView = mInflater.inflate(R.layout.select_continents_item, null);
				holder.imgBtn = (ImageButton)convertView.findViewById(R.id.selectContImg);
				holder.amout = (TextView)convertView.findViewById(R.id.selectContAmountText);
				holder.checkBox = (CheckBox)convertView.findViewById(R.id.selectContCb);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder)convertView.getTag();
			}
			EntityContinent tempContinent = ActivitySelectContinents.this.listAllContinents.get(position);
			holder.imgBtn.setImageResource(myApp.getContinentResourceId(tempContinent.getCode()));
			if(tempContinent.getAmount() > 0) {
				holder.amout.setText(tempContinent.getAmount()+" "+ActivitySelectContinents.this.getResources().getString(R.string.select_cont_amout));
			} else {
				holder.amout.setText(ActivitySelectContinents.this.getResources().getString(R.string.select_cont_amout_zero));
			}
			holder.checkBox.setText(tempContinent.getName());
			if(tempContinent.getAmount() <= 0) {
				holder.imgBtn.setEnabled(false);
				holder.checkBox.setChecked(false);
				holder.checkBox.setEnabled(false);
			} else {
				holder.imgBtn.setEnabled(true);
				if(ActivitySelectContinents.this.listChoseContinents.contains(tempContinent.getCode())) {
					holder.checkBox.setChecked(true);
				} else {
					holder.checkBox.setChecked(false);
				}
				holder.checkBox.setEnabled(true);
			}
			holder.imgBtn.setOnClickListener(new ContinentImgBtnClickListener(tempContinent.getCode()));
			holder.checkBox.setOnClickListener(new ContinentCheckBoxClickListener(tempContinent.getCode()));
			return convertView;
		}
	}
	class ContinentImgBtnClickListener implements OnClickListener {
		private String continentCode;
		public ContinentImgBtnClickListener(String continentCode) {
			this.continentCode = continentCode;
		}
		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(ActivitySelectContinents.this, ActivityListNations.class);
			Bundle bundle = new Bundle();
			bundle.putString("continentCode", continentCode);
			intent.putExtras(bundle);
			startActivity(intent);
//			ActivityChooseContinents.this.finish();
		}
	}
	class ContinentCheckBoxClickListener implements OnClickListener {
		private String continentCode;
		public ContinentCheckBoxClickListener(String continentCode) {
			this.continentCode = continentCode;
		}
		@Override
		public void onClick(View v) {
			if(((CheckBox)v).isChecked()) {
				if(!ActivitySelectContinents.this.listChoseContinents.contains(continentCode)) {
					ActivitySelectContinents.this.listChoseContinents.add(continentCode);
				}
			} else {
				ActivitySelectContinents.this.listChoseContinents.remove(continentCode);
			}
		}
	}

}
