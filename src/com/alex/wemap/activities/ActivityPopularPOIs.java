/**
 * <p>Title: ActivityPopularPOIs.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-11-3
 * @version 1.0
 */
package com.alex.wemap.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.alex.wemap.R;
import com.alex.wemap.model.Position;
import com.alex.wemap.utils.OnHttpRequestReturnListener;
import com.alex.wemap.utils.SmartToast;
import com.alex.wemap.utils.KLog;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.huewu.pla.lib.MultiColumnListView;
import com.huewu.pla.lib.internal.PLA_AbsListView;
import com.huewu.pla.lib.internal.PLA_AbsListView.OnScrollListener;
import com.huewu.pla.lib.internal.PLA_AdapterView;
import com.huewu.pla.lib.internal.PLA_AdapterView.OnItemClickListener;
import com.ta.util.bitmap.TABitmapCacheWork;
import com.ta.util.bitmap.TABitmapCallBackHanlder;
import com.ta.util.bitmap.TADownloadBitmapHandler;
import com.ta.util.extend.draw.DensityUtils;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.api.PlaceAPI;
import com.weibo.sdk.android.api.WeiboAPI.SORT2;
import com.weibo.sdk.android.model.Place;
import com.weibo.sdk.android.model.Poi;
import com.weibo.sdk.android.model.PoiList;
import com.weibo.sdk.android.model.Status;
import com.weibo.sdk.android.model.WeiboException;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 热门地点微博
 * @author caisenchuan
 */
public class ActivityPopularPOIs extends BaseActivity implements OnClickListener, OnScrollListener{
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "ActivityPopularPOIs";
    
    ///////////////mBaseHandler msg what//////////////
    /**刷新列表内容*/
    public static final int MSG_REFRESH_LIST         = MSG_EXTEND_BASE + 1;
    /**打开加载提示框*/
    public static final int MSG_SHOW_LOADING_HINT    = MSG_EXTEND_BASE + 2;
    /**关闭加载提示框*/
    public static final int MSG_DISMISS_LOADING_HINT = MSG_EXTEND_BASE + 3;
    
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /**
     * 评论列表Adapter
     * @author caisenchuan
     */
    public class ListAdapter extends BaseAdapter {
        /**
         * 一个条目的ViewHolder
         */
        private class ListItemViewHolder {
            /**标题*/
            public TextView mTitle;
            /**内容*/
            public TextView mContent;
            /**图片*/
            public ImageView mPic;
        }
        
        /**
         * inflater
         */
        private LayoutInflater mInflater = null;
        
        /**
         * 构造函数
         */
        public ListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            if(mStatus != null) {
                return mStatus.size();
            } else {
                return 0;
            }
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return null;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return 0;
        }

        /* (non-Javadoc)
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemViewHolder holder = null;
            
            if(convertView == null) {
                holder = new ListItemViewHolder();
                
                convertView = mInflater.inflate(R.layout.list_item_weibo_pics, null);
                holder.mTitle = (TextView)convertView.findViewById(R.id.waterfall_title);
                holder.mContent = (TextView)convertView.findViewById(R.id.waterfall_content);
                holder.mPic = (ImageView)convertView.findViewById(R.id.waterfall_pic);
                convertView.setTag(holder);
            } else {
                holder = (ListItemViewHolder)convertView.getTag();
            }
            
            //设置条目内容
            Status status = getPosItem(position);
            //图片
            mImageFetcher.loadFormCache(status.getBmiddle_pic(), holder.mPic);
            //其他信息
            if(status != null) {
                Place place = status.getPlace();
                if(place != null) {
                    holder.mTitle.setText(place.title);
                }
                holder.mContent.setText(getStripContent(status.getText()));
            }
            
            return convertView;
        }
    }
    
    /**
     * 读取微博信息的回调函数
     * @author caisenchuan
     */
    private class GetPoisListener extends OnHttpRequestReturnListener {

        /**
         * 读取微博信息的回调函数
         * @param base 用于显示Toast的Activity对象
         */
        public GetPoisListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            try {
                List<Poi> list = null;
                if(mPoiList == null) {
                    mPoiList = new PoiList(arg0);
                    list = mPoiList.getList();
                } else {
                    list = PoiList.getPoiList(arg0);
                    mPoiList.appendList(list);
                }
                mCurrPoiPage++;
                
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    KLog.w(TAG, "Exception", e);
                }
                
                getPoiListStatuses(list);
            } catch (JSONException e) {
                KLog.w(TAG, "JSONException while build status", e);
                showToastOnUIThread(R.string.hint_json_parse_faild);
                onLoadFinish();
            } finally {
                mGettingPoiList = false;
            }
        }
        
        /* (non-Javadoc)
         * @see com.alex.wemap.utils.OnHttpRequestReturnListener#onComplete4binary(java.io.ByteArrayOutputStream)
         */
        @Override
        public void onComplete4binary(ByteArrayOutputStream arg0) {
            try {
                super.onComplete4binary(arg0);
            } finally {
                onLoadFinish();
            }
        }
        
        /* (non-Javadoc)
         * @see com.alex.wemap.utils.OnHttpRequestReturnListener#onError(com.weibo.sdk.android.WeiboException)
         */
        @Override
        public void onError(com.weibo.sdk.android.WeiboException e) {
            try {
                super.onError(e);
            } finally {
                onLoadFinish();
            }
        }
        
        /* (non-Javadoc)
         * @see com.alex.wemap.utils.OnHttpRequestReturnListener#onIOException(java.io.IOException)
         */
        @Override
        public void onIOException(IOException e) {
            try {
                super.onIOException(e);
            } finally {
                onLoadFinish();
            }
        }
    }
    
    /**
     * 读取微博信息的回调函数
     * @author caisenchuan
     */
    private class GetPoiStatusesListener extends OnHttpRequestReturnListener {

        /**
         * 读取微博信息的回调函数
         * @param base 用于显示Toast的Activity对象
         */
        public GetPoiStatusesListener(BaseActivity base) {
            super(base);
        }

        /* (non-Javadoc)
         * @see com.weibo.sdk.android.net.RequestListener#onComplete(java.lang.String)
         */
        @Override
        public void onComplete(String arg0) {
            try {
                KLog.d(TAG, "ret : " + arg0);
                List<Status> list = Status.constructStatuses(arg0);
                sendMessageToBaseHandler(MSG_REFRESH_LIST, 0, 0, list);
            } catch (com.weibo.sdk.android.org.json.JSONException e) {
                KLog.w(TAG, "Exception", e);
                showToastOnUIThread(R.string.hint_read_weibo_error);
            } catch (WeiboException e) {
                KLog.w(TAG, "Exception", e);
                showToastOnUIThread(R.string.hint_read_weibo_error);
            } finally {
                onLoadFinish();
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
            
            if(!mFirstGetPoiList) {
                getNextNearbyPois();
                mFirstGetPoiList = true;
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
            if (poiLocation == null) {
                return;
            }
        }
    }
    
    /**
     * 列表的某个条目被点击时的调用
     */
    private class MyOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(PLA_AdapterView<?> parent, View view,
                int position, long id) {
            KLog.d(TAG, "onItemClick : %d", position);
            Status status = getPosItem(position);
            if(status != null) {
                openDetailWeiboActivity(status);
            }
        }
    }
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    ////////////////////////////Views////////////////////////
    /**listview*/
    private MultiColumnListView mListContent = null;
    /**底部加载提示*/
    private View mLoadView = null;
    /**列表的Adapter*/
    private ListAdapter mAdapter = null;
    
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
    ////////////////////////////数据/////////////////////////
    /**Poi列表*/
    private PoiList mPoiList = null;
    /**查询poi列表的当前页码*/
    private int mCurrPoiPage = 0;
    /**微博列表*/
    private List<Status> mStatus = new ArrayList<Status>();
    
    ///////////////////////////标志位及计数//////////////////
    /**列表中最后一个项目的序号*/
    private int mLastItem = 0;
    /**列表中项目的总个数*/
    private int mCount = 0;
    /**是否正在读取poi列表*/
    private boolean mGettingPoiList = false;
    /**首次读取poi列表的标志位*/
    private boolean mFirstGetPoiList = false;
    
    /////////////////////////其他///////////////////////////
    /**图片缓存加载器*/
    private TABitmapCacheWork mImageFetcher = null;
    /** 定位监听器 */
    private BDLocationListener mLocListener = new MyLocationListener();
    /**位置API*/
    private PlaceAPI mPlaceApi = null;
    
    /*--------------------------
     * public方法
     *-------------------------*/
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(PLA_AbsListView view, int scrollState) {
        //KLog.d(TAG, "scrollState = " + scrollState);
        //下拉到空闲是，且最后一个item的数等于数据的总数时，进行更新
        if(mLastItem == mCount  && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if(mPoiList == null || mPoiList.hasMore()) {
                getNextNearbyPois();
            } else {
                SmartToast.showLongToast(this, R.string.hint_no_more, true);
            }
        }
    }

    @Override
    public void onScroll(PLA_AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        /*KLog.d(TAG, "firstVisibleItem = %d , visibleItemCount = %d , totalItemCount = %d",
                     firstVisibleItem, visibleItemCount, totalItemCount);
         */
        
        mLastItem = firstVisibleItem + visibleItemCount - 1;  //减1是因为上面加了个addFooterView
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.  
        super.onCreateOptionsMenu(menu);  
        //添加菜单项  
        MenuItem add=menu.add(0,0,0,"创建");
        //绑定到ActionBar    
        add.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        //绑定点击事件
        add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //拍照并创建新微博
                Intent it = new Intent(ActivityPopularPOIs.this, ActivityNewWeibo.class);
                it.putExtra(ActivityNewWeibo.INTENT_EXTRA_TAKE_PHOTO, true);
                startActivity(it);
                return false;
            }
        });
        return true; 
    }
    
    private String[] mPlanetTitles = {"A"};
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /* (non-Javadoc)
     * @see com.alex.wemap.activities.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_poi);
        
        //图片缓存相关
        TADownloadBitmapHandler downloadBitmapFetcher = new TADownloadBitmapHandler(
                this, DensityUtils.dipTopx(this, 128),
                DensityUtils.dipTopx(this, 128));
        TABitmapCallBackHanlder taBitmapCallBackHanlder = new TABitmapCallBackHanlder();
        taBitmapCallBackHanlder.setLoadingImage(this, R.drawable.empty_photo);
        mImageFetcher = new TABitmapCacheWork(this);
        mImageFetcher.setProcessDataHandler(downloadBitmapFetcher);
        mImageFetcher.setCallBackHandler(taBitmapCallBackHanlder);
        mImageFetcher.setFileCache(mApp.getFileCache());
        
        //设置drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setHomeButtonEnabled(true);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        //设置listview
        mListContent = (MultiColumnListView)findViewById(R.id.list_weibo_content);
        //底部加载提示
        mLoadView = getLayoutInflater().inflate(R.layout.footer_load, null);
        mListContent.addFooterView(mLoadView);
        //设置adapter
        mAdapter = new ListAdapter(this);
        mListContent.setAdapter(mAdapter);
        //监听滑动
        mListContent.setOnScrollListener(this);
        //监听点击
        mListContent.setOnItemClickListener(new MyOnItemClickListener());
        
        //刷新微博信息
        Oauth2AccessToken token = mApp.getAccessToken();
        if(token != null) {
            mPlaceApi = new PlaceAPI(token);
            //若位置有效，则查询周边信息，否则等位置有效后再查询
            if(mApp.getCurrentLocation().isValid()) {
                getNextNearbyPois();
                mFirstGetPoiList = true;
            }
        } else {
            if(token == null) {
                SmartToast.showShortToast(this, R.string.hint_auth_invalid, false);
            }
        }
    }
    
    @Override
    protected void onStart() {
        KLog.d(TAG, "onStart");

        //注册定位监听器
        mApp.getLocationClient().registerLocationListener(mLocListener);
        
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        KLog.d(TAG, "onStop");
        
        //取消注册定位监听器
        mApp.getLocationClient().unRegisterLocationListener(mLocListener);
        
        super.onStop();
    }
    
    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        
        // Handle action buttons
        switch(item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void handleBaseMessage(Message msg) {
        switch(msg.what) {
            case MSG_REFRESH_LIST: {
                Object obj = msg.obj;
                List<Status> list = (List<Status>)obj;
                mStatus.addAll(list);
                mCount = mStatus.size();
                if(mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;
            }
            
            case MSG_SHOW_LOADING_HINT: {
                setLoadView(true);
                break;
            }
            
            case MSG_DISMISS_LOADING_HINT: {
                setLoadView(false);
                break;
            }
            
            default: {
                KLog.w(TAG, "Unknown msg : " + msg.what);
                super.handleBaseMessage(msg);
                break;
            }
        }
    };
    
    /*--------------------------
     * private方法
     *-------------------------*/
    /**
     * 读取附近的poi信息，每调用一次，都会尝试读取下一组poi
     */
    private void getNextNearbyPois() {
        if(mGettingPoiList) {
            KLog.w(TAG, "Getting poi list already!");
        } else {
            if(mPlaceApi != null) {
                Position pos = mApp.getCurrentLocation();
                String lat = String.valueOf(pos.latitude);
                String lon = String.valueOf(pos.longtitude);
                int page = mCurrPoiPage + 1;        //读取下一组
                KLog.d(TAG, "getNextNearbyPois, lat : %s , lon : %s , page : %s", lat, lon, page);
                mPlaceApi.nearbyPois(lat,
                                     lon, 
                                     2000,
                                     "",
                                     "64",
                                     5,
                                     page,
                                     false,
                                     new GetPoisListener(this));
                
                sendMessageToBaseHandler(MSG_SHOW_LOADING_HINT);
                mGettingPoiList = true;
            }
        }
    }
    
    /**
     * 加载完成时关闭加载提示以及设置变量（无论加载成功或失败都这么做）
     */
    private void onLoadFinish() {
        sendMessageToBaseHandler(MSG_DISMISS_LOADING_HINT);
        mGettingPoiList = false;
    }
    
    /**
     * 读取一组poi的对应微博
     * @param list
     */
    private void getPoiListStatuses(List<Poi> list) {
        if(list != null) {
            for(Poi poi : list) {
                getPoiStatuses(poi);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    KLog.w(TAG, "Exception", e);
                }
            }
        }
    }
    
    /**
     * 读取某个poi对应的微博
     * @param poi
     */
    private void getPoiStatuses(Poi poi) {
        if(poi != null) {
            KLog.d(TAG, "getPoiStatuses : " + poi.poiid);
            mPlaceApi.poisPhotos(poi.poiid,
                                 10,
                                 1,
                                 SORT2.SORT_BY_TIME,
                                 false,
                                 new GetPoiStatusesListener(this));
        }
    }
    
    /**
     * 设置加载提示的显示
     * @param enable
     * @author caisenchuan
     */
    private void setLoadView(boolean enable) {
        if(mLoadView != null) {
            if(enable) {
                mLoadView.setVisibility(View.VISIBLE);
            } else {
                mLoadView.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * 读取某个位置上的项目
     */
    private Status getPosItem(int position) {
        Status ret = null;
        
        if(mStatus != null) {
            if(position >= 0 && position < mStatus.size()) {
                ret = mStatus.get(position);
            }
        }
        
        return ret;
    }
    
    /**
     * 获得处理过的正文
     * @param text
     * @return
     * @author caisenchuan
     */
    private static String getStripContent(String text) {
        String ret = "";
        
        if(!TextUtils.isEmpty(text)) {
            int i = text.indexOf("我在这里");
            if(i > 0) {
                ret = text.substring(0, i);
            }
        }
        
        return ret;
    }
    
    /**
     * 启动某条公共墙微博的Activity
     * @param sg 要查看的微博对象
     * @author caisenchuan
     */
    private void openDetailWeiboActivity(Status s) {
        if(s != null) {
            Intent intent = new Intent(ActivityPopularPOIs.this, ActivityDetailWeibo.class);
            intent.putExtra(ActivityDetailWeibo.INTENT_EXTRA_WEIBO_STATUS_OBJ, s);
            startActivity(intent);
        }
    }
}
