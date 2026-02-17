package com.SDIOS.ServiceControl.Recycler;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.SDIOS.ServiceControl.ConfigurationManager;
import com.SDIOS.ServiceControl.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.MyViewHolder> {
    private final List<ClassifiersPackage> mData;
    private final ChooseUpdate chooseUpdateActivity;

    PackageAdapter(ChooseUpdate mContext, List<ClassifiersPackage> mData) {
        this.chooseUpdateActivity = mContext;
        this.mData = mData;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PackageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View myView;
        myView = LayoutInflater.from(chooseUpdateActivity).inflate(R.layout.card_view, parent, false);
        return new MyViewHolder(myView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int _position) {
        int position = holder.getBindingAdapterPosition();
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.t1.setText(mData.get(position).package_name);
        holder.t2.setText(mData.get(position).version);
        holder.t3.setText(mData.get(position).description);
        if (mData.get(position).installed) {
            Drawable drawable = chooseUpdateActivity.getResources().getDrawable(R.drawable.check_foreground);
            holder.img.setImageDrawable(drawable);
        }
        holder.card.setOnClickListener(view -> {
            ClassifiersPackage chosen_package = mData.get(position);
            if (chosen_package.installed) {
                ConfigurationManager.getInstance().setDefaultConfiguration();
                Toast.makeText(chooseUpdateActivity, "You already have it! setting to default", Toast.LENGTH_SHORT).show();
            } else {
                chooseUpdateActivity.getPackage(chosen_package);
            }
        });
    }

    // Return the size of your data-set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        final View card;
        final TextView t1;
        final TextView t2;
        final TextView t3;
        final ImageView img;

        MyViewHolder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_item);
            t1 = itemView.findViewById(R.id.pack_name);
            t2 = itemView.findViewById(R.id.pack_version);
            t3 = itemView.findViewById(R.id.pack_description);
            img = itemView.findViewById(R.id.img);
        }
    }
}