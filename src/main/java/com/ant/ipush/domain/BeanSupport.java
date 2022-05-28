package com.ant.ipush.domain;

import com.google.common.collect.ImmutableSet;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class BeanSupport {

    public static Map<String, Object> convertMapFromBean(Object target) {

        return convertMapFromBean(target, false);
    }

    public static Map<String, Object> convertMapFromBean(Object target, boolean isNull) {
        Map<String, Object> map = new HashMap<String, Object>();
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(target.getClass());

            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                // 过滤class属性
                if (!"class".equals(key)) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(target);
                    if (!isNull) {
                        if (value != null) {
                            map.put(key, value);
                        }
                    } else {
                        map.put(key, value);
                    }


                }
            }
        } catch (Exception e) {
//            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return map;
    }


    /**
     * 循环向上转型,获取对象的DeclaredField.
     *
     * @throws NoSuchFieldException 如果没有该Field时抛出.
     */
    public static Field getDeclaredField(Object object, String propertyName)
            throws NoSuchFieldException {
        return getDeclaredField(object.getClass(), propertyName);
    }

    public static List<Field> getDeclaredFields(Object target) {
        List<Field> fields = new ArrayList<>();
        for (Class superClass = target.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            fields.addAll(Arrays.stream(superClass.getDeclaredFields()).collect(Collectors.toList()));
        }
        return fields;
    }

    private static Field getDeclaredField(Class clazz, String propertyName)
            throws NoSuchFieldException {

        for (Class superClass = clazz; superClass != Object.class; superClass = superClass
                .getSuperclass()) {

            try {
                return superClass.getDeclaredField(propertyName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        throw new NoSuchFieldException("No such field: " + clazz.getName()
                + '.' + propertyName);
    }


    public static <T> T copyProperties(Object source, T target, Consumer<T> consumer) {
        copyProperties(source, target);
        consumer.accept(target);
        return target;
    }

    public static <T> T copyProperties(Object source, T target, String... ignore) {
        if (source == null) {
            return target;
        }
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());
            PropertyDescriptor[] targetPds = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor targetPd : targetPds) {
                Method writeMethod = targetPd.getWriteMethod();
                String name = targetPd.getName();
                //忽略
                if (new ImmutableSet.Builder().add(ignore).build().contains(name)) continue;
                if (writeMethod != null && !"class".equals(name)) {
                    PropertyDescriptor sourcePd = new PropertyDescriptor(name, source.getClass());
                    if (sourcePd != null) {
                        Method readMethod = sourcePd.getReadMethod();
                        if (readMethod != null) {
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            if (value == null) {
                                continue;
                            }
                            if (value instanceof Number) {
                                if (((Number) value).intValue() == 0) {
                                    continue;
                                }
                            }
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        }
                    }
                }
            }
        } catch (IntrospectionException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return target;
    }

//    public static void main(String[] args) {
//
//        LogAnalytics logAnalytics = new LogAnalytics();
//        logAnalytics.setUid(2);
//        logAnalytics.setTs(234L);
//        System.out.println(BeanSupport.copyProperties(logAnalytics, new LogAnalytics()).getTs());
//    }
}
