package efestoarts.wearfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.Time;
import android.util.Log;
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

        private Paint minutesBubblePaint;
        private Paint hoursBubblePaint;
        private Paint backgroundPaint;
        private Paint minutesBubblePaintAmbientMode;
        private Paint backgroundPaintAmbientMode;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            minutesBubblePaint = new Paint();
            Resources resources = getResources();

            minutesBubblePaint.setColor(resources.getColor(R.color.minutes_bubble_color));
            minutesBubblePaint.setAntiAlias(true);

            hoursBubblePaint = new Paint();
            hoursBubblePaint.setColor(resources.getColor(R.color.hours_bubble_color));
            hoursBubblePaint.setAntiAlias(true);

            minutesBubblePaintAmbientMode = new Paint();
            minutesBubblePaintAmbientMode.setColor(resources.getColor(R.color.minutes_bubble_color_ambient));


            backgroundPaint = new Paint();
            backgroundPaint.setColor(resources.getColor(R.color.background_color));

            backgroundPaintAmbientMode = new Paint();
            backgroundPaintAmbientMode.setColor(resources.getColor(R.color.background_color_ambient));

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
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            time.setToNow();

            float hoursBubbleRadius = ((bounds.width() / 24) * time.hour) / 2;
            float minutesBubbleRadius = ((bounds.width() / 60) * time.minute) / 2;

            if (!isInAmbientMode())
            {
                canvas.drawPaint(backgroundPaint);
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), hoursBubbleRadius, hoursBubblePaint);
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), minutesBubbleRadius, minutesBubblePaint);
            }
            else
            {
                canvas.drawPaint(backgroundPaintAmbientMode);
                canvas.drawCircle(bounds.centerX(), bounds.centerY(), minutesBubbleRadius, minutesBubblePaintAmbientMode);

            }

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
        }
    }
}
