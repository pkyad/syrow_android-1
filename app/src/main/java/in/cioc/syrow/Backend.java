package in.cioc.syrow;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;

import in.cioc.syrow.helper.MyPreferenceManager;

public class Backend {
//    public static String url = "http://192.168.1.115:8000/";
    public static String url = "http://syrow.cioc.in/";
    public Context context;
    MyPreferenceManager sessionManager;

    public Backend(Context context){
        this.context = context;
    }

//    public AsyncHttpClient getHTTPClient(){
//        sessionManager = new MyPreferenceManager(context);
//        final String csrftoken = sessionManager.getCsrfId();
//        final String sessionid = sessionManager.getSessionId();
//        AsyncHttpClient client = new AsyncHttpClient();
//        client.addHeader("X-CSRFToken" , csrftoken);
//        if (sessionid.length()>csrftoken.length()) {
//            client.addHeader("COOKIE", String.format("csrftoken=%s; sessionid=%s", sessionid, csrftoken));
//        } else {
//            client.addHeader("COOKIE", String.format("csrftoken=%s; sessionid=%s", csrftoken, sessionid));
//        }
//        return client;
//    }
}
