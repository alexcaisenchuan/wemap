/**
 * <p>Title: WeMapAPI.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.wemap.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alex.wemap.exception.RetErrorException;
import com.alex.wemap.model.Suggest;
import com.alex.wemap.utils.KLog;
import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.net.AsyncWeiboRunner;
import com.weibo.sdk.android.net.RequestListener;

/**
 * @author caisenchuan
 *
 */
public class WeMapAPI {
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    /**
     * 服务器的地址
     */
    public static final String API_SERVER = "http://caisenchuan.com/weibo/";
    /**
     * post请求方式
     */
    public static final String HTTPMETHOD_POST = "POST";
    /**
     * get请求方式
     */
    public static final String HTTPMETHOD_GET = "GET";
    
    /////////////////请求附带参数//////////////////
    /**
     * 指明要进行的操作
     */
    public static final String PARAM_ACTION = "action";
    /**
     * session id
     */
    public static final String PARAM_SESSION_ID = "session_id";
    
    
    ///////////////网页返回的字符串解析相关//////////
    //flag
    /**请求操作是否成功*/
    public final static String FLAG_RET = "ret";
    /**请求操作的错误代码*/
    public final static String FLAG_ERR_CODE = "err_code";
    public final static String FLAG_SESSION_ID = "session_id";
    public final static String FLAG_RET_WORD_ADD = "ret_word_add"; //添加词汇是否成功的信息
    public final static String FLAG_NICKNAME = "nickname";
    public final static String FLAG_WORD_LIST = "word_list";
    public final static String FLAG_REMOTE_RID = "remote_rid";
    public final static String FLAG_VERSION_CODE = "version_code";
    //err_code
    /**请求操作成功*/
    public final static String CODE_NO_ERR = "success";
    /**请求操作失败*/
    public final static String CODE_ERROR = "error";
    public final static String CODE_ERR_DB = "err_db";
    public final static String CODE_ERR_PHONE_EXIST = "err_phone_exist";
    public final static String CODE_ERR_PASSWD = "err_passwd";
    public final static String CODE_ERR_USER_OFFLINE = "err_user_offline";
    public final static String CODE_ERR_NOT_LOGIN = "err_not_login";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**
     * 部分API要使用的session id
     */
    protected String mSessionID = "";

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 检查服务端的返回值
     * @param str 要进行检查的服务端返回字符串
     * @return true - 操作成功; false - 操作失败，有错误；
     * @throws RetErrorException 返回值错误
     * @throws JSONException JSON解析失败
     */
    public static boolean checkRet(String str) throws RetErrorException, JSONException{
        boolean ret = true;
        
        JSONObject json = new JSONObject(str);
        String retStr = json.optString(WeMapAPI.FLAG_RET, WeMapAPI.CODE_ERROR);
        if(retStr.equals(WeMapAPI.CODE_NO_ERR)) {
            ret = true;
        } else {
            String err_code = json.optString(WeMapAPI.FLAG_ERR_CODE, "");
            throw new RetErrorException(err_code);
        }
        
        return ret;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/
    /**
     * 进行网络请求
     * @param url 要请求的页面地址，不包括主机地址，只需要把页面相对路径发来即可
     * @param params
     * @param httpMethod
     * @param listener
     * @author caisenchuan
     */
    protected void request( final String url, final WeiboParameters params,
            final String httpMethod,RequestListener listener) {
        String fullUrl = API_SERVER + url;      //url不包括根路径
        AsyncWeiboRunner.request(fullUrl, params, httpMethod, listener);
    }
    
    /*--------------------------
     * private方法
     *-------------------------*/

}
