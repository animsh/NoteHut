package com.animsh.notehut.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.FileUriExposedException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.animsh.notehut.BuildConfig;
import com.animsh.notehut.R;
import com.animsh.notehut.database.NotesDatabase;
import com.animsh.notehut.entities.Note;
import com.animsh.notehut.entities.TODO;
import com.animsh.notehut.listeners.NoteListeners;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.animsh.notehut.activities.MainActivity.noteAdapter;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public static String backgroundColor;
    private final NoteListeners noteListeners;
    private final List<Note> notesSources;
    private final Context context;
    private final boolean isGrid;
    private Note note;
    private List<Note> notes;
    private Timer timer;
    private AlertDialog dialogDeleteNote;

    public NoteAdapter(List<Note> notes, NoteListeners noteListeners, Context context, boolean isGrid) {
        this.notes = notes;
        this.noteListeners = noteListeners;
        notesSources = notes;
        this.context = context;
        this.isGrid = isGrid;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NoteViewHolder view;
        if (isGrid) {
            view = new NoteViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_container_note,
                            parent,
                            false
                    )
            );
        } else {
            view = new NoteViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.item_container_note_list,
                            parent,
                            false
                    )
            );
        }
        return view;
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(notes.get(position), context, isGrid);
        note = notes.get(position);
        holder.layoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteListeners.onNoteClicked(notes.get(position), position);
            }
        });

        holder.layoutNote.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View bottomDialogLayout = ((FragmentActivity) context).getLayoutInflater().inflate(R.layout.bottom_sheet_layout_note, null);
                final BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);

                TextView title, deleteNoteBtn, shareNoteBtn;

                title = bottomDialogLayout.findViewById(R.id.title);
                deleteNoteBtn = bottomDialogLayout.findViewById(R.id.btn_delete);
                shareNoteBtn = bottomDialogLayout.findViewById(R.id.btn_share);

                title.setSelected(true);
                title.setText(notes.get(position).getTitle());

                deleteNoteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDeleteNoteDialog(position);
                        dialog.dismiss();
                    }
                });

                shareNoteBtn.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onClick(View view) {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        File file = new File(notes.get(position).getImagePath());
                        StringBuilder tasks = new StringBuilder();
                        if (notes.get(position).getTodoList() != null) {
                            List<TODO> list = notes.get(position).getTodoList();
                            for (int i = 0; i < notes.get(position).getTodoList().size(); i++) {
                                tasks.append(i + 1).append(". ").append(list.get(i).getTaskName()).append("\n");
                            }
                            Log.d("TASKLIST", "onClick: " + tasks);
                        }
                        Uri imageUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                        if (!notes.get(position).getImagePath().equals("")) {
                            share.putExtra(Intent.EXTRA_TEXT, notes.get(position).getTitle() + "\n" + notes.get(position).getSubtitle() + "\n" + notes.get(position).getNoteText() + "\n " + tasks);
                            share.putExtra(Intent.EXTRA_STREAM, imageUri);
                            share.setType("image/*");
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } else {
                            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            share.setType("text/plain");
                            share.putExtra(Intent.EXTRA_TEXT, notes.get(position).getTitle() + "\n" + notes.get(position).getSubtitle() + "\n" + notes.get(position).getNoteText() + "\n " + tasks);
                        }

                        try {
                            context.startActivity(Intent.createChooser(share, "Share " + title.getText()));
                        } catch (FileUriExposedException e) {
                            Log.e("ERROR : ", e.getMessage());
                        }
                        dialog.dismiss();
                    }
                });
                dialog.setContentView(bottomDialogLayout);
                dialog.show();
                return true;
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

    private void showDeleteNoteDialog(int position) {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) ((Activity) context).findViewById(R.id.layout_delete_note_container)
            );
            builder.setView(view);
            dialogDeleteNote = builder.create();
            if (dialogDeleteNote.getWindow() != null) {
                dialogDeleteNote.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.text_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(context).noteDao()
                                    .deleteNote(notes.get(position));
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            notes.remove(position);
                            noteAdapter.notifyItemRemoved(position);
                            dialogDeleteNote.dismiss();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDeleteNote.dismiss();
                }
            });
        }

        dialogDeleteNote.show();
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
        }, 0);
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

        void setNote(Note note, Context context, boolean isGrid) {
            textTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty() || note.getSubtitle().trim().equals("")) {
                textSubtitle.setVisibility(View.GONE);
            } else {
                textSubtitle.setVisibility(View.VISIBLE);
                textSubtitle.setText(note.getSubtitle());
            }
            if (note.getNoteText().trim().isEmpty() || note.getNoteText().trim().equals("")) {
                textNote.setVisibility(View.GONE);
            } else {
                if (note.getSubtitle().trim().isEmpty() || note.getSubtitle().trim().equals("")) {
                    textNote.setVisibility(View.VISIBLE);
                    textNote.setText(note.getNoteText());
                } else if (note.getSubtitle().trim().length() < 30) {
                    textNote.setVisibility(View.VISIBLE);
                    textNote.setText(note.getNoteText());
                } else {
                    textNote.setVisibility(View.GONE);
                }
            }
            textDate.setText(note.getDate());
            GradientDrawable drawable = (GradientDrawable) textLabel.getBackground();

            GradientDrawable gradientDrawable = (GradientDrawable) layoutNote.getBackground();
            if (isGrid) {
                if (note.getColor() != null) {
                    gradientDrawable.setColor(Color.parseColor(note.getColor()));
                    backgroundColor = note.getColor();
                    if (note.getColor().equals("#FDBE3B") ||
                            note.getColor().equals("#FF4842") ||
                            note.getColor().equals("#4285F4") ||
                            note.getColor().equals("#ECECEC")) {
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
            } else {
                gradientDrawable.setColor(Color.parseColor("#1F1F1F"));
                View view = itemView.findViewById(R.id.view_color);
                GradientDrawable colorDrawable = (GradientDrawable) view.getBackground();
                if (!note.getColor().trim().equals("#1F1F1F")) {
                    backgroundColor = note.getColor();
                    colorDrawable.setColor(Color.parseColor(note.getColor()));
                } else {
                    backgroundColor = "#1F1F1F";
                    colorDrawable.setColor(Color.parseColor("#2A2A2A"));
                }
                textTitle.setTextColor(Color.parseColor("#ffffff"));
                textSubtitle.setTextColor(Color.parseColor("#ffffff"));
                textNote.setTextColor(Color.parseColor("#969696"));
                textDate.setTextColor(Color.parseColor("#969696"));
                textLabel.setTextColor(context.getResources().getColor(R.color.colorText3));
                drawable.setStroke(1, context.getResources().getColor(R.color.colorText3)); // set stroke width and stroke color
            }

            if (note.getImagePath() != null) {
                imageNote.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            } else {
                imageNote.setVisibility(View.GONE);
            }


            if (note.getTodoList() != null) {
                TODOAdapter todoAdapter = new TODOAdapter(note.getTodoList(), context, "main", note, isGrid);
                todoRecyclerView.setHasFixedSize(true);
                todoRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                todoRecyclerView.setAdapter(todoAdapter);
                todoRecyclerView.setVisibility(View.VISIBLE);
                todoAdapter.notifyDataSetChanged();
            }

            if (note.getTodoList() != null && !note.getNoteText().equals("")) {
                textLabel.setText(R.string.notes);
            } else if (note.getTodoList() != null && note.getNoteText().equals("")) {
                textLabel.setText(R.string.task_list);
            } else if (note.getTodoList() == null && !note.getNoteText().equals("")) {
                textLabel.setText(R.string.notes);
            }
        }
    }
}
