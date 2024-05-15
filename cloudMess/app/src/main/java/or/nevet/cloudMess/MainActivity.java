package or.nevet.cloudMess;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.util.Linkify;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.pedromassango.doubleclick.DoubleClickListener;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, DoubleClickListener, View.OnClickListener {
    //change this and the things on firebase to send it to someone.
    public static final String name = "Amit";
    Button send;
    EditText editText;
    private FirebaseAnalytics mFirebaseAnalytics;
    CollectionReference dr;
    CollectionReference mr;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    public static SoundPool soundPool;
    public static int bubble;
    ScrollView scrollView;
    ImageButton settings;
    Button down;
    public static SQLiteDatabase mDatabase;
    public static Vibrator v;
    ConstraintLayout coco;
    volatile LinearLayout linearLayout;
    ConstraintLayout sala;
    Button up;
    ConstraintLayout main;
    CollectionReference users;
    public static int delete;
    public static Toast toast;
    boolean isstart = true;
    LinearLayout reply;
    TextView reply_name;
    TextView reply_text;
    TextView reply_time;
    private Mess repliedMess = null;
    TextView loading;
    int dots = 0;
    Handler handler = new Handler(Looper.getMainLooper());
    public static volatile boolean isLoading = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initiate the variables
        loading = findViewById(R.id.loading);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.editText);
        linearLayout = findViewById(R.id.lili);
        coco = findViewById(R.id.coco);
        scrollView = findViewById(R.id.scroll);
        main = findViewById(R.id.main);
        settings = findViewById(R.id.settings);
        down = findViewById(R.id.down);
        up = findViewById(R.id.up);
        reply = findViewById(R.id.reply);
        reply_name = findViewById(R.id.reply_name);
        reply_text = findViewById(R.id.reply_text);
        Linkify.addLinks(reply_text, Linkify.WEB_URLS);
        reply_time = findViewById(R.id.reply_time);
        sala = findViewById(R.id.sala);
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        users = FirebaseFirestore.getInstance().collection("users");
        dr  = users.document(name).collection("Deletes");
        mr = users.document(name).collection("Messes");
        MesSqLiteHelper mesSqLiteHelper = new MesSqLiteHelper(this);
        mDatabase = mesSqLiteHelper.getWritableDatabase();
        AudioAttributes audioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
        soundPool = new SoundPool.Builder().setMaxStreams(20).setAudioAttributes(audioAttributes).build();
        bubble = soundPool.load(this, R.raw.bubble, 1);
        delete = soundPool.load(this, R.raw.delete, 1);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SettingsActivity.isAutoScroll = sharedPreferences.getBoolean("IsAutoScroll", false);
        SettingsActivity.isAutoDismiss = sharedPreferences.getBoolean("IsAutoDismiss", false);
        FirebaseListeners listeners = new FirebaseListeners(mr, dr, db, MainActivity.this, mDatabase, linearLayout, scrollView, v, loading);
        reply.setOnLongClickListener(this);
        reply_text.setOnLongClickListener(this);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                });
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        main.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if(isstart){
                    isstart = false;
                    coco.getLayoutParams().height = (int)((double) Resources.getSystem().getDisplayMetrics().heightPixels*0.15d);
                    sala.getLayoutParams().height = (int)((double) Resources.getSystem().getDisplayMetrics().heightPixels*0.1d);
                    coco.requestLayout();
                    sala.requestLayout();
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String mess = editText.getText().toString();
                final OReplyMess replyMess;
                final long replyTime;
                final boolean isReply = repliedMess != null;
                if (isReply) {
                    replyMess = new OReplyMess(MainActivity.this, repliedMess);
                    String text = repliedMess.getText().toString();
                    Date tmp = null;
                    try {
                        tmp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(text.substring(text.lastIndexOf("\n\ntime: ") + "\n\ntime: ".length()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (tmp != null)
                        replyTime = tmp.getTime();
                    else
                        replyTime = 0;
                }
                else {
                    replyMess = null;
                    replyTime = 0;
                }
                if (mess.equals("")) {
                    if(toast != null)
                        toast.cancel();
                    toast = Toast.makeText(MainActivity.this, "You need to insert text to send it ;)", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    editText.setText("");
                    editText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
                    final Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                NTPUDPClient client = new NTPUDPClient();
                                client.setDefaultTimeout(5000);
                                InetAddress inetAddress = InetAddress.getByName("time.google.com");
                                TimeInfo timeInfo2 = client.getTime(inetAddress);
                                final long timeinmillis = timeInfo2.getMessage().getTransmitTimeStamp().getTime();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        FieldPath fieldPath = FieldPath.documentId();
                                        users.whereNotEqualTo(fieldPath, name).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                final HashMap<String, Object> message = new HashMap<>();
                                                message.put("message", mess);
                                                message.put("time", timeinmillis);
                                                message.put("name", name);
                                                if (isReply)
                                                    message.put("reply", replyTime);
                                                for(DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                                    users.document(documentSnapshot.getId()).collection("Messes").add(message);
                                                }
                                            }
                                        });
                                        db.getReference().child("Messes").push().setValue(name+"\n\n"+mess+"\n\n"+timeinmillis);
                                        if (isReply) {
                                            linearLayout.addView(replyMess);
                                        }
                                        Mess message = new Mess(MainActivity.this, name + ":\n\n" + mess + "\n\ntime: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(timeinmillis)), replyMess, true);
                                        linearLayout.addView(message);
                                        ContentValues cv = new ContentValues();
                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_NAME, name);
                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_MESS, mess);
                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_TIME, timeinmillis);
                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_REPLY, replyTime);
                                        mDatabase.insert(MesSqLite.MesSqLitEntry.TABLE_NAME, null, cv);
                                        scrollView.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                            }
                                        });
                                        if (SettingsActivity.isAutoDismiss) {
                                            hideReply();
                                        }
                                        soundPool.play(bubble, 1, 1, 0, 0, 1);
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_ALARM).build());
                                                } else
                                                    v.vibrate(300);
                                            }
                                        }).start();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(toast != null)
                                            toast.cancel();
                                        toast = Toast.makeText(MainActivity.this, "There was an error, make sure that you are connected to the internet", Toast.LENGTH_SHORT);
                                        toast.show();
                                    }
                                });
                            }
                        }
                    });
                    t.start();
                }
            }
        });
    }

    @Override
    public boolean onLongClick(final View view) {
        if (view instanceof Mess) {
            //This deletes the message and its reply.
            final Mess mess = (Mess) view;
            new AlertDialog.Builder(MainActivity.this).setMessage("What do you want to do?").setTitle("Options").setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String text = mess.getText().toString();
                    ClipData clipData = ClipData.newPlainText("message", text.substring(text.indexOf("\n\n") + "\n\n".length(), text.lastIndexOf("\n\n")));
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(clipData);
                    if (toast != null)
                        toast.cancel();
                    toast = Toast.makeText(MainActivity.this, "The message has been copied", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }).setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new AlertDialog.Builder(MainActivity.this).setMessage("Are you sure that you want to delete this message?").setTitle("Message delete").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String text = mess.getText().toString();
                            if (text.substring(0, name.length() + 1).equals(name + ":")) {
                                long t = 0;
                                try {
                                    t = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(text.substring(text.lastIndexOf("\n\ntime: ") + "\n\ntime: ".length())).getTime();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                final long time = t;
                                FieldPath fieldPath = FieldPath.documentId();
                                users.whereNotEqualTo(fieldPath, name).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        HashMap<String, Long> map = new HashMap<>();
                                        map.put("delete", time);
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            users.document(documentSnapshot.getId()).collection("Deletes").add(map);
                                        }
                                    }
                                });
                                db.getReference().child("Deletes").push().setValue(time + "");
                                linearLayout.removeView(mess);
                                OReplyMess replyMess = mess.replyMess;
                                if (replyMess != null)
                                    linearLayout.removeView(replyMess);
                                mDatabase.delete(MesSqLite.MesSqLitEntry.TABLE_NAME, MesSqLite.MesSqLitEntry.COLUMN_TIME + "=" + time, null);
                                soundPool.play(delete, 1, 1, 0, 0, 1);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            v.vibrate(VibrationEffect.createOneShot(700, VibrationEffect.DEFAULT_AMPLITUDE), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_ALARM).build());
                                        } else
                                            v.vibrate(700);
                                    }
                                }).start();
                            } else {
                                if (toast != null)
                                    toast.cancel();
                                toast = Toast.makeText(MainActivity.this, "You can not delete messages that were not sent by you!", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    }).show();
                }
            }).show();
        }
        else if (view instanceof OReplyMess){
            //This scrolls to the top of the message that the message that the user clicked on replied to.
            final OReplyMess reply = (OReplyMess)view;
            if (reply.mess != null) {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.scrollTo(0, reply.mess.getTop());
                    }
                });
            }
        }
        else if (view instanceof TextView) {
            try {
                //This scrolls to the top of the message that the message that the user clicked on replied to.
                final OReplyMess reply = (OReplyMess) (view.getParent().getParent());
                if (reply.mess != null) {
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, reply.mess.getTop());
                        }
                    });
                }
            }
            catch (Exception e) {
                //This scrolls to the top of the message that the user replied to.
                if (repliedMess != null) {
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.scrollTo(0, repliedMess.getTop());
                        }
                    });
                }
            }
        }
        else if(repliedMess != null) {
            //This scrolls to the top of the message that the user replied to.
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, repliedMess.getTop());
                }
            });
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        load();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
        MyFirebaseMessagingService.notifications.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onSingleClick(View view) {

    }

    @Override
    public void onDoubleClick(View view) {
        repliedMess = (Mess) view;
        showReply(repliedMess);
    }

    private void showReply(Mess mess) {
        String text = mess.getText().toString();
        String time = text.substring(text.lastIndexOf("\n\ntime: ")+"\n\ntime: ".length());
        String message = text.substring(text.indexOf("\n\n")+"\n\n".length(), text.lastIndexOf("\n\n"));
        String n = text.substring(0, text.indexOf(":\n\n"));
        reply_name.setText(n);
        reply_text.setText(message);
        reply_time.setText(time);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        Rect rectText = new Rect();
        Rect rectName = new Rect();
        reply_text.getPaint().getTextBounds(reply_text.getText().toString(), 0, reply_text.getText().length(), rectText);
        reply_name.getPaint().getTextBounds(reply_name.getText().toString(), 0, reply_name.getText().length(), rectName);

        if (rectText.height() > rectName.height()) {
            layoutParams3.weight = 1;
            reply_name.setLayoutParams(layoutParams3);
        }
        else {
            layoutParams3.weight = 2;
            reply_text.setLayoutParams(layoutParams3);
        }
        reply.setVisibility(View.VISIBLE);
    }

    private void hideReply() {
        repliedMess = null;
        reply_name.setText("");
        reply_text.setText("");
        reply_time.setText("");
        reply.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        hideReply();
    }

    private void load() {
        if (!isLoading) {
            loading.setVisibility(View.GONE);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (dots == 3) {
                        dots = 0;
                        loading.setText("Loading");
                    }
                    else {
                        dots++;
                        loading.setText(loading.getText().toString() + ".");
                    }
                    load();
                }
            }, 100);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}