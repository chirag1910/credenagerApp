package com.credenager.utils;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.biometric.BiometricManager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Globals {
    public static final String AUTH_FILE_NAME = "USER_INFO";
    public static final String SETTINGS_FILE_NAME = "USER_SETTINGS";
    public static final String DATA_FILE_NAME = "USER_DATA";
    public static final String MISC_FILE_NAME = "MISC_DATA";

    public static final String EMAIL_KEY = "USER_EMAIL";
    public static final String JWT_KEY = "JWT_TOKEN";
    public static final String USER_KEY_KEY = "USER_ENCRYPTION_KEY";
    public static final String OFFLINE_KEY = "OFFLINE_MODE";
    public static final String BIOMETRIC_KEY = "BIOMETRIC_MODE";
    public static final String BYPASS_KEY_KEY = "BYPASS_KEY";
    public static final String DATA_KEY = "USER_OFFLINE_DATA";
    public static final String AUTOMATIC_UNLOCK_TIMEOUT_KEY = "AUTOMATIC_UNLOCK_TIMEOUT";

    public static final String LOGIN_FRAGMENT_TAG = "LOGIN_FRAG";
    public static final String SIGNUP_FRAGMENT_TAG = "SIGNUP_FRAG";
    public static final String RESET_PASS_FRAGMENT_TAG = "RESET_PASS_FRAG";
    public static final String RESET_KEY_FRAGMENT_TAG = "RESET_KEY_FRAG";
    public static final String GROUP_FRAGMENT_TAG = "GROUP_TAG";
    public static final String SEARCH_FRAGMENT_TAG = "SEARCH_TAG";
    public static final String FROM_SEARCH_FRAGMENT_TAG = "FROM_SEARCH_FRAGMENT";

    public static final int GOOGLE_REQUEST_CODE = 69420;
    public static final String GOOGLE_CLIENT_ID = "274800871089-k8rdu64cfu1uq1n8hcf9tndldmnqhl27.apps.googleusercontent.com";

    public static final String DUMMY_ACCOUNT_EMAIL = "whatever@a.a";
    public static final String DUMMY_ACCOUNT_KEY = "123";

    public static final long AUTOMATIC_UNLOCK_TIMEOUT = 3 * 24 * 60 * 60 * 1000;

    public static boolean isValidEmail(String email){
        Pattern pattern = Pattern.compile("^(.+)@(.+)$");
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static float dpToPx(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static int getBiometricStatus(Context context) {
        int biometricStatus = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG | BIOMETRIC_WEAK);

        if (biometricStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            return 1;
        } else if (biometricStatus == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            return 0;
        } else {
            return -1;
        }
    }

    public static void saveUserState(Context context, String email, String token){
        context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).edit().putString(EMAIL_KEY, email).apply();
        context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).edit().putString(JWT_KEY, token).apply();
    }

    public static HashMap<String, String> getUserState(Context context){
        String email = context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).getString(EMAIL_KEY, null);
        String token = context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).getString(JWT_KEY, null);

        HashMap<String, String> response = new HashMap<>();
        response.put(EMAIL_KEY, email);
        response.put(JWT_KEY, token);

        return response;
    }

    public static void saveKey(Context context, String key){
        context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).edit().putString(USER_KEY_KEY, key).apply();
    }

    public static String getKey(Context context){
        return context.getSharedPreferences(AUTH_FILE_NAME, Context.MODE_PRIVATE).getString(USER_KEY_KEY, null);
    }

    public static void saveSettings(Context context, Boolean allowOffline, Boolean bypassKey, Boolean allowBiometric) {
        if (allowOffline != null){
            context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit().putBoolean(OFFLINE_KEY, allowOffline).apply();
        }
        if (allowBiometric != null){
            context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit().putBoolean(BIOMETRIC_KEY, allowBiometric).apply();
        }
        if (bypassKey != null){
            context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).edit().putBoolean(BYPASS_KEY_KEY, bypassKey).apply();
        }
    }

    public static HashMap<String, Object> getSettings(Context context) {
        Boolean allowOffline =  context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).getBoolean(OFFLINE_KEY, false);
        Boolean allowBiometric =  context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).getBoolean(BIOMETRIC_KEY, false);
        Boolean bypassKey =  context.getSharedPreferences(SETTINGS_FILE_NAME, Context.MODE_PRIVATE).getBoolean(BYPASS_KEY_KEY, false);

        HashMap<String, Object> response = new HashMap<>();
        response.put(OFFLINE_KEY, allowOffline);
        response.put(BIOMETRIC_KEY, allowBiometric);
        response.put(BYPASS_KEY_KEY, bypassKey);

        return response;
    }

    public static void saveData(Context context, String data){
        context.getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE).edit().putString(DATA_KEY, data).apply();
    }

    public static String getData(Context context) {
        return context.getSharedPreferences(DATA_FILE_NAME, Context.MODE_PRIVATE).getString(DATA_KEY, null);
    }

    public static void saveAutomaticUnlockValue(Context context){
        long timeLimit = Calendar.getInstance().getTimeInMillis() + AUTOMATIC_UNLOCK_TIMEOUT;
        context.getSharedPreferences(MISC_FILE_NAME, Context.MODE_PRIVATE).edit().putLong(AUTOMATIC_UNLOCK_TIMEOUT_KEY, timeLimit).apply();
    }

    public static long getAutomaticUnlockTimeout(Context context){
        return context.getSharedPreferences(MISC_FILE_NAME, Context.MODE_PRIVATE).getLong(AUTOMATIC_UNLOCK_TIMEOUT_KEY, 0);
    }

    public static void logout(Context context) {
        saveUserState(context, null, null);
        saveKey(context, null);
        saveData(context, null);
        Session.setUserState(null, null);
        Session.setKey(null);
        Data.dataString = null;
    }
}
