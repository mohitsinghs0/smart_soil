package com.example.smart_soil.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.activities.BaseActivity;
import com.example.smart_soil.activities.HistoryActivity;
import com.example.smart_soil.activities.SoilTestActivity;
import com.example.smart_soil.models.Farm;
import com.example.smart_soil.services.RetrofitClient;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

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

        // Fixed Navigation for TEST button
        holder.testButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, SoilTestActivity.class);
            intent.putExtra("farm_id", farm.id);
            intent.putExtra("farm_name", farm.name);
            context.startActivity(intent);
        });

        // Fixed Navigation for History button
        holder.historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, HistoryActivity.class);
            intent.putExtra("farm_id", farm.id);
            context.startActivity(intent);
        });

        // Popup Menu for Edit/Delete
        holder.moreButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, holder.moreButton);
            popup.getMenuInflater().inflate(R.menu.menu_farm_options, popup.getMenu());
            
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    showEditDialog(farm, position);
                    return true;
                } else if (id == R.id.action_delete) {
                    deleteFarm(farm, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void deleteFarm(Farm farm, int position) {
        if (!(context instanceof BaseActivity)) return;
        
        String token = ((BaseActivity) context).getAuthToken();
        
        RetrofitClient.getApiService().deleteFarm(token, farm.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    farmList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, farmList.size());
                    Toast.makeText(context, "Farm deleted successfully", Toast.LENGTH_SHORT).show();
                    
                    // If activity has an updateUI method, call it to show empty state if needed
                    try {
                        context.getClass().getMethod("updateUI").invoke(context);
                    } catch (Exception ignored) {}
                    
                } else {
                    Toast.makeText(context, "Failed to delete farm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Timber.e(t, "Delete farm failure");
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(Farm farm, int position) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_edit_farm);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText etName = dialog.findViewById(R.id.edit_farm_name);
        EditText etCrop = dialog.findViewById(R.id.edit_current_crop);
        EditText etCity = dialog.findViewById(R.id.edit_city);
        EditText etDistrict = dialog.findViewById(R.id.edit_district);
        EditText etVillage = dialog.findViewById(R.id.edit_village);
        
        MaterialButton btnSave = dialog.findViewById(R.id.btn_save);
        MaterialButton btnCancel = dialog.findViewById(R.id.btn_cancel);
        ImageView btnClose = dialog.findViewById(R.id.btn_close);

        // Pre-fill data
        etName.setText(farm.name);
        etCrop.setText(farm.crop_type);
        etCity.setText(farm.city);
        etDistrict.setText(farm.district);
        etVillage.setText(farm.village);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String crop = etCrop.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String district = etDistrict.getText().toString().trim();
            String village = etVillage.getText().toString().trim();

            if (name.isEmpty()) {
                etName.setError("Name required");
                return;
            }

            Farm updatedFarm = new Farm(name, village, city, district, crop, farm.latitude, farm.longitude, farm.area);
            updatedFarm.id = farm.id;
            updatedFarm.user_id = farm.user_id;

            if (context instanceof BaseActivity) {
                String token = ((BaseActivity) context).getAuthToken();
                RetrofitClient.getApiService().updateFarm(token, farm.id, updatedFarm).enqueue(new Callback<Farm>() {
                    @Override
                    public void onResponse(Call<Farm> call, Response<Farm> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            farmList.set(position, response.body());
                            notifyItemChanged(position);
                            Toast.makeText(context, "Farm updated successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Farm> call, Throwable t) {
                        Timber.e(t, "Update farm failure");
                        Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
