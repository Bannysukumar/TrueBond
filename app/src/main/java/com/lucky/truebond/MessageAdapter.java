package com.lucky.truebond;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages;
    private String currentUserId;
    private OnMessageLongClickListener longClickListener;

    public interface OnMessageLongClickListener {
        void onMessageLongClick(Message message);
    }

    public MessageAdapter(List<Message> messages, String currentUserId, OnMessageLongClickListener listener) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        
        // Set message text or show deleted message
        if (message.isDeleted()) {
            holder.textViewMessage.setText("This message was deleted");
            holder.textViewMessage.setAlpha(0.5f);
        } else {
            holder.textViewMessage.setText(message.getText());
            holder.textViewMessage.setAlpha(1.0f);
        }

        // Set message time
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.textViewTime.setText(sdf.format(new Date(message.getTimestamp())));

        // Show read receipt for sent messages
        if (message.getSenderId().equals(currentUserId)) {
            holder.imageViewReadReceipt.setVisibility(View.VISIBLE);
            if (message.isRead()) {
                holder.imageViewReadReceipt.setImageResource(R.drawable.ic_double_tick_read);
            } else {
                holder.imageViewReadReceipt.setImageResource(R.drawable.ic_double_tick);
            }
        } else {
            holder.imageViewReadReceipt.setVisibility(View.GONE);
        }

        // Show typing indicator
        if (message.isTyping()) {
            holder.textViewTyping.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTyping.setVisibility(View.GONE);
        }

        // Set message alignment
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.cardViewMessage.getLayoutParams();
        if (message.getSenderId().equals(currentUserId)) {
            params.horizontalBias = 1.0f;
            holder.cardViewMessage.setCardBackgroundColor(Color.parseColor("#DCF8C6"));
        } else {
            params.horizontalBias = 0.0f;
            holder.cardViewMessage.setCardBackgroundColor(Color.WHITE);
        }
        holder.cardViewMessage.setLayoutParams(params);

        // Set long click listener for message deletion
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onMessageLongClick(message);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardViewMessage;
        TextView textViewMessage;
        TextView textViewTime;
        ImageView imageViewReadReceipt;
        TextView textViewTyping;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            cardViewMessage = itemView.findViewById(R.id.cardViewMessage);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            imageViewReadReceipt = itemView.findViewById(R.id.imageViewReadReceipt);
            textViewTyping = itemView.findViewById(R.id.textViewTyping);
        }
    }
} 