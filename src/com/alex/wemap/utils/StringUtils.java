package com.alex.wemap.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * 字符串处理
 * @author caisenchuan
 */
public class StringUtils {
    /*--------------------------
     * 常量
     *-------------------------*/

    /*--------------------------
     * 自定义类型
     *-------------------------*/

    /*--------------------------
     * 成员变量
     *-------------------------*/

    /*--------------------------
     * public方法
     *-------------------------*/
    /**
     * 根据输入日期与当前日期比较,得到相差的时间,显示给用户
     * @param date - 要比较的时间
     * @return 时间字符串
     * @author caisenchuan
     **/
    public static String getDateString(Date date) {
        Calendar c_t = Calendar.getInstance();
        c_t.setTime(date);
        int y_t = c_t.get(Calendar.YEAR);
        int m_t = c_t.get(Calendar.MONTH) + 1;
        int d_t = c_t.get(Calendar.DAY_OF_MONTH);
        int h_t = c_t.get(Calendar.HOUR_OF_DAY);
        int mi_t = c_t.get(Calendar.MINUTE);
        
        Calendar c_now = Calendar.getInstance();
        int y_now = c_now.get(Calendar.YEAR);
        int m_now = c_now.get(Calendar.MONTH) + 1;
        int d_now = c_now.get(Calendar.DAY_OF_MONTH);
        int h_now = c_now.get(Calendar.HOUR_OF_DAY);
        int mi_now = c_now.get(Calendar.MINUTE);
        
        System.out.println(String.format("%s %s %s %s %s", y_t, m_t, d_t, h_t, mi_t));
        System.out.println(String.format("%s %s %s %s %s", y_now, m_now, d_now, h_now, mi_now));
        
        String ret = String.format("%s年%s月%s日", y_t, m_t, d_t);
        int diff = 0;
        if(c_t.after(c_now)) {
            //直接返回默认字符串
        } else if(y_t < y_now) {
            //返回默认字符串
        } else if(m_t < m_now) {
            //返回月日
            ret = String.format("%s月%s日", m_t, d_t);
        } else if(d_t < d_now) {
            //X天前
            diff = d_now - d_t;
            if(diff == 1) {
                ret = "昨天";
            } else {
                ret = diff + "天前";
            }
        } else if(h_t < h_now) {
            //X小时前
            diff = h_now - h_t;
            ret = diff + "小时前";
        } else if(mi_t < mi_now) {
            //X分钟前
            diff = mi_now - mi_t;
            ret = diff + "分钟前";
        } else {
            ret = "刚刚";
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
