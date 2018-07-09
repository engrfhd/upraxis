package com.engr.fhd.hired.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DataSnapshot;

public class Users implements Parcelable {
    String id;
    String first_name;
    String last_name;
    String email;
    String address;
    String birthday;
    String contact_person;
    String contact_number;
    String photo_url;
    String age;

    static String password;

    public Users() {

    }

    public Users(String first_name) {
        this.first_name = first_name;
    }

    public Users(String first_name, String last_name, String email, String address, String birthday, String contact_person, String contact_number,  String photo_url, String age) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.address = address;
        this.birthday = birthday;
        this.contact_person = contact_person;
        this.contact_number = contact_number;
        this.photo_url = photo_url;
        this.age = age;

    }

    public Users(DataSnapshot dataSnapshot) {
        this.id = dataSnapshot.getKey();

        if(null != dataSnapshot.child("first_name").getValue(String.class)) {
            this.first_name = dataSnapshot.child("first_name").getValue(String.class);
        }
        if(null != dataSnapshot.child("last_name").getValue(String.class)) {
            this.last_name = dataSnapshot.child("last_name").getValue(String.class);
        }
        if(null != dataSnapshot.child("email").getValue(String.class)) {
            this.email = dataSnapshot.child("email").getValue(String.class);
        }
        if(null != dataSnapshot.child("address").getValue(String.class)) {
            this.address = dataSnapshot.child("address").getValue(String.class);
        }
        if(null != dataSnapshot.child("birthday").getValue(String.class)) {
            this.birthday = dataSnapshot.child("birthday").getValue(String.class);
        }
        if(null != dataSnapshot.child("contact_person").getValue(String.class)) {
            this.contact_person = dataSnapshot.child("contact_person").getValue(String.class);
        }
        if(null != dataSnapshot.child("contact_number").getValue(String.class)) {
            this.contact_number = dataSnapshot.child("contact_number").getValue(String.class);
        }
        if(null != dataSnapshot.child("photo_url").getValue(String.class)) {
            this.photo_url = dataSnapshot.child("photo_url").getValue(String.class);
        }
        if(null != dataSnapshot.child("age").getValue(String.class)) {
            this.age = dataSnapshot.child("age").getValue(String.class);
        }
    }

    protected Users(Parcel in) {
        id = in.readString();
        first_name = in.readString();
        last_name = in.readString();
        email = in.readString();
        address = in.readString();
        birthday = in.readString();
        contact_person = in.readString();
        contact_number = in.readString();
        photo_url = in.readString();
        age = in.readString();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };


    public String getFirst_name() {
        return first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getBirthday() {
        return birthday;
    }

    public String getContact_person() {
        return contact_person;
    }

    public String getContact_number() {
        return contact_number;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public String getAge() {
        return age;
    }

    public static String getPassword() {
        return password;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(first_name);
        dest.writeString(last_name);
        dest.writeString(email);
        dest.writeString(address);
        dest.writeString(birthday);
        dest.writeString(contact_person);
        dest.writeString(contact_number);
        dest.writeString(photo_url);
        dest.writeString(age);
    }
}
