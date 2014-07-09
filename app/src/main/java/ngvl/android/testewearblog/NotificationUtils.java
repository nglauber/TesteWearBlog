package ngvl.android.testewearblog;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.RemoteInput;
import java.io.IOException;

public class NotificationUtils {

    private static final int NOTIFICATION_ID = 1;

    private static NotificationCompat.Builder newNotification(Context ctx, String title, String text) {

        return new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.drawable.ic_like)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
    }

    private static void dispatchNotification(Context ctx, Notification notification, int id) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctx);
        notificationManager.notify(id, notification);
    }

    private static Bitmap getPhoto(Context ctx){
        Bitmap photo = null;
        try {
            photo = BitmapFactory.decodeStream(ctx.getAssets().open("my_photo.jpg"));
        } catch (IOException ioex){}

        return photo;
    }

    public static void simpleNotification(Context ctx) {
        Intent viewIntent = new Intent(ctx, MainActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(ctx, 0, viewIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                newNotification(ctx, "Simples", "Notificação simples")
                        .setContentIntent(viewPendingIntent);

        dispatchNotification(ctx, notificationBuilder.build(), NOTIFICATION_ID);
    }

    public static void notificacaoComBigView(Context ctx) {
        Intent viewIntent = new Intent(ctx, MainActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(ctx, 0, viewIntent, 0);

        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText("Texto mais longo que aparecerá do relógio e do aparelho e que podemos visualizar inclusive fazendo o scroll. Isso demonstra um pouco do poder das notificações");

        NotificationCompat.Builder notificationBuilder =
                newNotification(ctx, "BigView", null)
                        .setLargeIcon(getPhoto(ctx))
                        .setContentIntent(viewPendingIntent)
                        .setStyle(bigStyle);
        dispatchNotification(ctx, notificationBuilder.build(), NOTIFICATION_ID);
    }

    public static void notificationWithAction(Context ctx) {
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        Uri geoUri = Uri.parse("geo:0,0?q=" + Uri.encode("Av.Caxangá"));
        mapIntent.setData(geoUri);
        PendingIntent mapPendingIntent =
                PendingIntent.getActivity(ctx, 0, mapIntent, 0);

        NotificationCompat.Builder notificationBuilder =
                newNotification(ctx, "Ação Customizada", "Abrir mapa na: Av. Caxangá")
                        .setContentIntent(mapPendingIntent)
                        .addAction(R.drawable.ic_map, "Abrir mapa", mapPendingIntent);
        dispatchNotification(ctx, notificationBuilder.build(), NOTIFICATION_ID);
    }

    public static void notificationWithReply(Context ctx) {
        RemoteInput remoteInput = new RemoteInput.Builder(DetalheActivity.EXTRA_VOICE_REPLY)
                .setLabel("Diga a resposta")
                .build();

        Intent replyIntent = new Intent(ctx, DetalheActivity.class);
        PendingIntent replyPendingIntent =
                PendingIntent.getActivity(ctx, 0, replyIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_reply,
                        "Responder", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.WearableExtender nwe =
                new NotificationCompat.WearableExtender();
        Notification notification =
                newNotification(ctx, "Com resposta", "Passe a página para responder")
                        .extend(nwe.addAction(action))
                        .build();
        dispatchNotification(ctx, notification, NOTIFICATION_ID);
    }

    public static void notificacaoWithPages(Context ctx) {
        Intent viewIntent = new Intent(ctx, MainActivity.class);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(ctx, 0, viewIntent, 0);

        NotificationCompat.Builder nb =
                newNotification(ctx, "Com páginas", "Essa é a primeira página")
                        .setContentIntent(viewPendingIntent);

        NotificationCompat.BigTextStyle pagesStyle =
                new NotificationCompat.BigTextStyle()
                        .setBigContentTitle("Segunda página")
                        .bigText("Um texto qualquer que você queira colocar na segunda página");

        Notification secondPageNotification =
                new NotificationCompat.Builder(ctx)
                        .setStyle(pagesStyle)
                        .build();

        Notification twoPageNotification =
                new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification)
                        .extend(nb)
                        .build();

        dispatchNotification(ctx, twoPageNotification, NOTIFICATION_ID);
    }

    public static void groupedNotifications(Context ctx) {
        final String GROUP_KEY_EMAILS = "group_key_emails";

        Notification notif =
                newNotification(ctx, "Novo email de Glauber", "Wearables")
                        .setGroup(GROUP_KEY_EMAILS)
                        .build();

        dispatchNotification(ctx, notif, NOTIFICATION_ID + 1);

        Notification notif2 =
                newNotification(ctx, "Novo email de Johnny", "Android")
                        .setGroup(GROUP_KEY_EMAILS)
                        .build();

        dispatchNotification(ctx, notif2, NOTIFICATION_ID + 2);

        Notification notif3 =
                newNotification(ctx, "Novo email de Nelson", "Smartwatches")
                        .setGroup(GROUP_KEY_EMAILS)
                        .build();

        dispatchNotification(ctx, notif3, NOTIFICATION_ID + 3);


        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setBackground(getPhoto(ctx));

        Notification summaryNotification =
                newNotification(ctx, "3 novos emails", null)
                        .setLargeIcon(getPhoto(ctx))
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine("Glauber   Wearables")
                                .addLine("Johnny   Android")
                                .addLine("Nelson    Smartwatches")
                                .setBigContentTitle("3 novos emails")
                                .setSummaryText("nglaubervasc@gmail.com"))
                        .extend(wearableExtender)
                        .setGroup(GROUP_KEY_EMAILS)
                        .setGroupSummary(true)
                        .build();

        dispatchNotification(ctx, summaryNotification, NOTIFICATION_ID);
    }
}
