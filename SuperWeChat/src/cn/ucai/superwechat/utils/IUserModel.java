package cn.ucai.superwechat.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by Administrator on 2017/3/29 0029.
 */

public interface IUserModel {
    void register(Context context, String username, String nick, String password,
                  OnCompleteListener<String> listener);

    void unregister(Context context, String username, OnCompleteListener<String> listener);

    void login(Context context, String username, String password, OnCompleteListener<String> listener);

    void loadUserInfo(Context context, String username, OnCompleteListener<String> listener);

    void updateAvatar(Context context, String username, File avatarFile, OnCompleteListener<String> listener);

    void updateNick(Context context, String username, String newNick, OnCompleteListener<String> listener);

    void addContact(Context context, String username, String cname, OnCompleteListener<String> listener);

    void loadAllContact(Context context,String username,OnCompleteListener<String> listener);

    void delContact(Context context, String username, String cname, OnCompleteListener<String> listener);

}
