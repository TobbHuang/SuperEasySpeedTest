package com.example.supereasyspeedtest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	int screenHeight, screenWidth;
	double CurrentSpeed=0;//��¼��ǰ�ٶȣ���λ m/s
	double HighSpeed=0;//��¼����ٶȣ���λ m/s
	double BaiduCurrentSpeed=0;
	double BaiduHighSpeed=0;
	boolean isFirstLocation=true;
	LatLng LastLocation;
	boolean isBaiduSpeed=false;

	// ��λ���
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private MyLocationConfiguration.LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker = null;

	// ��ͼʵ��
	private MapView mMapView = null;
	private BaiduMap mBaiduMap;
	
	TextView tv_currentspeed,tv_highspeed,tv_BaiduCurrentSpeed,tv_BaiduHighSpeed,tv_location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		// ��Ļ����
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenHeight = metric.heightPixels;
		screenWidth = metric.widthPixels;

		// ��ʼ����ͼ���������Ҫ�в���Ҫ������ִ��
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		
		//find���ؼ�
		tv_currentspeed=(TextView)findViewById(R.id.tv_currentspeed);
		tv_highspeed=(TextView)findViewById(R.id.tv_highspeed);
		tv_BaiduCurrentSpeed=(TextView)findViewById(R.id.tv_BaiduCurrentSpeed);
		tv_BaiduHighSpeed=(TextView)findViewById(R.id.tv_BaiduHighSpeed);
		tv_location=(TextView)findViewById(R.id.tv_location);
		
		//��̬���ڵ�ͼ��С
		LinearLayout ll_mapframe=(LinearLayout)findViewById(R.id.ll_mapframe);
		ll_mapframe.getLayoutParams().height=(int) (screenHeight*0.5);

		// ��õ�ͼʵ��
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();

		// ɾ�����ſؼ�
		//mMapView.removeViewAt(2);

		// ��λ
		mCurrentMode = LocationMode.NORMAL;
		mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
				mCurrentMode, true, mCurrentMarker));
		mBaiduMap.setMyLocationEnabled(true);
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������ option.setScanSpan(1000);
		option.setScanSpan(1000);//���÷���λ����ļ��ʱ��Ϊ1000ms
		option.setIsNeedAddress(true);//���صĶ�λ���������ַ��Ϣ
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		Button btn_reset=(Button)findViewById(R.id.btn_reset);
		btn_reset.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HighSpeed=0;
				tv_highspeed.setText("0m/s  0km/h");
				BaiduHighSpeed=0;
				tv_BaiduHighSpeed.setText("0m/s  0km/h");
			}
		});

	}

	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || mMapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);

			LatLng ll = new LatLng(location.getLatitude(),
					location.getLongitude());
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
			mBaiduMap.animateMapStatus(u);
			
			if(isFirstLocation)
				isFirstLocation=false;
			else{
				//���ݶ�λ���ٶ�
				CurrentSpeed=DistanceUtil.getDistance(ll, LastLocation);
				
				//ȡ��λС��
				BigDecimal b = new BigDecimal(CurrentSpeed);
				CurrentSpeed = b.setScale(4, BigDecimal.ROUND_HALF_UP)
						.doubleValue();
				
				tv_currentspeed.setText(CurrentSpeed+"m/s  "+CurrentSpeed*3.6+"km/h");
				if(CurrentSpeed>HighSpeed){
					HighSpeed=CurrentSpeed;
					tv_highspeed.setText(HighSpeed+"m/s  "+HighSpeed*3.6+"km/h");
				}
				
				//�ٶ�SDK�ṩ���ٶ�
				if(location.hasSpeed()){
					isBaiduSpeed=true;
					BaiduCurrentSpeed = location.getSpeed();
					BigDecimal b1 = new BigDecimal(BaiduCurrentSpeed);
					BaiduCurrentSpeed = b1
							.setScale(4, BigDecimal.ROUND_HALF_UP)
							.doubleValue();
					tv_BaiduCurrentSpeed.setText(BaiduCurrentSpeed/3.6+"m/s  "+BaiduCurrentSpeed+"km/h");
					if(BaiduCurrentSpeed>BaiduHighSpeed){
						BaiduHighSpeed=BaiduCurrentSpeed;
						tv_BaiduHighSpeed.setText(BaiduHighSpeed/3.6+"m/s  "+BaiduHighSpeed+"km/h");
					}
				}
				else{
					tv_BaiduCurrentSpeed.setText("�����ٶ�����");
					isBaiduSpeed=false;
				}
				
				
				//���켣
				drawLine(LastLocation,ll);
				
			}
			
			if(location.hasAddr())
				tv_location.setText(location.getAddrStr());
			else
				tv_location.setText("");
			
			
			LastLocation=ll;		

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	
	//�����ٶ��ڵ�ͼ�ϻ��켣��
	void drawLine(LatLng l1,LatLng l2){
		List<LatLng> points = new ArrayList<LatLng>();
		points.add(l1);
		points.add(l2);
		
		//����׼ȷ�ٶ���Ϣʱ��ʾ��ɫ
		int color=getResources().getColor(R.color.black);
		if(isBaiduSpeed){
			if(BaiduCurrentSpeed<50)
				color=getResources().getColor(R.color.green);
			else if(BaiduCurrentSpeed<100)
				color=getResources().getColor(R.color.yellow);
			else if(BaiduCurrentSpeed<150)
				color=getResources().getColor(R.color.orange);
			else if(BaiduCurrentSpeed<200)
				color=getResources().getColor(R.color.pink);
			else
				color=getResources().getColor(R.color.red);
		}
				
		OverlayOptions ooPolyline = new PolylineOptions().width(10)
				.color(color).points(points);
		mBaiduMap.addOverlay(ooPolyline);
	}
	
	@Override
	protected void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		super.onDestroy();
	}

}
