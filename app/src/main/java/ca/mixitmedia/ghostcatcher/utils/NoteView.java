package ca.mixitmedia.ghostcatcher.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ca.mixitmedia.ghostcatcher.app.R;

/**
 * TODO: document your custom view class.
 */
public class NoteView extends RelativeLayout {

    public enum NoteType {QUESTION, INFO, ALERT, TODO, DONE,}

    public NoteView(Context context, String[] titles, Tuple<String, NoteType>[][] items) {
        super(context);
        int idCounter = 0;
        for (int i = 0; i < titles.length; i++) {

            TextView title = new TextView(context);
            title.setText(titles[i]);
            title.setId(++idCounter);
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (idCounter == 1) lp.addRule(ALIGN_PARENT_LEFT, TRUE);
            else lp.addRule(BELOW, idCounter - 1);
            title.setLayoutParams(lp);
            addView(title);

            for (int j = 0; j < items[i].length; j++) {

                ImageView bullet = new ImageView(context);
                switch (items[i][j].second) {
                    case QUESTION:
                        bullet.setImageResource(R.drawable.q);
                        break;
                    case INFO:
                        bullet.setImageResource(R.drawable.info);
                        break;
                    case ALERT:
                        bullet.setImageResource(R.drawable.alert);
                        break;
                    case TODO:
                        bullet.setImageResource(R.drawable.bullet);
                        break;
                    case DONE:
                        bullet.setImageResource(R.drawable.check);
                        break;
                }
                bullet.setId(101 + idCounter);
                LayoutParams blp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                blp.addRule(BELOW, idCounter);
                bullet.setLayoutParams(lp);
                addView(bullet);

                TextView content = new TextView(context);
                content.setText(items[i][j].first);
                content.setId(++idCounter);
                LayoutParams nlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                nlp.addRule(BELOW, idCounter - 1);
                nlp.addRule(RIGHT_OF, idCounter + 100);
                content.setLayoutParams(lp);
                addView(content);
            }

        }
    }
}
