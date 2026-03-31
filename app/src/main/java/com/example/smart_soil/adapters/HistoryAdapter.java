package com.example.smart_soil.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.smart_soil.R;
import com.example.smart_soil.activities.AIChatActivity;
import com.example.smart_soil.models.SoilTest;
import com.example.smart_soil.services.RetrofitClient;

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

        holder.idLabel.setText("Test #" + test.id); 
        holder.testDate.setText(test.test_date != null ? test.test_date : "N/A");
        
        String crops = test.recommended_crops != null ? test.recommended_crops : "N/A";
        String values = String.format(Locale.getDefault(),
                "SOC: %.2f%% • N: %.1f • pH: %.1f",
                test.soc, test.nitrogen, test.ph);
        holder.testValues.setText(values);
        
        if (holder.cropsLabel != null) {
            holder.cropsLabel.setText(crops);
        }

        if (test.overallScore != null && holder.scoreLabel != null) {
            holder.scoreLabel.setText(test.overallScore + "%");
            holder.scoreLabel.setVisibility(View.VISIBLE);
            
            int color;
            if (test.overallScore > 80) color = R.color.brand_green;
            else if (test.overallScore > 50) color = R.color.status_medium;
            else color = R.color.status_low;
            
            holder.scoreLabel.setTextColor(ContextCompat.getColor(context, color));
        } else if (holder.scoreLabel != null) {
            holder.scoreLabel.setVisibility(View.GONE);
        }

        if (test.image_path != null && !test.image_path.isEmpty()) {
            String imageUrl = RetrofitClient.getBaseUrl() + "uploads/" + test.image_path;
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_sprout)
                    .into(holder.soilImage);
        } else {
            holder.soilImage.setImageResource(R.drawable.ic_sprout);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AIChatActivity.class);
            intent.putExtra("soil_test_id", test.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return testList.size();
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView idLabel, testDate, testValues, cropsLabel, scoreLabel;
        ImageView soilImage;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            idLabel = itemView.findViewById(R.id.farm_name);
            testDate = itemView.findViewById(R.id.test_date);
            testValues = itemView.findViewById(R.id.test_values);
            cropsLabel = itemView.findViewById(R.id.crops_label);
            scoreLabel = itemView.findViewById(R.id.score_label);
            soilImage = itemView.findViewById(R.id.soil_image);
        }
    }
}
