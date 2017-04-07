package cn.ucai.superwechat.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.widget.EaseAlertDialog;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.MFGT;

/**
 * Created by Administrator on 2017/4/5 0005.
 */

public class FriendProfileActivity extends BaseActivity {
    @BindView(R.id.title_bar)
    EaseTitleBar mTitleBar;
    @BindView(R.id.my_nick)
    EditText mMyNick;
    @BindView(R.id.friend_nick)
    EditText mFriendNick;
    User user;
    String friendNick;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.friend_profile_activity);
        ButterKnife.bind(this);
        user = new User();
        initData();
        initView();
    }

    private void initView() {
        mTitleBar.setLeftLayoutClickListener(listener);

    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MFGT.finish(FriendProfileActivity.this);
        }
    };

    private void initData() {
        user = SuperWeChatHelper.getInstance().getUserProfileManager().getCurrentAppUser();
        if (user != null) {
            mMyNick.setText("我是" + user.getMUserNick());
            friendNick = getIntent().getStringExtra(I.User.NICK);
            mFriendNick.setHint(friendNick);
        }
    }

    /**
     * add contact
     */
    @OnClick(R.id.btn_send)
    public void addContact() {
        if (EMClient.getInstance().getCurrentUser().equals(friendNick)) {
            new EaseAlertDialog(this, R.string.not_add_myself).show();
            return;
        }

        if (SuperWeChatHelper.getInstance().getAppContactList().containsKey(friendNick)) {
            //let the user know the contact already in your contact list
            if (EMClient.getInstance().contactManager().getBlackListUsernames().contains(friendNick)) {
                new EaseAlertDialog(this, R.string.user_already_in_contactlist).show();
                return;
            }
            new EaseAlertDialog(this, R.string.This_user_is_already_your_friend).show();
            return;
        }

        progressDialog = new ProgressDialog(this);
        String stri = getResources().getString(R.string.Is_sending_a_request);
        progressDialog.setMessage(stri);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        new Thread(new Runnable() {
            public void run() {
                try {
                    //demo use a hardcode reason here, you need let user to input if you like
                    String s = mMyNick.getText().toString();
                    EMClient.getInstance().contactManager().addContact(friendNick, s);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s1 = getResources().getString(R.string.send_successful);
                            Toast.makeText(getApplicationContext(), s1, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final Exception e) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressDialog.dismiss();
                            String s2 = getResources().getString(R.string.Request_add_buddy_failure);
                            Toast.makeText(getApplicationContext(), s2 + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).start();
    }


}
