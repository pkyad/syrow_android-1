package in.cioc.syrow.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        if (message.getMessage().equals("")||message.getMessage().equals("null")||message.getMessage()==null){
            if (!(message.getMessageImg().equals("")||message.getMessageImg().equals("null")||message.getMessageImg()==null)) {
                holder.message.setVisibility(View.GONE);
                holder.messageImage.setVisibility(View.VISIBLE);
//                Uri uri = Uri.parse(message.getMessageImg());
//                holder.messageImage.setImageURI(uri);
                Glide.with(mContext)
                        .load(message.getMessageImg())
                        .into(holder.messageImage);
                holder.messageImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ViewPagerActivity.class);
                        intent.putExtra("position", imagePosition);
                        intent.putExtra("imageUrl", message.getMessageImg());
                        mContext.startActivity(intent);
                    }
                });
            }
        } else {
            holder.messageImage.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            ((ViewHolder) holder).message.setText(message.getMessage());
        }

        String timestamp = "";//getTimeStamp(message.getCreatedAt());
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        inputFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        outputFormat.setTimeZone(TimeZone.getDefault());
        Date date = null;
        int hours, minute;
        String format;
        try {
            date = inputFormat.parse(message.getCreatedAt());
            int hourOfDay = date.getHours();
//            Toast.makeText(mContext, "Time : "+date.getHours()+date.getMinutes(), Toast.LENGTH_SHORT).show();
            if (hourOfDay == 0) {
                hourOfDay += 12;
                format = "AM";
            } else if (hourOfDay == 12) {
                format = "PM";
            } else if (hourOfDay > 12) {
                hourOfDay -= 12;
                format = "PM";
            } else {
                format = "AM";
            }
            if (date.getHours()>12) {
                timestamp = date.getHours()+":"+date.getMinutes()+" PM";
            } else if (date.getHours()<=12) {
                timestamp = date.getHours()+":"+date.getMinutes()+" AM";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (message.getCreatedAt() != null) {
//            if (timestamp.equals("12:00"))
            timestamp = timestamp + ". ";
        }
        holder.timestamp.setText(timestamp);
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

    public static String getTimeStamp(String dateStr) {
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        inputFormat.setTimeZone(TimeZone.getTimeZone("IST"));
//        outputFormat.setTimeZone(TimeZone.getDefault());

        Date date2 = null;
        String timestamp = "";
        try {
            date2 = inputFormat.parse(dateStr);
            timestamp = outputFormat.format(date2);

        } catch (ParseException e) {
            e.printStackTrace();
        }
//        today = today.length() < 2 ? "0" + today : today;
//
//        try {
//            Date date = format.parse(dateStr);
//            SimpleDateFormat todayFormat = new SimpleDateFormat("dd");
//            String dateToday = todayFormat.format(date);
//            format = dateToday.equals(today) ? new SimpleDateFormat("hh:mm a") : new SimpleDateFormat("dd LLL, hh:mm a");
//            String date1 = format.format(date);
//            timestamp = date1.toString();
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        return timestamp;
    }
}

