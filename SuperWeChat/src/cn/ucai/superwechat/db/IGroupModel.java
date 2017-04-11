package cn.ucai.superwechat.db;

import android.content.Context;

import java.io.File;

/**
 * Created by Administrator on 2017/4/10 0010.
 */

public interface IGroupModel {
    void createGroup(Context context, String hxId, String groupName, String des, String owner,
                    boolean isPublic, boolean isInvites, File file, OnCompleteListener<String> listener);

    void addGroupMembers(Context context, String members, String hxid, OnCompleteListener<String> listener);

    void removeUserFromGroup(Context context, String hxid, String username, OnCompleteListener<String> listener);
}
