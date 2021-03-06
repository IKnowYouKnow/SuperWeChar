package cn.ucai.superwechat.parse;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.SuperWeChatHelper.DataSyncListener;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.db.IUserModel;
import cn.ucai.superwechat.db.OnCompleteListener;
import cn.ucai.superwechat.utils.PreferenceManager;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;
import cn.ucai.superwechat.db.UserModel;

public class UserProfileManager {
    private static final String TAG = UserProfileManager.class.getSimpleName();

    /**
     * application context
     */
    protected Context appContext = null;

    /**
     * init flag: test if the sdk has been inited before, we don't need to init
     * again
     */
    private boolean sdkInited = false;

    /**
     * HuanXin sync contact nick and avatar listener
     */
    private List<DataSyncListener> syncContactInfosListeners;

    private boolean isSyncingContactInfosWithServer = false;

    private EaseUser currentUser;
    private User currentAppUser;
    IUserModel mModel;

    public UserProfileManager() {
    }

    public synchronized boolean init(Context context) {
        if (sdkInited) {
            return true;
        }
        ParseManager.getInstance().onInit(context);
        syncContactInfosListeners = new ArrayList<DataSyncListener>();
        sdkInited = true;
        appContext = context;
        currentAppUser = new User();
        mModel = new UserModel();
        return true;
    }

    public void addSyncContactInfoListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (!syncContactInfosListeners.contains(listener)) {
            syncContactInfosListeners.add(listener);
        }
    }

    public void removeSyncContactInfoListener(DataSyncListener listener) {
        if (listener == null) {
            return;
        }
        if (syncContactInfosListeners.contains(listener)) {
            syncContactInfosListeners.remove(listener);
        }
    }

    public void asyncFetchContactInfosFromServer(List<String> usernames, final EMValueCallBack<List<EaseUser>> callback) {
        if (isSyncingContactInfosWithServer) {
            return;
        }
        isSyncingContactInfosWithServer = true;
        ParseManager.getInstance().getContactInfos(usernames, new EMValueCallBack<List<EaseUser>>() {

            @Override
            public void onSuccess(List<EaseUser> value) {
                isSyncingContactInfosWithServer = false;
                // in case that logout already before server returns,we should
                // return immediately
                if (!SuperWeChatHelper.getInstance().isLoggedIn()) {
                    return;
                }
                if (callback != null) {
                    callback.onSuccess(value);
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                isSyncingContactInfosWithServer = false;
                if (callback != null) {
                    callback.onError(error, errorMsg);
                }
            }

        });

    }

    public void notifyContactInfosSyncListener(boolean success) {
        for (DataSyncListener listener : syncContactInfosListeners) {
            listener.onSyncComplete(success);
        }
    }

    public boolean isSyncingContactInfoWithServer() {
        return isSyncingContactInfosWithServer;
    }

    public synchronized void reset() {
        isSyncingContactInfosWithServer = false;
        currentUser = null;
        currentAppUser = null;
        PreferenceManager.getInstance().removeCurrentUserInfo();
    }

    public synchronized EaseUser getCurrentUserInfo() {
        if (currentUser == null) {
            String username = EMClient.getInstance().getCurrentUser();
            currentUser = new EaseUser(username);
            String nick = getCurrentUserNick();
            currentUser.setNick((nick != null) ? nick : username);
            currentUser.setAvatar(getCurrentUserAvatar());
        }
        return currentUser;
    }

    public synchronized User getCurrentAppUser() {
        Log.e(TAG, "getCurrentAppUser: currentAppUser++++++++" + currentAppUser);
        if (currentAppUser == null || currentAppUser.getMUserName() == null) {
            String username = EMClient.getInstance().getCurrentUser();
            Log.e(TAG, "getCurrentAppUser: ++++++++" + username);
            currentAppUser = new User(username);
            String nick = getCurrentUserNick();
            currentAppUser.setMUserNick((nick != null) ? nick : username);
            currentAppUser.setAvatar(getCurrentUserAvatar());
        }
        return currentAppUser;
    }

    public boolean updateCurrentUserNickName(final String nickname) {
        Log.i("main", "updateCurrentUserNickName,nickname=" + nickname);
        mModel.updateNick(appContext, EMClient.getInstance().getCurrentUser(), nickname, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                boolean success = false;
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result != null && result.getRetData() != null) {
                        User user = (User) result.getRetData();
                        if (user != null) {
                            success = true;
                            SuperWeChatHelper.getInstance().getUserProfileManager().setCurrentAppUserNick(user.getMUserNick());
                            setCurrentUserNick(user.getMUserNick());
                            Log.i("main", "updateCurrentUserNickName,user=" + user);

                        }

                    } else {
                        CommonUtils.showShortToast(R.string.toast_updatenick_fail);
                    }
                }
                appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_USER_NICK)
                        .putExtra(I.User.USER_NAME, success));
            }

            @Override
            public void onError(String error) {
                CommonUtils.showShortToast(R.string.toast_updatenick_fail);
                appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_USER_NICK)
                        .putExtra(I.User.USER_NAME, false));
            }
        });
        return false;
    }

    public void uploadUserAvatar(File file) {
        mModel.updateAvatar(appContext, EMClient.getInstance().getCurrentUser(), file,
                new OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        boolean success = false;

                        if (s != null) {
                            Result result = ResultUtils.getResultFromJson(s, User.class);
                            if (result != null && result.isRetMsg()) {
                                User user = (User) result.getRetData();
                                if (user != null) {
                                    success = true;
                                    setCurrentAppUserAvatar(user.getAvatar());
                                    SuperWeChatHelper.getInstance().getUserProfileManager()
                                            .setCurrentAppUserAvatar(user.getAvatar());
                                }
                            }
                        }
                        appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_AVATAR)
                                .putExtra(I.Avatar.UPDATE_TIME, success));
                    }

                    @Override
                    public void onError(String error) {
                        appContext.sendBroadcast(new Intent(I.REQUEST_UPDATE_AVATAR)
                                .putExtra(I.Avatar.UPDATE_TIME, false));
                    }
                });
//        String avatarUrl = ParseManager.getInstance().uploadParseAvatar(data);
//        if (avatarUrl != null) {
//            setCurrentUserAvatar(avatarUrl);
//        }
//        return avatarUrl;
    }

    public void updateUserInfo(User user) {
        setCurrentAppUserNick(user.getMUserNick());
        setCurrentAppUserAvatar(user.getAvatar());
        SuperWeChatHelper.getInstance().saveAppContact(user);
    }
    public void asyncGetCurrentAppUserInfo() {
        Log.i("main","UserProfileManager,asyncGetCurrentAppUserInfo,username="+EMClient.getInstance().getCurrentUser());
        mModel.loadUserInfo(appContext, EMClient.getInstance().getCurrentUser(), new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result != null && result.isRetMsg()) {
                        Log.e(TAG, "onSuccess: result=" + result);
                        User user = (User) result.getRetData();
                        if (user != null) {
                            updateUserInfo(user);
                            currentAppUser.cloneByOther(user);
                            Log.i("main", "UserProfileManager,asyncGetCurrentAppUserInfo,user=" + user);
                        }
                    }
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void asyncGetCurrentUserInfo() {
        ParseManager.getInstance().asyncGetCurrentUserInfo(new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser value) {
                if (value != null) {
                    setCurrentUserNick(value.getNick());
                    setCurrentUserAvatar(value.getAvatar());
                }
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });

    }

    public void asyncGetUserInfo(final String username, final EMValueCallBack<EaseUser> callback) {
        ParseManager.getInstance().asyncGetUserInfo(username, callback);
    }

    private void setCurrentAppUserNick(String nick) {
        PreferenceManager.getInstance().setCurrentUserNick(nick);
        getCurrentAppUser().setMUserNick(nick);
    }

    private void setCurrentAppUserAvatar(String avatar) {
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
        getCurrentAppUser().setAvatar(avatar);
    }

    private void setCurrentUserNick(String nickname) {
        getCurrentUserInfo().setNick(nickname);
        PreferenceManager.getInstance().setCurrentUserNick(nickname);
    }

    private void setCurrentUserAvatar(String avatar) {
        getCurrentUserInfo().setAvatar(avatar);
        PreferenceManager.getInstance().setCurrentUserAvatar(avatar);
    }

    private String getCurrentUserNick() {
        Log.e(TAG, "getCurrentUserNick: ++++PreferenceManager.getInstance().getCurrentUserNick()++++" + PreferenceManager.getInstance().getCurrentUserNick());
        return PreferenceManager.getInstance().getCurrentUserNick();
    }

    private String getCurrentUserAvatar() {
        return PreferenceManager.getInstance().getCurrentUserAvatar();
    }

}
