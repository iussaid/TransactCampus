package ussaid.iqbal.transactcampus.app;

import static ussaid.iqbal.transactcampus.utils.Constants.OFFLINE_FILE;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import ussaid.iqbal.transactcampus.R;
import ussaid.iqbal.transactcampus.utils.Constants;


public class App extends Application{

    public static final String TAG = App.class.getSimpleName();

    private RequestQueue mRequestQueue;

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static void writeOfflineData(JSONArray data, Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(OFFLINE_FILE, Context.MODE_APPEND);
            JSONArray oldData = loadOfflineData(context);
            Log.e(Constants.TAG, "Old Size : "+oldData.length()+" - Page = "+Constants.CURRENT_PAGE_INDEX);
            if(oldData.length() == 0){
                fos.write(data.toString().getBytes(StandardCharsets.UTF_8));
            }else {
                String s1 = oldData.toString();
                String s2 = data.toString();
                s1 = s1.substring(1, s1.length() - 1);
                s2 = s2.substring(1, s2.length() - 1);
                String finalData = new String("");
                finalData = "["+s1+","+s2+"]";
                JSONArray jsonArr = new JSONArray(finalData);
                Log.e(Constants.TAG, "New Size Final Data : "+jsonArr.length());
                fos.write(finalData.getBytes(StandardCharsets.UTF_8));
            }
            fos.close();
            JSONArray oldData2 = loadOfflineData(context);
            Log.e(Constants.TAG, "New Size : "+oldData2.length());
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", "File write failed: " + e.getLocalizedMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Exception", "File write failed: " + e.getLocalizedMessage());
        }
    }

    public static JSONArray loadOfflineData(Context context) throws JSONException {
        String ret = "[]";
        try {
            InputStream inputStream = context.openFileInput(OFFLINE_FILE);
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append("\n").append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(Constants.TAG, e.getLocalizedMessage());
        }
        if(ret.isEmpty()){
            ret = "[]";
        }
        return new JSONArray(ret);
    }
    private static  ProgressDialog pd;
    public static void ShowHUD(Context context){
        pd = new ProgressDialog(context);
        pd.setMessage(context.getString(R.string.msg_processing));
        pd.show();
    }

    public static void HideHUD(){
       if(pd.isShowing()){
           pd.dismiss();
       }
    }

}