package com.poiuyreq0.soulmateletter;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.poiuyreq0.soulmateletter.databinding.ViewItemBinding;

public class LetterViewHolder extends RecyclerView.ViewHolder {

    TextView sender;
    TextView text;

    public LetterViewHolder(ViewItemBinding binding) {
        super(binding.getRoot());

        sender = binding.sender;
        text = binding.text;
    }
}
