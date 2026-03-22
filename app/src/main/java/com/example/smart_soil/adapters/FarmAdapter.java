package com.example.smart_soil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.models.Farm;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FarmAdapter extends RecyclerView.Adapter<FarmAdapter.FarmViewHolder> {

    private final Context context;
    private final List<Farm> farmList;

    public FarmAdapter(Context context, List<Farm> farmList) {
        this.context = context;
        this.farmList = farmList;
    }

    @NonNull
    @Override
    public FarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_farm, parent, false);
        return new FarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FarmViewHolder holder, int position) {
        Farm farm = farmList.get(position);

        holder.farmName.setText(farm.name);
        String location = farm.village + ", " + farm.district;
        holder.farmLocation.setText(location);
        
        // Dummy data for weather and last test
        holder.weatherTemp.setText("27.1°C");
        holder.weatherHumidity.setText("45%");
        holder.weatherWind.setText("9.4 km/h");
        holder.lastTestInfo.setText("Last test: 09/03/2026 • " + farm.crop_type);

        // Click listeners
        holder.testButton.setOnClickListener(v -> {
            Toast.makeText(context, "Starting test for " + farm.name, Toast.LENGTH_SHORT).show();
            // Intent to SoilTestActivity would go here
        });

        holder.historyButton.setOnClickListener(v -> {
            Toast.makeText(context, "Viewing history for " + farm.name, Toast.LENGTH_SHORT).show();
            // Intent to HistoryActivity would go here
        });

        holder.moreButton.setOnClickListener(v -> {
            // TODO: Implement popup menu for Edit/Delete
            Toast.makeText(context, "More options for " + farm.name, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return farmList.size();
    }

    public static class FarmViewHolder extends RecyclerView.ViewHolder {
        TextView farmName, farmLocation, weatherTemp, weatherHumidity, weatherWind, lastTestInfo;
        ImageView moreButton;
        MaterialButton testButton, historyButton;

        public FarmViewHolder(@NonNull View itemView) {
            super(itemView);
            farmName = itemView.findViewById(R.id.farm_name);
            farmLocation = itemView.findViewById(R.id.farm_location);
            weatherTemp = itemView.findViewById(R.id.weather_temp);
            weatherHumidity = itemView.findViewById(R.id.weather_humidity);
            weatherWind = itemView.findViewById(R.id.weather_wind);
            lastTestInfo = itemView.findViewById(R.id.last_test_info);
            moreButton = itemView.findViewById(R.id.more_button);
            testButton = itemView.findViewById(R.id.test_button);
            historyButton = itemView.findViewById(R.id.history_button);
        }
    }
}
