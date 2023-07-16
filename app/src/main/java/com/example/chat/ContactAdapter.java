package com.example.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chat.databinding.ContactItemBinding;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder>{

    private List<User> contacts;

    private OnItemClickListener clickListener;
    public ContactAdapter(List<User> contacts) {
        this.contacts = contacts;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ContactItemBinding binding = ContactItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ContactViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        holder.setContactData(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return this.contacts.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        ContactItemBinding binding;

        ContactViewHolder(ContactItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            clickListener.onItemClick(view, position);
                        }
                    }
                }
            });
        }

        void setContactData(User user) {
            binding.contactName.setText(user.getDisplayName());
            binding.profilePic.setImageBitmap(getImage(user.getProfilePic()));
        }
    }

    private Bitmap getImage(String encodedImg) {
        if(encodedImg == null) {
            //return R.drawable.default_pic;
        }
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }
}
