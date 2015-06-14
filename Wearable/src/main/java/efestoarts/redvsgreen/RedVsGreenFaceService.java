package efestoarts.redvsgreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.*;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.TimeZone;

public class RedVsGreenFaceService extends CanvasWatchFaceService {


    @Override
    public Engine onCreateEngine() {
        return new RedVsGreenEngine();
    }

    class RedVsGreenEngine extends CanvasWatchFaceService.Engine {

        Time time;
        /* receiver to update the time zone */
        final BroadcastReceiver timeZoneUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                time.clear(intent.getStringExtra("time-zone"));
                time.setToNow();
            }
        };

        private Paint minutesBubblePaint;
        private Paint hoursBubblePaint;
        private Paint backgroundPaint;
        private Paint batteryBubblePaint;
        private Paint digitPaint;

        private int digitSize = 70;
        private int maxDigitWidth = 120;
        private boolean timeZoneUpdateReceiverIsRegistered = false;
        private Paint batteryDigitPaint;

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

            backgroundPaint = new Paint();
            backgroundPaint.setColor(resources.getColor(R.color.background));

            batteryBubblePaint = new Paint();
            batteryBubblePaint.setColor(getResources().getColor(R.color.battery_bubble));
            batteryBubblePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            batteryBubblePaint.setAntiAlias(true);

            digitPaint = new Paint();
            digitPaint.setColor(resources.getColor(R.color.digit));
            digitPaint.setTextSize(digitSize);
            digitPaint.setTextAlign(Paint.Align.CENTER);
            digitPaint.setAntiAlias(true);

            batteryDigitPaint = new Paint(digitPaint);
            batteryDigitPaint.setTextSize(30);


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

            int hoursColor = R.color.hours_bubble;
            int minutesColor = R.color.minutes_bubble;
            int batteryColor = R.color.battery_bubble;

            if (inAmbientMode)
            {
                hoursColor = R.color.hours_bubble_ambient;
                minutesColor = R.color.minutes_bubble_ambient;
                batteryColor = R.color.battery_bubble_ambient;
            }

            hoursBubblePaint.setColor(getResources().getColor(hoursColor));
            minutesBubblePaint.setColor(getResources().getColor(minutesColor));
            batteryBubblePaint.setColor(getResources().getColor(batteryColor));

            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            time.setToNow();

            canvas.drawPaint(backgroundPaint);
            drawHoursBubble(canvas, bounds);
            drawMinutesBubble(canvas, bounds);
            drawChargeBubble(canvas, bounds);
        }

        private void drawChargeBubble(Canvas canvas, Rect bounds)
        {
            //TODO: ci vuole qualcosa per far capire che è la batteria
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            int batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            int bubbleRadius = bounds.width() / 8;
            int alphaMin = 50;
            int alphaValue = Math.round(((batteryCapacity / 100f)) * (255 - alphaMin)) + alphaMin;

            Log.d("DEBUG!!", "AlphaValue: " + alphaValue);

            batteryBubblePaint.setAlpha(alphaValue);

            int centerY = bounds.height() - bubbleRadius;
            int centerX = bounds.centerX();

            canvas.drawCircle(centerX, centerY, bubbleRadius, batteryBubblePaint);

            canvas.drawText(String.format("%02d%n", batteryCapacity), centerX + 5, centerY + (batteryDigitPaint.getTextSize() / 2) - 5, batteryDigitPaint);
        }

        private void drawHoursBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxDigitWidth / 2;

            float maxBubbleRadius = bounds.width() - maxDigitWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 23) * time.hour) + minBubbleRadius;
            float bubbleCenterX = maxDigitWidth / 2;

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, hoursBubblePaint);

            drawDigitInBubble(canvas, time.hour, bubbleCenterX, bounds.centerY(), digitPaint);
        }

        private void drawMinutesBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxDigitWidth / 2;

            float maxBubbleRadius = bounds.width() - maxDigitWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 59) * time.minute) + minBubbleRadius;
            float bubbleCenterX = bounds.width() - (maxDigitWidth / 2);

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, minutesBubblePaint);

            drawDigitInBubble(canvas, time.minute, bubbleCenterX, bounds.centerY(), digitPaint);
        }

        private void drawDigitInBubble(Canvas canvas, int time, float bubbleCenterX, float bubbleCenterY, Paint paint) {
            canvas.drawText(String.format("%02d%n", time), bubbleCenterX + 10, bubbleCenterY + (paint.getTextSize() / 2) - 10, paint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible && !timeZoneUpdateReceiverIsRegistered) {
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                registerReceiver(timeZoneUpdateReceiver, filter);

                // Update time zone in case it changed while we weren't visible.
                time.clear(TimeZone.getDefault().getID());
                time.setToNow();
            } else if (timeZoneUpdateReceiverIsRegistered) {
                unregisterReceiver(timeZoneUpdateReceiver);
            }
        }
    }
}
