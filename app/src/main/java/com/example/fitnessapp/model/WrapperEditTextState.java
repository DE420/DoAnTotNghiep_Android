package com.example.fitnessapp.model;

import android.graphics.drawable.Drawable;
import android.widget.EditText;
import android.widget.TextView;

public class WrapperEditTextState {
    private EditText editText;
    private boolean tag;
    private Drawable background;
    private TextView tvLabel;
    private TextView tvSupport;
    private int color;
    private int visibilityOfTvSupport;

    public WrapperEditTextState(Builder builder) {
        this.editText = builder.editText;
        this.tag = builder.tag;
        this.background = builder.background;
        this.tvLabel = builder.tvLabel;
        this.tvSupport = builder.tvSupport;
        this.color = builder.color;
        this.visibilityOfTvSupport = builder.visibilityOfTvSupport;
    }

    public EditText getEditText() {
        return editText;
    }

    public boolean isTag() {
        return tag;
    }

    public Drawable getBackground() {
        return background;
    }

    public TextView getTvLabel() {
        return tvLabel;
    }

    public TextView getTvSupport() {
        return tvSupport;
    }

    public int getColor() {
        return color;
    }

    public int getVisibilityOfTvSupport() {
        return visibilityOfTvSupport;
    }

    public static class Builder {
        private EditText editText;
        private boolean tag;
        private Drawable background;
        private TextView tvLabel;
        private TextView tvSupport;
        private int color;
        private int visibilityOfTvSupport;

        public Builder editText(EditText editText) {
            this.editText = editText;
            return this;
        }

        public Builder tag(boolean tag) {
            this.tag = tag;
            return this;
        }

        public Builder background(Drawable background) {
            this.background = background;
            return this;
        }

        public Builder tvLabel(TextView tvLabel) {
            this.tvLabel = tvLabel;
            return this;
        }

        public Builder tvSupport(TextView tvSupport) {
            this.tvSupport = tvSupport;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder visibilityOfTvSupport(int visibilityOfTvSupport) {
            this.visibilityOfTvSupport = visibilityOfTvSupport;
            return this;
        }

        public WrapperEditTextState build() {
            return new WrapperEditTextState(this);
        }

    }

}