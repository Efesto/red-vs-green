package efestoarts.redvsgreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RedVsGreenFaceService extends CanvasWatchFaceService {


    @Override
    public Engine onCreateEngine() {
        return new RedVsGreenEngine();
    }

    class RedVsGreenEngine extends CanvasWatchFaceService.Engine {

        GregorianCalendar time;

        private Paint minutesBubblePaint;
        private Paint hoursBubblePaint;
        private Paint backgroundPaint;
        private Paint batteryBubblePaint;
        private Paint digitPaint;

        private int digitSize = 70;
        private int maxDigitWidth = 120;
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

            timeZoneUpdateReceiver = new TimeZoneUpdateReceiver();

            //This because, for undetectable reasons, the statusBar gravity goes to the deep right of the screen on LG Watch R
            setWatchFaceStyle(
                    new WatchFaceStyle.Builder(RedVsGreenFaceService.this)
                            .setStatusBarGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP)
                            .build());

            time = new GregorianCalendar();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            int hoursColor = R.color.hours_bubble;
            int minutesColor = R.color.minutes_bubble;
            int batteryColor = R.color.battery_bubble;

            if (inAmbientMode) {
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
            setTimeToNow();

            canvas.drawPaint(backgroundPaint);
            drawHoursBubble(canvas, bounds);
            drawMinutesBubble(canvas, bounds);
            drawBatteryChargeBubble(canvas, bounds);
        }

        private void drawBatteryChargeBubble(Canvas canvas, Rect bounds) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
            int batteryCapacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            int bubbleRadius = bounds.width() / 10;
            int centerY = bounds.height() - bubbleRadius;
            int centerX = bounds.centerX();

            float bubbleAngle = 3.6f * batteryCapacity;

            RectF oval = new RectF(centerX - bubbleRadius, centerY - bubbleRadius, centerX + bubbleRadius, centerY + bubbleRadius);
            canvas.drawArc(oval, 270, bubbleAngle, true, batteryBubblePaint);
            Drawable batteryDrawable = getDrawable(R.drawable.ic_battery_std_white_18dp);
            batteryDrawable.setBounds(centerX - batteryDrawable.getMinimumWidth() / 2,
                    centerY - batteryDrawable.getMinimumHeight() / 2,
                    centerX + batteryDrawable.getMinimumWidth() / 2,
                    centerY + batteryDrawable.getMinimumHeight() / 2);

            batteryDrawable.draw(canvas);

        }

        private void drawHoursBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxDigitWidth / 2;

            float maxBubbleRadius = bounds.width() - maxDigitWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 23) * time.get(Calendar.HOUR_OF_DAY)) + minBubbleRadius;
            float bubbleCenterX = maxDigitWidth / 2;

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, hoursBubblePaint);

            drawDigitInBubble(canvas, time.get(Calendar.HOUR_OF_DAY), bubbleCenterX, bounds.centerY(), digitPaint);
        }

        private void drawMinutesBubble(Canvas canvas, Rect bounds) {
            int minBubbleRadius = maxDigitWidth / 2;

            float maxBubbleRadius = bounds.width() - maxDigitWidth - 2 * minBubbleRadius;
            float bubbleRadius = ((maxBubbleRadius / 59) * time.get(Calendar.MINUTE)) + minBubbleRadius;
            float bubbleCenterX = bounds.width() - (maxDigitWidth / 2);

            canvas.drawCircle(bubbleCenterX, bounds.centerY(), bubbleRadius, minutesBubblePaint);

            drawDigitInBubble(canvas, time.get(Calendar.MINUTE), bubbleCenterX, bounds.centerY(), digitPaint);
        }

        private void drawDigitInBubble(Canvas canvas, int time, float bubbleCenterX, float bubbleCenterY, Paint paint) {
            canvas.drawText(String.format("%02d%n", time), bubbleCenterX + 10, bubbleCenterY + (paint.getTextSize() / 2) - 10, paint);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                timeZoneUpdateReceiver.register();

                // Update time zone in case it changed while we weren't visible.
                time.setTimeZone(TimeZone.getDefault());
                setTimeToNow();
            } else {
                timeZoneUpdateReceiver.unregister();
            }
        }

        private TimeZoneUpdateReceiver timeZoneUpdateReceiver;

        class TimeZoneUpdateReceiver extends BroadcastReceiver {
            public boolean isRegistered = false;

            @Override
            public void onReceive(Context context, Intent intent) {
                time.setTimeZone(TimeZone.getTimeZone(intent.getStringExtra("time-zone")));
                setTimeToNow();
            }

            public void register() {
                if (!isRegistered) {
                    IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                    registerReceiver(this, filter);
                    isRegistered = true;
                }
            }

            public void unregister() {
                if (isRegistered) {
                    unregisterReceiver(this);
                    isRegistered = false;
                }
            }

        }

        private void setTimeToNow() {
            time.setTime(new Date());
        }
    }
}
