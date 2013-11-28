/**
 * <p>Title: Suggest.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-19
 * @version 1.0
 */
package com.alex.wemap.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.alex.wemap.utils.KLog;

/**
 * 一条公共墙微博对象
 * @author caisenchuan
 *
 */
public class Suggest {
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "Suggest";
    
    //////////////时间格式///////////////
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_DEFAULT = "1970-1-1 00:00:00";
    
    //////////////在json之间解析使用的tag////////////////
    public static final String TAG_ID = "id";
    public static final String TAG_WEIBO_ID = "weibo_id";
    public static final String TAG_WEIBO_MID = "weibo_mid";
    public static final String TAG_LATITUDE = "latitude";
    public static final String TAG_LONGTITUDE = "longtitude";
    public static final String TAG_SUGGEST_USER = "suggest_user";
    public static final String TAG_LIKE_NUM = "like_num";
    public static final String TAG_TO_GROUP = "to_group";
    public static final String TAG_ADD_TIME = "add_time";
    public static final String TAG_IS_LIKE = "is_like";
    public static final String TAG_WEIBO_TEXT = "weibo_text";
    public static final String TAG_WEIBO_NICKNAME = "weibo_nickname";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**suggest表中的id*/
    public long id = -1L;
    public long weibo_id = -1L;
    public long weibo_mid = -1L;
    public double latitude = 0.0;
    public double longtitude = 0.0;
    public long suggest_user = 0L;
    public int like_num = -1;
    public int to_group = -1;
    public Date add_time = new Date(0);
    /**是否已经赞过*/
    public boolean is_like = false;
    /**微博内容*/
    public String weibo_text = "";
    /**微博作者昵称*/
    public String weibo_nickname = "";
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 无参数构造方法
     */
    public Suggest() {
        
    }
    
    /**
     * 通过json字符串构造此对象
     */
    public Suggest(String jsonStr) throws JSONException {
        this(new JSONObject(jsonStr));
    }
    
    /**
     * 通过json对象构造此对象
     */
    public Suggest(JSONObject json) throws JSONException{
        this.id = json.optLong(TAG_ID, -1L);
        this.weibo_id = json.optLong(TAG_WEIBO_ID, -1L);
        this.weibo_mid = json.optLong(TAG_WEIBO_MID, -1L);
        this.latitude = json.optDouble(TAG_LATITUDE, 0.0);
        this.longtitude = json.optDouble(TAG_LONGTITUDE, 0.0);
        this.suggest_user = json.optLong(TAG_SUGGEST_USER, -1L);
        this.like_num = json.optInt(TAG_LIKE_NUM, -1);
        
        String date = json.optString(TAG_ADD_TIME, DATETIME_DEFAULT);
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
            this.add_time = sdf.parse(date);
        } catch (ParseException e) {
            KLog.w(TAG, "Exception while build date", e);
        }
        
        int is = json.optInt(TAG_IS_LIKE, 0);
        if(is == 0) {
            this.is_like = false;
        } else {
            this.is_like = true;
        }
        
        this.weibo_text = json.optString(TAG_WEIBO_TEXT, "");
        this.weibo_nickname = json.optString(TAG_WEIBO_NICKNAME, "");
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
