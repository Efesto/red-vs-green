package efestoarts.wearfaces;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Bundle;
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

//        final Handler mUpdateTimeHandler = new Handler() {
//            @Override
//            public void handleMessage(Message message) {
//                switch (message.what) {
//                    case MSG_UPDATE_TIME:
//                        invalidate();
//                        if (isVisible() && !isInAmbientMode()) {
//                            long timeMs = System.currentTimeMillis();
//                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
//                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
//                            mUpdateTimeHandler
//                                    .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
//                        }
//                        break;
//                }
//            }
//        };

        private Paint minutesBubblePaint;
        private Paint hoursBubblePaint;
        private Paint backgroundPaint;
        private Paint minutesBubblePaintAmbientMode;
        private Paint backgroundPaintAmbientMode;
        private Paint hoursBubblePaintAmbientMode;
        private Paint textPaint;
        private int textSize = 70;
        private int maxTextWidth = 100;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            Resources resources = getResources();

            minutesBubblePaint = new Paint();
            minutesBubblePaint.setColor(resources.getColor(R.color.minutes_bubble_color));
            minutesBubblePaint.setAntiAlias(true);

            hoursBubblePaint = new Paint();
            hoursBubblePaint.setColor(resources.getColor(R.color.hours_bubble_color));
            hoursBubblePaint.setAntiAlias(true);

            minutesBubblePaintAmbientMode = new Paint();
            minutesBubblePaintAmbientMode.setColor(resources.getColor(R.color.minutes_bubble_color_ambient));

            hoursBubblePaintAmbientMode = new Paint();
            hoursBubblePaintAmbientMode.setColor(resources.getColor(R.color.minutes_bubble_color_ambient));

            backgroundPaint = new Paint();
            backgroundPaint.setColor(resources.getColor(R.color.background_color));

            backgroundPaintAmbientMode = new Paint();
            backgroundPaintAmbientMode.setColor(resources.getColor(R.color.background_color_ambient));

            textPaint = new Paint();
            textPaint.setColor(resources.getColor(R.color.text_color));
            textPaint.setTextSize(textSize);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setAntiAlias(true);

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

            canvas.drawPaint(backgroundPaint);
            drawMinutesBubble(canvas, bounds);
            drawHoursBubble(canvas, bounds);
        }

        private void drawHoursBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxTextWidth / 2;

            float maxBubbleRadius = bounds.width() - maxTextWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 23) * time.hour) + minBubbleRadius;
            float bubbleCenterX = maxTextWidth / 2;

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, hoursBubblePaint);

            drawTimeInBubble(canvas, time.hour, bubbleCenterX, bounds.centerY());
        }

        private void drawMinutesBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxTextWidth / 2;

            float maxBubbleRadius = bounds.width() - maxTextWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 59) * time.minute) + minBubbleRadius;
            float bubbleCenterX = bounds.width() - (maxTextWidth / 2);

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, minutesBubblePaint);

            drawTimeInBubble(canvas, time.minute, bubbleCenterX, bounds.centerY());
        }

        private void drawTimeInBubble(Canvas canvas, int time, float bubbleCenterX, float bubbleCenterY)
        {
            canvas.drawText(String.format("%02d%n", time), bubbleCenterX + 10, bubbleCenterY + (textSize / 2) - 10, textPaint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
        }
    }
}
