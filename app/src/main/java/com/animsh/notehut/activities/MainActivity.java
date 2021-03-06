package com.animsh.notehut.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.animsh.materialsearchbar.MaterialSearchBar;
import com.animsh.notehut.R;
import com.animsh.notehut.adapters.NoteAdapter;
import com.animsh.notehut.database.NotesDatabase;
import com.animsh.notehut.entities.Note;
import com.animsh.notehut.listeners.NoteListeners;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteListeners {

    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTES = 3;
    public static final int REQUEST_CODE_SELECT_IMAGE = 4;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 5;
    private static final String TAG = "MAIN_ACTIVITY";
    private static final String MYPREFERENCE = "MY_PREF";
    public static NoteAdapter noteAdapter;
    public AlertDialog dialogAddUrl;
    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private int noteClickedPosition = -1;
    private AlertDialog dialogAddChecklistItem;
    private MaterialSearchBar materialSearchBar;
    private LottieAnimationView listGrid;
    private boolean isGrid = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        materialSearchBar = findViewById(R.id.searchBar);
        listGrid = findViewById(R.id.animationView);
        materialSearchBar.getPlaceHolderView().setTextAppearance(this, R.style.MyCustomFontTextAppearance);

        ImageView imageAddNoteMain = findViewById(R.id.image_add_note_main);
        imageAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        });

        notesRecyclerView = findViewById(R.id.notes_recyclerview);
        isGrid = restorePref();
        listGrid.setMinAndMaxFrame(75, 150);
        if (isGrid) {
            listGrid.setSpeed(-5);
            listGrid.playAnimation();
            notesRecyclerView.setLayoutManager(
                    new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            );
        } else {
            listGrid.setSpeed(5);
            listGrid.playAnimation();
            notesRecyclerView.setLayoutManager(
                    new LinearLayoutManager(this)
            );
        }
        notesRecyclerView.setNestedScrollingEnabled(true);
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this, this, isGrid);
        notesRecyclerView.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();

        getNotes(REQUEST_CODE_SHOW_NOTES, false);
        Log.d(TAG, "onCreate: " + listGrid.getMaxFrame());
        listGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isGrid) {
                    isGrid = false;
                    listGrid.setSpeed(5);
                    listGrid.playAnimation();
                    notesRecyclerView.setLayoutManager(
                            new LinearLayoutManager(MainActivity.this)
                    );
                } else {
                    isGrid = true;
                    listGrid.setSpeed(-5);
                    listGrid.playAnimation();
                    notesRecyclerView.setLayoutManager(
                            new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    );
                }
                noteAdapter = new NoteAdapter(noteList, MainActivity.this, MainActivity.this, isGrid);
                savePref(isGrid);
                notesRecyclerView.setAdapter(noteAdapter);
                noteAdapter.notifyDataSetChanged();
            }
        });

        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (noteList.size() != 0) {
                    noteAdapter.searchNotes(editable.toString().trim());
                }
            }
        });

        findViewById(R.id.image_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        });

        findViewById(R.id.image_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddURLDialog();
            }
        });

        findViewById(R.id.image_add_checklist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddChecklistItemDialog();
            }
        });
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

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
        intent.putExtra("isViewOrUpdate", true);
        intent.putExtra("note", note);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_NOTE);
    }

    private void getNotes(final int requestCode, final boolean isNoteDeleted) {
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase
                        .getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                Log.d("MY_NOTES: ", notes.toString());
                if (requestCode == REQUEST_CODE_SHOW_NOTES) {
                    noteList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                } else if (requestCode == REQUEST_CODE_ADD_NOTE) {
                    noteList.add(0, notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    notesRecyclerView.smoothScrollToPosition(0);
                } else if (requestCode == REQUEST_CODE_UPDATE_NOTE) {
                    noteList.remove(noteClickedPosition);
                    if (isNoteDeleted) {
                        noteAdapter.notifyItemRemoved(noteClickedPosition);
                    } else {
                        noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                        noteAdapter.notifyItemChanged(noteClickedPosition);
                    }
                }
            }
        }

        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes(REQUEST_CODE_ADD_NOTE, false);
        } else if (requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode == RESULT_OK) {
            if (data != null) {
                getNotes(REQUEST_CODE_UPDATE_NOTE, data.getBooleanExtra("isNoteDeleted", false));
            }
        } else if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        String selectedImagePath = getPathFromUri(selectedImageUri);
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("QuickActionType", "image");
                        intent.putExtra("imagePath", selectedImagePath);
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    } catch (Exception ex) {
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showAddChecklistItemDialog() {
        if (dialogAddChecklistItem == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                        Toast.makeText(MainActivity.this, "Enter item name", Toast.LENGTH_SHORT).show();
                    } else {
                        dialogAddChecklistItem.dismiss();
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("QuickActionType", "TODO");
                        intent.putExtra("TODO", inputTODO.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
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


    private void showAddURLDialog() {
        if (dialogAddUrl == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            View view = LayoutInflater.from(this).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById(R.id.layout_add_url_container)
            );
            builder.setView(view);

            dialogAddUrl = builder.create();
            if (dialogAddUrl.getWindow() != null) {
                dialogAddUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText inputURl = view.findViewById(R.id.input_url);
            inputURl.requestFocus();

            view.findViewById(R.id.text_add_url).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (inputURl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(MainActivity.this, "Enter URl", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(inputURl.getText().toString()).matches()) {
                        Toast.makeText(MainActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        dialogAddUrl.dismiss();
                        Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                        intent.putExtra("isFromQuickAction", true);
                        intent.putExtra("QuickActionType", "URL");
                        intent.putExtra("URL", inputURl.getText().toString());
                        startActivityForResult(intent, REQUEST_CODE_ADD_NOTE);
                    }
                }
            });

            view.findViewById(R.id.text_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogAddUrl.dismiss();
                }
            });
        }
        dialogAddUrl.show();
    }

    public void savePref(boolean isGrid) {
        SharedPreferences sharedpreferences = getSharedPreferences(MYPREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isGrid", isGrid);
        editor.apply();
    }

    public boolean restorePref() {
        SharedPreferences sharedpreferences = getSharedPreferences(MYPREFERENCE, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("isGrid", true);
    }

}