package com.poiuyreq0.soulmateletter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.poiuyreq0.soulmateletter.databinding.ViewItemBinding;

import java.util.List;

public class LetterAdapter extends RecyclerView.Adapter<LetterViewHolder> {

    private List<Letter> letters;

    public LetterAdapter(List<Letter> letters) {
        this.letters = letters;
    }

    @Override
    public LetterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewItemBinding binding = ViewItemBinding.inflate(layoutInflater, parent, false);

        return new LetterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(LetterViewHolder holder, int position) {

        final Context context = holder.itemView.getContext();
        final int current = position;

        String sender = letters.get(current).getSender()
                .replace("+82", "0")
                .replaceFirst("(\\d{3})(\\d{4})(\\d+)", "$1-$2-$3");

        holder.sender.setText(sender);
        holder.text.setText(letters.get(current).getText());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                intent.putExtra("sender", sender);
                intent.putExtra("text", letters.get(current).getText());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return letters.size();
    }
}
