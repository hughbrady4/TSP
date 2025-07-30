package com.organicsystemsllc.travelingsalesman.ui.user;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.net.Uri;
import android.util.Patterns;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;

public class UserViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mValidState;
    private final MutableLiveData<String> mValidUserName;
    private final MutableLiveData<String> mValidPW;
    private final MutableLiveData<String> mText;
    private final MutableLiveData<FirebaseUser> mUser;

    private final MutableLiveData<Uri> mPhotoUri;
    private final MutableLiveData<Boolean> mTrackToggle;
    private final MutableLiveData<Boolean> mOnlineToggle;
    private final MutableLiveData<Timestamp> mTS;
    private final MutableLiveData<UserData>  mUserData;



    UserViewModel() {
        mValidState = new MutableLiveData<>();
        mValidUserName = new MutableLiveData<>();
        mValidPW = new MutableLiveData<>();
        mText = new MutableLiveData<>();
        mUser = new MutableLiveData<>();
        mPhotoUri = new MutableLiveData<>();
        mTrackToggle = new MutableLiveData<>();
        mOnlineToggle = new MutableLiveData<>();
        mTS = new MutableLiveData<>();
        mUserData = new MutableLiveData<>();
    }

    public void loginDataChanged(String username, String password) {
        if (isUserNameValid(username)) {
            mValidUserName.setValue(null);
        } else {
            mValidUserName.setValue("Invalid user name.");
        }

        if (isPasswordValid(password)) {
            mValidPW.setValue(null);
        } else {
            mValidPW.setValue("Password must be 5 characters.");
        }

        if (isUserNameValid(username) && isPasswordValid(password)) {
            mValidState.setValue(true);
        } else {
            mValidState.setValue(false);
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    LiveData<Boolean> getState() {
        return mValidState;
    }

    public MutableLiveData<String> getValidUserName() {
        return mValidUserName;
    }

    public MutableLiveData<String> getValidPW() {
        return mValidPW;
    }

    public MutableLiveData<FirebaseUser> getUser() { return mUser; }

    public MutableLiveData<String> getText() { return mText; }

    public MutableLiveData<Uri> getPhotoUri() {
        return mPhotoUri;
    }

    public MutableLiveData<Boolean> getTrackToggle() {
        return mTrackToggle;
    }

    public MutableLiveData<Boolean> getOnlineToggle() {
        return mOnlineToggle;
    }

    public MutableLiveData<Timestamp> getTS() {
        return mTS;
    }

    public MutableLiveData<UserData> getUserData() {
        return mUserData;
    }
}