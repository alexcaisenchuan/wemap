/**
 * <p>Title: SuggestAPI.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-19
 * @version 1.0
 */
package com.alex.wemap.api;

import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.net.RequestListener;

/**
 * 公共墙微博相关API
 * @author caisenchuan
 *
 */
public class SuggestAPI extends WeMapAPI{
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String URL_SUGGEST = "weibolist.php";
    
    
    
    //////////////////////////http请求相关///////////////////////////
    //params
    private static final String PARAM_WEIBO_ID = "geoweibo_id";
    private static final String PARAM_WEIBO_MID = "geoweibo_mid";
    private static final String PARAM_LATITUDE = "latitude";
    private static final String PARAM_LONGTITUDE = "longtitude";
    private static final String PARAM_WEIBO_TEXT = "weibo_text";
    private static final String PARAM_WEIBO_NICKNAME = "weibo_nickname";
    
    //values
    /**读取公共墙微博*/
    private static final String VALUE_ACTION_SUGGEST_GET = "suggest_get";
    /**添加一条公共墙微博*/
    private static final String VALUE_ACTION_SUGGEST_ADD = "suggest_add";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 公共墙微博相关API
     */
    public SuggestAPI(String session_id) {
        this.mSessionID = session_id;
    }
    
    /**
     * 读取公共墙微博列表
     * @param listener 回调函数
     * @author caisenchuan
     */
    public void getSuggestList(RequestListener listener) { 
        WeiboParameters params = new WeiboParameters();
        params.add(PARAM_SESSION_ID, this.mSessionID);
        params.add(PARAM_ACTION, VALUE_ACTION_SUGGEST_GET);
        
        request(URL_SUGGEST, params, HTTPMETHOD_POST, listener);
    }
    
    /**
     * 添加一条微博到公共墙
     * @param id 微博id
     * @param mid 微博mid
     * @param latitude 纬度
     * @param longtitude 经度
     * @param weibo_text 微博正文
     * @param weibo_nickname 微博作者
     * @param listener 回调函数
     */
    public void addSuggest(long id, long mid, 
                           double latitude, double longtitude, 
                           String weibo_text, String weibo_nickname, 
                           RequestListener listener) { 
        WeiboParameters params = new WeiboParameters();
        //基本参数
        params.add(PARAM_SESSION_ID, this.mSessionID);
        params.add(PARAM_ACTION, VALUE_ACTION_SUGGEST_ADD);
        //其他参数
        params.add(PARAM_WEIBO_ID, id);
        params.add(PARAM_WEIBO_MID, mid);
        params.add(PARAM_LATITUDE, String.valueOf(latitude));
        params.add(PARAM_LONGTITUDE, String.valueOf(longtitude));
        params.add(PARAM_WEIBO_TEXT, weibo_text);
        params.add(PARAM_WEIBO_NICKNAME, weibo_nickname);
        
        request(URL_SUGGEST, params, HTTPMETHOD_POST, listener);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
