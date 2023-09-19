package com.asamman.kidhasphonealertparent;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class StringAdapter extends ArrayAdapter<String> {
    private ArrayList<String> stringList;
    private SharedPreferences sharedPreferences;

    public StringAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        stringList = objects;
        sharedPreferences = context.getSharedPreferences("com.asamman.kidhasphonealertparent", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_string, parent, false);
        }

        TextView textView = view.findViewById(R.id.stringTextView);
        ImageView deleteButton = view.findViewById(R.id.deleteButton);

        textView.setText(stringList.get(position));

        // Handle delete button click
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position >= 0 && position < stringList.size()) {
                    // Remove the item from the ArrayList
                    String removedString = stringList.remove(position);

                    // Remove the corresponding value from SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(removedString);
                    editor.apply();

                    notifyDataSetChanged();
                }
            }

        });

        return view;
    }

    public void add(String string) {
        stringList.add(string);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(string, 0);
        editor.apply();
    }
}

