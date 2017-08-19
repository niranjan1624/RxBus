package com.niranjan.rxbus;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Niranjan P on 8/9/2017.
 */

public class RxBus {

    private HashMap<String, PublishSubject<Object>> subjectStringHashMap = new HashMap<>();
    private HashMap<Object, List<String>> identifierMap = new HashMap<>();
    private static RxBus defaultInstance;
    private Object object;
    private List<Disposable> disposableList = new ArrayList<>();

    public static RxBus getDefault() {
        if (defaultInstance == null) {
            synchronized (RxBus.class) {
                if (defaultInstance == null)
                    defaultInstance = new RxBus();
            }
        }
        return defaultInstance;
    }

    public void publish(@NonNull Object object, String identifier) {
        if (subjectStringHashMap.containsKey(identifier)) {
            subjectStringHashMap.get(identifier).onNext(object);
        } else {
            throwIllegalStateException("No subscription for given identifier");
        }
    }

    public Disposable subscribe(@NonNull Consumer<Object> action, @NonNull String identifier) {
        String exception = getEligibilityException(identifier);
        if (TextUtils.isEmpty(exception)) {
            return processSubscriptionAndGetDisposable(action, identifier);
        } else {
            throwIllegalStateException(exception);
            return Disposables.empty();
        }
    }

    private Disposable processSubscriptionAndGetDisposable(Consumer<Object> action, String identifier) {
        addIdentifierToMap(identifier);
        PublishSubject<Object> sSubject = PublishSubject.create();
        Disposable disposable = sSubject.subscribe(action);
        subjectStringHashMap.put(identifier, sSubject);
        disposableList.add(disposable);
        return disposable;
    }

    private String getEligibilityException(String identifier) {
        if (TextUtils.isEmpty(identifier)) {
            return "No identifier provided";
        } else if (object == null || !identifierMap.containsKey(object)) {
            return "Subscriber class not registered!";
        } else if (isIdentifierDuplicate(identifier)) {
            return "Already subscribed with given identifier";
        }
        return null;
    }

    private void addIdentifierToMap(String identifier) {
        List<String> identifierList = identifierMap.get(object);
        identifierList.add(identifier);
        identifierMap.put(object, identifierList);
    }

    private boolean isIdentifierDuplicate(@NonNull String identifier) {
        if (identifierMap.get(object).contains(identifier))
            return true;
        return false;
    }

    private void checkAndUnregister() {
        if (object != null) {
            unRegister(object);
        }
    }

    public void register(Object object) {
        checkAndUnregister();
        if (identifierMap.containsKey(object)) {
            throwIllegalStateException("Subscriber already registered");
        } else {
            this.object = object;
            identifierMap.put(this.object, new ArrayList<>());
        }
    }

    public void unRegister(Object object) {
        if (identifierMap.containsKey(object)) {
            disposeAllSubscribers();
            releaseAllData();
        } else {
            throwIllegalStateException("Subscriber is not registered");
        }
    }

    private void disposeAllSubscribers() {
        for (Disposable disposable : disposableList) {
            disposable.dispose();
        }
    }

    private void releaseAllData() {
        identifierMap.clear();
        subjectStringHashMap.clear();
        disposableList.clear();
        this.object = null;
    }

    public void throwIllegalStateException(String exception) {
        try {
            throw new IllegalStateException(exception);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
