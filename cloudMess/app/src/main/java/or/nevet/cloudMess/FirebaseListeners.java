package or.nevet.cloudMess;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import static or.nevet.cloudMess.MainActivity.toast;

public class FirebaseListeners {

    volatile LinearLayout linearLayout;
    volatile boolean busy = false;

    public FirebaseListeners(final CollectionReference mr, final CollectionReference dr, FirebaseDatabase db, final Context context, final SQLiteDatabase mDatabase, final LinearLayout li, final ScrollView scrollView, final Vibrator v, final TextView loading) {

        linearLayout = li;
        final int bubble = MainActivity.bubble;
        final SoundPool soundPool = MainActivity.soundPool;
        final String name = MainActivity.name;
        final int delete = MainActivity.delete;
        FirebaseMessaging.getInstance().subscribeToTopic("friends")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
        db.getReference().child("users").child(name).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                new AlertDialog.Builder(context).setTitle("You got fucked!").setMessage("Or Nevet blocked you!!!!!! lololololol").setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        ((Activity)context).finish();
                    }
                }).show();
            }
        });
        db.getReference().child("users").child(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Handler handler = new Handler(Looper.getMainLooper());
                        final Cursor mCursor = mDatabase.query(MesSqLite.MesSqLitEntry.TABLE_NAME,null,null,null,null,null,MesSqLite.MesSqLitEntry.COLUMN_TIME+" ASC");
                        int count = mCursor.getCount();
                        if(count != 0) {
                            while(mCursor.moveToNext()) {
                                final String currentName = mCursor.getString(mCursor.getColumnIndex(MesSqLite.MesSqLitEntry.COLUMN_NAME));
                                long reply = mCursor.getLong(mCursor.getColumnIndex(MesSqLite.MesSqLitEntry.COLUMN_REPLY));
                                final long time = mCursor.getLong(mCursor.getColumnIndex(MesSqLite.MesSqLitEntry.COLUMN_TIME));
                                OReplyMess replyMess = null;
                                if (reply != 0) {
                                    int c = linearLayout.getChildCount();
                                    if (c == 0) {
                                        replyMess = new OReplyMess(context, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(reply)));
                                    }
                                    for (int i = 0; i < c; i++) {
                                        View v = linearLayout.getChildAt(i);
                                        if (v instanceof Mess) {
                                            Mess repliedMess = (Mess) v;
                                            String text = repliedMess.getText().toString();
                                            try {
                                                long t = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(text.substring(text.lastIndexOf("\n\ntime: ") + "\n\ntime: ".length())).getTime();
                                                if (t == reply) {
                                                    replyMess = new OReplyMess(context, repliedMess);
                                                    break;
                                                }
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        if (i == c - 1)
                                            replyMess = new OReplyMess(context, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(reply)));
                                    }
                                    final OReplyMess finalReplyMess = replyMess;
                                    busy = true;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                        linearLayout.addView(finalReplyMess);
                                        busy = false;
                                        }
                                    });
                                    while (busy);
                                }
                                final OReplyMess finalReplyMess1 = replyMess;
                                final String m = mCursor.getString(mCursor.getColumnIndex(MesSqLite.MesSqLitEntry.COLUMN_MESS));
                                busy = true;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        final Mess mess = new Mess(context, currentName + ":\n\n" + m +"\n\ntime: "+new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(time)), finalReplyMess1, name.equals(currentName));
                                        linearLayout.addView(mess);
                                        busy = false;
                                    }
                                });
                                while (busy);
                            }
                            scrollView.post(new Runnable() {
                                @Override
                                public void run() {
                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                }
                            });
                        }
                        MainActivity.isLoading = false;
                        mCursor.close();
                        mr.orderBy("time", Query.Direction.ASCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                                final Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (task.isSuccessful()) {
                                            try {
                                                final QuerySnapshot value = task.getResult();
                                                if (value != null && !value.isEmpty()) {
                                                    Handler handler = new Handler(Looper.getMainLooper());
                                                    int i = 0;
                                                    boolean added = false;
                                                    for (DocumentChange documentChange : value.getDocumentChanges()) {
                                                        final DocumentSnapshot documentSnapshot = value.getDocuments().get(i);
                                                        if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                                            added = true;
                                                            long reply = 0;
                                                            final String time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(documentSnapshot.getLong("time")));
                                                            OReplyMess replyMess = null;
                                                            if (documentSnapshot.contains("reply")) {
                                                                reply = documentSnapshot.getLong("reply");
                                                                int c = linearLayout.getChildCount();
                                                                if (c == 0) {
                                                                    replyMess = new OReplyMess(context, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(reply)));
                                                                }
                                                                for (int ii = 0; ii < c; ii++) {
                                                                    View v = linearLayout.getChildAt(ii);
                                                                    if (v instanceof Mess) {
                                                                        Mess repliedMess = (Mess) v;
                                                                        String text = repliedMess.getText().toString();
                                                                        try {
                                                                            long t = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(text.substring(text.lastIndexOf("\n\ntime: ") + "\n\ntime: ".length())).getTime();
                                                                            if (t == reply) {
                                                                                replyMess = new OReplyMess(context, repliedMess);
                                                                                break;
                                                                            }
                                                                        } catch (ParseException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                    if (ii == c - 1)
                                                                        replyMess = new OReplyMess(context, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(reply)));
                                                                }
                                                                final OReplyMess finalReplyMess = replyMess;
                                                                busy = true;
                                                                handler.post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        linearLayout.addView(finalReplyMess);
                                                                        busy = false;
                                                                    }
                                                                });
                                                                while (busy) ;
                                                            }
                                                            final OReplyMess finalReplyMess1 = replyMess;
                                                            busy = true;
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    final Mess mess = new Mess(context, documentSnapshot.getString("name") + ":\n\n" + documentSnapshot.getString("message") + "\n\ntime: " + time, finalReplyMess1, false);
                                                                    linearLayout.addView(mess);
                                                                    busy = false;
                                                                }
                                                            });
                                                            while (busy) ;
                                                            documentSnapshot.getReference().delete();
                                                            ContentValues cv = new ContentValues();
                                                            cv.put(MesSqLite.MesSqLitEntry.COLUMN_NAME, documentSnapshot.getString("name"));
                                                            cv.put(MesSqLite.MesSqLitEntry.COLUMN_MESS, documentSnapshot.getString("message"));
                                                            cv.put(MesSqLite.MesSqLitEntry.COLUMN_TIME, documentSnapshot.getLong("time"));
                                                            cv.put(MesSqLite.MesSqLitEntry.COLUMN_REPLY, reply);
                                                            mDatabase.insert(MesSqLite.MesSqLitEntry.TABLE_NAME, null, cv);
                                                        }
                                                        i++;
                                                    }
                                                    if (added) {
                                                        soundPool.play(bubble, 1, 1, 0, 0, 1);
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_ALARM).build());
                                                        } else
                                                            v.vibrate(300);
                                                        if (SettingsActivity.isAutoScroll) {
                                                            scrollView.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                                                }
                                                            });
                                                        }
                                                    }
                                                }
                                                mr.orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable final QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                            if (error != null) {
                                                                handler.post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (toast != null)
                                                                            toast.cancel();
                                                                        toast = Toast.makeText(context, "There was an error, make sure that you are connected to the internet", Toast.LENGTH_SHORT);
                                                                        toast.show();
                                                                    }
                                                                });
                                                            } else {
                                                                final Thread t = new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            Handler handler = new Handler(Looper.getMainLooper());
                                                                            if (value != null && !value.isEmpty()) {
                                                                                int i = 0;
                                                                                boolean added = false;
                                                                                for (DocumentChange documentChange : value.getDocumentChanges()) {
                                                                                    final DocumentSnapshot documentSnapshot = value.getDocuments().get(i);
                                                                                    if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                                                                        added = true;
                                                                                        long reply = 0;
                                                                                        final String time = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(documentSnapshot.getLong("time")));
                                                                                        OReplyMess replyMess = null;
                                                                                        if (documentSnapshot.contains("reply")) {
                                                                                            reply = documentSnapshot.getLong("reply");
                                                                                            int c = linearLayout.getChildCount();
                                                                                            for (int ii = 0; ii < c; ii++) {
                                                                                                View v = linearLayout.getChildAt(ii);
                                                                                                if (v instanceof Mess) {
                                                                                                    Mess repliedMess = (Mess) v;
                                                                                                    String text = repliedMess.getText().toString();
                                                                                                    try {
                                                                                                        long t = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(text.substring(text.lastIndexOf("\n\ntime: ") + "\n\ntime: ".length())).getTime();
                                                                                                        if (t == reply) {
                                                                                                            replyMess = new OReplyMess(context, repliedMess);
                                                                                                            break;
                                                                                                        }
                                                                                                    } catch (ParseException e) {
                                                                                                        e.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                                if (ii == c - 1)
                                                                                                    replyMess = new OReplyMess(context, new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").format(new Date(reply)));
                                                                                            }
                                                                                            final OReplyMess finalReplyMess = replyMess;
                                                                                            busy = true;
                                                                                            handler.post(new Runnable() {
                                                                                                @Override
                                                                                                public void run() {
                                                                                                    linearLayout.addView(finalReplyMess);
                                                                                                    busy = false;
                                                                                                }
                                                                                            });
                                                                                            while (busy) ;
                                                                                        }
                                                                                        final OReplyMess finalReplyMess1 = replyMess;
                                                                                        busy = true;
                                                                                        handler.post(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                final Mess mess = new Mess(context, documentSnapshot.getString("name") + ":\n\n" + documentSnapshot.getString("message") + "\n\ntime: " + time, finalReplyMess1, false);
                                                                                                linearLayout.addView(mess);
                                                                                                busy = false;
                                                                                            }
                                                                                        });
                                                                                        while (busy) ;
                                                                                        documentSnapshot.getReference().delete();
                                                                                        ContentValues cv = new ContentValues();
                                                                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_NAME, documentSnapshot.getString("name"));
                                                                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_MESS, documentSnapshot.getString("message"));
                                                                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_TIME, documentSnapshot.getLong("time"));
                                                                                        cv.put(MesSqLite.MesSqLitEntry.COLUMN_REPLY, reply);
                                                                                        mDatabase.insert(MesSqLite.MesSqLitEntry.TABLE_NAME, null, cv);
                                                                                    }
                                                                                    i++;
                                                                                }
                                                                                if (added) {
                                                                                    soundPool.play(bubble, 1, 1, 0, 0, 1);
                                                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                                        v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_ALARM).build());
                                                                                    } else
                                                                                        v.vibrate(300);
                                                                                    if (SettingsActivity.isAutoScroll) {
                                                                                        scrollView.post(new Runnable() {
                                                                                            @Override
                                                                                            public void run() {
                                                                                                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                                                                                            }
                                                                                        });
                                                                                    }
                                                                                }
                                                                            }
                                                                        } catch (Exception e) {
                                                                            handler.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    if (toast != null)
                                                                                        toast.cancel();
                                                                                    toast = Toast.makeText(context, "There was an error when receiving a message. Please notify the developer :)", Toast.LENGTH_SHORT);
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
                                                dr.orderBy("delete", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onEvent(@Nullable final QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                            if (error != null) {
                                                                handler.post(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (toast != null)
                                                                            toast.cancel();
                                                                        toast = Toast.makeText(context, "There was an error, make sure that you are connected to the internet", Toast.LENGTH_SHORT);
                                                                        toast.show();
                                                                    }
                                                                });
                                                            } else if (value != null) {
                                                                Thread t = new Thread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            Handler handler = new Handler(Looper.getMainLooper());
                                                                            boolean isremoved = false;
                                                                            int num = 0;
                                                                            for (DocumentChange documentChange : value.getDocumentChanges()) {
                                                                                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                                                                                    for (int i = 0; i < linearLayout.getChildCount(); i++) {
                                                                                        try {
                                                                                            final Mess t = (Mess) linearLayout.getChildAt(i);
                                                                                            String text = t.getText().toString();
                                                                                            final DocumentSnapshot documentSnapshot = value.getDocuments().get(num);
                                                                                            if (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(text.substring(text.lastIndexOf("\n\ntime: ") + "\n\ntime: ".length())).getTime() == documentSnapshot.getLong("delete")) {
                                                                                                busy = true;
                                                                                                handler.post(new Runnable() {
                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        linearLayout.removeView(t);
                                                                                                        OReplyMess replyMess = t.replyMess;
                                                                                                        if (replyMess != null)
                                                                                                            linearLayout.removeView(replyMess);
                                                                                                        documentSnapshot.getReference().delete();
                                                                                                        busy = false;
                                                                                                    }
                                                                                                });
                                                                                                while (busy)
                                                                                                    ;
                                                                                                mDatabase.delete(MesSqLite.MesSqLitEntry.TABLE_NAME, MesSqLite.MesSqLitEntry.COLUMN_TIME + "=" + documentSnapshot.getLong("delete"), null);
                                                                                                isremoved = true;
                                                                                                break;
                                                                                            }
                                                                                        } catch (Exception e) {

                                                                                        }
                                                                                    }
                                                                                }
                                                                                num++;
                                                                            }
                                                                            if (isremoved) {
                                                                                soundPool.play(delete, 1, 1, 0, 0, 1);
                                                                                Timer ti = new Timer();
                                                                                final int[] ii = new int[1];
                                                                                ti.schedule(new TimerTask() {
                                                                                    @Override
                                                                                    public void run() {
                                                                                        if (ii[0] >= 4)
                                                                                            cancel();
                                                                                        ii[0]++;
                                                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                                            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE), new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).setUsage(AudioAttributes.USAGE_ALARM).build());
                                                                                        } else
                                                                                            v.vibrate(80);
                                                                                    }
                                                                                }, 0, 200);
                                                                            }
                                                                        } catch (Exception e) {
                                                                            handler.post(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    if (toast != null)
                                                                                        toast.cancel();
                                                                                    toast = Toast.makeText(context, "There was an error when deleting a message. Please notify the developer :)", Toast.LENGTH_SHORT);
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
                                            } catch (Exception e) {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (toast != null)
                                                            toast.cancel();
                                                        toast = Toast.makeText(context, "There was an error when receiving a message. Please notify the developer :)", Toast.LENGTH_SHORT);
                                                        toast.show();
                                                    }
                                                });
                                            }
                                        } else {
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (toast != null)
                                                        toast.cancel();
                                                    toast = Toast.makeText(context, "There was an error, make sure that you are connected to the internet", Toast.LENGTH_SHORT);
                                                    toast.show();
                                                }
                                            });
                                        }
                                    }
                                });
                                t.start();
                            }
                        });
                    }
                });
                t.start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                new AlertDialog.Builder(context).setTitle("You got fucked!").setMessage("Or Nevet blocked you!!!!!! lololololol").setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        ((Activity)context).finish();
                    }
                }).show();
            }
        });
    }
}
