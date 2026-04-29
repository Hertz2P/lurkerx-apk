package com.lurkerx.gpsjob.smsjob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("SmsReceiver", "OnReceive");
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    SmsDatabaseHelper dbHelper = new SmsDatabaseHelper(context);

                    for (Object pdu : pdus) {
                        String format = bundle.getString("format");
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
                        String address = sms.getOriginatingAddress();
                        String body = sms.getMessageBody();
                        long date = sms.getTimestampMillis();
                        String type = "received";

                        dbHelper.insertSms(address, body, date, type);

                        Log.d("SmsReceiver", "Saved SMS: " + address + " - " + body);
                    }
                }
            }
        }
    }
}
