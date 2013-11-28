/**
 * <p>Title: ActivityMain.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.wemap.activities;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.alex.wemap.R;
import com.alex.wemap.api.SuggestAPI;
import com.alex.wemap.api.UserAPI;
import com.alex.wemap.api.WeMapAPI;
import com.alex.wemap.exception.RetErrorException;
import com.alex.wemap.model.Suggest;
import com.alex.wemap.model.SuggestList;
import com.alex.wemap.model.SuggestAndMapMark;
import com.alex.wemap.utils.OnHttpRequestReturnListener;
import com.alex.wemap.utils.SmartToast;
import com.alex.wemap.utils.KLog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

/**
 * 主界面
 * @author caisenchuan
 *
 */
public class ActivityMap extends BaseActivity implements OnClickListener{
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 注册请求的响应处理
     */
    private class LoginListener extends OnHttpRequestReturnListener  {

        /**
         * 注册请求的响应处理
         * @param base 用于显示Toast的Activity对象
         */
        public LoginListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            JSONObject res;
            try {
                res = new JSONObject(arg0);
                String web_ret = res.getString(WeMapAPI.FLAG_RET);
                if(web_ret.equals(WeMapAPI.CODE_NO_ERR)) {
                    String session = res.optString(WeMapAPI.FLAG_SESSION_ID, "");
                    mApp.setSessionID(session);
                    showToastOnUIThread(getString(R.string.hint_login_success) + ",\n session : " + session);
                    refreshSuggest();
                } else {
                    String err_code = res.getString(WeMapAPI.FLAG_ERR_CODE);
                    Log.d(TAG, "userLogin , err_code : " + err_code);
                    showToastOnUIThread(getString(R.string.hint_login_faild) + ",\n err code : " + err_code);
                }
            } catch (Exception e) {
                KLog.w(TAG, "Exception while login complete", e);
            }
        }
    }
    
    /**
     * 读取公共墙的回调监听类
     * @author caisenchuan
     *
     */
    private class SuggestGetListener extends OnHttpRequestReturnListener {

        /**
         * @param base
         */
        public SuggestGetListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            try {
                mSuggestList = new SuggestList(arg0);
                sendMessageToBaseHandler(MSG_ADD_SUGGEST_LIST);
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException while build SuggestList", e);
                showToastOnUIThread(getString(R.string.hint_json_parse_faild));
            } catch (RetErrorException e) {
                KLog.w(TAG, "RetErrorException while build SuggestList : " + e.getErrCode(), e);
                showToastOnUIThread(getString(R.string.hint_ret_error) + e.getErrCode());
            }
        }
        
    }
    
    /**
     * 百度定位的监听器
     */
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null) {
                return;
            }
            
            //通知google地图更新所在位置
            if(mLocChangedListener != null) {
                //构造Location对象
                Location loc = new Location("BaiduLocationProvider");
                loc.setLatitude(location.getLatitude());
                loc.setLongitude(location.getLongitude());
                loc.setAccuracy(location.getRadius());
                if(location.getLocType() == BDLocation.TypeGpsLocation) {
                    loc.setSpeed(location.getSpeed());
                    loc.setAltitude(location.getAltitude());
                }
                
                mLocChangedListener.onLocationChanged(loc);
            }
            
            //第一次定位还要移动地图到所在位置
            if(mFirstLocate) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL);
                mMap.animateCamera(update);
                mFirstLocate = false;
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }

    /**
     * 自定义位置信息提供器，用于google地图上展示当前位置使用
     */
    private class MyLocationProvider implements LocationSource {

        @Override
        public void activate(OnLocationChangedListener listener) {
            SmartToast.showLongToast(ActivityMap.this, R.string.hint_activate, true);
            mLocChangedListener = listener;
        }

        @Override
        public void deactivate() {
            SmartToast.showLongToast(ActivityMap.this, R.string.hint_deactivate, true);
        }
        
    }
    
    /**
     * 某个Maker被点击时调用的方法
     * 
     * @author caisenchuan
     */
    private class MyOnMarkClickListener implements OnMarkerClickListener {

        /* (non-Javadoc)
         * @see com.google.android.gms.maps.GoogleMap.OnMarkerClickListener#onMarkerClick(com.google.android.gms.maps.model.Marker)
         */
        @Override
        public boolean onMarkerClick(Marker marker) {
            if(mSuggestAndMapMark != null) {
                SuggestList list = mSuggestAndMapMark.getSuggestList(marker);
                if(list != null && list.size() > 0) {
                    marker.setTitle(list.size() + getString(R.string.text_weibo_num));
                    setSuggestListToListView(list);     //设置列表数据
                    showSuggestListView(true);      //显示列表
                }
            }
            return false;
        }
        
    }
    
    /**
     * 地图点击事件
     * @author caisenchuan
     */
    private class MyOnMapClickListener implements OnMapClickListener {

        /* (non-Javadoc)
         * @see com.google.android.gms.maps.GoogleMap.OnMapClickListener#onMapClick(com.google.android.gms.maps.model.LatLng)
         */
        @Override
        public void onMapClick(LatLng point) {
            showSuggestListView(false);
        }
        
    }
    
    /**
     * 公共墙微博列表中的某一项被点击时的事件处理
     * @author caisenchuan
     */
    private class OnSuggestListItemClickListener implements OnItemClickListener {

        /* (non-Javadoc)
         * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Suggest sg = getSuggestOfCurrentList(position);
            openDetailWeiboActivity(sg);
        }
        
    }
    
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityMain";

    /** 默认的缩放级别*/
    private static final float DEFAULT_ZOOM_LEVEL = 16.0f;
    
    ///////////////msg type///////////////////
    /**将公共墙微博添加到地图上*/
    private static final int MSG_ADD_SUGGEST_LIST = MSG_EXTEND_BASE + 1;
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    ///////////////全局成员////////////////////
    
    ///////////////地图与定位////////////////////
    /** 全局地图对象 */
    private GoogleMap mMap = null;

    /** 定位监听器 */
    private BDLocationListener mLocListener = new MyLocationListener();
    
    /** google地图位置变化监听器 */
    private OnLocationChangedListener mLocChangedListener = null;
    
    /** 进来后的第一次定位，把地图位置显示到用户所在位置*/
    private boolean mFirstLocate = true;
    
    ////////////////数据成员/////////////////////
    /**公共墙微博列表*/
    private SuggestList mSuggestList = null;
    /**公共墙与Marker的Map*/
    private SuggestAndMapMark mSuggestAndMapMark = null;
    /**微博列表的Adapter*/
    private ArrayAdapter<String> mSuggestListAdapter = null;
    /**当前微博列表，对应某个坐标点的微博集合*/
    private SuggestList mCurrentSuggestList = null;
    
    ////////////////界面元素/////////////////////
    private Button mRefreshButton = null;
    private Button mBackButton = null;
    private ImageButton mAddWeiboButton = null;
    private ListView mSuggestListView = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.button_refresh:
                //刷新
                refreshSuggest();
                break;
                
            case R.id.button_nearby: {
                //poi列表
                Intent it = new Intent(ActivityMap.this, ActivityPopularPOIs.class);
                startActivity(it);
                break;
            }
                
            case R.id.button_new_weibo: {
                //拍照并创建新微博
                Intent it = new Intent(ActivityMap.this, ActivityNewWeibo.class);
                it.putExtra(ActivityNewWeibo.INTENT_EXTRA_TAKE_PHOTO, true);
                startActivity(it);
                break;
            }
            
            default:
                break;
        }
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //发起登录请求
        UserAPI user = new UserAPI();
        user.login(mApp.getAccessToken().getToken(), mApp.getWeiboUserid(), new LoginListener(this));
        
        //设置定位相关
        mApp.getLocationClient().registerLocationListener(mLocListener);
        
        //注册地图
        setUpMapIfNeeded();
        mMap.setMyLocationEnabled(true);
        mMap.setLocationSource(new MyLocationProvider());
        mMap.setOnMarkerClickListener(new MyOnMarkClickListener());
        mMap.setOnMapClickListener(new MyOnMapClickListener());
        
        //////////////设置界面元素//////////////
        //刷新按钮
        mRefreshButton = (Button)findViewById(R.id.button_refresh);
        mRefreshButton.setOnClickListener(this);
        //返回按钮
        mBackButton = (Button)findViewById(R.id.button_nearby);
        mBackButton.setOnClickListener(this);
        //添加按钮
        mAddWeiboButton = (ImageButton)findViewById(R.id.button_new_weibo);
        mAddWeiboButton.setOnClickListener(this);
        //微博列表
        mSuggestListAdapter = new ArrayAdapter<String>(this, R.layout.list_item_weibo_simple);
        mSuggestListView = (ListView)findViewById(R.id.list_weibo);
        mSuggestListView.setAdapter(mSuggestListAdapter);
        mSuggestListView.setOnItemClickListener(new OnSuggestListItemClickListener());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        KLog.d(TAG, "onResume");
        
        //刷新公共墙微博
        refreshSuggest();
    }

    @Override
    protected void onPause() {
        KLog.d(TAG, "onPause");
        
        super.onPause();
    }
    
    @Override
    protected void onStart() {
        KLog.d(TAG, "onStart");

        //启动location client
        if (mApp.getLocationClient() != null) {
            mApp.getLocationClient().start();
        }
        
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        KLog.d(TAG, "onStop");

        //停止location client
        if (mApp.getLocationClient() != null) {
            mApp.getLocationClient().stop();
        }
        
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        KLog.d(TAG, "onDestory");
        //设置定位相关
        mApp.getLocationClient().unRegisterLocationListener(mLocListener);
        
        super.onDestroy();
    }
    
    /* (non-Javadoc)
     * @see com.alex.wemap.activities.BaseActivity#handleBaseMessage(android.os.Message)
     */
    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_ADD_SUGGEST_LIST: {
                addSuggestToMap(mSuggestList);
                break;
            }
        
            default: {
                super.handleBaseMessage(msg);
                break;
            }
        }
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 设置地图对象 摘自：google开发文档
     * */
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            if (mMap != null) {
                //...
            }
        }
    }
    
    /**
     * 刷新公共墙微博
     * @author caisenchuan
     */
    private void refreshSuggest() {
        String session_id = mApp.getSessionID();
        if(!TextUtils.isEmpty(session_id)) {
            //调用API，请求数据
            SuggestAPI api = new SuggestAPI(session_id);
            api.getSuggestList(new SuggestGetListener(this));
        } else {
            showToastOnUIThread(getString(R.string.hint_logining));
        }
    }
    
    /**
     * 把公共墙微博添加到地图上
     * @author caisenchuan
     * @param list
     */
    private void addSuggestToMap(SuggestList list) {
        if(mMap != null && list != null) {
            mMap.clear();       //清空原来的标记
            mSuggestAndMapMark = new SuggestAndMapMark(list, mMap);
        }
    }
    
    /**
     * 把公共墙微博列表设置到界面上
     * @param list
     * @author caisenchuan
     */
    private void setSuggestListToListView(SuggestList list) {
        if(list != null && mSuggestListAdapter != null) {
            //记录当前
            mCurrentSuggestList = list;
            //先清空原来的列表
            mSuggestListAdapter.clear();
            //把所有公共墙微博添加到列表中
            ArrayList<Suggest> sg_list = list.getSuggestList();
            for(Suggest st : sg_list) {
                String str = String.format("%s:%s", st.weibo_nickname, st.weibo_text);
                mSuggestListAdapter.add(str);
            }
            //刷新界面
            mSuggestListAdapter.notifyDataSetChanged();
        }
    }
    
    /**
     * 显示或隐藏公共墙微博列表
     * @param show
     * @author caisenchuan
     */
    private void showSuggestListView(boolean show) {
        if(mSuggestListView != null) {
            if(show) {
                mSuggestListView.setVisibility(View.VISIBLE);
            } else {
                mSuggestListView.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * 从当前显示的微博列表中读取某条微博
     * @param index 要读取的微博的编号
     * @return
     * @author caisenchuan
     */
    private Suggest getSuggestOfCurrentList(int index) {
        Suggest ret = null;
        if(mCurrentSuggestList != null) {
            if(index >= 0 && index < mCurrentSuggestList.size()) {
                ret = mCurrentSuggestList.get(index);
            }
        }
        
        return ret;
    }
    
    /**
     * 启动某条公共墙微博的Activity
     * @param sg 要查看的公共墙微博对象
     * @author caisenchuan
     */
    private void openDetailWeiboActivity(Suggest sg) {
        if(sg != null) {
            Intent intent = new Intent(ActivityMap.this, ActivityDetailWeibo.class);
            intent.putExtra(ActivityDetailWeibo.INTENT_EXTRA_WEIBO_MID, sg.weibo_mid);
            intent.putExtra(ActivityDetailWeibo.INTENT_EXTRA_WEIBO_TEXT, sg.weibo_text);
            intent.putExtra(ActivityDetailWeibo.INTENT_EXTRA_WEIBO_NICKNAME, sg.weibo_nickname);
            startActivity(intent);
        }
    }
}
