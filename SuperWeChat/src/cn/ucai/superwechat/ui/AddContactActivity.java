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
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.utils.IUserModel;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.utils.OnCompleteListener;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;
import cn.ucai.superwechat.utils.UserModel;

public class AddContactActivity extends BaseActivity {
    @BindView(R.id.title_bar)
    EaseTitleBar mTitleBar;
    @BindView(R.id.no_user)
    TextView mNoUser;
    private EditText editText;
    private String toAddUsername;
    private ProgressDialog progressDialog;
    IUserModel mModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.em_activity_add_contact);
        ButterKnife.bind(this);

        editText = (EditText) findViewById(R.id.edit_note);
        String strUserName = getResources().getString(R.string.user_name);
        editText.setHint(strUserName);
        mModel = new UserModel();
        mTitleBar.setLeftLayoutClickListener(listener);
        progressDialog = new ProgressDialog(AddContactActivity.this);

    }
    View.OnClickListener listener  = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MFGT.finish(AddContactActivity.this);
        }
    };


    /**
     * search contact
     *
     */
    @OnClick(R.id.search)
    public void searchContact() {
        showDialog();
        final String name = editText.getText().toString();
        toAddUsername = name;
        if (TextUtils.isEmpty(name)) {
            new EaseAlertDialog(this, R.string.Please_enter_a_username).show();
            return;
        }
        mModel.loadUserInfo(AddContactActivity.this, toAddUsername, new OnCompleteListener<String>() {
            @Override
            public void onSuccess(String s) {
                boolean success = false;
                if (s != null) {
                    Result result = ResultUtils.getResultFromJson(s, User.class);
                    if (result != null && result.isRetMsg()) {
                        User user = (User) result.getRetData();
                        Log.i("main", "AddContactActivity,user=" + user);
                        success = true;
                        progressDialog.dismiss();
                        MFGT.gotoContact(AddContactActivity.this,user);
                    }
                }
                showUserInfo(success);
            }

            @Override
            public void onError(String error) {
                showUserInfo(false);
                progressDialog.dismiss();
            }
        });

    }

    private void showDialog() {
        progressDialog.setMessage(getString(R.string.searching));
        progressDialog.show();
    }

    private void showUserInfo(boolean success) {
        mNoUser.setVisibility(success ? View.GONE : View.VISIBLE);
    }


}
