package com.example.smart_soil.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smart_soil.R;
import com.example.smart_soil.models.SoilTest;

import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final Context context;
    private final List<SoilTest> testList;

    public HistoryAdapter(Context context, List<SoilTest> testList) {
        this.context = context;
        this.testList = testList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_test_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SoilTest test = testList.get(position);

        // For now, using placeholder farm name. In a real app, you'd look this up from the farm_id.
        holder.farmName.setText("Rice"); 
        holder.testDate.setText(test.test_date);
        
        String values = String.format(Locale.getDefault(),
                "SOC: %.2f%% • N: %.1f • pH: %.1f • %s",
                test.soc, test.nitrogen, test.ph, test.recommended_crops.get(0));
        holder.testValues.setText(values);

        // TODO: Load image using Glide if image_path is available
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView farmName, testDate, testValues;
        // ImageView soilImage;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            farmName = itemView.findViewById(R.id.farm_name);
            testDate = itemView.findViewById(R.id.test_date);
            testValues = itemView.findViewById(R.id.test_values);
            // soilImage = itemView.findViewById(R.id.soil_image);
        }
    }
}
