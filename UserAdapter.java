package com.engr.fhd.hired.Adapter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.engr.fhd.hired.Models.Users;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import com.engr.fhd.hired.R;

import static com.facebook.FacebookSdk.getApplicationContext;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<Users> userList;
    private DatabaseReference mRef;

    private UserAdapter.OnItemClickListener onItemClickListener;

    public UserAdapter(List<Users> userList) {
        this.userList = userList;
        mRef = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_users, parent, false);
        UserViewHolder requestViewHolder = new UserAdapter.UserViewHolder(v, onItemClickListener);

        return requestViewHolder;
    }

    @Override
    public void onBindViewHolder(final UserAdapter.UserViewHolder holder, int position) {
        Users users = userList.get(position);

        Glide.with(getApplicationContext()).load(users.getPhoto_url()).fitCenter().into(holder.profile_picture);
        holder.profile_name.setText(users.getFirst_name()  + "  " + users.getLast_name());
        holder.profile_email.setText(users.getEmail());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {

        return  userList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cardView;
        ImageView profile_picture;
        TextView profile_name;
        TextView profile_email;
        OnItemClickListener onItemClickListener;

        public UserViewHolder(View itemView, OnItemClickListener onItemClickListener) {
            super(itemView);

            cardView = (CardView)itemView.findViewById(R.id.user_cardview);

            profile_picture = (ImageView) itemView.findViewById(R.id.profile_picture);
            profile_name = (TextView)itemView.findViewById(R.id.profile_name);
            profile_email = (TextView)itemView.findViewById(R.id.profile_email);

            this.onItemClickListener = onItemClickListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v,getAdapterPosition());
        }
    }
}
