package com.bluethunder.tar2.views;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bluethunder.tar2.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by Varun John on 4 Dec, 2018
 * Github : https://github.com/varunjohn
 */
public class AudioRecordView extends FrameLayout {

    public static String messageText = "";
    private View imageViewAudio, imageViewLockArrow, imageViewLock, imageViewMic, dustin, dustin_cover, imageViewStop, imageViewSend;
    private View layoutDustin, layoutMessage, imageViewAttachment;
    private View layoutSlideCancel, layoutLock;
    private EditText editTextMessage;
    private TextView timeText;
    private ImageView stop, audio, send;
    private Animation animBlink, animJump, animJumpFast;
    private boolean isDeleting;
    private boolean stopTrackingAction;
    private Handler handler;
    private int audioTotalTime;
    private TimerTask timerTask;
    private Timer audioTimer;
    private final SimpleDateFormat timeFormatter = new SimpleDateFormat("m:ss", Locale.getDefault());
    private float lastX, lastY;
    private float firstX, firstY;
    private float directionOffset, cancelOffset, lockOffset;
    private float dp = 0;
    private boolean isLocked = false;
    private UserBehaviour userBehaviour = UserBehaviour.NONE;
    private RecordingListener recordingListener;

    public AudioRecordView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public AudioRecordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AudioRecordView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.recording_layout, null);
        addView(view);

        imageViewAttachment = view.findViewById(R.id.imageViewAttachment);
        editTextMessage = view.findViewById(R.id.editTextMessage);

        send = view.findViewById(R.id.imageSend);
        stop = view.findViewById(R.id.imageStop);
        audio = view.findViewById(R.id.imageAudio);

        imageViewAudio = view.findViewById(R.id.imageViewAudio);
        imageViewStop = view.findViewById(R.id.imageViewStop);
        imageViewSend = view.findViewById(R.id.imageViewSend);
        imageViewLock = view.findViewById(R.id.imageViewLock);
        imageViewLockArrow = view.findViewById(R.id.imageViewLockArrow);
        layoutDustin = view.findViewById(R.id.layoutDustin);
        layoutMessage = view.findViewById(R.id.layoutMessage);
        timeText = view.findViewById(R.id.textViewTime);
        layoutSlideCancel = view.findViewById(R.id.layoutSlideCancel);
        layoutLock = view.findViewById(R.id.layoutLock);
        imageViewMic = view.findViewById(R.id.imageViewMic);
        dustin = view.findViewById(R.id.dustin);
        dustin_cover = view.findViewById(R.id.dustin_cover);

        handler = new Handler(Looper.getMainLooper());

        dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getContext().getResources().getDisplayMetrics());

        animBlink = AnimationUtils.loadAnimation(getContext(),
                R.anim.blink);
        animJump = AnimationUtils.loadAnimation(getContext(),
                R.anim.jump);
        animJumpFast = AnimationUtils.loadAnimation(getContext(),
                R.anim.jump_fast);

        setupRecording();
    }

    public void setAudioRecordButtonImage(int imageResource) {
        audio.setImageResource(imageResource);
    }

    public void setStopButtonImage(int imageResource) {
        stop.setImageResource(imageResource);
    }

    public void setSendButtonImage(int imageResource) {
        send.setImageResource(imageResource);
    }

    public RecordingListener getRecordingListener() {
        return recordingListener;
    }

    public void setRecordingListener(RecordingListener recordingListener) {
        this.recordingListener = recordingListener;
    }

    public View getSendView() {
        return imageViewSend;
    }

    public View getAttachmentView() {
        return imageViewAttachment;
    }

    public EditText getMessageView() {
        return editTextMessage;
    }

    private void setupRecording() {

        imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100).setInterpolator(new LinearInterpolator()).start();

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                messageText = s.toString();
//                if (messageText.length() > 0) {
//                    String last = String.valueOf(messageText.charAt(messageText.length() - 1));
//                    if (last.equals("@")) {
//                        if (ChatActivity.mentionView.getVisibility()==GONE){
//                            showUsersToMention();
//                        }
//
//                    }else {
//                        if (ChatActivity.mentionView.getVisibility()==VISIBLE){
//                            String fileName = messageText.substring(messageText.lastIndexOf("@") + 1);
//                            filterRecucler(fileName);
//                        }
//                    }
//
//                } else {
//                    if (ChatActivity.mentionView.getVisibility() == VISIBLE)
//                        hideUsersToMention();
//                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    if (imageViewSend.getVisibility() != View.GONE) {
                        imageViewSend.setVisibility(View.GONE);
                        imageViewSend.animate().scaleX(0f).scaleY(0f).setDuration(100).setInterpolator(new LinearInterpolator()).start();
                    }
                } else {
                    if (imageViewSend.getVisibility() != View.VISIBLE && !isLocked) {
                        imageViewSend.setVisibility(View.VISIBLE);
                        imageViewSend.animate().scaleX(1f).scaleY(1f).setDuration(100).setInterpolator(new LinearInterpolator()).start();
                    }
                }
            }
        });

        imageViewAudio.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (isDeleting) {
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    cancelOffset = (float) (imageViewAudio.getX() / 2.8);
                    lockOffset = (float) (imageViewAudio.getX() / 2.5);

                    if (firstX == 0) {
                        firstX = motionEvent.getRawX();
                    }

                    if (firstY == 0) {
                        firstY = motionEvent.getRawY();
                    }

                    startRecord();

                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                        || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        stopRecording(RecordingBehaviour.RELEASED);
                    }

                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {

                    if (stopTrackingAction) {
                        return true;
                    }

                    UserBehaviour direction = UserBehaviour.NONE;

                    float motionX = Math.abs(firstX - motionEvent.getRawX());
                    float motionY = Math.abs(firstY - motionEvent.getRawY());

                    if (motionX > directionOffset &&
                            motionX > directionOffset &&
                            lastX < firstX && lastY < firstY) {

                        if (motionX > motionY && lastX < firstX) {
                            direction = UserBehaviour.CANCELING;

                        } else if (motionY > motionX && lastY < firstY) {
                            direction = UserBehaviour.LOCKING;
                        }

                    } else if (motionX > motionY && motionX > directionOffset && lastX < firstX) {
                        direction = UserBehaviour.CANCELING;
                    } else if (motionY > motionX && motionY > directionOffset && lastY < firstY) {
                        direction = UserBehaviour.LOCKING;
                    }

                    if (direction == UserBehaviour.CANCELING) {
                        if (userBehaviour == UserBehaviour.NONE || motionEvent.getRawY() + imageViewAudio.getWidth() / 2 > firstY) {
                            userBehaviour = UserBehaviour.CANCELING;
                        }

                        if (userBehaviour == UserBehaviour.CANCELING) {
                            translateX(-(firstX - motionEvent.getRawX()));
                        }
                    } else if (direction == UserBehaviour.LOCKING) {
                        if (userBehaviour == UserBehaviour.NONE || motionEvent.getRawX() + imageViewAudio.getWidth() / 2 > firstX) {
                            userBehaviour = UserBehaviour.LOCKING;
                        }

                        if (userBehaviour == UserBehaviour.LOCKING) {
                            translateY(-(firstY - motionEvent.getRawY()));
                        }
                    }

                    lastX = motionEvent.getRawX();
                    lastY = motionEvent.getRawY();
                }
                view.onTouchEvent(motionEvent);
                return true;
            }
        });

        imageViewStop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isLocked = false;
                stopRecording(RecordingBehaviour.LOCK_DONE);
            }
        });
    }

    private void translateY(float y) {
        if (y < -lockOffset) {
            locked();
            imageViewAudio.setTranslationY(0);
            return;
        }

        if (layoutLock.getVisibility() != View.VISIBLE) {
            layoutLock.setVisibility(View.VISIBLE);
        }

        imageViewAudio.setTranslationY(y);
        layoutLock.setTranslationY(y / 2);
        imageViewAudio.setTranslationX(0);
    }

    private void translateX(float x) {
        if (x < -cancelOffset) {
            canceled();
            imageViewAudio.setTranslationX(0);
            layoutSlideCancel.setTranslationX(0);
            return;
        }

        imageViewAudio.setTranslationX(x);
        layoutSlideCancel.setTranslationX(x);
        layoutLock.setTranslationY(0);
        imageViewAudio.setTranslationY(0);

        if (Math.abs(x) < imageViewMic.getWidth() / 2) {
            if (layoutLock.getVisibility() != View.VISIBLE) {
                layoutLock.setVisibility(View.VISIBLE);
            }
        } else {
            if (layoutLock.getVisibility() != View.GONE) {
                layoutLock.setVisibility(View.GONE);
            }
        }
    }

    private void locked() {
        stopTrackingAction = true;
        stopRecording(RecordingBehaviour.LOCKED);
        isLocked = true;
//        Animator animator = ViewAnimationUtils.createCircularReveal()
    }

    private void canceled() {
        stopTrackingAction = true;
        stopRecording(RecordingBehaviour.CANCELED);
    }

    private void stopRecording(RecordingBehaviour recordingBehaviour) {

        stopTrackingAction = true;
        firstX = 0;
        firstY = 0;
        lastX = 0;
        lastY = 0;

        userBehaviour = UserBehaviour.NONE;

        imageViewAudio.animate().scaleX(1f).scaleY(1f).translationX(0).translationY(0).setDuration(100).setInterpolator(new LinearInterpolator()).start();
        layoutSlideCancel.setTranslationX(0);
        layoutSlideCancel.setVisibility(View.GONE);

        layoutLock.setVisibility(View.GONE);
        layoutLock.setTranslationY(0);
        imageViewLockArrow.clearAnimation();
        imageViewLock.clearAnimation();

        if (isLocked) {
            return;
        }

        if (recordingBehaviour == RecordingBehaviour.LOCKED) {
            imageViewStop.setVisibility(View.VISIBLE);

            if (recordingListener != null)
                recordingListener.onRecordingLocked();

        } else if (recordingBehaviour == RecordingBehaviour.CANCELED) {
            timeText.clearAnimation();
            timeText.setVisibility(View.INVISIBLE);
            imageViewMic.setVisibility(View.INVISIBLE);
            imageViewStop.setVisibility(View.GONE);

            timerTask.cancel();
            delete();

            if (recordingListener != null)
                recordingListener.onRecordingCanceled();

        } else if (recordingBehaviour == RecordingBehaviour.RELEASED || recordingBehaviour == RecordingBehaviour.LOCK_DONE) {
            timeText.clearAnimation();
            timeText.setVisibility(View.INVISIBLE);
            imageViewMic.setVisibility(View.INVISIBLE);
            layoutMessage.setVisibility(View.VISIBLE);
            imageViewAttachment.setVisibility(View.VISIBLE);
            imageViewStop.setVisibility(View.GONE);

            timerTask.cancel();

            if (recordingListener != null)
                recordingListener.onRecordingCompleted();
        }
    }

    private void startRecord() {
        if (recordingListener != null)
            recordingListener.onRecordingStarted();

        stopTrackingAction = false;
        layoutMessage.setVisibility(View.GONE);
        imageViewAttachment.setVisibility(View.INVISIBLE);
        imageViewAudio.animate().scaleXBy(1f).scaleYBy(1f).setDuration(200).setInterpolator(new OvershootInterpolator()).start();
        timeText.setVisibility(View.VISIBLE);
        layoutLock.setVisibility(View.VISIBLE);
        layoutSlideCancel.setVisibility(View.VISIBLE);
        imageViewMic.setVisibility(View.VISIBLE);
        timeText.startAnimation(animBlink);
        imageViewLockArrow.clearAnimation();
        imageViewLock.clearAnimation();
        imageViewLockArrow.startAnimation(animJumpFast);
        imageViewLock.startAnimation(animJump);

        if (audioTimer == null) {
            audioTimer = new Timer();
            timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        timeText.setText(timeFormatter.format(new Date(audioTotalTime * 1000)));
                        audioTotalTime++;
                    }
                });
            }
        };

        audioTotalTime = 0;
        audioTimer.schedule(timerTask, 0, 1000);
    }

    private void delete() {
        imageViewMic.setVisibility(View.VISIBLE);
        imageViewMic.setRotation(0);
        isDeleting = true;
        imageViewAudio.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isDeleting = false;
                imageViewAudio.setEnabled(true);
                imageViewAttachment.setVisibility(View.VISIBLE);
            }
        }, 1250);

        imageViewMic.animate().translationY(-dp * 150).rotation(180).scaleXBy(0.6f).scaleYBy(0.6f).setDuration(500).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                dustin.setTranslationX(-dp * 40);
                dustin_cover.setTranslationX(-dp * 40);

                dustin_cover.animate().translationX(0).rotation(-120).setDuration(350).setInterpolator(new DecelerateInterpolator()).start();

                dustin.animate().translationX(0).setDuration(350).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        dustin.setVisibility(View.VISIBLE);
                        dustin_cover.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                imageViewMic.animate().translationY(0).scaleX(1).scaleY(1).setDuration(350).setInterpolator(new LinearInterpolator()).setListener(
                        new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                imageViewMic.setVisibility(View.INVISIBLE);
                                imageViewMic.setRotation(0);

                                dustin_cover.animate().rotation(0).setDuration(150).setStartDelay(50).start();
                                dustin.animate().translationX(-dp * 40).setDuration(200).setStartDelay(250).setInterpolator(new DecelerateInterpolator()).start();
                                dustin_cover.animate().translationX(-dp * 40).setDuration(200).setStartDelay(250).setInterpolator(new DecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        layoutMessage.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                }).start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        }
                ).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).start();
    }

    public enum UserBehaviour {
        CANCELING,
        LOCKING,
        NONE
    }

    public enum RecordingBehaviour {
        CANCELED,
        LOCKED,
        LOCK_DONE,
        RELEASED
    }

    public interface RecordingListener {

        void onRecordingStarted();

        void onRecordingLocked();

        void onRecordingCompleted();

        void onRecordingCanceled();

    }
}


