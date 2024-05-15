package or.nevet.cloudMess;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OReplyMess extends LinearLayout {

    Mess mess = null;

    public OReplyMess(Context context, Mess mess) {
        super(context);
        this.mess = mess;
        setOnLongClickListener((OnLongClickListener)context);
        String message = mess.getText().toString();
        String name = message.substring(0, message.indexOf(":\n\n"));
        String text = message.substring(message.indexOf("\n\n")+"\n\n".length(), message.lastIndexOf("\n\n"));
        String time = message.substring(message.lastIndexOf("\n\ntime: ")+"\n\ntime: ".length());
        setOrientation(VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(HORIZONTAL);
        linearLayout.setLayoutParams(layoutParams);
        linearLayout.setWeightSum(3);

        TextView nameView = new TextView(context);
        nameView.setText(name);
        nameView.setBackgroundColor(Color.WHITE);
        nameView.setTextColor(Color.BLACK);
        nameView.setGravity(Gravity.CENTER);
        nameView.setPadding(10, 10, 10, 10);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        LayoutParams layoutParams2 = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams2.weight = 1;
        nameView.setLayoutParams(layoutParams2);
        linearLayout.addView(nameView);
        addView(linearLayout);

        TextView textView = new TextView(context);
        textView.setMaxLines(3);
        textView.setText(text);
        textView.setBackgroundColor(Color.YELLOW);
        textView.setTextColor(Color.BLACK);
        textView.setPadding(10, 10, 10, 10);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setClickable(true);
        textView.setOnLongClickListener((OnLongClickListener)context);
        textView.setLinksClickable(true);
        LayoutParams layoutParams1 = new LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.weight = 2;
        textView.setLayoutParams(layoutParams1);
        linearLayout.addView(textView);

        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        Rect rectText = new Rect();
        Rect rectName = new Rect();
        textView.getPaint().getTextBounds(textView.getText().toString(), 0, textView.getText().length(), rectText);
        nameView.getPaint().getTextBounds(nameView.getText().toString(), 0, nameView.getText().length(), rectName);

        if (rectText.height() > rectName.height()) {
            layoutParams3.weight = 1;
            nameView.setLayoutParams(layoutParams3);
        }
        else {
            layoutParams3.weight = 2;
            textView.setLayoutParams(layoutParams3);
        }

        TextView timeView = new TextView(context);
        timeView.setText(time);
        timeView.setBackgroundColor(Color.YELLOW);
        timeView.setTextColor(Color.BLACK);
        timeView.setPadding(10, 10, 10, 10);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        LayoutParams layoutParams4 = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeView.setLayoutParams(layoutParams4);
        addView(timeView);
    }

    public OReplyMess(Context context, String time) {
        super(context);
        setOrientation(VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
        TextView textView = new TextView(context);
        textView.setText("You don't have this message");
        textView.setPadding(10, 10, 10, 10);
        textView.setBackgroundColor(Color.GRAY);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(layoutParams1);
        addView(textView);

        TextView timeView = new TextView(context);
        timeView.setText(time);
        timeView.setBackgroundColor(Color.GRAY);
        timeView.setTextColor(Color.BLACK);
        timeView.setPadding(10, 10, 10, 10);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        LayoutParams layoutParams3 = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timeView.setLayoutParams(layoutParams3);
        addView(timeView);
    }

}
