package com.haleydu.cimoc.utils;

import android.os.Build;
import androidx.annotation.RequiresApi;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @AUTO SDK对JSON解析工具类
 * @Author Cyril
 * @DATE 2022/11/25
 */
public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();

    public static List<?> toList(String jsonStr) {
        try {
            return (List<?>) jsonStr2JsonArray(jsonStr);
        } catch (JSONException e) {
            LogUtil.e(TAG, "Exception:" + e.getMessage());
        }
        return null;
    }

    public static Map<String, ?> toMap(String jsonStr) {
        try {
            return (Map<String, ?>) jsonStr2JsonObject(jsonStr);
        } catch (JSONException e) {
            LogUtil.e(TAG, "Exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * 将对象转换成JSON字符串
     */
    public static String toJsonString(Object obj) {
        try {
            return toJsonObject(obj).toString();
        } catch (JSONException e) {
            LogUtil.e(TAG, "Exception:" + e.getMessage());
        }
        return null;
    }

    /**
     * 将list转为JSONArray,将对象转为JSONObject
     */
    public static final Object toJsonObject(Object obj) throws JSONException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JSONObject || obj instanceof JSONArray) {
            return obj;
        }
        if (obj instanceof Map) { //如果为Map
            Map map = (Map) obj;
            JSONObject jsonObject = new JSONObject();
            for (Object key : map.keySet()) {
                Object value = map.get(key);
                jsonObject.put(String.valueOf(key), toJsonObject(value));
            }
            return jsonObject;
        } else if (obj instanceof List) {// 为List
            List list = (List) obj;
            JSONArray jsonArray = new JSONArray();
            for (Object o : list) {
                jsonArray.put(toJsonObject(o));
            }
            return jsonArray;
        } else if (obj.getClass().isArray()) { // 为数组
            int length = Array.getLength(obj);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < length; i++) {
                jsonArray.put(toJsonObject(Array.get(obj, i)));
            }
            return jsonArray;
        } else {
            JSONObject jsonObject = new JSONObject();
            Class<?> clazz = obj.getClass();
            parseObject(clazz, jsonObject, obj);
            return jsonObject;
        }
    }

    /**
     * @param clazz   反射实体类
     * @param jsonObj JSON对象
     * @param object  原始数据
     */
    private static void parseObject(Class<?> clazz, JSONObject jsonObj, Object object) {
        if (clazz == null) {
            return;
        }
        // 通过反射获取到对象的所有属性
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            // 判断如果给定field.getModifiers()参数包含transient修饰符
            if (Modifier.isTransient(field.getModifiers())) {
                continue;
            }
            try {
                // 获取属性的属性名
                String fieldName = field.getName();
                // getDeclaredMethod 获得类声明的命名的方法，但无法获取父类的字段，从类中获取了一个方法后，可以用 invoke() 方法来调用这个方法
                Method method = clazz.getDeclaredMethod("get" + captureName(fieldName));
                if (method != null) {
                    jsonObj.put(fieldName, toJsonObject(method.invoke(object)));
                }
            } catch (Exception e) {
                LogUtil.e(TAG, "Exception:" + e.getMessage());
            }
        }
        //clazz 的父类解析，继承关系时获取父类信息
        parseObject(clazz.getSuperclass(), jsonObj, object);
    }

    public static final JSONArray jsonStr2JsonArray(String jsonStr) throws JSONException {
        if (isJsonCorrect(jsonStr)) {
            return new JSONArray(jsonStr);
        }
        return null;
    }

    public static final JSONObject jsonStr2JsonObject(String jsonStr) throws JSONException {
        if (isJsonCorrect(jsonStr)) {
            return new JSONObject(jsonStr);
        }
        return null;
    }

    /**
     * 判断Json格式是否正确
     */
    public static boolean isJsonCorrect(String s) {
        if (s == null || s.equals("[]") || s.equals("{}") || s.equals("") || s.equals("[null]") || s.equals("{null}") || s.equals("null")) {
            return false;
        }
        return true;
    }

    /**
     * 用get方法获取数据，首字母大写，如getName()
     */
    private static String captureName(String name) {
        char[] cs = name.toCharArray();
        cs[0] -= 32;//ascii 码表 ，如 n=110，N=78
        return String.valueOf(cs);
    }
}