package efestoarts.wearfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.Time;
import android.view.SurfaceHolder;

import java.util.concurrent.TimeUnit;

public class BubblesFaceService extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new BubblesEngine();
    }

    class BubblesEngine extends CanvasWatchFaceService.Engine {

        private final static int MSG_UPDATE_TIME = 0;
        private final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

        Time time;
        /* receiver to update the time zone */
        final BroadcastReceiver timeZoneUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                time.clear(intent.getStringExtra("time-zone"));
                time.setToNow();
            }
        };

        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (isVisible() && !isInAmbientMode()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        Paint minutesBubblePaint;
        Paint hoursBubblePaint;
        Paint backgroundPaint;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            minutesBubblePaint = new Paint();
            minutesBubblePaint.setColor(getResources().getColor(R.color.minutes_bubble_color));
            minutesBubblePaint.setStrokeCap(Paint.Cap.ROUND);
            minutesBubblePaint.setAntiAlias(true);

            hoursBubblePaint = new Paint();
            hoursBubblePaint.setColor(getResources().getColor(R.color.hours_bubble_color));
            hoursBubblePaint.setStrokeCap(Paint.Cap.ROUND);
            hoursBubblePaint.setAntiAlias(true);


            backgroundPaint = new Paint();
            backgroundPaint.setColor(getResources().getColor(R.color.background_color));

            time = new Time();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            /* get device features (burn-in, low-bit ambient) */
        }

        @Override
        public void onTimeTick() {
            //Questo di default scatta una volta al minuto
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            /* the wearable switched between modes */
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            time.setToNow();

            float hoursBubbleRadius = ((bounds.width() / 24) * time.hour) / 2;
            float minutesBubbleRadius = ((bounds.width() / 60) * time.minute) / 2;

            canvas.drawPaint(backgroundPaint);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), hoursBubbleRadius, hoursBubblePaint);
            canvas.drawCircle(bounds.centerX(), bounds.centerY(), minutesBubbleRadius, minutesBubblePaint);



            //Qua ci deve essere una somma matematica dei colori calcolando l'area minima
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
        }
    }
}
