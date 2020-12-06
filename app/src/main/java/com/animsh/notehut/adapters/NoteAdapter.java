package com.animsh.notehut.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.animsh.notehut.R;
import com.animsh.notehut.entities.Note;
import com.animsh.notehut.listeners.NoteListeners;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public static String backgroundColor;
    public static Note note;
    private List<Note> notes;
    private NoteListeners noteListeners;
    private Timer timer;
    private List<Note> notesSources;
    private Context context;

    public NoteAdapter(List<Note> notes, NoteListeners noteListeners, Context context) {
        this.notes = notes;
        this.noteListeners = noteListeners;
        notesSources = notes;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_note,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position), context);
        note = notes.get(position);
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteListeners.onNoteClicked(notes.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void searchNotes(final String searchKeyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    notes = notesSources;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : notesSources) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textSubtitle, textDate, textLabel, textNote;
        LinearLayout layoutNote;
        RoundedImageView imageNote;
        RecyclerView todoRecyclerView;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_title);
            textSubtitle = itemView.findViewById(R.id.text_subtitle);
            textDate = itemView.findViewById(R.id.text_date);
            textNote = itemView.findViewById(R.id.text_note);
            layoutNote = itemView.findViewById(R.id.layout_note);
            imageNote = itemView.findViewById(R.id.image_note);
            todoRecyclerView = itemView.findViewById(R.id.todo_recyclerview);
            textLabel = itemView.findViewById(R.id.text_label);
        }

        void setNote(Note note, Context context) {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setVisibility(View.VISIBLE);
                textSubtitle.setText(note.getSubtitle());
            }
            if (note.getNoteText().trim().isEmpty()) {
                textNote.setVisibility(View.GONE);
            } else {
                if (textSubtitle.getText().toString().isEmpty()) {
                    textNote.setVisibility(View.VISIBLE);
                    textNote.setText(note.getNoteText());
                } else if (textSubtitle.getText().toString().trim().length() < 30) {
                    textNote.setVisibility(View.VISIBLE);
                    textNote.setText(note.getNoteText());
                } else {
                    textNote.setVisibility(View.GONE);
                }
            }
            textDate.setText(note.getDate());

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (note.getColor() != null) {
                backgroundColor = note.getColor();
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
                GradientDrawable drawable = (GradientDrawable) textLabel.getBackground();
                if (note.getColor().equals("#FDBE3B") ||
                        note.getColor().equals("#FF4842") ||
                        note.getColor().equals("#4285F4")) {
                    textTitle.setTextColor(Color.parseColor("#000000"));
                    textSubtitle.setTextColor(Color.parseColor("#000000"));
                    textNote.setTextColor(Color.parseColor("#000000"));
                    textDate.setTextColor(Color.parseColor("#000000"));
                    textLabel.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    drawable.setStroke(1, context.getResources().getColor(R.color.colorBlack)); // set stroke width and stroke color
                } else {
                    textTitle.setTextColor(Color.parseColor("#ffffff"));
                    textSubtitle.setTextColor(Color.parseColor("#ffffff"));
                    textNote.setTextColor(Color.parseColor("#969696"));
                    textDate.setTextColor(Color.parseColor("#969696"));
                    textLabel.setTextColor(context.getResources().getColor(R.color.colorText3));
                    drawable.setStroke(1, context.getResources().getColor(R.color.colorText3)); // set stroke width and stroke color
                }
            } else {
                backgroundColor = "#1F1F1F";
                gradientDrawable.setColor(Color.parseColor("#1F1F1F"));
            }

            if (note.getImagePath() != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }

            if (note.getTodoList() != null) {
                TODOAdapter todoAdapter = new TODOAdapter(note.getTodoList(), context, "main", note);
                todoRecyclerView.setHasFixedSize(true);
                todoRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                todoRecyclerView.setAdapter(todoAdapter);
                todoRecyclerView.setVisibility(View.VISIBLE);
                todoAdapter.notifyDataSetChanged();
            }

            if (note.getTodoList() != null && !note.getNoteText().equals("")) {
                textLabel.setText("Notes");
            } else if (note.getTodoList() != null && note.getNoteText().equals("")) {
                textLabel.setText("Task List");
            } else if (note.getTodoList() == null && !note.getNoteText().equals("")) {
                textLabel.setText("Notes");
            }
        }
    }
}
