package com.expensetracker.ui.noaccount;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NoaccountViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public NoaccountViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is noaccount fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}