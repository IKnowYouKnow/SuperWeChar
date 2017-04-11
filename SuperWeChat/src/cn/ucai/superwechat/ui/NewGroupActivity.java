/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ucai.superwechat.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.easeui.domain.Group;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.db.GroupModel;
import cn.ucai.superwechat.db.IGroupModel;
import cn.ucai.superwechat.db.OnCompleteListener;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;

import static cn.ucai.superwechat.I.REQUEST_CODE_PICK_PIC;
import static cn.ucai.superwechat.ui.UserProfileActivity.getAvatarPath;

public class NewGroupActivity extends BaseActivity {
    private EditText groupNameEditText;
    private ProgressDialog progressDialog;
    private EditText introductionEditText;
    private CheckBox publibCheckBox;
    private CheckBox memberCheckbox;
    private TextView secondTextView;
    IGroupModel mModel;
    String groupName;
    RelativeLayout layoutAvatar;
    ImageView groupAvatar;
    String avatarName;
    File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_new_group);
        mModel = new GroupModel();
        groupNameEditText = (EditText) findViewById(R.id.edit_group_name);
        introductionEditText = (EditText) findViewById(R.id.edit_group_introduction);
        publibCheckBox = (CheckBox) findViewById(R.id.cb_public);
        memberCheckbox = (CheckBox) findViewById(R.id.cb_member_inviter);
        secondTextView = (TextView) findViewById(R.id.second_desc);
        layoutAvatar = (RelativeLayout) findViewById(R.id.layoutAvatar);
        groupAvatar = (ImageView) findViewById(R.id.groupAvatar);

        setListener();

    }

    private void setListener() {
        publibCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    secondTextView.setText(R.string.join_need_owner_approval);
                } else {
                    secondTextView.setText(R.string.Open_group_members_invited);
                }
            }
        });
        layoutAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadHeadPhoto();
            }
        });
    }
    private void uploadHeadPhoto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dl_title_upload_photo);
        builder.setItems(new String[]{getString(R.string.dl_msg_take_photo), getString(R.string.dl_msg_local_upload)},
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                Toast.makeText(NewGroupActivity.this, getString(R.string.toast_no_support),
                                        Toast.LENGTH_SHORT).show();
                                break;
                            case 1:

                                Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
                                pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                                startActivityForResult(pickIntent, I.REQUEST_CODE_PICK_PIC);
                                break;
                            default:
                                break;
                        }
                    }
                });
        builder.create().show();
    }
    private void updateRemoteNick(final String nickName) {
        showDialog();
        SuperWeChatHelper.getInstance().getUserProfileManager()
                .updateCurrentUserNickName(nickName);
        if (NewGroupActivity.this.isFinishing()) {
            return;
        }
    }

    /**
     * @param v
     */
    public void save(View v) {
        groupName = groupNameEditText.getText().toString();
        if (TextUtils.isEmpty(groupName)) {
            new EaseAlertDialog(this, R.string.Group_name_cannot_be_empty).show();
        } else {
            // select from contact list
            startActivityForResult(new Intent(this, GroupPickContactsActivity.class)
                    .putExtra("groupName", groupName), I.REQUEST_CODE_PICK_CONTACT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_PICK_PIC:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            case I.REQUEST_CODE_PICK_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            case I.REQUEST_CODE_PICK_CONTACT:
                if (resultCode == RESULT_OK) {
                    //new group

                    createEMGroup(data);
                }
                break;
            default:
                break;
        }
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
        startActivityForResult(intent, I.REQUEST_CODE_PICK_CUTTING);
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
            groupAvatar.setImageDrawable(drawable);
            saveBitmapFile(photo);
        }

    }
    private void saveBitmapFile(Bitmap bitmap) {
        if (bitmap != null) {
            String imagePath = getAvatarPath(NewGroupActivity.this, I.AVATAR_TYPE) + "/" + getAvatarName() + ".jpg";
            File file = new File(imagePath);
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
                bos.flush();
                bos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            mFile = file;
        }
    }

    private String getAvatarName(){
        avatarName = I.AVATAR_TYPE_GROUP_PATH+ System.currentTimeMillis();
        return avatarName;
    }

    private void createEMGroup(final Intent data) {
        showDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String st2 = getResources().getString(R.string.Failed_to_create_groups);
                final String groupName = groupNameEditText.getText().toString().trim();
                String desc = introductionEditText.getText().toString();
                String[] members = data.getStringArrayExtra("newmembers");
                try {
                    EMGroupOptions option = new EMGroupOptions();
                    option.maxUsers = 200;
                    option.inviteNeedConfirm = true;

                    String reason = NewGroupActivity.this.getString(R.string.invite_join_group);
                    reason = EMClient.getInstance().getCurrentUser() + reason + groupName;

                    if (publibCheckBox.isChecked()) {
                        option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePublicJoinNeedApproval : EMGroupStyle.EMGroupStylePublicOpenJoin;
                    } else {
                        option.style = memberCheckbox.isChecked() ? EMGroupStyle.EMGroupStylePrivateMemberCanInvite : EMGroupStyle.EMGroupStylePrivateOnlyOwnerInvite;
                    }
                    EMGroup group = EMClient.getInstance().groupManager().createGroup(groupName, desc, members, reason, option);
                    Log.i("main", "NewGroupActivity,emGroup=" + groupName);
                    createAppGroup(group,members);

                } catch (final HyphenateException e) {
                    createSuccess(false);
                    Log.i("main","NewGroupActivity,EM=" + e);
                }
            }
        }).start();
    }

    private void createSuccess(final boolean success) {
        runOnUiThread(new Runnable() {
            public void run() {
                progressDialog.dismiss();
                if (success) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(NewGroupActivity.this, R.string.Failed_to_create_groups, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void showDialog() {
        String st1 = getResources().getString(R.string.Is_to_create_a_group_chat);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(st1);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void createAppGroup(final EMGroup emGroup, final String[] members) {
        if (emGroup != null) {
            mModel.createGroup(NewGroupActivity.this, emGroup.getGroupId(), emGroup.getGroupName(),
                    emGroup.getDescription(),emGroup.getOwner(), emGroup.isPublic(),
                    emGroup.isMemberAllowToInvite(), mFile, new OnCompleteListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            boolean success = false;
                            if (s != null) {
                                Result result = ResultUtils.getResultFromJson(s, Group.class);
                                if (result != null && result.isRetMsg()) {
                                    Group group = (Group) result.getRetData();
                                    if (group != null) {
                                        if (members.length > 0) {
                                            addGroupMembers(getMembers(members),emGroup.getGroupId());
                                        }else {
                                            success = true;
                                        }
                                    }
                                }
                            }
                            if (members.length <= 0) {
                                createSuccess(success);
                            }
                        }
                        @Override
                        public void onError(String error) {
                            createSuccess(false);
                            Log.i("main","NewGroupActivity,App=" + error);
                        }
                    });
        }
    }

    private String getMembers(String[] members) {
        StringBuffer sb = new StringBuffer();
        for (String str : members) {
            sb.append(str).append(",");
        }
        return sb.toString();
    }

    private void addGroupMembers(String members, String hxid) {
        mModel.addGroupMembers(NewGroupActivity.this, members, hxid, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                boolean success = false;
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, Group.class);
                    if (result != null && result.isRetMsg()) {
                        success = true;
                    }
                }
                createSuccess(success);
            }

            @Override
            public void onError(String error) {
                createSuccess(false);
            }
        });
    }
    public void back(View view) {
        finish();
    }
}
