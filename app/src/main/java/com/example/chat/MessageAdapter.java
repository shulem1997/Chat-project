package com.example.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ReceivedMessageBinding;

import com.example.chat.databinding.SentMessageBinding;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<Message> msgList;
    private String logged;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVE = 2;

    public MessageAdapter(List<Message> msgList, String logged) {
        this.msgList = msgList;
        this.logged = logged;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageView(
                    SentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent, false));
        }
        return new ReceivedMessageView(
                ReceivedMessageBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageView)holder).setData(msgList.get(position));
        } else {
            ((ReceivedMessageView)holder).setData(msgList.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return msgList.size();
    }
    public int getItemViewType(int position) {
        if(msgList.get(position).getSenderName().equals(logged)) {
            return VIEW_TYPE_SENT;
        }
        return VIEW_TYPE_RECEIVE;
    }

    static class SentMessageView extends RecyclerView.ViewHolder {
        private final SentMessageBinding binding;
        SentMessageView(SentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(Message m) {
            binding.textMessage.setText(m.getContent());
            binding.date.setText(m.getCreated());
        }
    }

    static class ReceivedMessageView extends RecyclerView.ViewHolder {
        private final ReceivedMessageBinding binding;
        ReceivedMessageView(ReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void setData(Message m) {
            binding.textMessage.setText(m.getContent());
            binding.date.setText(m.getCreated());
        }
    }

}


