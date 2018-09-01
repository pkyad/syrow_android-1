package in.cioc.syrow.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;
import in.cioc.syrow.Backend;
import in.cioc.syrow.R;
import in.cioc.syrow.adapter.ChatRoomThreadAdapter;
import in.cioc.syrow.app.Config;
import in.cioc.syrow.helper.Utility;
import in.cioc.syrow.model.AdminChat;
import in.cioc.syrow.model.ChatThread;
import in.cioc.syrow.model.MediaMessage;
import in.cioc.syrow.model.Message;
import in.cioc.syrow.model.User;
import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.messages.Publish;
import io.crossbar.autobahn.wamp.types.EventDetails;
import io.crossbar.autobahn.wamp.types.ExitInfo;
import io.crossbar.autobahn.wamp.types.InvocationDetails;
import io.crossbar.autobahn.wamp.types.Publication;
import io.crossbar.autobahn.wamp.types.Registration;
import io.crossbar.autobahn.wamp.types.SessionDetails;
import io.crossbar.autobahn.wamp.types.Subscription;

public class ChatRoomActivity extends AppCompatActivity {

    private String TAG = ChatRoomActivity.class.getSimpleName();
    Session session;
    Client client1;
    CompletableFuture<ExitInfo> exitInfoCompletableFuture;
    private String chatRoomId, path, base64="", userChooseTask, millSec;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EditText inputMessage;
    private TextView userName;
    private View offLineAndOnLine;
    private ImageView btnSend, btnAttach, userImage;
    private AsyncHttpClient client;
    boolean thread = true;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1,  choose;
    Bitmap bitmap;
    Context context;
    public static final long INTERVAL = 1000 * 25;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    TimerTask timerTask;

    SharedPreferences sp;
    SharedPreferences.Editor spe;
    String companyID = "1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        this.context = ChatRoomActivity.this;


        this.context = context;
        sp = context.getSharedPreferences("registered_status", Context.MODE_PRIVATE);
        spe = sp.edit();

        client = new AsyncHttpClient();
        choose=1;
        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        btnAttach = findViewById(R.id.btn_attach);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userName = findViewById(R.id.user_name);
        userImage = findViewById(R.id.user_image);
        offLineAndOnLine = findViewById(R.id.off_on_line);
//
//        userImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(getBaseContext(),
//                        "Name in ToolBar clicked",
//                        Toast.LENGTH_LONG).show();
//                userName.setText("User Name");
//                offLineAndOnLine.setBackground(getResources().getDrawable(R.drawable.chat_bubble_green_circle));
//            }
//        });

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
//        String title = intent.getStringExtra("name");

//        getSupportActionBar().setTitle("Syrow");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        toggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_arrow_back_white_24dp, getApplicationContext().getTheme());
        toggle.setHomeAsUpIndicator(drawable);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFeedbackForm();
            }
        });


        if (mTimer != null)
            mTimer.cancel();
        else
            mTimer = new Timer(); // recreate new timer
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, INTERVAL);

        if (chatRoomId == null) {
            Toast.makeText(getApplicationContext(), "Chat room not found!", Toast.LENGTH_SHORT).show();
            //finish();
        }
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        messageArrayList = new ArrayList<>();


        // self user id is to identify the message owner
        String selfUserId = "3333";

        mAdapter = new ChatRoomThreadAdapter(this, messageArrayList, selfUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push message is received
                    handlePushNotification(intent);
                }
            }
        };

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("");
            }
        });

        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        millSec = sp.getString("millSec", null);

        if (millSec == null){
            resetUID();
        }

        fetchChatThread();
        session = new Session();
        session.addOnJoinListener(this::demonstrateSubscribe);
        client1 = new Client(session, "ws://wamp.cioc.in:8090/ws", "default");
        exitInfoCompletableFuture = client1.connect();

        if (Build.VERSION.SDK_INT >= 11) {
            recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (bottom < oldBottom) {
                        recyclerView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.smoothScrollToPosition(
                                        recyclerView.getAdapter().getItemCount() - 1);
                            }
                        }, 100);
                    }
                }
            });
        }



        client.get(Backend.url + "/api/support/customerProfile/?service=" + companyID , new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    JSONObject compProfile = response.getJSONObject(0);

                    String firstMessage = compProfile.getString("firstMessage");
                    userName.setText(compProfile.getString("name"));


                    Date todaysDate = new Date();
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

                    Message message = new Message();
                    message.setUid(millSec);
                    message.setSentByAgent(true);
                    message.setMessage(firstMessage);
                    message.setUser("00");
                    message.setCreated(df.format(todaysDate));
                    messageArrayList.add(message);
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(ChatRoomActivity.this, "onFailure "+thread, Toast.LENGTH_SHORT).show();
            }

        });

//        resetUID();
    }

    public void showFeedbackForm(){
        View v = getLayoutInflater().inflate(R.layout.layout_feedback_rating, null, false);
        Button btnCancel = v.findViewById(R.id.action_btn_cancel);
        Button btnSubmit = v.findViewById(R.id.action_btn_submit);
        RatingBar ratingFeedback = v.findViewById(R.id.rating_bar);
        EditText feedbackText = v.findViewById(R.id.feedback_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        AlertDialog ad = builder.create();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ad.dismiss();
                finish();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String feedback = feedbackText.getText().toString().trim();
                Toast.makeText(context, "Feedback: "+ratingFeedback.getRating()+"\n"+feedback, Toast.LENGTH_SHORT).show();
                resetUID();
                ad.dismiss();
                finish();
            }
        });
        ad.show();
    }

    public void resetUID(){
        millSec = Long.toString(Calendar.getInstance().getTimeInMillis());
        spe.putString("millSec" , millSec );
        spe.apply();
    }

    private String add2(List<Integer> args, InvocationDetails details) {
        return millSec;
    }

    public void demonstrateSubscribe(Session session, SessionDetails details) {
        CompletableFuture<Subscription> subFuture = session.subscribe("service.support.chat." + millSec,
                this::onEvent);
        subFuture.whenComplete((subscription, throwable) -> {
            if (throwable == null) {
                System.out.println("Subscribed to topic " + subscription.topic);
                Toast.makeText(getApplicationContext(), "Subscribed", Toast.LENGTH_SHORT).show();
            } else {
                throwable.printStackTrace();
            }
        });


        CompletableFuture<Registration> regFuture = session.register("service.support.heartbeat." + millSec , this::add2);
        regFuture.thenAccept(registration ->
                System.out.println("Successfully registered procedure: " + registration.procedure));

    }

    private void onEvent(List<Object> args, Map<String, Object> kwargs, EventDetails details) {

        System.out.println(String.format("Got event: %s", args.get(0)));
        Toast.makeText(getApplicationContext(), "event "+args.get(0), Toast.LENGTH_SHORT).show();

        // add a notification strip here

        Object[] argsMap = ((LinkedHashMap)args.get(1)).entrySet().toArray();


        String agentName = args.get(2).toString();

//        getSupportActionBar().setTitle( agentName.substring(0, 1).toUpperCase() + agentName.substring(1));
        userName.setText(agentName.substring(0, 1).toUpperCase() + agentName.substring(1));


        if (args.get(0).toString().equals("M") || args.get(0).toString().equals("ML")){
            try {
                User user = new User( ((LinkedHashMap.Entry <String ,Object> )argsMap[4]).getValue().toString() , "Agent", null);
            }catch(Exception e){
                e.printStackTrace();
            }


            Message message = new Message();
            message.setPk(((LinkedHashMap.Entry <String ,Object> )argsMap[5]).getValue().toString());
            message.setUser(((LinkedHashMap.Entry <String ,Object> )argsMap[4]).getValue().toString());
            message.setSentByAgent(true);

            try{
                message.setMessage(((LinkedHashMap.Entry <String ,Object> )argsMap[6]).getValue().toString());
            }catch (Exception e){
                e.printStackTrace();
            }

            message.setCreated(((LinkedHashMap.Entry <String ,Object> )argsMap[1]).getValue().toString());

            try{
                message.setAttachment(((LinkedHashMap.Entry <String ,Object> )argsMap[7]).getValue().toString());
            }catch (Exception e){
                e.printStackTrace();
            }

            try{
                message.setAttachmentType(((LinkedHashMap.Entry <String ,Object> )argsMap[2]).getValue().toString());
            }catch (Exception e){
                e.printStackTrace();
            }

            messageArrayList.add(message);


            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }

            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{

            //http://syrow.cioc.in/api/support/supportChat/334/

            client.get(Backend.url+"/api/support/supportChat/" + ((LinkedHashMap.Entry <String ,Object> )argsMap[0]).getValue().toString() + "/" , new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    try {
                        Message message = new Message();
                        message.setPk(response.getString("pk"));
                        message.setUser(response.getString("user"));
                        message.setSentByAgent(response.getBoolean("sentByAgent"));
                        message.setMessage(response.getString("message"));
                        message.setAttachment(response.getString("attachment"));
                        message.setCreated(response.getString("created"));
                        message.setAttachmentType(response.getString("attachmentType"));
                        messageArrayList.add(message);

                        mAdapter.notifyDataSetChanged();
                        if (mAdapter.getItemCount() > 1) {
                            // scrolling to bottom of the recycler view
                            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Toast.makeText(ChatRoomActivity.this, "onFailure "+thread, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    private class TimeDisplayTimerTask extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }

    /**
     * Handling new push message, will add the message to
     * recycler view and scroll it to bottom
     * */
    private void handlePushNotification(Intent intent) {
        Message message = (Message) intent.getSerializableExtra("message");
        String chatRoomId = intent.getStringExtra("chat_room_id");

        if (message != null && chatRoomId != null) {
            messageArrayList.add(message);
            mAdapter.notifyDataSetChanged();
            if (mAdapter.getItemCount() > 1) {
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
            }
        }
    }

    private void selectImage() {
        View v = getLayoutInflater().inflate(R.layout.layout_gallery_and_camera, null, false);
        ImageView btnCamera = v.findViewById(R.id.btn_camera);
        ImageView btnGallery = v.findViewById(R.id.btn_gallery);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        AlertDialog ad = builder.create();
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraIntent();
                ad.dismiss();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryIntent();
                ad.dismiss();
            }
        });
        ad.show();
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */

    private void sendMessage(String msg) {
        final String message = this.inputMessage.getText().toString().trim();
        this.inputMessage.setText("");
        RequestParams params = new RequestParams();
        if (msg.equals("")) {
            if (TextUtils.isEmpty(message)) {
                Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            params.put("message", message);
            params.put("sentByAgent", false);
            params.put("uid", millSec);
        } else {
            if (bitmap!=null) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                byte[] image = output.toByteArray();
                params.put("attachment", new ByteArrayInputStream(image), msg);
                params.put("sentByAgent", false);
                params.put("attachmentType", "image");
                params.put("uid", millSec);
            }
        }
        client.post(Backend.url+"/api/support/supportChat/", params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject object) {
                try {
                    String userId = object.getString("uid");
                    Message message = new Message();
                    message.setPk(object.getString("pk"));
                    message.setUid(object.getString("uid"));
//                    message.setUser(object.getString("user"));
                    message.setSentByAgent(object.getBoolean("sentByAgent"));
                    message.setMessage(object.getString("message"));
                    message.setAttachment(object.getString("attachment"));
                    message.setCreated(object.getString("created"));
                    message.setAttachmentType(object.getString("attachmentType"));
                    messageArrayList.add(message);
                    if (!(message.getMessage().equals("null"))){
                        session.publish("service.support.agent", userId, "M", message);
                    }else{

                        MediaMessage mm = new MediaMessage();
                        mm.setFilePk(object.getString("pk"));
                        mm.setTyp("image");
                        mm.setUser(object.getString("user"));

                        session.publish("service.support.agent", userId, "MF", mm);
                    }



//                    (3) […]
//​
//                    0: "1535747475253"
//​
//                    1: "MF"
//​
//                    2: {…}
//​​
//                    filePk: 327
//​​
//                    typ: "image"
//​​
//                    user: 1
//​​



                    mAdapter.notifyDataSetChanged();
                    if (mAdapter.getItemCount() > 1) {
                        // scrolling to bottom of the recycler view
                        recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                Toast.makeText(getApplicationContext(), "JSONObject error: " + statusCode, Toast.LENGTH_SHORT).show();

            }
        });

        RequestParams threadParams = new RequestParams();
        threadParams.put("company", companyID);
        threadParams.put("uid",millSec);
        if (thread){
            client.post(Backend.url+"/api/support/chatThread/", threadParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    thread = false;
                    try {
                        new ChatThread(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                    Toast.makeText(ChatRoomActivity.this, "onFailure "+thread, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChooseTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChooseTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");

        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ivImage.setImageBitmap(thumbnail);
        sendMessage(destination.getAbsolutePath());

        base64 = bitmapToBase64(bitmap);
        Toast.makeText(context, ""+destination.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        Log.e("onCaptureImageResult",""+destination.getAbsolutePath());
    }

    private String bitmapToBase64(Bitmap bitmap) {
        byte[] byteArray = new byte[0];
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 65, byteArrayOutputStream);
            byteArray = byteArrayOutputStream.toByteArray();

        }catch (Exception e){
            e.printStackTrace();
        }
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }

    private Bitmap base64ToBitmap(String b64) {
        byte[] imageAsBytes = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            path = data.getData().getPath();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("onSelectFromGalleryResult",""+path);
            Toast.makeText(context, ""+path, Toast.LENGTH_SHORT).show();
        }
//        ivImage.setImageBitmap(bm);
        base64 = bitmapToBase64(bitmap);
        sendMessage(path);
    }



    /**
     * Fetching all the messages of a single chat room
     * */
    private void fetchChatThread() {
        //api/support/supportChat/?uid=1535435396312
        client.get(Backend.url+"/api/support/supportChat/?uid=" + millSec, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                for (int i=0; i<response.length(); i++){
                    try {
                        JSONObject object = response.getJSONObject(i);
                        User user = new User("self", "pkyad", null);
                        Message message = new Message();
                        message.setPk(object.getString("pk"));
                        message.setUser(object.getString("user"));
                        message.setSentByAgent(object.getBoolean("sentByAgent"));
                        message.setMessage(object.getString("message"));
                        message.setAttachment(object.getString("attachment"));
                        message.setCreated(object.getString("created"));
                        message.setAttachmentType(object.getString("attachmentType"));
                        messageArrayList.add(message);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                mAdapter.notifyDataSetChanged();
                if (mAdapter.getItemCount() > 1) {
                    recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

//        for (int i = 0; i < 4; i++) {
//
//            User user = new User("pradeeep", "pkyad", null);
//
//            Message message = new Message();
//            message.setId(Integer.toString(i) );
//            message.setMessage("sample message " +  Integer.toString(i) );
//            message.setCreatedAt("12:89 am");
//            message.setUser(user);
//
//            messageArrayList.add(message);
//        }

//        User user = new User("self", "pkyad", null);
//
//        Message message = new Message();
//        message.setId("dsds");
//        message.setMessage("sample messadsadasge " );
//        message.setCreatedAt("12:89 am");
//        message.setUser(user);

//        messageArrayList.add(message);
        mAdapter.notifyDataSetChanged();
        if (mAdapter.getItemCount() > 1) {
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, mAdapter.getItemCount() - 1);
        }
    }
    boolean res=false;

    @Override
    public void onBackPressed() {
        showFeedbackForm();
        if (res)
        super.onBackPressed();
    }
}
