/**
 * <p>Title: UserAPI.java</p>
 * <p>Description: 用户相关API</p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-8
 * @version 1.0
 */
package com.alex.wemap.api;

import com.weibo.sdk.android.WeiboParameters;
import com.weibo.sdk.android.net.RequestListener;

/**
 * 用户相关API
 * @author caisenchuan
 *
 */
public class UserAPI extends WeMapAPI{
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String URL_USER = "user.php";
    
    private static final String PARAM_ACCESS_TOKEN = "access_token";
    private static final String PARAM_WEIBO_USERID = "weibo_userid";
    
    private static final String VALUE_ACTION_LOGIN = "login";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 构造函数
     */
    public UserAPI() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 用户登录
     * @param access_token
     * @param weibo_userid
     * @param listener
     * @author caisenchuan
     */
    public void login(String access_token, String weibo_userid,
            RequestListener listener) {
        
        WeiboParameters params = new WeiboParameters();
        params.add(PARAM_ACTION, VALUE_ACTION_LOGIN);
        params.add(PARAM_ACCESS_TOKEN, access_token);
        params.add(PARAM_WEIBO_USERID, weibo_userid);
        
        request(URL_USER, params, HTTPMETHOD_POST, listener);
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
