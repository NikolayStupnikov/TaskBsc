package com.example.taskbsk.recycle;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskbsk.CircleTransform;
import com.example.taskbsk.R;
import com.example.taskbsk.model.Item;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Item> itemList;

    private int size;

    public void addItem(List<Item> itemList) {
        this.itemList.addAll(itemList);
        notifyDataSetChanged();
    }

    public ListAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.itemList = new ArrayList<>();
        size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40,
                context.getResources().getDisplayMetrics());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);

        holder.name.setText(item.getName());

        Picasso.get()
                .load(item.getUrl())
                .resize(size, size)
                .transform(new CircleTransform())
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.avatar = itemView.findViewById(R.id.avatar);
            this.name = itemView.findViewById(R.id.name);
        }
    }

}
