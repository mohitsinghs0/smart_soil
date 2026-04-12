package com.example.smart_soil.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_soil.R;
import com.example.smart_soil.activities.SoilTestActivity;
import com.example.smart_soil.database.FarmEntity;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class FarmEntityAdapter extends RecyclerView.Adapter<FarmEntityAdapter.ViewHolder> {

    private final Context context;
    private List<FarmEntity> farms = new ArrayList<>();

    public FarmEntityAdapter(Context context) {
        this.context = context;
    }

    public void setFarms(List<FarmEntity> farms) {
        this.farms = farms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_farm, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FarmEntity farm = farms.get(position);
        holder.name.setText(farm.getName());
        holder.location.setText(farm.getLocation());
        
        holder.testButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SoilTestActivity.class);
            intent.putExtra("farm_id", farm.getId());
            intent.putExtra("farm_name", farm.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return farms.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, location;
        MaterialButton testButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.farm_name);
            location = itemView.findViewById(R.id.farm_location);
            testButton = itemView.findViewById(R.id.test_button);
        }
    }
}
