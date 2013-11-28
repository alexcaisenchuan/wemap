/**
 * <p>Title: SuggetstAndMapMark.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2013</p>
 * <p>Company: </p>
 * @author caisenchuan
 * @date 2013-9-20
 * @version 1.0
 */
package com.alex.wemap.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * 公共墙微博与地图mark的混合类，主要用于组织mark与微博的对应关系
 * @author caisenchuan
 *
 */
public class SuggestAndMapMark {
    /*--------------------------
     * 自定义类型
     *-------------------------*/
    /*--------------------------
     * 常量
     *-------------------------*/
    private static final String TAG = "SuggetstAndMapMark";
    
    /*--------------------------
     * 成员变量
     *-------------------------*/
    /**
     * Marker与Position对象的HashMap
     */
    private HashMap<Marker, Position> mMarkPosMap = new HashMap<Marker, Position>();
    /**
     * Position与公共墙微博列表的HashMap
     */
    private HashMap<Position, SuggestList> mPosArrayMap = new HashMap<Position, SuggestList>();

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 通过SuggestList构造对象，将对应关系创建出来，同时将Marker添加到地图上
     * @param sg_list 要添加的公共墙微博列表
     * @param map 要添加Marker到的地图对象
     */
    public SuggestAndMapMark(SuggestList sg_list, GoogleMap map) {
        if(sg_list != null && map != null) {
            ArrayList<Suggest> list = sg_list.getSuggestList();
            for(Suggest sg : list) {
                Position pos = new Position(sg.latitude, sg.longtitude);
                //查找是否sg对应的位置（Position）已经在mPosArrayMap里
                SuggestList sg_list_t = mPosArrayMap.get(pos);
                if(sg_list_t != null) {
                    //若是，则添加到对应的ArrayList中
                    sg_list_t.add(sg);
                } else {
                    //否则，创建ArrayList并且添加到mPosArrayMap中
                    SuggestList arr_new = new SuggestList(sg);
                    mPosArrayMap.put(pos, arr_new);
                    //同时向地图上添加Mark，并且将Mark与Position的对应关系添加到mMarPosMap中
                    MarkerOptions ops = new MarkerOptions();
                    ops.position(new LatLng(sg.latitude, sg.longtitude));
                    Marker maker = map.addMarker(ops);
                    mMarkPosMap.put(maker, pos);
                }
            }
        }
    }
    
    /**
     * 根据marker读出对应的SuggestList
     * @param mark
     * @return marker对应的公共墙微博列表，读取失败返回null
     * @author caisenchuan
     */
    public SuggestList getSuggestList(Marker mark) {
        SuggestList ret = null;
        
        //从mMarkPosMap中读取Position
        Position pos = mMarkPosMap.get(mark);
        if(pos != null) {
            //从mPosArrayMap中读出ArrayList
            ret = mPosArrayMap.get(pos);
        }
        
        return ret;
    }
    
    /*--------------------------
     * protected、packet方法
     *-------------------------*/

    /*--------------------------
     * private方法
     *-------------------------*/

}
