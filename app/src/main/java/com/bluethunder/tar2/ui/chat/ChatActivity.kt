package com.bluethunder.tar2.ui.chat

import android.Manifest
import android.app.ActivityManager
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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bluethunder.tar2.R
import com.bluethunder.tar2.SessionConstants.currentLoggedInUserModel
import com.bluethunder.tar2.cloud_db.FirestoreReferences
import com.bluethunder.tar2.cloud_db.StorageReferences
import com.bluethunder.tar2.databinding.ActivityChatBinding
import com.bluethunder.tar2.model.NotificationType
import com.bluethunder.tar2.model.notifications.NotificationDataModel
import com.bluethunder.tar2.ui.auth.model.UserModel
import com.bluethunder.tar2.ui.chat.adapter.ChatAdapter
import com.bluethunder.tar2.ui.chat.model.ChatHead
import com.bluethunder.tar2.ui.chat.model.Message
import com.bluethunder.tar2.ui.chat.model.MessageType
import com.bluethunder.tar2.ui.extentions.getViewModelFactory
import com.bluethunder.tar2.ui.home.viewmodel.NotificationsViewModel
import com.bluethunder.tar2.ui.splash.SplashActivity
import com.bluethunder.tar2.ui.splash.viewmodel.SplashViewModel
import com.bluethunder.tar2.views.AudioRecordView
import com.bluethunder.tar2.views.AudioRecordView.RecordingListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.esafirm.imagepicker.features.ImagePicker
import com.esafirm.imagepicker.model.Image
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class ChatActivity : AppCompatActivity(), RecordingListener {

    private val viewModel by viewModels<SplashViewModel> { getViewModelFactory() }
    private lateinit var binding: ActivityChatBinding

    private var chatHead: ChatHead? = null
    private val token: String? = null
    private var startTimeRecording: Long = 0
    private var adapter: ChatAdapter? = null
    private lateinit var recycler_view: RecyclerView
    private lateinit var audioRecordView: AudioRecordView
    private val user: User? = null
    private var options: UCrop.Options? = null
    private var thumbBitmap: Bitmap? = null
    private lateinit var imageBytes: ByteArray
    private var mRecorder: MediaRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        caseData()
        initToolbar()
        iniComponent()
        getMessages()
    }

    fun caseData() {
        val intent = intent
        chatHead = intent.getSerializableExtra(CHAT_HEAD_EXTRA_KEY) as ChatHead?
        getCaseUserData()
    }

    var anotherUserDetails: UserModel? = null
    private fun getCaseUserData() {
        var anotherUserId = ""
        chatHead!!.users.forEach {
            if (it != currentLoggedInUserModel!!.id)
                anotherUserId = it
        }
        FirebaseFirestore.getInstance().collection(FirestoreReferences.UsersCollection.value)
            .document(anotherUserId)
            .get().addOnCompleteListener {
                try {
                    if (it.isSuccessful) {
                        anotherUserDetails = it.result!!.toObject(UserModel::class.java)
                        binding.recordingView.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                }

            }
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
        binding.backBtn.setOnClickListener { onBackPressed() }
        binding.caseDetailsV.setOnClickListener {
            viewModel.getCaseDetailsAndOpenIt(
                this,
                chatHead!!.caseId!!
            )
        }
        binding.titleTv.text = chatHead!!.caseTitle

        val circularProgressDrawable =
            CircularProgressDrawable(this)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(this)
            .asBitmap()
            .placeholder(circularProgressDrawable)
            .load(chatHead!!.caseImage!!)
            .override(200, 200)
            .optionalTransform(CircleCrop())
            .error(resources.getDrawable(R.drawable.ic_place_holder))
            .into(binding.mainImageView)

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
            .addOnCompleteListener { task: Task<Void?> ->
                if (task.isSuccessful) {
                    Log.d(TAG, "onComplete: success ")
                }
            }
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

    private val notificationViewModel by viewModels<NotificationsViewModel> { getViewModelFactory() }
    private fun sendNotification(content: String) {

        val data = NotificationDataModel(
            currentLoggedInUserModel!!.id!!,
            chatHead!!.caseId,
            chatHead!!.caseTitle,
            Gson().toJson(chatHead),
            NotificationType.Chat.name
        )
        val jsonString = Gson().toJson(data)
        anotherUserDetails?.let {
            it.pushToken?.let {
                notificationViewModel.getHMSAccessTokenAndSendNotification(
                    isTopic = false,
                    sendTo = it,
                    jsonString
                )
            }
        }
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
            mFileName = cacheDir.absolutePath
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

    override fun onBackPressed() {
        val mngr = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val taskList = mngr.getRunningTasks(10)
        if (taskList[0].numActivities == 1 && taskList[0].topActivity!!.className == this.javaClass.name) {
            Log.i(TAG, "This is last activity in the stack")
            startActivity(Intent(this, SplashActivity::class.java))
            super.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val USER_ID_EXTRA_KEY = "user_id_extra_key"
        const val CHAT_HEAD_EXTRA_KEY = "chat_head_extra_key"
        private const val TAG = "ChatActivity"
        var insideChat = false
        private var mFileName: String? = null
    }

}