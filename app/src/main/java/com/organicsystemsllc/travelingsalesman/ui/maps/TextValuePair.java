package com.organicsystemsllc.travelingsalesman.ui.maps;

import androidx.annotation.NonNull;

public class TextValuePair {
    public TextValuePair() {
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
    }

    public Long getValue() {
        return mValue;
    }

    public void setValue(Long value) {
        this.mValue = value;
    }

    @NonNull
    @Override
    public String toString() {
        return "TextValuePair{" +
                "Text='" + mText + '\'' +
                ", Value=" + mValue +
                '}';
    }

    private String mText;
    private Long mValue;


}