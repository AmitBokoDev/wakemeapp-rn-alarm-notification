/**
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.emekalites.react.alarm.notification;

import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * com.facebook.internal is solely for the use of other packages within the
 * Facebook SDK for
 * Android. Use of any of the classes in this package is unsupported, and they
 * may be modified or
 * removed without warning at any time.
 *
 * A helper class that can round trip between JSON and Bundle objects that
 * contains the types:
 * Boolean, Integer, Long, Double, String
 * If other types are found, an IllegalArgumentException is thrown.
 */
public class BundleJSONConverter {
    private static final Map<Class<?>, Setter> SETTERS = new HashMap<Class<?>, Setter>();

    static {
        SETTERS.put(Boolean.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putBoolean(key, (Boolean) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(Integer.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putInt(key, (Integer) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(Long.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putLong(key, (Long) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(Double.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putDouble(key, (Double) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(String.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                bundle.putString(key, (String) value);
            }

            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                json.put(key, value);
            }
        });
        SETTERS.put(String[].class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                throw new IllegalArgumentException("Unexpected type from JSON");
            }

            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                JSONArray jsonArray = new JSONArray();
                for (String stringValue : (String[]) value) {
                    jsonArray.put(stringValue);
                }
                json.put(key, jsonArray);
            }
        });

        SETTERS.put(JSONArray.class, new Setter() {
            public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException {
                JSONArray jsonArray = (JSONArray) value;
                ArrayList<String> stringArrayList = new ArrayList<String>();
                // Empty list, can't even figure out the type, assume an ArrayList<String>
                if (jsonArray.length() == 0) {
                    bundle.putStringArrayList(key, stringArrayList);
                    return;
                }

                // Only strings are supported for now
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object current = jsonArray.get(i);
                    if (current instanceof String) {
                        stringArrayList.add((String) current);
                    } else {
                        throw new IllegalArgumentException("Unexpected type in an array: " + current.getClass());
                    }
                }
                bundle.putStringArrayList(key, stringArrayList);
            }

            @Override
            public void setOnJSON(JSONObject json, String key, Object value) throws JSONException {
                throw new IllegalArgumentException("JSONArray's are not supported in bundles.");
            }
        });
    }

    public interface Setter {
        public void setOnBundle(Bundle bundle, String key, Object value) throws JSONException;

        public void setOnJSON(JSONObject json, String key, Object value) throws JSONException;
    }

    public static JSONObject convertToJSON(Bundle bundle) throws JSONException {
        JSONObject json = new JSONObject();

        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            if (value == null) {
                // Null is not supported.
                continue;
            }

            // Special case List<String> as getClass would not work, since List is an
            // interface
            if (value instanceof List<?>) {
                JSONArray jsonArray = new JSONArray();
                @SuppressWarnings("unchecked")
                List<String> listValue = (List<String>) value;
                for (String stringValue : listValue) {
                    jsonArray.put(stringValue);
                }
                json.put(key, jsonArray);
                continue;
            }

            // Special case Bundle as it's one way, on the return it will be JSONObject
            if (value instanceof Bundle) {
                json.put(key, convertToJSON((Bundle) value));
                continue;
            }

            Setter setter = SETTERS.get(value.getClass());
            if (setter == null) {
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
            }
            setter.setOnJSON(json, key, value);
        }

        return json;
    }

    public static Bundle convertToBundle(JSONObject jsonObject) throws JSONException {
        Bundle bundle = new Bundle();
        @SuppressWarnings("unchecked")
        Iterator<String> jsonIterator = jsonObject.keys();
        while (jsonIterator.hasNext()) {
            String key = jsonIterator.next();
            Object value = jsonObject.get(key);
            if (value == null || value == JSONObject.NULL) {
                // Null is not supported.
                continue;
            }

            // Special case JSONObject as it's one way, on the return it would be Bundle.
            if (value instanceof JSONObject) {
                bundle.putBundle(key, convertToBundle((JSONObject) value));
                continue;
            }

            Setter setter = SETTERS.get(value.getClass());
            if (setter == null) {
                throw new IllegalArgumentException("Unsupported type: " + value.getClass());
            }
            setter.setOnBundle(bundle, key, value);
        }

        return bundle;
    }
}