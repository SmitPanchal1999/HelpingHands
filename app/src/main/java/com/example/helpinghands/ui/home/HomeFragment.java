package com.example.helpinghands.ui.home;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.example.helpinghands.MainActivity;
import com.example.helpinghands.R;
import com.example.helpinghands.User;
import com.example.helpinghands.emergencycontacts;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Button button;
    private Button make_call;
    private Button test;
    private ToggleButton sosalert;
    int cntflag = 0;
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("001", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 122: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("request_status","accepted");
                    sosalert.setChecked(false);
                    Toast.makeText(getActivity(),"You can trigger emergency alert now.",Toast.LENGTH_SHORT).show();
                } else {
                    Log.v("request_status","rejected");
                    sosalert.setChecked(false);
                }
                return;
            }

            case 120: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v("request_status","accepted");
                    make_call.callOnClick();
                } else {
                    Log.v("request_status","rejected");
                }
                return;
            }
        }
    }

    public void sendsms(String number,String message){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, message, null, null);
    }

    public void alert_contact(User user) {
        int flag = 0;
        String message = "Emergency Alert\n"+user.getFName()+" "+user.getLName()+" has just triggered an emergency call.\n"+user.getFName()+" has listed you as an emergency contact.";
        Log.v("Message",message);
        String lastloc = "";
        if(user.getLatitude() != "" && user.getLongitude() != ""){
            flag = 1;
            //https://www.google.com/maps/@22.683591,72.880947,18z
            lastloc = "Last Known Location is in this area :\n";
            lastloc =lastloc + "https://www.google.com/maps/@"+user.getLatitude()+","+user.getLongitude()+",18z";
            Log.v("location",lastloc);
        }
        if(user.getEcon1() != 0){
            Log.v("status","sending message to "+user.getEcon1());
            sendsms(user.getEcon1().toString(),message);
            Log.v("status","Message sent to "+user.getEcon1());
            if(flag == 1){sendsms(user.getEcon1().toString(),lastloc);}
        }
        if(user.getEcon2() != 0){
            Log.v("status","sending message to "+user.getEcon2());
            sendsms(user.getEcon2().toString(),message);
            Log.v("status","Message sent to "+user.getEcon2());
            if(flag == 1){sendsms(user.getEcon2().toString(),lastloc);}
        }
        if(user.getEcon3() != 0){
            Log.v("status","sending message to "+user.getEcon3());
            sendsms(user.getEcon3().toString(),message);
            Log.v("status","Message sent to "+user.getEcon3());
            if(flag == 1){sendsms(user.getEcon3().toString(),lastloc);}
        }
        Toast.makeText(getActivity(),"Emergency contacts have been notified via text message",Toast.LENGTH_SHORT).show();
    }

    public void makecall(User user){
        Intent callIntent =new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:100"));
        startActivity(callIntent);
    }


    public void initiate_emergency(User user){
        final User myuser = user;
        Thread sos = new Thread(){
            public void run(){
                    Log.v("SOSThread","Thread Started");
                    new CountDownTimer(5000,1000){
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if(myuser.getSosflag() == 0){this.cancel();Log.v("SOSThread","Thread Ended");}
                        }
                        @Override
                        public void onFinish() {
                            if(myuser.getLatitude() != "" && myuser.getLongitude() != ""){
                                String lastloc = "Last Known Location is in this area :\n";
                                lastloc = lastloc + "https://www.google.com/maps/@"+myuser.getLatitude()+","+myuser.getLongitude()+",18z";
                                Log.v("SOSThread","Sending Message = "+lastloc);
                                if(myuser.getEcon1()!=0){sendsms(myuser.getEcon1().toString(),lastloc);}
                                if(myuser.getEcon2()!=0){sendsms(myuser.getEcon2().toString(),lastloc);}
                                if(myuser.getEcon3()!=0){sendsms(myuser.getEcon3().toString(),lastloc);}
                            }
                            Log.v("SOSThread","SOS flag = "+myuser.getSosflag());
                            if(myuser.getSosflag() == 1){this.start();}
                        }
                    }.start();
            }
        };
        sos.run();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final User user = new User(getActivity());
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final LayoutInflater myinflater = inflater;
        final ViewGroup mycontainer = container;
        createNotificationChannel();
        button = root.findViewById(R.id.button4);
        make_call = root.findViewById(R.id.button7);
        test = root.findViewById(R.id.button8);
        sosalert = root.findViewById(R.id.toggleButton);
        if(user.getSosflag() == 1){sosalert.setChecked(true);}
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in =new Intent(getContext(), MainActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, in, 0);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),"001")
                        .setSmallIcon(R.drawable.ic_helpinghands)
                        .setContentTitle("Incoming Request")
                        .setContentText("Someone within your area needs help")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
                NotificationManagerCompat nm = NotificationManagerCompat.from(getContext());
                nm.notify(0,builder.build());
            }
        });

        make_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
                    Log.v("status", "asking for permission");
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE},120);
                }
                else{
                    makecall(user);
                }
            }
        });
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MapFragment.generateEmergency();
            }
        });
        sosalert.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                        Log.v("status", "asking for permission");
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS},122);
                    }
                    else{
                        if(user.getEcon1() == 0 && user.getEcon2() == 0 && user.getEcon3() == 0){
                            sosalert.setChecked(false);
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setCancelable(true);
                            builder.setTitle("Emergency Contacts is not set");
                            builder.setMessage("You haven't set any Emergency contacts yet ! Do you want to add Emergency contacts ?");
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(getContext(), emergencycontacts.class);
                                    startActivity(intent);
                                    ((Activity)getContext()).overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                                    return;
                                }
                            });
                            builder.show();
                        }
                        else{
                            final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                                    .setTitle("Sending Emergency Message").setMessage("Waiting...").setCancelable(false);
                            dialog.setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            cntflag = 1;
                                            dialog.dismiss();
                                        }
                                    });
                            final AlertDialog alert = dialog.create();
                            alert.show();
                            new CountDownTimer(5000, 1000) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    Log.v("status",(int)((millisUntilFinished+1000)/1000)+"");
                                    alert.setMessage("Alerting Emergency Contact via text message in "+(int)((millisUntilFinished+1000)/1000)+" sec...");
                                    if(cntflag == 1){
                                        cntflag=0;
                                        this.cancel();
                                        sosalert.setChecked(false);
                                        Toast.makeText(getActivity(),"SOS Alert is stopped",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onFinish() {
                                    alert.dismiss();
                                    cntflag = 0;
                                    alert_contact(user);
                                    user.setSosflag(1);
                                    initiate_emergency(user);
                                }
                            }.start();
                        }
                    }
                }
                else{
                    Log.v("status","unchecked");
                    Toast.makeText(getActivity(),"SOS Alert is stopped",Toast.LENGTH_SHORT).show();
                    user.setSosflag(0);
                }
            }
        });
        return root;
    }
}