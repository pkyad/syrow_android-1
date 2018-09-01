package in.cioc.syrow.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import in.cioc.syrow.R;
import in.cioc.syrow.activity.FullscreenActivity;
import in.cioc.syrow.activity.ViewPagerActivity;
import in.cioc.syrow.model.Message;

public class ChatRoomThreadAdapter extends RecyclerView.Adapter<ChatRoomThreadAdapter.ViewHolder> {

    private static String TAG = ChatRoomThreadAdapter.class.getSimpleName();

    private String userId;
    private int SELF = 100;
    private static String today;

    private Context mContext;
    private ArrayList<Message> messageArrayList;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message, timestamp;
        ImageView messageImage;

        public ViewHolder(View view) {
            super(view);
            message = itemView.findViewById(R.id.message);
            messageImage =  itemView.findViewById(R.id.message_image);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    public ChatRoomThreadAdapter(Context mContext, ArrayList<Message> messageArrayList, String userId) {
        this.mContext = mContext;
        this.messageArrayList = messageArrayList;
        this.userId = "self";

        Calendar calendar = Calendar.getInstance();
        today = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        // view type is to identify where to render the chat message
        // left or right
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_other, parent, false);
        } else {
            // others message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_self, parent, false);
        }
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        int imagePosition = 0;

        try {
//            if (message.getMessage().equals("")||message.getMessage().equals("null")||message.getMessage()==null){
                if (!(message.getAttachment().equals("")||message.getAttachment().equals("null")||message.getAttachment()==null)) {
                    holder.message.setVisibility(View.GONE);
                    holder.messageImage.setVisibility(View.VISIBLE);
//                Uri uri = Uri.parse(message.getMessageImg());
//                holder.messageImage.setImageURI(uri);
                    Glide.with(mContext)
                            .load(message.getAttachment())
                            .into(holder.messageImage);
                    holder.messageImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, ViewPagerActivity.class);
                            intent.putExtra("position", imagePosition);
                            intent.putExtra("imageUrl", message.getAttachment());
                            mContext.startActivity(intent);
                        }
                    });
                }
            else {
                if((message.getAttachmentType().equals("youtubeLink"))){
                    holder.message.setVisibility(View.GONE);
                    holder.messageImage.setVisibility(View.VISIBLE);
//                    String[] id = message.getMessage().split("https://www.youtube.com/embed/");
                    List<String> strings = Arrays.asList(message.getMessage().split("/"));
                    String id = strings.set(strings.size() - 1,"");
//                    if(message.getMessage().split("/") != null && message.getMessage().split("/").length > 0) {
//                        id = message.getMessage().split("/")[message.getMessage().split("/").length - 1];
//                    }
                    Glide.with(mContext)
                            .load("https://img.youtube.com/vi/"+id+"/0.jpg")
                            .into(holder.messageImage);
                    holder.messageImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
                            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("http://www.youtube.com/watch?v=" + id));
                            try {
                                mContext.startActivity(appIntent);
                            } catch (ActivityNotFoundException ex) {
                                mContext.startActivity(webIntent);
                            }
                        }
                    });

                } else if (message.getMessage().contains("<p>")){
                    holder.messageImage.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).message.setText(Html.fromHtml(message.getMessage(), Html.FROM_HTML_MODE_COMPACT));
                }else{
                    holder.messageImage.setVisibility(View.GONE);
                    holder.message.setVisibility(View.VISIBLE);
                    ((ViewHolder) holder).message.setText(message.getMessage());
                }



            }

            String timestamp = "";//getTimeStamp(message.getCreatedAt());
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat inputFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
            inputFormat.setTimeZone(TimeZone.getTimeZone("IST"));
            outputFormat.setTimeZone(TimeZone.getDefault());
            Date date = null;
            String format;
            try {

                try{
                    date = inputFormat.parse(message.getCreated());
                }catch (Exception e){
                    date = inputFormat2.parse(message.getCreated());
                }

                int hourOfDay = date.getHours();
                if (hourOfDay == 0) {
                    hourOfDay += 12;
                    format = " AM";
                } else if (hourOfDay == 12) {
                    format = " PM";
                } else if (hourOfDay > 12) {
                    hourOfDay -= 12;
                    format = " PM";
                } else {
                    format = " AM";
                }
                if (String.valueOf(hourOfDay).length()==1 && String.valueOf(date.getMinutes()).length()==1)
                    timestamp = "0"+hourOfDay + ":0"+ date.getMinutes() + format;
                else  if (String.valueOf(hourOfDay).length()==1||String.valueOf(date.getMinutes()).length()==1)
                    if (String.valueOf(hourOfDay).length()==1)
                        timestamp = "0"+hourOfDay + ":" + date.getMinutes() + format;
                if (String.valueOf(date.getMinutes()).length()==1)
                    timestamp = hourOfDay + ":0" + date.getMinutes() + format;
                else
                    timestamp = hourOfDay + ":" + date.getMinutes() + format;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (message.getCreated() != null) {
                holder.timestamp.setText(timestamp);
            } else {
                holder.timestamp.setText(timestamp);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.isSentByAgent()) {
            return SELF;
        }
        return position;
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public String extractYoutubeId(String url) throws MalformedURLException {
        String query = new URL(url).getQuery();
        String[] param = query.split("&");
        String id = null;
        for (String row : param) {
            String[] param1 = row.split("=");
            if (param1[0].equals("v")) {
                id = param1[1];
            }
        }
        return id;
    }

    public static String getTimeStamp(String dateStr) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        inputFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        Date date2 = null;
        String timestamp = "";
        try {
            date2 = inputFormat.parse(dateStr);
            timestamp = outputFormat.format(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }
}

