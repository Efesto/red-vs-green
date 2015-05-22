package efestoarts.wearfaces.wear;

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

public class RedVsGreenFaceService extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        return new RedVsGreenEngine();
    }

    class RedVsGreenEngine extends CanvasWatchFaceService.Engine {

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
        private Paint minutesBubblePaintAmbient;
        private Paint hoursBubblePaintAmbient;
        private Paint digitPaint;

        private int digitSize = 70;
        private int maxDigitWidth = 120;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            Resources resources = getResources();

            minutesBubblePaint = new Paint();
            minutesBubblePaint.setColor(resources.getColor(R.color.minutes_bubble));
            minutesBubblePaint.setAntiAlias(true);
            minutesBubblePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

            hoursBubblePaint = new Paint();
            hoursBubblePaint.setColor(resources.getColor(R.color.hours_bubble));
            hoursBubblePaint.setAntiAlias(true);
            hoursBubblePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

            minutesBubblePaintAmbient = new Paint();
            minutesBubblePaintAmbient.setColor(resources.getColor(R.color.minutes_bubble_ambient));
            minutesBubblePaintAmbient.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            minutesBubblePaintAmbient.setAntiAlias(true);

            hoursBubblePaintAmbient = new Paint();
            hoursBubblePaintAmbient.setColor(resources.getColor(R.color.hours_bubble_ambient));
            hoursBubblePaintAmbient.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            hoursBubblePaintAmbient.setAntiAlias(true);

            backgroundPaint = new Paint();
            backgroundPaint.setColor(resources.getColor(R.color.background));

            digitPaint = new Paint();
            digitPaint.setColor(resources.getColor(R.color.digit));
            digitPaint.setTextSize(digitSize);
            digitPaint.setTextAlign(Paint.Align.CENTER);
            digitPaint.setAntiAlias(true);

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
            drawHoursBubble(canvas, bounds);
            drawMinutesBubble(canvas, bounds);
        }

        private void drawHoursBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxDigitWidth / 2;

            float maxBubbleRadius = bounds.width() - maxDigitWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 23) * time.hour) + minBubbleRadius;
            float bubbleCenterX = maxDigitWidth / 2;

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, getHoursBubblePaint());

            drawDigitInBubble(canvas, time.hour, bubbleCenterX, bounds.centerY(), digitPaint);
        }

        private void drawMinutesBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxDigitWidth / 2;

            float maxBubbleRadius = bounds.width() - maxDigitWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 59) * time.minute) + minBubbleRadius;
            float bubbleCenterX = bounds.width() - (maxDigitWidth / 2);

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, getMinutesBubblePaint());

            drawDigitInBubble(canvas, time.minute, bubbleCenterX, bounds.centerY(), digitPaint);
        }

        private void drawSecondsBubble(Canvas canvas, Rect bounds) {
            Paint secondsBubblePaint = new Paint();
            secondsBubblePaint.setColor(getResources().getColor(R.color.seconds_bubble));
            secondsBubblePaint.setAntiAlias(true);
            secondsBubblePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

            Paint secondsDigitPaint = new Paint(digitPaint);
            secondsDigitPaint.setTextSize(digitSize / 2);

            int maxSecondsDigitWidth = 60;
            int bubbleRadius = maxSecondsDigitWidth / 2;

            canvas.drawCircle(bounds.centerX(), bounds.centerY(), bubbleRadius, secondsBubblePaint);
            drawDigitInBubble(canvas, time.second, bounds.centerX(), bounds.centerY(), secondsDigitPaint);
        }

        private void drawDigitInBubble(Canvas canvas, int time, float bubbleCenterX, float bubbleCenterY, Paint paint) {
            canvas.drawText(String.format("%02d%n", time), bubbleCenterX + 10, bubbleCenterY + (paint.getTextSize() / 2) - 10, paint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            /* the watch face became visible or invisible */
        }

        private Paint getHoursBubblePaint() {
            return isInAmbientMode() ? hoursBubblePaintAmbient : hoursBubblePaint;
        }

        private Paint getMinutesBubblePaint() {
            return isInAmbientMode() ? minutesBubblePaintAmbient : minutesBubblePaint;
        }
    }
}
