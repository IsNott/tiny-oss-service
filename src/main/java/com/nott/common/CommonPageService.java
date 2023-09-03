package com.nott.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Nott
 * @Date 2023/8/10
 */

@Service
@Slf4j
public class CommonPageService<T> {

    static final List<String> COMMON_SQLEXP = Arrays.asList("eq", "ne", "gt", "ge", "lt", "le", "like", "notLike", "likeLeft", "likeRight");
    static final List<String> IN_SQLEXP = Arrays.asList("in", "notIn");
    static final List<String> ISNULL_SQLEXP = Arrays.asList("isNull", "isNotNull");
    static final List<String> EXIST_SQLEXP = Arrays.asList("exists", "notExists");
    static final List<String> BTW_SQLEXP = Arrays.asList("between", "notBetween");
    static final List<String> SORT_EXCEPTION = Arrays.asList("group", "order");

    static final String CONDITION_ONFIELD_TRUE = "1";
    static final boolean CONDITION = true;
    static final String SORT_OREDR = "order";
    static final String SORT_GROUP = "group";
    static final String ORDER_ASC = "isAsc";
    static final String JSON_FIELD_QUERY = "query";
    static final String JSON_FIELD_SORT = "sort";
        static final String CHILD_FIELD_EXPRESSION = "expression";
    static final String CHILD_FIELD_ATTR = "attr";
    static final String CHILD_FIELD_VAL= "val";

    // 准备一个常量 Map，以 SQLEXP 为 key，对应可变长参数类型数组为 value
    private static final Map<List<String>, Class<?>[]> methodNameMap = new HashMap<>();
    private static final Map<String, Class<?>[]> sortOrGroupKeyMap = new HashMap<>();


    static {
        // SQL方法
        methodNameMap.put(BTW_SQLEXP, new Class[]{boolean.class, Object.class, Object.class, Object.class});
        methodNameMap.put(COMMON_SQLEXP, new Class[]{boolean.class, Object.class, Object.class});
        methodNameMap.put(EXIST_SQLEXP, new Class[]{boolean.class, Object.class, String.class});
        methodNameMap.put(IN_SQLEXP, new Class[]{boolean.class, Object.class, Collection.class});
        methodNameMap.put(ISNULL_SQLEXP, new Class[]{boolean.class, Object.class, Object.class});
        sortOrGroupKeyMap.put(SORT_OREDR, new Class[]{boolean.class, boolean.class, Object[].class});
        sortOrGroupKeyMap.put(SORT_GROUP, new Class[]{boolean.class, Object[].class});
    }


    //通用的方法执行类
    public static void invokeMethod(Object obj, Method method, Object... args) throws Exception {
        try {
            method.invoke(obj, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw e;
        }
    }

    public QueryWrapper<T> initMbpWrapper(JSONObject req) throws Exception {
        JSONObject sort = req.getJSONObject(JSON_FIELD_SORT);
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        Class<?> superclass = wrapper.getClass().getSuperclass();
        if (StringUtils.isNotEmpty(req.getString(JSON_FIELD_QUERY))) {
            try {
                JSONArray query = req.getJSONArray(JSON_FIELD_QUERY);
                Iterator iterator = query.iterator();
                while (iterator.hasNext()) {
                    JSONObject next = JSON.parseObject(JSON.toJSONString(iterator.next()));
                    String expression = next.getString(CHILD_FIELD_EXPRESSION);
                    String attr = next.getString(CHILD_FIELD_ATTR);
                    Object val = next.get(CHILD_FIELD_VAL);

                    if (StringUtils.isNotEmpty(expression)) {
                        // 然后通过以下方法获得方法名，并且使用一个通用的invokeMethod方法调用具体方法
                        Set<List<String>> keySet = methodNameMap.keySet();
                        Class<?>[] parameterTypes = null;
                        List<String> methodKey = keySet.stream().filter(r -> r.contains(expression)).findFirst().orElseGet(null);
                        if (methodKey != null) {
                            parameterTypes = methodNameMap.get(methodKey);
                        }
                        if (parameterTypes != null) {
                            Method method = superclass.getMethod(expression, parameterTypes);
                            if (BTW_SQLEXP.contains(expression)) {
                                String valStr = String.valueOf(val);
                                invokeMethod(wrapper, method, CONDITION, attr, valStr.split(","));
                                continue;
                            }
                            invokeMethod(wrapper, method, CONDITION, attr, val);
                        }
                    }
                }
                if (Objects.nonNull(sort)) {
                    Iterator<String> sortKeyIterator = sort.keySet().iterator();
                    while (sortKeyIterator.hasNext()) {
                        String key = sortKeyIterator.next();
                        if (SORT_EXCEPTION.contains(key)) {
                            String keyInMap = SORT_EXCEPTION.stream().filter(r -> r.equals(key)).findFirst().orElseGet(null);
                            if (StringUtils.isEmpty(keyInMap)) {
                                continue;
                            }
                            JSONObject attrJson = sort.getJSONObject(keyInMap);
                            Object attrs = attrJson.getJSONArray(CHILD_FIELD_ATTR).toArray();
                            String isAsc = attrJson.getString(ORDER_ASC);
                            Class<?>[] methods = sortOrGroupKeyMap.get(keyInMap);
                            Method method = superclass.getMethod(keyInMap + "By", methods);
                            if (StringUtils.isNotEmpty(isAsc)) {
                                invokeMethod(wrapper, method, CONDITION, CONDITION_ONFIELD_TRUE.equals(isAsc), attrs);
                                continue;
                            }
                            invokeMethod(wrapper, method, CONDITION, attrs);
                        }
                    }
                }
            } catch (Exception e) {
                log.info("findByPage error：{}", e.getMessage(), e);
                throw e;
            }
        }
        return wrapper;
    }
}
