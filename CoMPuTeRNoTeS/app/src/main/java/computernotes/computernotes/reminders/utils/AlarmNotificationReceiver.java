package computernotes.computernotes.reminders.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AlarmNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getBooleanExtra("whatsapp",false)) {
            String number = intent.getStringExtra("number");
            Log.d("number", number);

            String numberWhatsapp = "";

            if(number.startsWith("00")) numberWhatsapp = number.substring(2,number.length());
            else if(number.startsWith("0")) numberWhatsapp = "49" + number.substring(1,number.length());
            else if(number.startsWith("+")) numberWhatsapp = number.substring(1,number.length());
            else numberWhatsapp = number;

            Log.d("CDA", "alarm Recieved");
            Log.d("numberWhatsapp", numberWhatsapp);


            String message =  intent.getStringExtra("message");



            String textWhatsapp = message.replace(" ", "%20");

            String link =  "https://api.whatsapp.com/send?phone="+numberWhatsapp+"&text="+textWhatsapp+"&source=&data=%20";

            System.out.println(link);
            System.out.println(numberWhatsapp);

            Uri uriUrl = Uri.parse(link);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            context.startActivity(launchBrowser);
        }
        else{
            Toast.makeText(context, "alarm Recieved", Toast.LENGTH_LONG).show();
            Uri uriUrl = Uri.parse("https://api.whatsapp.com/send?phone=491636238088&text=my%20name%20is%20_writeyournamehere_%20.%20nice%20to%20meet%20you%20chagai%20&source=&data=");
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            context.startActivity(launchBrowser);
            Log.d("CDA", "alarm Recieved");
        }
    }
}
