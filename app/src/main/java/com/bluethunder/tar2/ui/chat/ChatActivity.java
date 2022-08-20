package com.bluethunder.tar2.ui.chat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluethunder.tar2.R;
import com.bluethunder.tar2.SessionConstants;
import com.bluethunder.tar2.ui.chat.adapter.ChatAdapter;
import com.bluethunder.tar2.ui.chat.model.ChatHead;
import com.bluethunder.tar2.ui.chat.model.Message;
import com.bluethunder.tar2.utils.Enums;
import com.bluethunder.tar2.views.AudioRecordView;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Date;

import id.zelory.compressor.Compressor;

public class ChatActivity extends AppCompatActivity implements AudioRecordView.RecordingListener {


    public static boolean insideChat = false;
    private static String mFileName = null;
    private final String TAG = "ChatActivity";
    private String token;
    private long startTimeRecording;
    private ChatAdapter adapter;
    private RecyclerView recycler_view;
    private ActionBar actionBar;
    private AudioRecordView audioRecordView;
    private Toolbar toolbar;
    private User user;
    private UCrop.Options options;
    private Bitmap thumbBitmap = null;
    private byte[] imageBytes;
    // firebase
    private FirebaseFirestore db;
    private CollectionReference colRefMessages;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getTokenID();

        initToolbar();
        iniComponent();
        initFireStore();

        getMessages();
    }

    public void getTokenID() {
//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                if (!task.isSuccessful()) {
//                    //    Log.w(TAG, "getInstanceId failed", task.getException());
//                    return;
//                }
//
//                // Get new Instance ID token
//                token = task.getResult().getToken();
//
//                // Log and toast
//                //  String msg = getString(R.string.msg_token_fmt, token);
//                // Log.d(TAG, msg);
//                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
//                //   CommonUtil.showToast("current token is : " + token);
//            }
//        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        insideChat = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        insideChat = false;
    }

    private void initFireStore() {
        db = FirebaseFirestore.getInstance();
        colRefMessages = db.collection(Enums.Messages.name());

    }

    public void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
//        Tools.setSystemBarColorInt(this, Color.parseColor("#006ACF"));
    }

    public void iniComponent() {
        options = new UCrop.Options();
        options.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        audioRecordView = findViewById(R.id.recordingView);
        audioRecordView.setRecordingListener(this);
        recycler_view = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setHasFixedSize(true);

        adapter = new ChatAdapter(this);
        recycler_view.setAdapter(adapter);

        audioRecordView.getSendView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String msg = audioRecordView.getMessageView().getText().toString();
                if (msg.isEmpty()) return;
                sendTextMessage(msg);
                audioRecordView.getMessageView().setText("");
            }
        });

        audioRecordView.getAttachmentView().setOnClickListener(v -> {
            ImagePicker.create(ChatActivity.this)
                    .limit(1)
                    .theme(R.style.UCrop)
                    .folderMode(false)
                    .start();
        });

//        audioRecordView.getMessageView().addTextChangedListener(contentWatcher);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToolBarChooseDialog();
            }
        });
    }

    private void showToolBarChooseDialog() {

    }

    private void getMessages() {
        
        colRefMessages.document(AGConnectAuth.getInstance().getCurrentUser().getUid())
                .collection(Enums.Messages.name())
                .orderBy(Enums.date.name(), Query.Direction.ASCENDING).addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "listen:error", e);
                        return;
                    }
    
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Message message = dc.getDocument().toObject(Message.class);
                                adapter.insertItem(message);
                                recycler_view.scrollToPosition(adapter.getItemCount() - 1);
    
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Modified city: " + dc.getDocument().getData());
                                break;
                            case REMOVED:
                                Log.d(TAG, "Removed city: " + dc.getDocument().getData());
                                break;
                        }
                    }
    
                });


    }

    private void sendTextMessage(String content) {
        String headId = AGConnectAuth.getInstance().getCurrentUser().getUid();
        String messageID = colRefMessages.document(headId).collection(Enums.Messages.name()).document().getId();

        Message message = new Message(
                messageID,
                Enums.Text.name(),
                new Date(),
                content,
                Enums.USER.name(),
                Enums.Text.name(),
                Enums.Text.name(),
                adapter.getItemCount() % 5 == 0
        );

        ChatHead chatHead = new ChatHead(
                headId,
                AGConnectAuth.getInstance().getCurrentUser().getUid(),
                message,
                SessionConstants.INSTANCE.getCurrentLoggedInUserModel().getName(),
                new Date(),
                token,
                false
        );

        colRefMessages.document(headId)
                .set(chatHead)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: success ");
                    }
                });

        colRefMessages.document(headId)
                .collection(Enums.Messages.name())
                .document(messageID)
                .set(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success ");
                        }
                    }
                });

        sendNotification(content);

    }

    private void sendRecordMessage(String recordUri) {

        String headId = AGConnectAuth.getInstance().getCurrentUser().getUid();
        String messageID = colRefMessages.document(headId).collection(Enums.Messages.name()).document().getId();

        Message message = new Message(
                messageID,
                Enums.Records.name(),
                new Date(),
                Enums.Records.name(),
                Enums.USER.name(),
                Enums.Records.name(),
                recordUri,
                adapter.getItemCount() % 5 == 0
        );

        ChatHead chatHead = new ChatHead(
                headId,
                headId,
                message,
                SessionConstants.INSTANCE.getCurrentLoggedInUserModel().getName(),
                new Date(),
                token,
                false
        );
        colRefMessages.document(headId)
                .set(chatHead)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success ");
                        }
                    }
                });

        colRefMessages.document(headId)
                .collection(Enums.Messages.name())
                .document(messageID)
                .set(message)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onComplete: success ");
                    }
                });

        sendNotification(Enums.Records.name());

    }

    private void sendNotification(String content) {
//        FirebaseFirestore.getInstance().collection(Enums.AdminTokens.name())
//                .addSnapshotListener((snapshots, e) -> {
//                    if (e != null) {
//                        Log.w(TAG, "listen:error", e);
//                        return;
//                    }
//
//                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                        switch (dc.getType()) {
//                            case ADDED:
//                                Admin message = dc.getDocument().toObject(Admin.class);
//                                SendNotification.sendWithOtherThread(message.getToken(), SessionConstants.INSTANCE.getCurrentLoggedInUserModel().getName(), content, null);
//                                break;
//                            case MODIFIED:
//                                break;
//                            case REMOVED:
//                                break;
//                        }
//                    }
//
//                });

    }

    private void sendRecordMessage() {

        Log.d(TAG, "sendRecordMessage: try send ");
        final StorageReference thumbFilePathRef = FirebaseStorage.getInstance().getReference().
                child(Enums.Records.name()).child(mFileName);

        thumbFilePathRef.putFile(Uri.fromFile(new File(mFileName).getAbsoluteFile()))
                .addOnSuccessListener(taskSnapshot -> thumbFilePathRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d(TAG, "onSuccess: ");
                    sendRecordMessage(uri.toString());
                }).addOnFailureListener(e -> Log.d(TAG, "onFailure: " + e.getMessage())));

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            //Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            return;
        }
        String destinationFileName = "SAMPLE_CROPPED_IMAGE_NAME" + ".jpg";

        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {

            Image image = ImagePicker.getFirstImageOrNull(data);
            Uri res_url = Uri.fromFile(new File((image.getPath())));
            CropImage(image, res_url);

        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            //  if (resultUri!=null)
            assert resultUri != null;
            bitmapCompress(resultUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
            imageBytes = byteArrayOutputStream.toByteArray();
            uploadThumbImage(imageBytes);
            Log.d("TAG", "onActivityResult: " + Arrays.toString(imageBytes));
        }

    }

    //upload thumb image
    private void uploadThumbImage(byte[] thumbByte) {
        final StorageReference thumbFilePathRef = FirebaseStorage.getInstance().getReference().
                child(Enums.ChatImages.name()).child(System.currentTimeMillis() + ".jpg");
        thumbFilePathRef.putBytes(thumbByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                thumbFilePathRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri thumbUri) {
                        sendImageMessage(thumbUri);

                    }
                });
            }
        });
    }

    private void sendImageMessage(Uri uri) {

        String headId = AGConnectAuth.getInstance().getCurrentUser().getUid();
        String messageID = colRefMessages.document(headId).collection(Enums.Messages.name()).document().getId();

        Message message = new Message(
                messageID,
                Enums.Image.name(),
                new Date(),
                Enums.Image.name(),
                Enums.USER.name(),
                uri.toString(),
                Enums.Image.name(),
                adapter.getItemCount() % 5 == 0
        );


        ChatHead chatHead = new ChatHead(
                headId,
                headId,
                message,
                SessionConstants.INSTANCE.getCurrentLoggedInUserModel().getName(),
                new Date(),
                token,
                false
        );

        colRefMessages.document(headId)
                .set(chatHead)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success ");
                        }
                    }
                });

        colRefMessages.document(headId)
                .set(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success ");
                        }
                    }
                });

        colRefMessages.document(headId)
                .collection(Enums.Messages.name())
                .document(messageID)
                .set(message)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: success ");
                        }
                    }
                });

        sendNotification(Enums.Image.name());
    }

    private void CropImage(Image image, Uri res_url) {
        UCrop.of(res_url, Uri.fromFile(new File(getCacheDir(), image.getName())))
                .withOptions(options)
                .start(ChatActivity.this);
    }

    private void bitmapCompress(Uri resultUri) {
        final File thumbFilepathUri = new File(resultUri.getPath());

        try {
            thumbBitmap = new Compressor(this)
                    .setQuality(50)
                    .compressToBitmap(thumbFilepathUri);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        hideKeyboard();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onRecordingStarted() {
        debug("started");
        requestForSpecificPermission();
        startRecording();
    }


    private void requestForSpecificPermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                102);
    }

    @Override
    public void onRecordingLocked() {
        debug("locked");
    }

    @Override
    public void onRecordingCompleted() {
        debug("completed");
        stopRecording(false);
    }

    @Override
    public void onRecordingCanceled() {
        debug("canceled");
        stopRecording(true);
    }

    private void debug(String log) {
        Log.d("VarunJohn", log);
    }

    private void startRecording() {

        startTimeRecording = System.currentTimeMillis();
        try {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/" + System.currentTimeMillis() + ".3gp";
            mRecorder.setOutputFile(mFileName);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();
            mRecorder.start();


        } catch (Exception e) {
            Log.d("gggggggggggg", "startRecording: " + e.getMessage());
        }

    }

    private void stopRecording(boolean cancele) {
        if (cancele) {
            try {
                if (mRecorder != null) {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
            } catch (Exception e) {
                Log.d("gggggggggggg", "startstopniging: " + e.getMessage());
                Log.d("gggggggggggg", "startstopniging: " + e.getLocalizedMessage());

            }
            return;
        }
        if ((startTimeRecording + 1000) <= System.currentTimeMillis()) {
            startTimeRecording = 0;
            try {
                if (mRecorder != null) {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
                sendRecordMessage();
            } catch (Exception e) {
                Log.d("gggggggggggg", "startstopniging: " + e.getMessage());

            }
        } else {
            Toast.makeText(this, "يجب ان يكون التسجيل اكثر من ثانية", Toast.LENGTH_SHORT).show();
            startTimeRecording = 0;
            try {

                if (mRecorder != null) {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
            } catch (Exception e) {
                Log.d("gggggggggggg", "startstopniging: " + e.getMessage());

            }
        }

    }

}
