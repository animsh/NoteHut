package com.animsh.nothut.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.animsh.nothut.R;
import com.animsh.nothut.database.NotesDatabase;
import com.animsh.nothut.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDataTime;
    private View subtitleIndicator;
    private String selectedNoteColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        inputNoteTitle = findViewById(R.id.input_note_title);
        inputNoteSubtitle = findViewById(R.id.input_note_subtitle);
        inputNoteText = findViewById(R.id.input_note);
        textDataTime = findViewById(R.id.text_data_and_time);
        subtitleIndicator = findViewById(R.id.view_subtitle_indicator);

        textDataTime.setText(
                new SimpleDateFormat("EEE, MMM d, ''yy 'at' h:mm a", Locale.getDefault())
                        .format(new Date())
        );

        ImageView imageSave = findViewById(R.id.image_save);
        imageSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
            }
        });
        selectedNoteColor = "#1F1F1F";
        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } /*else if (inputNoteSubtitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note ub title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }*/ else if (inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDateTime(textDataTime.getText().toString());
        note.setColor(selectedNoteColor);
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }

        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.text_miscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.image_color_1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.image_color_2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.image_color_3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.image_color_4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.image_color_5);

        layoutMiscellaneous.findViewById(R.id.view_color_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#1F1F1F";
                imageColor1.setImageResource(R.drawable.ic_round_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FDBE3B";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_round_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_round_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#4285F4";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_round_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_round_done);
                setSubtitleIndicatorColor();
            }
        });

    }

    private void setSubtitleIndicatorColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }
}