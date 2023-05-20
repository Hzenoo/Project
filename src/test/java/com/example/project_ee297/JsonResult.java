package com.example.project_ee297;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
    使用@RestController或@ResponseBody时，可直接返回该对象，
    Spring Boot默认使用Jackson会自动将该对象转换为json字符串
    如{"code": 0,"msg": "","data": [{}, {}]}
 */
public class JsonResult extends HashMap<String, Object> implements Serializable {
    public static final long serialVersionUID = 1L;

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;
    public static final int ERROR = 2;
    public static final int LOGOUT = 1001;

    public JsonResult(int code, String msg, Object data) {
        super(3);  //继承自Map，设置初始容量
        this.put("code", code); //状态码，layui中code=0表示成功
        this.put("msg", msg);   //提示消息
        this.put("data", data); //数据体
    }

    //一般返回code、msg和data这三个即可，但layui加载table时还要求count值
    //添加额外的返回值
    public JsonResult put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    //快速返回请求成功
    public static JsonResult success(Object data) {
        return new JsonResult(SUCCESS, "ok", data);
    }

    //快速返回请求失败
    public static JsonResult fail(String msg) {
        return new JsonResult(FAIL, msg, null);
    }

    public static JsonResult error(String msg, Object data) {
        return new JsonResult(ERROR, msg, data);
    }

    public static JsonResult logout() {
        return new JsonResult(LOGOUT, "未登录", null);
    }

    //快速生成一个Map键值对
    public static Map<String, Object> fastMap(String key, Object value) {
        Map<String, Object> data = new HashMap<>(1);
        data.put(key, value);
        return data;
    }
}
