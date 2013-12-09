package com.riccardopedrielli.fastlight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class WidgetProvider
        extends AppWidgetProvider
{
    private static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("LightOn", true);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setOnClickPendingIntent(R.id.WidgetButton, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds)
        {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }
}
