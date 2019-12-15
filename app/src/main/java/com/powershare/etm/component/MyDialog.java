package com.powershare.etm.component;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.powershare.etm.R;

public class MyDialog extends AlertDialog {

    private Context mContext;
    private Builder builder;
    private Button btnSure;

    private MyDialog(Context context, Builder builder) {
        super(context, R.style.CustomDialog);
        this.mContext = context;
        this.builder = builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_dialog, null);
        setContentView(view);
        TextView content = view.findViewById(R.id.content);
        btnSure = view.findViewById(R.id.sure);
        Button btnCancel = view.findViewById(R.id.cancel);
        if (builder.contentText != null) {
            content.setText(builder.contentText);
        }
        if (builder.cancelText != null) {
            btnCancel.setText(builder.cancelText);
        }
        if (builder.sureText != null) {
            btnSure.setText(builder.sureText);
        }
        btnSure.setOnClickListener(v -> {
            cancel();
            if (builder.sureListener != null) {
                builder.sureListener.onClick(v);
            }
        });
        this.setCanceledOnTouchOutside(false);
        btnCancel.setOnClickListener(v -> cancel());
    }

    public Button getSureButton() {
        return btnSure;
    }

    public static class Builder {
        private Context mContext;
        private String cancelText;
        private String sureText;
        private String contentText;
        private View.OnClickListener sureListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setSureText(String text) {
            sureText = text;
            return this;
        }

        public Builder setCancelText(String text) {
            cancelText = text;
            return this;
        }

        public Builder setContent(String text) {
            contentText = text;
            return this;
        }

        public Builder setSureListener(View.OnClickListener listener) {
            this.sureListener = listener;
            return this;
        }

        public MyDialog create() {
            return new MyDialog(mContext, this);
        }
    }
}