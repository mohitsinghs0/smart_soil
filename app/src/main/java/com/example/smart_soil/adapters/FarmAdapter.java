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
            
            // Dummy data for weather to match original UI look
            binding.weatherTemp.setText("27.1°C");
            binding.weatherHumidity.setText("45%");
            binding.weatherWind.setText("9.4 km/h");
            binding.lastTestInfo.setText("Last test: 09/03/2026 • " + farm.getCropType());

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
                   oldItem.getServerId() != null && oldItem.getServerId().equals(newItem.getServerId());
        }
    }
}
