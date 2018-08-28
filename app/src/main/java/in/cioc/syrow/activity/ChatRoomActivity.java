package in.cioc.syrow.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import cz.msebera.android.httpclient.Header;
import in.cioc.syrow.Backend;
import in.cioc.syrow.R;
import in.cioc.syrow.adapter.ChatRoomThreadAdapter;
import in.cioc.syrow.app.Config;
import in.cioc.syrow.model.MyApplication;
import in.cioc.syrow.helper.Utility;
import in.cioc.syrow.model.ChatThread;
import in.cioc.syrow.model.Message;
import in.cioc.syrow.model.User;
import io.crossbar.autobahn.wamp.Client;
import io.crossbar.autobahn.wamp.Session;
import io.crossbar.autobahn.wamp.types.EventDetails;
import io.crossbar.autobahn.wamp.types.ExitInfo;
import io.crossbar.autobahn.wamp.types.SessionDetails;
import io.crossbar.autobahn.wamp.types.Subscription;

public class ChatRoomActivity extends AppCompatActivity {

    private String TAG = ChatRoomActivity.class.getSimpleName();

    private String chatRoomId;
    private RecyclerView recyclerView;
    private ChatRoomThreadAdapter mAdapter;
    private ArrayList<Message> messageArrayList;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private EditText inputMessage;
    private ImageView btnSend, btnAttach;
    private AsyncHttpClient client;
    boolean thread = true;
    Context context;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private Button btnSelect;
    private ImageView ivImage;
    String base64;
    private String userChoosenTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        this.context = ChatRoomActivity.this;
        client = new AsyncHttpClient();

        inputMessage = findViewById(R.id.message);
        btnSend = findViewById(R.id.btn_send);
        btnAttach = findViewById(R.id.btn_attach);

        Intent intent = getIntent();
        chatRoomId = intent.getStringExtra("chat_room_id");
        String title = intent.getStringExtra("name");

        getSupportActionBar().setTitle("Syrow");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (chatRoomId == null) {
            Toast.makeText(getApplicationContext(), "Chat room not found!", Toast.LENGTH_SHORT).show();
            //finish();
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        messageArrayList = new ArrayList<>();

        // self user id is to identify the message owner
        String selfUserId = "3333";

        mAdapter = new ChatRoomThreadAdapter(context, messageArrayList, selfUserId);

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
                sendMessage();
            }
        });

        btnAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        fetchChatThread();

        Session session;
        Client client;
        CompletableFuture<ExitInfo> exitInfoCompletableFuture;
        session = new Session();
        session.addOnJoinListener(this::demonstrateSubscribe);
        client = new Client(session, "ws://wamp.cioc.in:8090/ws", "default");
        exitInfoCompletableFuture = client.connect();
    }

    public void demonstrateSubscribe(Session session, SessionDetails details) {
        CompletableFuture<Subscription> subFuture = session.subscribe("service.self" ,
                this::onEvent);
        subFuture.whenComplete((subscription, throwable) -> {
            if (throwable == null) {
                System.out.println("Subscribed to topic " + subscription.topic);
                Toast.makeText(getApplicationContext(), "Subscribed", Toast.LENGTH_SHORT).show();
            } else {
                throwable.printStackTrace();
            }
        });
    }

    private void onEvent(List<Object> args, Map<String, Object> kwargs, EventDetails details) {
        System.out.println(String.format("Got event: %s", args.get(0)));


        // add a notification strip here

    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
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
        final CharSequence[] items = { "Take Photo", "Choose a photo from the gallery",
                "Cancel" };

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
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int item) {
//                boolean result = Utility.checkPermission(context);
//
//                if (items[item].equals("Take Photo")) {
//                    userChoosenTask = "Take Photo";
//                    if(result)
//                        cameraIntent();
//
//                } else if (items[item].equals("Choose a photo from the gallery")) {
//                    userChoosenTask ="Choose a photo from the gallery";
//                    if(result)
//                        galleryIntent();
//
//                } else if (items[item].equals("Cancel")) {
//                    dialog.dismiss();
//                }
//            }
//        });
        ad.show();
    }

    /**
     * Posting a new message in chat room
     * will make an http call to our server. Our server again sends the message
     * to all the devices as push notification
     * */
    private void sendMessage() {
        final String message = this.inputMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(getApplicationContext(), "Enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        long millSec = Calendar.getInstance().getTimeInMillis();
        this.inputMessage.setText("");
        base64="";
        RequestParams params = new RequestParams();
        params.put("message", message);
        params.put("sentByAgent", false);
        params.put("uid", millSec);

        client.post(Backend.url+"/api/support/supportChat/", params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject object) {
                try {
                    Toast.makeText(getApplicationContext(), "" + object.getString("message"), Toast.LENGTH_LONG).show();
                    String commentId = object.getString("pk");
                    String commentText = object.getString("message");
                    String createdAt = object.getString("created");
                    String userId = object.getString("uid");
                    String userName = object.getString("user");
                    String attachment = object.getString("attachment");
                    String attachmentType = object.getString("attachmentType");
                    Boolean sentByAgent = object.getBoolean("sentByAgent");
                    User user = new User(userId, userName, null);

                    Message message = new Message();
                    message.setId(userId);
                    message.setMessage(commentText);
                    message.setMessageImg(commentText);
                    message.setCreatedAt(createdAt);
                    message.setUser(user);
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
                Toast.makeText(getApplicationContext(), "JSONObject error: " + statusCode, Toast.LENGTH_SHORT).show();

            }
        });

        RequestParams threadParams = new RequestParams();
        threadParams.put("company", "1");
        threadParams.put("uid",millSec);
        if (thread){
            client.post(Backend.url+"/api/support/chatThread/", threadParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Toast.makeText(ChatRoomActivity.this, "success "+thread, Toast.LENGTH_SHORT).show();
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
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void galleryIntent()
    {
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
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

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
        base64 = bitmapToBase64(thumbnail);
        Toast.makeText(context, ""+base64, Toast.LENGTH_SHORT).show();
        Toast.makeText(context, ""+thumbnail, Toast.LENGTH_LONG).show();
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

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        ivImage.setImageBitmap(bm);
        base64 = bitmapToBase64(bm);
        Toast.makeText(context, ""+base64, Toast.LENGTH_SHORT).show();
        Toast.makeText(context, ""+bm, Toast.LENGTH_LONG).show();
    }



    /**
     * Fetching all the messages of a single chat room
     * */
    private void fetchChatThread() {
        //api/support/supportChat/?uid=1535435396312
        client.get(Backend.url+"/api/support/supportChat/?uid=1535453455607", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                for (int i=0; i<response.length(); i++){
                    try {
                        JSONObject object = response.getJSONObject(i);

                        User user = new User("self", "pkyad", null);
                        Message message = new Message();
                        message.setId(object.getString("uid"));
                        message.setMessage(object.getString("message"));
                        message.setMessageImg(object.getString("attachment"));
                        message.setCreatedAt(object.getString("created"));
                        message.setUser(user);
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

        for (int i = 0; i < 4; i++) {

            User user = new User("pradeeep", "pkyad", null);

            Message message = new Message();
            message.setId(Integer.toString(i) );
            message.setMessage("sample message " +  Integer.toString(i) );
            message.setCreatedAt("12:89 am");
            message.setUser(user);

            messageArrayList.add(message);
        }

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
}
