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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager.EMGroupOptions;
import com.hyphenate.chat.EMGroupManager.EMGroupStyle;
import com.hyphenate.easeui.domain.Group;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.exceptions.HyphenateException;

import cn.ucai.superwechat.R;
import cn.ucai.superwechat.db.GroupModel;
import cn.ucai.superwechat.db.IGroupModel;
import cn.ucai.superwechat.db.OnCompleteListener;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;

public class NewGroupActivity extends BaseActivity {
    private EditText groupNameEditText;
    private ProgressDialog progressDialog;
    private EditText introductionEditText;
    private CheckBox publibCheckBox;
    private CheckBox memberCheckbox;
    private TextView secondTextView;
    IGroupModel mModel;
    String groupName;

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
                    .putExtra("groupName", groupName), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            //new group
            showDialog();
            createEMGroup(data);
        }

    }

    private void createEMGroup(Intent data) {
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
            createAppGroup(group);

        } catch (final HyphenateException e) {
                    createSuccess(false);
                    Log.i("main","NewGroupActivity,EM=" + e);
                }
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

    private void createAppGroup(EMGroup emGroup) {
        if (emGroup != null) {

            mModel.createGroup(NewGroupActivity.this, emGroup.getGroupId(), emGroup.getGroupName(), emGroup.getDescription(),
                    emGroup.getOwner(), emGroup.isPublic(),
                    emGroup.isMemberAllowToInvite(), null, new OnCompleteListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            boolean success = false;
                            if (s != null) {
                                Result result = ResultUtils.getResultFromJson(s, Group.class);
                                if (result != null && result.isRetMsg()) {
                                    Group group = (Group) result.getRetData();
                                    if (group != null) {
                                        success = true;
                                    }
                                }
                            }
                            createSuccess(success);
                        }

                        @Override
                        public void onError(String error) {
                            createSuccess(false);
                            Log.i("main","NewGroupActivity,App=" + error);
                        }
                    });
        }
    }

    public void back(View view) {
        finish();
    }
}
