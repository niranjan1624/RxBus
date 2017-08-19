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
        if(subjectStringHashMap.containsKey(identifier)) {
            subjectStringHashMap.get(identifier).onNext(object);
        } else {
            throwIllegalStateException("No subscription for given identifier");
        }
    }

    public Disposable subscribe(@NonNull Consumer<Object> action, @NonNull String identifier) {
        if (TextUtils.isEmpty(identifier)) {
            throwIllegalStateException("No identifier provided");
        } else if (object == null || !identifierMap.containsKey(object)) {
            throw new IllegalStateException("Subscriber class not registered!");
        } else if (isIdentifierDuplicate(identifier)) {
            throwIllegalStateException("Already subscribed with given identifier");
        } else {
            addIdentifierToMap(identifier);
            PublishSubject<Object> sSubject = PublishSubject.create();
            Disposable disposable  = sSubject.subscribe(action);
            subjectStringHashMap.put(identifier, sSubject);
            disposableList.add(disposable);
            return disposable;
        }
        return Disposables.empty();
    }

    private void addIdentifierToMap(String identifier) {
        if (object == null)
            throwIllegalStateException("Subscriber class not registered");
        List<String> identifierList = identifierMap.get(object);
        if (identifierList != null) {
            identifierList = new ArrayList<>();
        }
        identifierList.add(identifier);
        identifierMap.put(object, identifierList);
    }

    private boolean isIdentifierDuplicate(@NonNull String identifier) {
        if (identifierMap.get(object).contains(identifier))
            return true;
        return false;
    }

    public void register(Object object) {
        if (identifierMap.containsKey(object)) {
            throwIllegalStateException("Subscriber already registered");
        } else {
            this.object = object;
            identifierMap.put(object, new ArrayList<>());
        }
    }

    public void unRegister(Object object) {
        if (identifierMap.containsKey(object)) {
            identifierMap.remove(object);
            for(Disposable disposable : disposableList) {
                disposable.dispose();
            }
            this.object = null;
        } else {
            throwIllegalStateException("Subscriber is not registered");
        }
    }

    public void throwIllegalStateException(String exception) {
        try {
            throw new IllegalStateException(exception);
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }
    }
}
