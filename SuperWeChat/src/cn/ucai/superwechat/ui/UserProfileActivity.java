package cn.ucai.superwechat.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.CommonUtils;
import cn.ucai.superwechat.db.IUserModel;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.db.UserModel;

public class UserProfileActivity extends BaseActivity {

    private static final int REQUESTCODE_PICK = 1;
    private static final int REQUESTCODE_CUTTING = 2;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.ivAvatar)
    ImageView mIvAvatar;
    @BindView(R.id.tvNick)
    TextView mTvNick;
    @BindView(R.id.tvUsername)
    TextView mTvUsername;
    private ProgressDialog dialog;
    IUserModel mModel;
    String username;
    UpdateUserNickReceiver mReceiver;
    UpdateUserAvatarReceiver mAvatarReceiver;
    User user;
    String avatarName;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_persnol_info);
        ButterKnife.bind(this);
        initData();
        mModel = new UserModel();
        initListener();
    }

    private void initListener() {
        mReceiver = new UpdateUserNickReceiver();
        IntentFilter filter = new IntentFilter(I.REQUEST_UPDATE_USER_NICK);
        registerReceiver(mReceiver, filter);

        mAvatarReceiver = new UpdateUserAvatarReceiver();
        IntentFilter intentFilter = new IntentFilter(I.REQUEST_UPDATE_AVATAR);
        registerReceiver(mAvatarReceiver, intentFilter);
    }

    private void initData() {
        mTvTitle.setText(R.string.title_user_profile);
        user = SuperWeChatHelper.getInstance().getUserProfileManager().getCurrentAppUser();
        username = user.getMUserName();
        mTvUsername.setText(username);
        EaseUserUtils.setAppUserAvatar(UserProfileActivity.this, username, mIvAvatar);
        EaseUserUtils.setAppUserNick(username, mTvNick);
    }

    public void asyncFetchUserInfo(String username) {
        SuperWeChatHelper.getInstance().getUserProfileManager().asyncGetUserInfo(username, new EMValueCallBack<EaseUser>() {

            @Override
            public void onSuccess(EaseUser user) {
                if (user != null) {
                    SuperWeChatHelper.getInstance().saveContact(user);
                    if (isFinishing()) {
                        return;
                    }
                    mTvNick.setText(user.getNick());
                    if (!TextUtils.isEmpty(user.getAvatar())) {
                        Glide.with(UserProfileActivity.this).load(user.getAvatar()).placeholder(R.drawable.default_hd_avatar).into(mIvAvatar);
                    } else {
                        Glide.with(UserProfileActivity.this).load(R.drawable.default_hd_avatar).into(mIvAvatar);
                    }
                }
            }

            @Override
            public void onError(int error, String errorMsg) {
                CommonUtils.showLongToast(errorMsg);
            }
        });
    }


    private void uploadHeadPhoto() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(UserProfileActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, REQUESTCODE_PICK);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }


    private void updateRemoteNick(final String nickName) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_nick), getString(R.string.dl_waiting));
        SuperWeChatHelper.getInstance().getUserProfileManager()
                .updateCurrentUserNickName(nickName);
        if (UserProfileActivity.this.isFinishing()) {
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);
    }

    /**
     * save the picture data
     *
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle extras = picdata.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(), photo);
            mIvAvatar.setImageDrawable(drawable);
            uploadAppUserAvatar(saveBitmapFile(photo));
        }

    }

    private void uploadAppUserAvatar(File file) {
        dialog = ProgressDialog.show(this, getString(R.string.dl_update_photo), getString(R.string.dl_waiting));
        SuperWeChatHelper.getInstance().getUserProfileManager().uploadUserAvatar(file);
        dialog.show();
    }

    public static String getAvatarPath(Context context, String path) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File folder = new File(dir, path);
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder.getAbsolutePath();
    }

    private String getAvatarName(){
        avatarName = user.getMUserName()+System.currentTimeMillis();
        return avatarName;
    }

    private File saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            String imagePath = getAvatarPath(UserProfileActivity.this, I.AVATAR_TYPE) + "/" + getAvatarName() + ".jpg";
            File file = new File(imagePath);
                try {
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
                    bos.flush();
                    bos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
            } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    @OnClick(R.id.iv_back)
    public void backArea() {
        MFGT.finish(UserProfileActivity.this);
    }

    @OnClick({R.id.layout_avatar, R.id.layout_nick, R.id.layout_username})
    public void changeInfo(View view) {
        switch (view.getId()) {
            case R.id.layout_avatar:
                uploadHeadPhoto();
                break;
            case R.id.layout_nick:
                final EditText editText = new EditText(this);
                editText.setText(mTvNick.getText().toString());
                editText.setSelectAllOnFocus(true);
                new Builder(this).setTitle(R.string.setting_nickname).setIcon(android.R.drawable.ic_dialog_info).setView(editText)
                        .setPositiveButton(R.string.dl_ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String nickString = editText.getText().toString();
                                if (TextUtils.isEmpty(nickString)) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.toast_nick_not_isnull), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (nickString.equals(user.getMUserNick())) {
                                    Toast.makeText(UserProfileActivity.this, "昵称未修改", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                updateRemoteNick(nickString);
                            }
                        }).setNegativeButton(R.string.dl_cancel, null).show();
                break;
            case R.id.layout_username:
                Toast.makeText(UserProfileActivity.this, "微信号不能修改", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void updateNick(boolean b) {
        if (!b) {
            Toast.makeText(UserProfileActivity.this,
                    getString(R.string.toast_updatenick_fail), Toast.LENGTH_SHORT)
                    .show();
            dialog.dismiss();
        } else {
            dialog.dismiss();
            Toast.makeText(UserProfileActivity.this,
                    getString(R.string.toast_updatenick_success), Toast.LENGTH_SHORT)
                    .show();
            user = SuperWeChatHelper.getInstance().getUserProfileManager().getCurrentAppUser();
            Log.i("main", "UserProfileActivity,updateNick,nick=" + user.getMUserNick());
            mTvNick.setText(user.getMUserNick());
        }
    }

    class UpdateUserNickReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(I.User.USER_NAME, false);
            updateNick(success);
        }

    }

    class UpdateUserAvatarReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean success = intent.getBooleanExtra(I.Avatar.UPDATE_TIME, false);
            updateAvatar(success);
        }

    }

    private void updateAvatar(boolean success) {
        dialog.dismiss();
        if (success) {
            Toast.makeText(this, R.string.toast_updatephoto_success
                    , Toast.LENGTH_SHORT).show();
            user = SuperWeChatHelper.getInstance().getUserProfileManager().getCurrentAppUser();
            EaseUserUtils.setAppUserAvatar(UserProfileActivity.this, user.getMUserName(), mIvAvatar);
        } else {
            CommonUtils.showShortToast(R.string.toast_updatephoto_fail);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        if (mAvatarReceiver != null) {
            unregisterReceiver(mAvatarReceiver);
        }
    }
}
