package com.libre.irremote.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.libre.irremote.R;
import com.libre.irremote.models.ModelAlexaLocalSupported;

import java.util.List;

public class ShowAlexaLanguageAdapter extends RecyclerView.Adapter<ShowAlexaLanguageAdapter.ShowAlexaLanguageHolder> {

    private Context context;
    private List<ModelAlexaLocalSupported> modelAlexaLocalSupporteds;
    private String prevSelectedLocale;

    private int lastSelectedPosition = -1;


    public ShowAlexaLanguageAdapter(Context context, List<ModelAlexaLocalSupported> modelAlexaLocalSupportedList, String prevSelectedLocale) {
        this.context = context;
        this.modelAlexaLocalSupporteds = modelAlexaLocalSupportedList;
        this.prevSelectedLocale = prevSelectedLocale;
    }

    @NonNull
    @Override
    public ShowAlexaLanguageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        return new ShowAlexaLanguageHolder(LayoutInflater.from(context).inflate(R.layout.adapter_show_language, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ShowAlexaLanguageHolder holder, int position) {

        setRadioButtonText(modelAlexaLocalSupporteds.get(position).Language, modelAlexaLocalSupporteds.get(position).Country, holder);

        if (lastSelectedPosition == -1) {
            //only at first
            if (modelAlexaLocalSupporteds.get(position).Locale.equals(prevSelectedLocale)) {
                resetLanguagesValuesToFalse(modelAlexaLocalSupporteds);
                modelAlexaLocalSupporteds.get(position).isChecked = true;
                lastSelectedPosition = position;
            }
        }
        //since only one radio button is allowed to be selected,
        // this condition un-checks previous selections

        holder.rbAlexaLang.setChecked(lastSelectedPosition == position);

    }

    public void setRadioButtonText(String language, String country, ShowAlexaLanguageHolder holder) {
        holder.rbAlexaLang.setText(language + "(" + country + ")");

    }

    @Override
    public int getItemCount() {
        return modelAlexaLocalSupporteds.size();
    }

    class ShowAlexaLanguageHolder extends RecyclerView.ViewHolder {
        RadioButton rbAlexaLang;

        public ShowAlexaLanguageHolder(@NonNull View itemView) {
            super(itemView);
            rbAlexaLang = itemView.findViewById(R.id.rbAlexaLang);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetLanguagesValuesToFalse(modelAlexaLocalSupporteds);
                    lastSelectedPosition = getAdapterPosition();
                    modelAlexaLocalSupporteds.get(lastSelectedPosition).setChecked(true);
                    notifyDataSetChanged();

                }
            };

            itemView.setOnClickListener(clickListener);
            rbAlexaLang.setOnClickListener(clickListener);
        }
    }

    public void resetLanguagesValuesToFalse(List<ModelAlexaLocalSupported> modelAlexaLocalSupportedList) {
        for (ModelAlexaLocalSupported modelAlexaLocalSupported : modelAlexaLocalSupportedList) {
            modelAlexaLocalSupported.setChecked(false);
        }
    }
}
