package com.bluethunder.tar2.ui.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bluethunder.tar2.R;
import com.bluethunder.tar2.SessionConstants;
import com.bluethunder.tar2.ui.chat.model.Message;
import com.bluethunder.tar2.ui.chat.model.MessageType;
import com.bluethunder.tar2.utils.TimeAgo;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.common.base.Enums;
import com.rygelouv.audiosensei.player.AudioSenseiPlayerView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int CHAT_ME = 100;
    private final int CHAT_YOU = 200;
    List<Message> imagesList = new ArrayList<>();
    private List<Message> items = new ArrayList<>();
    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private String chatHeadId;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(Context context) {
        ctx = context;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == CHAT_ME) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me, parent, false);
            vh = new ItemViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_you, parent, false);
            vh = new ItemViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Message m = items.get(holder.getAbsoluteAdapterPosition());
        if (holder instanceof ItemViewHolder && m != null) {

            ItemViewHolder vItem = (ItemViewHolder) holder;
            TimeAgo timeAgo = new TimeAgo();
            timeAgo.locale(ctx);
            String result = timeAgo.getTimeAgo(m.getDate());
            vItem.text_time.setText(result);

            vItem.text_time.setVisibility(m.isShowTime() ? View.VISIBLE : View.GONE);

            vItem.lyt_parent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, m, holder.getAbsoluteAdapterPosition());
                }
            });

            if (m.getType().equals(MessageType.Image.name())) {

                vItem.image.setImageURI(m.getContent());
                vItem.image.setVisibility(View.VISIBLE);

                vItem.player.setVisibility(View.GONE);
                vItem.text.setVisibility(View.GONE);
                int po = imagesList.indexOf(m);
                vItem.image.setOnClickListener(v -> new ImageViewer.Builder<>(ctx, imagesList)
                        .setFormatter(Message::getContent)
                        .setStartPosition(po)
                        .hideStatusBar(true)
                        .allowZooming(true)
                        .allowSwipeToDismiss(true)
                        .show());
            } else if (m.getType().equals(MessageType.Records.name())) {

                vItem.player.setVisibility(View.VISIBLE);
                vItem.player.setAudioTarget(m.getContent());
                vItem.player.commitClickEvents();

                vItem.image.setVisibility(View.GONE);
                vItem.text.setVisibility(View.GONE);

            } else if (m.getType().equals(MessageType.Text.name())) {

                vItem.text.setVisibility(View.VISIBLE);
                vItem.text_content.setText(m.getContent());

                vItem.player.setVisibility(View.GONE);
                vItem.image.setVisibility(View.GONE);
            }

            vItem.player.commitClickEvents();
//
//            if (m.getMessageFrom().equals(Enums.Admin.name()) && !m.isRead()) {
//                Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
//                        + "://" + ctx.getPackageName() + "/raw/fortnite_notify");
//                Ringtone r = RingtoneManager.getRingtone(ctx, alarmSound);
//                r.play();
//                m.setRead(true);
//                FirebaseFirestore.getInstance().collection(Enums.Messages.name())
//                        .document(chatHeadId)
//                        .collection(Enums.Messages.name())
//                        .document(m.getId())
//                        .set(m)
//                        .addOnCompleteListener(new OnCompleteListener<Void>() {
//                            @Override
//                            public void onComplete(@NonNull Task<Void> task) {
//                                Log.d("chat", "onComplete: ");
//                            }
//                        });
//            }
        }
    }


    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getMessageFrom().equals(SessionConstants.INSTANCE.getCurrentLoggedInUserModel().getId()) ? CHAT_ME : CHAT_YOU;
    }

    public void insertItem(Message item) {
        this.items.add(item);
        notifyItemInserted(getItemCount());
        if (getItemCount() > 1) notifyItemChanged(getItemCount() - 2);

        if (item.getType().equals(MessageType.Image.name()))
            imagesList.add(item);
    }

    public void setItems(List<Message> items) {
        this.items = items;
    }

    public void setChatHeadId(String chatHeadId) {
        this.chatHeadId = chatHeadId;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, Message obj, int position);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        TextView text_content;
        TextView text_time;
        View lyt_parent;
        AudioSenseiPlayerView player;
        CardView text;
        SimpleDraweeView image;

        ItemViewHolder(View v) {
            super(v);
            text_content = v.findViewById(R.id.text_content);
            text_time = v.findViewById(R.id.text_time);
            lyt_parent = v.findViewById(R.id.lyt_parent);
            player = v.findViewById(R.id.audio_player);
            image = v.findViewById(R.id.message_image);
            text = v.findViewById(R.id.message_view);
        }
    }
}
