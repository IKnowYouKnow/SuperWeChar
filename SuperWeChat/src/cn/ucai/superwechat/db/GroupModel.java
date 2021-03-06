package cn.ucai.superwechat.db;

import android.content.Context;

import java.io.File;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.utils.OkHttpUtils;

/**
 * Created by Administrator on 2017/4/10 0010.
 */

public class GroupModel implements IGroupModel {
    @Override
    public void createGroup(Context context, String hxId, String groupName, String des,
                           String owner, boolean isPublic, boolean isInvites, File file,
                           OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_CREATE_GROUP)
                .addParam(I.Group.HX_ID, hxId)
                .addParam(I.Group.NAME, groupName)
                .addParam(I.Group.DESCRIPTION,des)
                .addParam(I.Group.OWNER,owner)
                .addParam(I.Group.IS_PUBLIC,String.valueOf(isPublic))
                .addParam(I.Group.ALLOW_INVITES,String.valueOf(isInvites))
                .addFile2(file)
                .targetClass(String.class)
                .post()
                .execute(listener);
    }

    @Override
    public void addGroupMembers(Context context, String members, String hxid, OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_ADD_GROUP_MEMBERS)
                .addParam(I.Member.USER_NAME,members)
                .addParam(I.Member.GROUP_HX_ID,hxid)
                .targetClass(String.class)
                .execute(listener);
    }

    @Override
    public void removeUserFromGroup(Context context, String groupId, String username, OnCompleteListener<String> listener) {
        OkHttpUtils<String> utils = new OkHttpUtils<>(context);
        utils.setRequestUrl(I.REQUEST_DELETE_GROUP_MEMBER)
                .addParam(I.Group.GROUP_ID,groupId)
                .addParam(I.Group.NAME,username)
                .targetClass(String.class)
                .execute(listener);
    }
}
