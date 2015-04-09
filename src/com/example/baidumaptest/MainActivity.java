package com.example.baidumaptest;

import java.util.List;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.DrivingRouteOverlay;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

public class MainActivity extends Activity implements BaiduMap.OnMapClickListener, OnGetGeoCoderResultListener, OnGetRoutePlanResultListener{
	// ui
	MapView mMapView;// // ��ͼView
	BaiduMap mBaiduMap;
	ImageButton settings;
	ImageButton send;
	ImageButton plantRoute;
	ImageButton myLocation;
	ImageButton targetLocation;
	
	// ��λ���
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	BitmapDescriptor mCurrentMarker;
	private LocationMode mCurrentMode;
	private Marker mMarkerA;

	//info of sms
	String recipient;
	String content;
	String plantRouteWay;
	
    String mFromAddress;
    String mMessage;
    SMSBroadcastReceiver mSMSBroadcastReceiver;
	boolean isFirstLoc = true;// �Ƿ��״ζ�λ
	GeoCoder mGeoCoderSearch = null; // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
	//��ǰλ�ã�Ŀ��λ��
	LatLng myLL, targetLL;
	
	//���·�߽ڵ����
	ImageButton before;//��һ���ڵ�
	ImageButton next;//��һ���ڵ�
    int nodeIndex = -1;//�ڵ�����,������ڵ�ʱʹ��
    RouteLine route = null;
    OverlayManager routeOverlay = null;
    boolean useDefaultIcon = false;
    private TextView popupText = null;//����view

    //��ͼ��أ�ʹ�ü̳�MapView��MyRouteMapViewĿ������дtouch�¼�ʵ�����ݴ���
    //���������touch�¼���������̳У�ֱ��ʹ��MapView����
    BaiduMap mBaidumap = null;
    //�������
    RoutePlanSearch mRoutePlanSearch = null;    // ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
	public String myLocationInfo;
	public String targetLocationInfo;
	
    
	public static final String ACTION_SMS_SENT = "com.example.baidumaptest.SMS_SENT_ACTION";
	private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        init();
        buttonSetOnClick();
        
	}
	
	/**
	 * ��ʼ��
	 */
	public void init(){
		mMapView = (MapView) findViewById(R.id.bmapView);
		before = (ImageButton)findViewById(R.id.ib_before);
		next = (ImageButton)findViewById(R.id.ib_next);
		settings = (ImageButton)findViewById(R.id.ib_settings);
		send = (ImageButton)findViewById(R.id.ib_send);
		plantRoute = (ImageButton)findViewById(R.id.ib_plant_route);
		myLocation = (ImageButton)findViewById(R.id.ib_my);
		// ��plantwayû��ʱ���ɼ�
		before.setVisibility(ImageView.INVISIBLE);
		next.setVisibility(ImageView.INVISIBLE);
		mBaiduMap = mMapView.getMap();
		readInfo();
		//------------------------MyLocation��λ----------------------------------
		//-----------------------------------------------------------------------
		// �޸�Ϊ�Զ���marker
		mCurrentMarker = null;
		mCurrentMode = LocationMode.NORMAL;
		mBaiduMap
				.setMyLocationConfigeration(new MyLocationConfiguration(
						mCurrentMode, true, mCurrentMarker));
		
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);
		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��gps
		option.setCoorType("bd09ll"); // ������������
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();
		
		// ��ʼ��GeoCoder����ģ�飬ע���¼�����
		mGeoCoderSearch = GeoCoder.newInstance();
		mGeoCoderSearch.setOnGetGeoCodeResultListener(this);
		
        // ��ʼ��RoutePlan����ģ�飬ע���¼�����
        mRoutePlanSearch = RoutePlanSearch.newInstance();
        mRoutePlanSearch.setOnGetRoutePlanResultListener(this);
		
		
	}
	/**
	 * ��ȡ���ͺ��롢�������ݣ�·�߹滮��ʽ
	 */
	public void readInfo(){
		SharedPreferences appPrefs = getSharedPreferences(
				"appPreferences",
				MODE_PRIVATE);
		recipient = appPrefs.getString("locationToTarget", "");
		content = appPrefs.getString("message", "");
		plantRouteWay =appPrefs.getString("list_preference", "");
		Toast.makeText(getBaseContext(), recipient + content +plantRouteWay ,  Toast.LENGTH_LONG).show();
	}
	/**
	 * ���ŷ��ʹ�����ʾ
	 */
	public void regsiterReceiver(){
		// Register broadcast receivers for SMS sent and delivered intents
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                boolean error = true;
                switch (getResultCode()) {
                case Activity.RESULT_OK:
                    message = "Message sent!";
                    Toast.makeText(getBaseContext(), "Message sent!", Toast.LENGTH_LONG);
                    error = false;
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    message = "Error.";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    message = "Error: No service.";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    message = "Error: Null PDU.";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    message = "Error: Radio off.";
                    Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG);
                    break;
                }
                
            }
        }, new IntentFilter(ACTION_SMS_SENT));
	}
	/**
	 * ��ť����
	 */
	public void  buttonSetOnClick(){
		
		settings.setOnClickListener(new MapButtonOnClickListener());
		send.setOnClickListener(new MapButtonOnClickListener());
		plantRoute.setOnClickListener(new MapButtonOnClickListener());
		before.setOnClickListener(new MapButtonOnClickListener());
		next.setOnClickListener(new MapButtonOnClickListener());
		myLocation.setOnClickListener(new View.OnClickListener() {
			boolean flag = true;
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(flag){
					flag=false;
					MapStatusUpdate myu = MapStatusUpdateFactory.newLatLng(myLL);
					mBaiduMap.animateMapStatus(myu);
				} 
				if(targetLL != null){
					if(!flag){
						flag=true;
						MapStatusUpdate targetu = MapStatusUpdateFactory.newLatLng(targetLL);
						mBaiduMap.animateMapStatus(targetu);
					}
				}else{
					flag=false;
					MapStatusUpdate myu = MapStatusUpdateFactory.newLatLng(myLL);
					mBaiduMap.animateMapStatus(myu);
				}
			}
		});
	}
	
	public class MapButtonOnClickListener implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.ib_settings) {
				Intent i = new Intent("com.example.baidumaptest.AppPreference");
				startActivity(i);
			} else if (id == R.id.ib_send) {
				if (TextUtils.isEmpty(recipient)) {
                    Toast.makeText(MainActivity.this, "Please enter a message recipient.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
				if (TextUtils.isEmpty(content)) {
                    Toast.makeText(MainActivity.this, "Please enter a message body.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
				SmsManager sms = SmsManager.getDefault();
				List<String> messages = sms.divideMessage(content);
				for (String message : messages) {
                    sms.sendTextMessage(recipient, null, message, PendingIntent.getBroadcast(
                            MainActivity.this, 0, new Intent(ACTION_SMS_SENT), 0), null);
                }
			} else if (id == R.id.ib_plant_route) {
				//��������ڵ��·������
		        route = null;
				before.setVisibility(ImageView.VISIBLE);
				next.setVisibility(ImageView.VISIBLE);
				
				
		       // mBaidumap.clear();
		        //�������յ���Ϣ������tranist search ��˵��������������
		        PlanNode stNode = PlanNode.withCityNameAndPlaceName("�ɶ�", myLocationInfo);
		        PlanNode enNode = PlanNode.withCityNameAndPlaceName("�ɶ�", targetLocationInfo);

		        // ʵ��ʹ�����������յ���н�����ȷ���趨
		        if (plantRouteWay.equals("drive")) {
		        	mRoutePlanSearch.drivingSearch((new DrivingRoutePlanOption())
		                    .from(stNode)
		                    .to(enNode));
		        } else if (plantRouteWay.equals("transit")) {
		        	mRoutePlanSearch.transitSearch((new TransitRoutePlanOption())
		                    .from(stNode)
		                    .city("�ɶ�")
		                    .to(enNode));
		        } else if (plantRouteWay.equals("walk")) {
		        	mRoutePlanSearch.walkingSearch((new WalkingRoutePlanOption())
		                    .from(stNode)
		                    .to(enNode));
		        }
			}
		}
		
	}
	/**
	 * ��λSDK��������
	 */
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
			if (isFirstLoc) {
				isFirstLoc = false;
				myLL = new LatLng(location.getLatitude(),
						location.getLongitude());
				
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(myLL);
				mBaiduMap.animateMapStatus(u);
				mGeoCoderSearch.reverseGeoCode(new ReverseGeoCodeOption().location(myLL));
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	
	/**
	 * �����յ���
	 */
	@Override
	protected void onStart() {
		super.onStart();
		//���ɹ㲥����  
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();  
  
        //ʵ����������������Ҫ���˵Ĺ㲥  
        IntentFilter intentFilter = new IntentFilter(ACTION);  
        intentFilter.setPriority(Integer.MAX_VALUE);  
        //ע��㲥  
        this.registerReceiver(mSMSBroadcastReceiver, intentFilter);  
  
        mSMSBroadcastReceiver.setOnReceivedMessageListener(new SMSBroadcastReceiver.MessageListener() {  
            @Override  
            public void onReceived(String message) { 
            	Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
            	
            	GetTargetInfo getTargetInfo = new GetTargetInfo();
            		
        		String locInfo = getTargetInfo.getTargetLatLng(message);
                float lnt = getTargetInfo.targrtLnt(locInfo);
                float lng = getTargetInfo.targrtLng(locInfo);
                targetLL = new LatLng(lnt, lng);
                Toast.makeText(getBaseContext(), locInfo, Toast.LENGTH_LONG).show();
              //����Markerͼ��  
                BitmapDescriptor bitmap = BitmapDescriptorFactory  
                    .fromResource(R.drawable.icon_geo);  
                //����MarkerOption�������ڵ�ͼ�����Marker  
                OverlayOptions option = new MarkerOptions()  
                    .position(targetLL) 
                    .icon(bitmap);  
                //�ڵ�ͼ�����Marker������ʾ  
                mMarkerA =  (Marker) mBaiduMap.addOverlay(option);
                MapStatusUpdate targetu = MapStatusUpdateFactory.newLatLng(targetLL);
                mGeoCoderSearch.reverseGeoCode(new ReverseGeoCodeOption().location(targetLL));
				mBaiduMap.animateMapStatus(targetu);
            }  
        });  
	}


	/**
     * �ڵ����
     */
    public void nodeClick(View v) {
        if (route == null ||
                route.getAllStep() == null) {
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.ib_before) {
        	return;
        }
        //���ýڵ�����
        if (v.getId() == R.id.ib_next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
            	nodeIndex++;
            } else {
            	return;
            }
        } else if (v.getId() == R.id.ib_before) {
        	if (nodeIndex > 0) {
        		nodeIndex--;
        	} else {
            	return;
            }
        }
        //��ȡ�ڽ����Ϣ
        LatLng nodeLocation = null;
        String nodeTitle = null;
        Object step = route.getAllStep().get(nodeIndex);
        if (step instanceof DrivingRouteLine.DrivingStep) {
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrace().getLocation();
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrace().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrace().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        //�ƶ��ڵ�������
        mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        popupText = new TextView(MainActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

    }


	@Override
	public void onMapClick(LatLng point) {
		 mBaidumap.hideInfoWindow();
	}
	

	@Override
	public boolean onMapPoiClick(MapPoi poi) {
		return false;
	}

	@Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
            // ��plantwayû��ʱ���ɼ�
    		before.setVisibility(ImageView.INVISIBLE);
    		next.setVisibility(ImageView.INVISIBLE);
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            before.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }

    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
            // ��plantwayû��ʱ���ɼ�
    		before.setVisibility(ImageView.INVISIBLE);
    		next.setVisibility(ImageView.INVISIBLE);
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            before.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
            // ��plantwayû��ʱ���ɼ�
    		before.setVisibility(ImageView.INVISIBLE);
    		next.setVisibility(ImageView.INVISIBLE);
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            before.setVisibility(View.VISIBLE);
            next.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new MyDrivingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }
    
  //����RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOverlay {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

	@Override
	public void onGetGeoCodeResult(GeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
					.show();
			return;
		}
		String strInfo = String.format("γ�ȣ�%f ���ȣ�%f",
				result.getLocation().latitude, result.getLocation().longitude);
		Toast.makeText(MainActivity.this, strInfo, Toast.LENGTH_LONG).show();
	}
	boolean flagReverseGeoCode = true;
	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ���ҵ����", Toast.LENGTH_LONG)
					.show();
			return;
		}
		Toast.makeText(MainActivity.this, result.getAddress(),
				Toast.LENGTH_LONG).show();
		if(flagReverseGeoCode){
			flagReverseGeoCode = false;
			myLocationInfo = result.getAddress();
		}if(!flagReverseGeoCode){
			targetLocationInfo = result.getAddress();
		}
	}
	
	@Override
	protected void onDestroy() {
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		mMapView.onDestroy();
		mMapView = null;
		 //ע�����ż����㲥  
		this.unregisterReceiver(mSMSBroadcastReceiver); 
		mRoutePlanSearch.destroy();
		mGeoCoderSearch.destroy();
		super.onDestroy();
		
	}
	
	@Override
	protected void onPause() {
		mMapView.onPause();
		
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
	}	
}
