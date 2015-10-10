package com.vinecom.recommend.storage.impl;

import com.vinecom.common.storage.Storage;

/**
 * Created by ndn on 7/21/2015.
 */
public class StorageFactory {
    private StorageFactory() {
    }

    public static  <T extends Storage> T getStorage(Class<T> clazz) {
        assert clazz != null;
        try {
            T result = clazz.newInstance();
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
