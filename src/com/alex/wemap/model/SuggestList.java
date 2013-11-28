/**
 * <p>Title: SuggestList.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-19
 * @version 1.0
 */
package com.alex.wemap.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alex.wemap.api.WeMapAPI;
import com.alex.wemap.exception.RetErrorException;
import com.alex.wemap.utils.KLog;

/**
 * 一组公共墙微博
 * @author caisenchuan
 *
 */
public class SuggestList {
    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "SuggestList";
    
    /**公共微博墙列表*/
    public static final String TAG_STATUSES = "statuses";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**公共墙微博列表*/
    private ArrayList<Suggest> mList = new ArrayList<Suggest>();
    
    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 无参数构造方法
     */
    public SuggestList() {
        
    }
    
    /**
     * 构件时添加第一个Suggest对象
     * @param sg
     */
    public SuggestList(Suggest sg) {
        mList.add(sg);
    }
    
    /**
     * 通过json字符串构造公共墙微博列表
     * @param jsonStr - 要构造的字符串
     * @throws JSONException JSON解析错误
     * @throws RetErrorException 请求操作失败
     */
    public SuggestList(String jsonStr) throws JSONException, RetErrorException {
        JSONObject json = new JSONObject(jsonStr);
        String ret = json.optString(WeMapAPI.FLAG_RET, WeMapAPI.CODE_ERROR);
        if(ret.equals(WeMapAPI.CODE_NO_ERR)) {
            JSONArray status = json.getJSONArray(TAG_STATUSES);
            if(status != null) {
                for(int i = 0; i < status.length(); i++) {
                    try {
                        JSONObject j = status.getJSONObject(i);
                        Suggest sg = new Suggest(j);
                        mList.add(sg);
                    } catch (JSONException e) {
                        //某一个解析失败，接着解析其他的
                        KLog.w(TAG, "Exception while opt suggest", e);
                        continue;
                    }
                }
            }
        } else {
            String err_code = json.optString(WeMapAPI.FLAG_ERR_CODE, "");
            throw new RetErrorException(err_code);
        }
    }
    
    /**
     * 读取公共墙微博列表
     * @return
     * @author caisenchuan
     */
    public ArrayList<Suggest> getSuggestList() {
        return mList;
    }
    
    /**
     * 添加一条公共墙微博
     * @param sg
     * @author caisenchuan
     */
    public void add(Suggest sg) {
        mList.add(sg);
    }
    
    /**
     * 读取某一条公共墙微博
     * @param index
     * @return
     * @author caisenchuan
     */
    public Suggest get(int index) {
        return mList.get(index);
    }
    
    /**
     * 读取个数
     * @return
     * @author caisenchuan
     */
    public int size() {
        return mList.size();
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
