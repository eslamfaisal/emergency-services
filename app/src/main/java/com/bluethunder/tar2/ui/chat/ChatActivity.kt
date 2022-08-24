package com.bluethunder.tar2.ui.chat

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.cloud_db.StorageReferences
import com.bluethunder.tar2.ui.chat.adapter.ChatAdapter
import com.bluethunder.tar2.ui.chat.model.ChatHead
import com.bluethunder.tar2.ui.chat.model.Message
import com.bluethunder.tar2.ui.chat.model.MessageType
import com.bluethunder.tar2.ui.edit_case.model.CaseModel
import com.bluethunder.tar2.views.AudioRecordView
import com.bluethunder.tar2.views.AudioRecordView.RecordingListener
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class ChatActivity : AppCompatActivity(), RecordingListener {
    private var caseModel: CaseModel? = null
    private var chatHead: ChatHead? = null
    private val token: String? = null
    private var startTimeRecording: Long = 0
    private var adapter: ChatAdapter? = null
    private lateinit var recycler_view: RecyclerView
    private var actionBar: ActionBar? = null
    private lateinit var audioRecordView: AudioRecordView
    private lateinit var toolbar: Toolbar
    private val user: User? = null
    private var options: UCrop.Options? = null
    private var thumbBitmap: Bitmap? = null
    private lateinit var imageBytes: ByteArray
    private var mRecorder: MediaRecorder? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        caseData
        tokenID
        initToolbar()
        iniComponent()
        getMessages()
    }

    private val caseData: Unit
        private get() {
            val intent = intent
            caseModel = intent.getSerializableExtra(CASE_EXTRA_KEY) as CaseModel?
            chatHead = intent.getSerializableExtra(CHAT_HEAD_EXTRA_KEY) as ChatHead?
        }

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
    val tokenID: Unit
        get() {
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

    override fun onResume() {
        super.onResume()
        insideChat = true
    }

    override fun onPause() {
        super.onPause()
        insideChat = false
    }

    fun initToolbar() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener(View.OnClickListener { v: View? -> onBackPressed() })
        actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setHomeButtonEnabled(true)
        //        Tools.setSystemBarColorInt(this, Color.parseColor("#006ACF"));
        toolbar.title = chatHead!!.caseTitle
    }

    fun iniComponent() {
        options = UCrop.Options()
        options!!.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        options!!.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        audioRecordView = findViewById(R.id.recordingView)
        audioRecordView.recordingListener = this
        recycler_view = findViewById(R.id.recyclerView)
        val layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        recycler_view.setHasFixedSize(true)
        adapter = ChatAdapter(this)
        recycler_view.adapter = adapter
        audioRecordView.sendView.setOnClickListener { view: View? ->
            val msg = audioRecordView.messageView.text.toString()
            if (msg.isEmpty()) return@setOnClickListener
            sendTextMessage(msg)
            audioRecordView.messageView.setText("")
        }
        audioRecordView.attachmentView.setOnClickListener { v: View? ->
            ImagePicker.create(this@ChatActivity)
                .limit(1)
                .theme(R.style.UCrop)
                .folderMode(false)
                .start()
        }

//        audioRecordView.getMessageView().addTextChangedListener(contentWatcher);
        toolbar.setOnClickListener { v: View? -> showToolBarChooseDialog() }
    }

    private fun showToolBarChooseDialog() {}
    private fun getMessages() {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.ChatHeadsCollection.value())
            .document(chatHead!!.id)
            .collection(FirestoreReferences.MessagesCollection.value())
            .orderBy(FirestoreReferences.DateField.value(), Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
                if (e != null) {
                    Log.w(TAG, "listen:error", e)
                    return@addSnapshotListener
                }
                for (dc in snapshots!!.documentChanges) {
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            val message = dc.document.toObject(
                                Message::class.java
                            )
                            adapter!!.insertItem(message)
                            recycler_view.scrollToPosition(adapter!!.itemCount - 1)
                        }
                        DocumentChange.Type.MODIFIED -> Log.d(
                            TAG,
                            "Modified city: " + dc.document.data
                        )
                        DocumentChange.Type.REMOVED -> Log.d(
                            TAG,
                            "Removed city: " + dc.document.data
                        )
                    }
                }
            }
    }

    private fun generateMessageID(): String {
        return FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.ChatHeadsCollection.value())
            .document(chatHead!!.id)
            .collection(FirestoreReferences.MessagesCollection.value())
            .document().id
    }

    private fun sendTextMessage(content: String) {
        sendMessage(getNewMessage(MessageType.Text, content))
        updateChatHead(content)
        sendNotification(content)
    }

    private fun sendMessage(message: Message) {
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.ChatHeadsCollection.value())
            .document(chatHead!!.id)
            .collection(FirestoreReferences.MessagesCollection.value())
            .document(message.id)
            .set(message)
            .addOnCompleteListener({ task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "onComplete: success ")
                }
            })
    }

    private fun updateChatHead(content: String) {
        chatHead!!.lastMessage = content
        chatHead!!.lastMessageAt = Date()
        chatHead!!.lastMessageSenderID = currentLoggedInUserModel!!.id
        FirebaseFirestore.getInstance()
            .collection(FirestoreReferences.ChatHeadsCollection.value())
            .document(chatHead!!.id)
            .set(chatHead!!)
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "onComplete: success ")
                }
            }
    }

    private fun sendRecordMessage(recordUri: String) {
        sendMessage(getNewMessage(MessageType.Records, recordUri))
        updateChatHead(MessageType.Records.name)
        sendNotification(MessageType.Records.name)
    }

    private fun sendNotification(content: String) {
//        FirebaseFirestore.getInstance().collection(MessageType.AdminTokens.name())
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

    private fun sendRecordMessage() {
        Log.d(TAG, "sendRecordMessage: try send ")
        val thumbFilePathRef =
            FirebaseStorage.getInstance().reference.child(MessageType.Records.name).child(
                mFileName!!
            )
        thumbFilePathRef.putFile(Uri.fromFile(File(mFileName).absoluteFile))
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                thumbFilePathRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                    Log.d(TAG, "onSuccess: ")
                    sendRecordMessage(uri.toString())
                }.addOnFailureListener { e: Exception -> Log.d(TAG, "onFailure: " + e.message) }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            //Toast.makeText(this, "", Toast.LENGTH_LONG).show();
            return
        }
        val destinationFileName = "SAMPLE_CROPPED_IMAGE_NAME" + ".jpg"
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            val image = ImagePicker.getFirstImageOrNull(data)
            val res_url = Uri.fromFile(File(image.path))
            CropImage(image, res_url)
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            val resultUri = UCrop.getOutput(data)!!
            bitmapCompress(resultUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            thumbBitmap!!.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
            imageBytes = byteArrayOutputStream.toByteArray()
            uploadThumbImage(imageBytes)
            Log.d("TAG", "onActivityResult: " + Arrays.toString(imageBytes))
        }
    }

    //upload thumb image
    private fun uploadThumbImage(thumbByte: ByteArray) {
        val thumbFilePathRef =
            FirebaseStorage.getInstance().reference.child(StorageReferences.ChatImagesFolder.value())
                .child(System.currentTimeMillis().toString() + ".jpg")
        thumbFilePathRef.putBytes(thumbByte)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                thumbFilePathRef.downloadUrl.addOnSuccessListener { thumbUri: Uri ->
                    sendImageMessage(
                        thumbUri
                    )
                }
            }
    }

    private fun sendImageMessage(uri: Uri) {
        sendMessage(getNewMessage(MessageType.Image, uri.toString()))
        updateChatHead(MessageType.Image.name)
        sendNotification(MessageType.Image.name)
    }

    private fun getNewMessage(type: MessageType, content: String): Message {
        return Message(
            generateMessageID(),
            type.name,
            Date(),
            content,
            currentLoggedInUserModel!!.id,
            adapter!!.itemCount % 5 == 0
        )
    }

    private fun CropImage(image: Image, res_url: Uri) {
        UCrop.of(res_url, Uri.fromFile(File(cacheDir, image.name)))
            .withOptions(options!!)
            .start(this@ChatActivity)
    }

    private fun bitmapCompress(resultUri: Uri?) {
        val thumbFilepathUri = File(resultUri!!.path)
        try {
            thumbBitmap = Compressor(this)
                .setQuality(50)
                .compressToBitmap(thumbFilepathUri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onRecordingStarted() {
        debug("started")
        requestForSpecificPermission()
        startRecording()
    }

    private fun requestForSpecificPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            102
        )
    }

    override fun onRecordingLocked() {
        debug("locked")
    }

    override fun onRecordingCompleted() {
        debug("completed")
        stopRecording(false)
    }

    override fun onRecordingCanceled() {
        debug("canceled")
        stopRecording(true)
    }

    private fun debug(log: String) {
        Log.d("VarunJohn", log)
    }

    private fun startRecording() {
        startTimeRecording = System.currentTimeMillis()
        try {
            mRecorder = MediaRecorder()
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mFileName = Environment.getExternalStorageDirectory().absolutePath
            mFileName += "/" + System.currentTimeMillis() + ".3gp"
            mRecorder!!.setOutputFile(mFileName)
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mRecorder!!.prepare()
            mRecorder!!.start()
        } catch (e: Exception) {
            Log.d("gggggggggggg", "startRecording: " + e.message)
        }
    }

    private fun stopRecording(cancele: Boolean) {
        if (cancele) {
            try {
                if (mRecorder != null) {
                    mRecorder!!.stop()
                    mRecorder!!.release()
                    mRecorder = null
                }
            } catch (e: Exception) {
                Log.d("gggggggggggg", "startstopniging: " + e.message)
                Log.d("gggggggggggg", "startstopniging: " + e.localizedMessage)
            }
            return
        }
        if (startTimeRecording + 1000 <= System.currentTimeMillis()) {
            startTimeRecording = 0
            try {
                if (mRecorder != null) {
                    mRecorder!!.stop()
                    mRecorder!!.release()
                    mRecorder = null
                }
                sendRecordMessage()
            } catch (e: Exception) {
                Log.d("gggggggggggg", "startstopniging: " + e.message)
            }
        } else {
            Toast.makeText(this, "يجب ان يكون التسجيل اكثر من ثانية", Toast.LENGTH_SHORT).show()
            startTimeRecording = 0
            try {
                if (mRecorder != null) {
                    mRecorder!!.stop()
                    mRecorder!!.release()
                    mRecorder = null
                }
            } catch (e: Exception) {
                Log.d("gggggggggggg", "startstopniging: " + e.message)
            }
        }
    }

    companion object {
        const val USER_ID_EXTRA_KEY = "user_id_extra_key"
        const val CASE_EXTRA_KEY = "case_extra_key"
        const val CHAT_HEAD_EXTRA_KEY = "chat_head_extra_key"
        private const val TAG = "ChatActivity"
        var insideChat = false
        private var mFileName: String? = null
    }
}