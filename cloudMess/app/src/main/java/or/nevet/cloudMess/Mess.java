package or.nevet.cloudMess;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;

import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;

import androidx.appcompat.widget.AppCompatTextView;
public class Mess extends AppCompatTextView {

    public OReplyMess replyMess;

    public Mess(Context context, String text, OReplyMess replyMess, boolean myMess) {
        super(context);
        this.replyMess = replyMess;
        setOnLongClickListener((View.OnLongClickListener) context);
        setOnClickListener(new DoubleClick((DoubleClickListener)context));
        SpannableString ss1 =  new SpannableString(text);
        ss1.setSpan(new RelativeSizeSpan(0.5882352941f), text.lastIndexOf("\n\ntime: "),text.length(), 0); // set size
        ss1.setSpan(new ForegroundColorSpan(Color.GREEN), text.lastIndexOf("\n\ntime: "), text.length(), 0);// set color
        if (myMess)
            setTextColor(Color.YELLOW);
        else
            setTextColor(Color.WHITE);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f);
        setText(ss1);
        setClickable(true);
        setLinksClickable(true);
        Linkify.addLinks(this, Linkify.WEB_URLS);
    }

}
