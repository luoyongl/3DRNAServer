package com.wtu.sj.ly.rna.openapi.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class RnaResult implements java.io.Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // 定义jackson对象
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // 响应业务状态
    private Integer status;

    // 响应消息
    private String msg;

    // 响应中的数据
    private Object data;

    public RnaResult() {

    }

    public RnaResult(Integer status, String msg, Object data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public RnaResult(Object data) {
        this.status = 200;
        this.msg = "操作成功";
        this.data = data;
    }

    public static RnaResult build(Integer status, String msg, Object data) {
        return new RnaResult(status, msg, data);
    }

    public static RnaResult ok(Object data) {
        return new RnaResult(data);
    }

    public static RnaResult ok() {
        return new RnaResult(200,"操作成功",null);
    }

    public static RnaResult fail() {
        return new RnaResult(400, "操作失败", null);
    }

    public static RnaResult fail(String msg) {
        return new RnaResult(400, msg, null);
    }

    public static RnaResult build(Integer status, String msg) {
        return new RnaResult(status, msg, null);
    }

    /**
     * 将json结果集转化为BroadcastResult对象
     *
     * @param jsonData json数据
     * @param clazz    BroadcastResult中的object类型
     * @return
     */
    public static RnaResult formatToPojo(String jsonData, Class<?> clazz) {
        try {
            if (clazz == null) {
                return MAPPER.readValue(jsonData, RnaResult.class);
            }
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (clazz != null) {
                if (data.isObject()) {
                    obj = MAPPER.readValue(data.traverse(), clazz);
                } else if (data.isTextual()) {
                    obj = MAPPER.readValue(data.asText(), clazz);
                }
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 没有object对象的转化
     *
     * @param json
     * @return
     */
    public static RnaResult format(String json) {
        try {
            return MAPPER.readValue(json, RnaResult.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Object是集合转化
     *
     * @param jsonData json数据
     * @param clazz    集合中的类型
     * @return
     */
    public static RnaResult formatToList(String jsonData, Class<?> clazz) {
        try {
            JsonNode jsonNode = MAPPER.readTree(jsonData);
            JsonNode data = jsonNode.get("data");
            Object obj = null;
            if (data.isArray() && data.size() > 0) {
                obj = MAPPER.readValue(data.traverse(),
                        MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            return build(jsonNode.get("status").intValue(), jsonNode.get("msg").asText(), obj);
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}


 

