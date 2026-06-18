package com.example.demo.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisService {

    void setString(String key, String value);

    void setStringWithExpire(String key, String value, long expireSeconds);

    String getString(String key);

    void deleteKey(String key);

    boolean existsKey(String key);

    void setList(String key, List<String> values);

    List<String> getList(String key, long start, long end);

    void addToList(String key, String value);

    long getListSize(String key);

    void setHash(String key, Map<String, String> hash);

    void putHash(String key, String field, String value);

    String getHash(String key, String field);

    Map<String, String> getHashAll(String key);

    void deleteHashField(String key, String field);

    void setSet(String key, Set<String> values);

    void addToSet(String key, String value);

    Set<String> getSet(String key);

    boolean isMemberOfSet(String key, String value);

    void sortedSetAdd(String key, String value, double score);

    Set<String> sortedSetRange(String key, long start, long end);

    Long tryLock(String key, long expireSeconds);

    boolean unlock(String key, Long value);
}