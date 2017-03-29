package cn.ucai.superwechat.utils;

import android.content.Context;

/**
 * Created by Administrator on 2017/3/29 0029.
 */

public interface IUserModel {
    void register(Context context, String username, String nick, String password,
                  OnCompleteListener<String> listener);

    void unregister(Context context, String username, OnCompleteListener<String> listener);

    void login(Context context, String username, String password, OnCompleteListener<String> listener);

    void loadUserInfo(Context context, String username, OnCompleteListener<String> listener);
}
