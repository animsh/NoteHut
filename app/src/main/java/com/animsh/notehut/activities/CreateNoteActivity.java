package com.animsh.notehut.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUriExposedException;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.animsh.notehut.BuildConfig;
import com.animsh.notehut.R;
import com.animsh.notehut.adapters.TODOAdapter;
import com.animsh.notehut.database.NotesDatabase;
import com.animsh.notehut.entities.Note;
import com.animsh.notehut.entities.TODO;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private static final String TAG = "CREATE_NOTE";
    public static String selectedNoteColor;
    public static TODOAdapter todoAdapter;
    LinearLayout layoutMiscellaneous;
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private EditText inputNoteTitle, inputNoteSubtitle, inputNoteText;
    private TextView textDate, textTime;
    private View subtitleIndicator;
    private String selectedImagePath;
    private ImageView imageNote;
    private TextView textWebUrl;
    private LinearLayout layoutWebURL;
    private AlertDialog dialogAlertURL;
    private AlertDialog dialogDeleteNote;
    private AlertDialog dialogAddChecklistItem;
    private AlertDialog dialogDiscardChanges;
    private RecyclerView todoRecyclerView;
    private Note alreadyAvailableNote;
    private List<TODO> todoList = new ArrayList<>();
    private boolean isViewOrUpdate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        layoutMiscellaneous = findViewById(R.id.layout_miscellaneous);
        bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);

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
        textDate = findViewById(R.id.text_date);
        textTime = findViewById(R.id.text_time);
        subtitleIndicator = findViewById(R.id.view_subtitle_indicator);
        imageNote = findViewById(R.id.image_note);
        textWebUrl = findViewById(R.id.text_web_url);
        layoutWebURL = findViewById(R.id.layout_web_url);
        todoRecyclerView = (RecyclerView) findViewById(R.id.todo_recyclerview);


        textDate.setText(
                new SimpleDateFormat("EEE, MMM d, ''yy", Locale.getDefault())
                        .format(new Date())
        );
        textTime.setText(
                new SimpleDateFormat(" 'at' h:mm a", Locale.getDefault())
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
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            isViewOrUpdate = true;
            alreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.image_remove_web_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textWebUrl.setText(null);
                layoutWebURL.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.image_remove_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageNote.setImageBitmap(null);
                imageNote.setVisibility(View.GONE);
                findViewById(R.id.image_remove_image).setVisibility(View.GONE);
                selectedImagePath = "";
            }
        });

        if (getIntent().getBooleanExtra("isFromQuickAction", false)) {
            String type = getIntent().getStringExtra("QuickActionType");
            if (type != null) {
                if (type.equals("image")) {
                    selectedImagePath = getIntent().getStringExtra("imagePath");
                    imageNote.setImageBitmap(BitmapFactory.decodeFile(selectedImagePath));
                    imageNote.setVisibility(View.VISIBLE);
                    findViewById(R.id.image_remove_image).setVisibility(View.VISIBLE);
                } else if (type.equals("URL")) {
                    textWebUrl.setText(getIntent().getStringExtra("URL"));
                    layoutWebURL.setVisibility(View.VISIBLE);
                } else if (type.equals("TODO")) {
                    todoList.add(new TODO(false, getIntent().getStringExtra("TODO")));
                    todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                    todoRecyclerView.setHasFixedSize(true);
                    todoRecyclerView.setLayoutManager(new LinearLayoutManager(CreateNoteActivity.this));
                    todoRecyclerView.setAdapter(todoAdapter);
                    todoAdapter.notifyDataSetChanged();
                }
            }
        }

        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    private void setViewOrUpdateNote() {
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubtitle.setText(alreadyAvailableNote.getSubtitle());
        inputNoteText.setText(alreadyAvailableNote.getNoteText());
        textDate.setText(alreadyAvailableNote.getDate());
        textTime.setText(alreadyAvailableNote.getTime());
        if (alreadyAvailableNote.getImagePath() != null && !alreadyAvailableNote.getImagePath().trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(alreadyAvailableNote.getImagePath()));
            imageNote.setVisibility(View.VISIBLE);
            findViewById(R.id.image_remove_image).setVisibility(View.VISIBLE);
            selectedImagePath = alreadyAvailableNote.getImagePath();
        }

        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().trim().isEmpty()) {
            textWebUrl.setText(alreadyAvailableNote.getWebLink());
            layoutWebURL.setVisibility(View.VISIBLE);
        }

        if (alreadyAvailableNote.getTodoList() != null) {
            todoList = alreadyAvailableNote.getTodoList();
            todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
            todoRecyclerView.setHasFixedSize(true);
            todoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            todoRecyclerView.setAdapter(todoAdapter);
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } /*else if (inputNoteSubtitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note ub title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText(CreateNoteActivity.this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } */

        final Note note = new Note();
        note.setTitle(inputNoteTitle.getText().toString());
        note.setSubtitle(inputNoteSubtitle.getText().toString());
        note.setNoteText(inputNoteText.getText().toString());
        note.setDate(textDate.getText().toString());
        note.setTime(textTime.getText().toString());
        note.setColor(selectedNoteColor);
        note.setImagePath(selectedImagePath);
        if (todoList != null) {
            note.setTodoList(todoList);
        } else {
            note.setTodoList(null);
        }

        if (layoutWebURL.getVisibility() == View.VISIBLE) {
            note.setWebLink(textWebUrl.getText().toString());
        }

        if (alreadyAvailableNote != null) {
            note.setId(alreadyAvailableNote.getId());
        }
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
                if (todoAdapter != null) {
                    todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                    todoRecyclerView.setAdapter(todoAdapter);
                    todoAdapter.notifyDataSetChanged();
                }
                setSubtitleIndicatorColor();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                if (todoAdapter != null) {
                    todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                    todoRecyclerView.setAdapter(todoAdapter);
                    todoAdapter.notifyDataSetChanged();
                }
                setSubtitleIndicatorColor();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                if (todoAdapter != null) {
                    todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                    todoRecyclerView.setAdapter(todoAdapter);
                    todoAdapter.notifyDataSetChanged();
                }
                setSubtitleIndicatorColor();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                if (todoAdapter != null) {
                    todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                    todoRecyclerView.setAdapter(todoAdapter);
                    todoAdapter.notifyDataSetChanged();
                }
                setSubtitleIndicatorColor();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
                if (todoAdapter != null) {
                    todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                    todoRecyclerView.setAdapter(todoAdapter);
                    todoAdapter.notifyDataSetChanged();
                }
                setSubtitleIndicatorColor();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        if (alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()) {
            switch (alreadyAvailableNote.getColor()) {
                case "#FDBE3B":
                    layoutMiscellaneous.findViewById(R.id.view_color_2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.view_color_3).performClick();
                    break;
                case "#4285F4":
                    layoutMiscellaneous.findViewById(R.id.view_color_4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.view_color_5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.layout_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.layout_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddURLDialog();
            }
        });

        layoutMiscellaneous.findViewById(R.id.layout_add_checklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddChecklistItemDialog();
            }
        });


        if (alreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById(R.id.layout_delete_note).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layout_share_note).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.layout_share_note).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    Intent share = new Intent(Intent.ACTION_SEND);
                    File file = new File(alreadyAvailableNote.getImagePath());
                    StringBuilder tasks = new StringBuilder();
                    if (alreadyAvailableNote.getTodoList() != null) {
                        List<TODO> list = alreadyAvailableNote.getTodoList();
                        for (int i = 0; i < alreadyAvailableNote.getTodoList().size(); i++) {
                            tasks.append(i + 1).append(". ").append(list.get(i).getTaskName()).append("\n");
                        }
                        Log.d("TASKLIST", "onClick: " + tasks);
                    }
                    Uri imageUri = FileProvider.getUriForFile(CreateNoteActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                    if (!alreadyAvailableNote.getImagePath().equals("")) {
                        share.putExtra(Intent.EXTRA_TEXT, alreadyAvailableNote.getTitle() + "\n" + alreadyAvailableNote.getSubtitle() + "\n" + alreadyAvailableNote.getNoteText() + "\n " + tasks);
                        share.putExtra(Intent.EXTRA_STREAM, imageUri);
                        share.setType("image/*");
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    } else {
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, alreadyAvailableNote.getTitle() + "\n" + alreadyAvailableNote.getSubtitle() + "\n" + alreadyAvailableNote.getNoteText() + "\n " + tasks);
                    }

                    try {
                        startActivity(Intent.createChooser(share, "Share Note"));
                    } catch (FileUriExposedException e) {
                        Log.e("ERROR : ", e.getMessage());
                    }
                }
            });
            layoutMiscellaneous.findViewById(R.id.layout_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }

    private void showDiscardChangesDialog() {
        if (dialogDiscardChanges == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View root = LayoutInflater.from(this).inflate(
                    R.layout.layout_discard_changes,
                    (ViewGroup) findViewById(R.id.layout_discard_changes)
            );
            builder.setView(root);

            dialogDiscardChanges = builder.create();
            if (dialogDiscardChanges.getWindow() != null) {
                dialogDiscardChanges.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            root.findViewById(R.id.text_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveNote();
                    dialogDiscardChanges.dismiss();
                }
            });

            root.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogDiscardChanges.dismiss();
                    finish();
                }
            });

        }
        dialogDiscardChanges.show();
    }


    private void showAddChecklistItemDialog() {
        if (dialogAddChecklistItem == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View root = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_checklist_item,
                    (ViewGroup) findViewById(R.id.layout_add_checklist_item_container)
            );
            builder.setView(root);

            dialogAddChecklistItem = builder.create();
            if (dialogAddChecklistItem.getWindow() != null) {
                dialogAddChecklistItem.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputTODO = root.findViewById(R.id.input_todo);
            inputTODO.requestFocus();

            root.findViewById(R.id.text_add_todo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputTODO.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter item name", Toast.LENGTH_SHORT).show();
                    } else {
                        todoList.add(new TODO(false, inputTODO.getText().toString()));
                        todoAdapter = new TODOAdapter(todoList, CreateNoteActivity.this, "createNote", alreadyAvailableNote, true);
                        todoRecyclerView.setHasFixedSize(true);
                        todoRecyclerView.setLayoutManager(new LinearLayoutManager(CreateNoteActivity.this));
                        todoRecyclerView.setAdapter(todoAdapter);
                        todoAdapter.notifyDataSetChanged();
                        dialogAddChecklistItem.dismiss();
                        inputTODO.setText("");
                    }
                }
            });

            root.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAddChecklistItem.dismiss();
                }
            });
        }
        dialogAddChecklistItem.show();
    }


    private void showDeleteNoteDialog() {
        if (dialogDeleteNote == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById(R.id.layout_delete_note_container)
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
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
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

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        findViewById(R.id.image_remove_image).setVisibility(View.VISIBLE);

                        selectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog() {
        if (dialogAlertURL == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layout_add_url_container)
            );
            builder.setView(view);

            dialogAlertURL = builder.create();
            if (dialogAlertURL.getWindow() != null) {
                dialogAlertURL.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURl = view.findViewById(R.id.input_url);
            inputURl.requestFocus();

            view.findViewById(R.id.text_add_url).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputURl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URl", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURl.getText().toString()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        textWebUrl.setText(inputURl.getText().toString());
                        layoutWebURL.setVisibility(View.VISIBLE);
                        dialogAlertURL.dismiss();
                    }
                }
            });

            view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAlertURL.dismiss();
                }
            });
        }
        dialogAlertURL.show();
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (isViewOrUpdate) {
            if (alreadyAvailableNote != null) {
                if (alreadyAvailableNote.getTitle().trim().equals(inputNoteTitle.getText().toString().trim())) {
                    if (alreadyAvailableNote.getSubtitle().trim().equals(inputNoteSubtitle.getText().toString().trim())) {
                        if (alreadyAvailableNote.getNoteText().trim().equals(inputNoteText.getText().toString().trim())) {
                            if (alreadyAvailableNote.getColor().trim().equals(selectedNoteColor)) {
                                if (alreadyAvailableNote.getImagePath().trim().equals(selectedImagePath.trim())) {
                                    if (layoutWebURL.getVisibility() == View.VISIBLE) {
                                        if (alreadyAvailableNote.getWebLink() == null && !textWebUrl.getText().toString().isEmpty()) {
                                            showDiscardChangesDialog();
                                        } else if (alreadyAvailableNote.getWebLink() != null && alreadyAvailableNote.getWebLink().trim().equals(textWebUrl.getText().toString())) {
                                            finish();
                                            return;
                                        } else {
                                            showDiscardChangesDialog();
                                        }
                                    } else {
                                        if (alreadyAvailableNote.getWebLink() != null && !alreadyAvailableNote.getWebLink().isEmpty()) {
                                            showDiscardChangesDialog();
                                        } else {
                                            finish();
                                            return;
                                        }
                                    }
                                } else {
                                    showDiscardChangesDialog();
                                }
                            } else {
                                showDiscardChangesDialog();
                            }
                        } else {
                            showDiscardChangesDialog();
                        }
                    } else {
                        showDiscardChangesDialog();
                    }
                } else {
                    showDiscardChangesDialog();
                }
            }
            return;
        }
        super.onBackPressed();
    }
}