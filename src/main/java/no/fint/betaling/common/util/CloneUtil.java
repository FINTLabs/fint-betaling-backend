package no.fint.betaling.common.util;

import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

public class CloneUtil {

    public static <T> T cloneObject(T source) {
        try {
            return (T) BeanUtils.cloneBean(source);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
