package cn.ucai.superwechat.utils;

import android.app.Activity;
import android.content.Intent;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.ui.AddContactActivity;
import cn.ucai.superwechat.ui.GuideActivity;
import cn.ucai.superwechat.ui.LoginActivity;
import cn.ucai.superwechat.ui.MainActivity;
import cn.ucai.superwechat.ui.RegisterActivity;
import cn.ucai.superwechat.ui.SettingsActivity;
import cn.ucai.superwechat.ui.UserProfileActivity;


/**
 * Created by Administrator on 2017/3/16 0016.
 */

public class MFGT {
    public static void startActivity(Activity activity,Class cla) {
        activity.startActivity(new Intent(activity,cla));
        activity.overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }

    public static void finish(Activity activity) {
        activity.finish();
        activity.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

    public static void startActivity(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }

    public static void gotoMain(Activity activity) {
        startActivity(activity, MainActivity.class);
    }


    public static void startActivityForResult(Activity activity, Intent intent, int requestCode) {
        activity.startActivityForResult(intent,requestCode);
        activity.overridePendingTransition(R.anim.push_right_in,R.anim.push_right_out);
    }

    public static void gotoGuide(Activity activity) {
        startActivity(activity,GuideActivity.class);
    }

    public static void gotoLoginActivity(Activity activity) {
        startActivity(activity, new Intent(activity,LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public static void gotoRegisterActivity(Activity activity) {
        startActivity(activity, RegisterActivity.class);
    }

    public static void gotoSettingActivity(Activity activity) {
        startActivity(activity,SettingsActivity.class);
    }

    public static void gotoUserProfileActivity(Activity activity) {
        startActivity(activity,UserProfileActivity.class);
    }

    public static void gotoAddContactActivity(Activity activity) {
        startActivity(activity,AddContactActivity.class);
    }
}
