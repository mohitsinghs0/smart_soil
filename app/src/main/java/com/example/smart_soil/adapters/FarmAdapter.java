package com.example.smart_soil.adapters;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smart_soil.R;
import com.example.smart_soil.database.FarmEntity;
import com.example.smart_soil.databinding.ListItemFarmBinding;
import com.example.smart_soil.models.WeatherResponse;
import com.example.smart_soil.services.RetrofitClient;
import com.example.smart_soil.services.SupabaseConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class FarmAdapter extends ListAdapter<FarmEntity, FarmAdapter.ViewHolder> {

    private final OnFarmClickListener listener;

    public interface OnFarmClickListener {
        void onFarmClick(FarmEntity farm);
        void onTestClick(FarmEntity farm);
        void onHistoryClick(FarmEntity farm);
        void onEditClick(FarmEntity farm);
        void onDeleteClick(FarmEntity farm);
    }

    public FarmAdapter(OnFarmClickListener listener) {
        super(new DiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ListItemFarmBinding binding = ListItemFarmBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ListItemFarmBinding binding;

        public ViewHolder(ListItemFarmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(FarmEntity farm, OnFarmClickListener listener) {
            binding.farmName.setText(farm.getName());
            binding.farmLocation.setText(farm.getLocation());
            binding.lastTestInfo.setText("Last test: 09/03/2026 • " + farm.getCropType());

            // Fetch live weather if coordinates are available
            if (farm.getLatitude() != 0.0 && farm.getLongitude() != 0.0) {
                fetchWeather(farm.getLatitude(), farm.getLongitude());
            } else {
                // Default/Fallback values if no GPS data
                binding.weatherTemp.setText("--°C");
                binding.weatherHumidity.setText("--%");
                binding.weatherWind.setText("-- km/h");
            }

            binding.getRoot().setOnClickListener(v -> listener.onFarmClick(farm));
            binding.testButton.setOnClickListener(v -> listener.onTestClick(farm));
            binding.historyButton.setOnClickListener(v -> listener.onHistoryClick(farm));
            
            binding.moreButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.menu_farm_options, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_edit) {
                        listener.onEditClick(farm);
                        return true;
                    } else if (id == R.id.action_delete) {
                        listener.onDeleteClick(farm);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        private void fetchWeather(double lat, double lon) {
            RetrofitClient.getWeatherApiService().getCurrentWeather(
                lat, lon, SupabaseConfig.WEATHER_API_KEY, "metric"
            ).enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        WeatherResponse weather = response.body();
                        binding.weatherTemp.setText(String.format(java.util.Locale.getDefault(), "%.1f°C", weather.main.temp));
                        binding.weatherHumidity.setText(String.format(java.util.Locale.getDefault(), "%d%%", weather.main.humidity));
                        binding.weatherWind.setText(String.format(java.util.Locale.getDefault(), "%.1f km/h", weather.wind.speed * 3.6)); // Convert m/s to km/h
                    } else {
                        Timber.e("Weather API error: %d", response.code());
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    Timber.e(t, "Weather API call failed");
                }
            });
        }
    }

    static class DiffCallback extends DiffUtil.ItemCallback<FarmEntity> {
        @Override
        public boolean areItemsTheSame(@NonNull FarmEntity oldItem, @NonNull FarmEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull FarmEntity oldItem, @NonNull FarmEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getLocation().equals(newItem.getLocation()) &&
                   oldItem.getCropType().equals(newItem.getCropType()) &&
                   oldItem.getLatitude() == newItem.getLatitude() &&
                   oldItem.getLongitude() == newItem.getLongitude();
        }
    }
}
