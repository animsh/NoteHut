package com.animsh.notehut.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.animsh.notehut.R;
import com.animsh.notehut.customlayouts.CustomCheckBox;
import com.animsh.notehut.database.NotesDatabase;
import com.animsh.notehut.entities.Note;
import com.animsh.notehut.entities.TODO;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import static com.animsh.notehut.activities.CreateNoteActivity.selectedNoteColor;
import static com.animsh.notehut.activities.CreateNoteActivity.todoAdapter;
import static com.animsh.notehut.adapters.NoteAdapter.backgroundColor;

public class TODOAdapter extends RecyclerView.Adapter<TODOAdapter.TODOViewHolder> {

    private List<TODO> todoList;
    private Context context;
    private String className;
    private Note note;

    public TODOAdapter(List<TODO> todoList, Context context, String className, Note note) {
        this.todoList = todoList;
        this.context = context;
        this.className = className;
        this.note = note;
    }

    @NonNull
    @Override
    public TODOViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TODOAdapter.TODOViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_todo,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TODOViewHolder holder, int position) {
        holder.todoTitle.setText(todoList.get(position).getTaskName());
        boolean isColored = false;
        if (className.equals("main")) {
            if (backgroundColor.equals("#FDBE3B") ||
                    backgroundColor.equals("#FF4842") ||
                    backgroundColor.equals("#4285F4") ||
                    backgroundColor.equals("#ECECEC")) {
                holder.todoCheckBox.setCheckedColor(Color.parseColor("#000000"));
                holder.todoCheckBox.setUnCheckedColor(Color.parseColor(backgroundColor));
                holder.todoCheckBox.setTickColor(Color.parseColor("#ffffff"));
                holder.todoTitle.setTextColor(Color.parseColor("#000000"));
                holder.todoCheckBox.setFloorColor(Color.parseColor("#000000"));
                holder.todoCheckBox.setFloorUnCheckedColor(Color.parseColor("#000000"));
                isColored = true;
            } else {
                holder.todoCheckBox.setCheckedColor(Color.parseColor("#FDBE3B"));
                holder.todoCheckBox.setUnCheckedColor(Color.parseColor(backgroundColor));
                holder.todoCheckBox.setTickColor(Color.parseColor("#000000"));
                holder.todoCheckBox.setFloorColor(Color.parseColor("#DFDFDF"));
                holder.todoCheckBox.setFloorUnCheckedColor(Color.parseColor("#DFDFDF"));
                holder.todoTitle.setTextColor(Color.parseColor("#ffffff"));
                isColored = false;
            }
        } else if (className.equals("createNote")) {
            if (selectedNoteColor != null) {
                if (selectedNoteColor.equals("#000000") || selectedNoteColor.equals("#1F1F1F")) {
                    holder.todoCheckBox.setTickColor(Color.parseColor("#ffffff"));
                } else {
                    holder.todoCheckBox.setTickColor(Color.parseColor("#000000"));
                }
                holder.todoCheckBox.setFloorColor(Color.parseColor("#DFDFDF"));
                holder.todoCheckBox.setFloorUnCheckedColor(Color.parseColor("#DFDFDF"));
                holder.todoCheckBox.setCheckedColor(Color.parseColor(selectedNoteColor));
            }

        }

        holder.todoCheckBox.setChecked(todoList.get(position).isChecked());
        if (holder.todoCheckBox.isChecked()) {
            if (isColored) {
                holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorBlack));
            } else {
                holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorText2));
            }
            holder.todoTitle.setPaintFlags(holder.todoTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            if (isColored) {
                holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorBlack));
            } else {
                holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorTextColor));
            }
            holder.todoTitle.setPaintFlags(holder.todoTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.layoutTODO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.todoCheckBox.isChecked()) {
                    holder.todoCheckBox.setChecked(false, true);
                } else if (!holder.todoCheckBox.isChecked()) {
                    holder.todoCheckBox.setChecked(true, true);
                }
            }
        });

        boolean finalIsColored = isColored;
        holder.todoCheckBox.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                if (isChecked) {
                    if (finalIsColored) {
                        holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    } else {
                        holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorText2));
                    }
                    holder.todoTitle.setPaintFlags(holder.todoTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    todoList.get(position).setChecked(true);
                } else {
                    if (finalIsColored) {
                        holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorBlack));
                    } else {
                        holder.todoTitle.setTextColor(context.getResources().getColor(R.color.colorTextColor));
                    }
                    holder.todoTitle.setPaintFlags(holder.todoTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    todoList.get(position).setChecked(false);
                }
                if (note != null) {
                    note.setTodoList(todoList);
                    @SuppressLint("StaticFieldLeak")
                    class SaveNoteTask extends AsyncTask<Void, Void, Void> {

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(context).noteDao().insertNote(note);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                        }
                    }

                    new SaveNoteTask().execute();
                }
            }
        });

        holder.layoutTODO.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (className.equals("createNote")){
                    View bottomDialogLayout = ((FragmentActivity) context).getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
                    final BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);

                    TextView deleteChecklistItem;

                    deleteChecklistItem = bottomDialogLayout.findViewById(R.id.text_delete_checklist_item);

                    deleteChecklistItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (todoList.size() == 1) {
                                todoList.remove(0);
                                todoAdapter.notifyDataSetChanged();
                            } else {
                                todoList.remove(position);
                                todoAdapter.notifyItemRemoved(position);
                            }

                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(bottomDialogLayout);
                    dialog.show();
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public class TODOViewHolder extends RecyclerView.ViewHolder {

        CustomCheckBox todoCheckBox;
        TextView todoTitle;
        LinearLayout layoutTODO;

        public TODOViewHolder(@NonNull View itemView) {
            super(itemView);
            todoCheckBox = itemView.findViewById(R.id.todo_checkbox);
            todoTitle = itemView.findViewById(R.id.todo_title);
            layoutTODO = itemView.findViewById(R.id.layout_todo);
        }


    }
}
