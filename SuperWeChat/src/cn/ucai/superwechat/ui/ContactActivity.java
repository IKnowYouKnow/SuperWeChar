package cn.ucai.superwechat.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.easeui.widget.EaseTitleBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.I;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.db.InviteMessgeDao;
import cn.ucai.superwechat.domain.InviteMessage;
import cn.ucai.superwechat.db.IUserModel;
import cn.ucai.superwechat.utils.MFGT;
import cn.ucai.superwechat.db.OnCompleteListener;
import cn.ucai.superwechat.utils.Result;
import cn.ucai.superwechat.utils.ResultUtils;
import cn.ucai.superwechat.db.UserModel;

/**
 * Created by Administrator on 2017/4/5 0005.
 */

public class ContactActivity extends BaseActivity {
    @BindView(R.id.ivAvatar)
    ImageView mIvAvatar;
    @BindView(R.id.tvContactNick)
    TextView mTvNick;
    @BindView(R.id.tvContactUsername)
    TextView mTvUsername;
    @BindView(R.id.send_msg)
    Button mSendMsg;
    User user;
    @BindView(R.id.add_friend)
    Button mAddFriend;
    @BindView(R.id.chat_video)
    Button mChatVideo;
    @BindView(R.id.titleBar)
    EaseTitleBar mTitleBar;
    InviteMessage msg;
    IUserModel mModel;
    boolean isFriend = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info);
        ButterKnife.bind(this);
        mModel = new UserModel();
        initData();
    }

    private void initData() {
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        Log.d("mingYue", "initData: " + user);
        if (user == null) {
            msg = (InviteMessage) getIntent().getSerializableExtra(I.User.NICK);
            if (msg != null) {
                user = new User();
                Log.d("mingYue", "initData: " + msg);
                user.setMUserName(msg.getFrom());
                user.setMUserNick(msg.getNick());
                user.setAvatar(msg.getUserAvatar());
            }
        }
        if (user == null) {
            String username = getIntent().getStringExtra(I.User.TABLE_NAME);
            if (username!=null)
                user = new User(username);
        }
        if (user == null) {
            MFGT.finish(ContactActivity.this);
        }else {
            initView();
            syncUserInfo();
        }
        mTitleBar.setLeftLayoutClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MFGT.finish(ContactActivity.this);
        }
    };

    private void initView() {
        isFriend = SuperWeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName());
        Log.i("main", "ContactActivity,isFriend=" + isFriend);
        if (isFriend) {
            SuperWeChatHelper.getInstance().saveAppContact(user);
            Log.i("main", "ContactActivity,initView,user=" + user);
        }
        EaseUserUtils.setAppUserAvatar(ContactActivity.this, user.getMUserName(), mIvAvatar);
        mTvUsername.setText("微信号：" + user.getMUserName());
        EaseUserUtils.setAppUserNick(user, mTvNick);

        showButton(isFriend);

    }

    private void showButton(boolean isFriend) {
        mSendMsg.setVisibility(isFriend ? View.VISIBLE : View.GONE);
        mChatVideo.setVisibility(isFriend ? View.VISIBLE : View.GONE);
        mAddFriend.setVisibility(isFriend ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.add_friend)
    public void addFriend() {
        boolean isConfirm = true;
        if (isConfirm) {
            MFGT.gotoFriendProfile(ContactActivity.this, user.getMUserNick());
        } else {
            // 直接添加，给好友发送消息
        }
    }

    private void syncUserInfo() {
        mModel.loadUserInfo(ContactActivity.this, user.getMUserName(),
                new OnCompleteListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        if (s != null) {
                            Result result = ResultUtils.getResultFromJson(s, User.class);
                            if (result != null && result.isRetMsg()) {
                                User u = (User) result.getRetData();
                                if (u != null) {
                                    if (msg != null) {
                                        ContentValues values = new ContentValues();
                                        values.put(InviteMessgeDao.COLUMN_USER_NICK, u.getMUserNick());
                                        values.put(InviteMessgeDao.COLUMN_USER_AVATAR, u.getAvatar());
                                        InviteMessgeDao dao = new InviteMessgeDao(ContactActivity.this);
                                        dao.updateMessage(msg.getId(), values);
                                    } else if (isFriend) {
                                        Log.i("main", "ContactActivity,syncUserInfo,u=" + u);
                                        SuperWeChatHelper.getInstance().getUserProfileManager().updateUserInfo(u);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(String error) {

                    }
                });
    }


    /**
     * make a video call
     */
    @OnClick(R.id.chat_video)
    public void startVideoCall() {
        if (!EMClient.getInstance().isConnected())
            Toast.makeText(ContactActivity.this, R.string.not_connect_to_server, Toast.LENGTH_SHORT).show();
        else {
            startActivity(new Intent(ContactActivity.this, VideoCallActivity.class).putExtra("username", user.getMUserName())
                    .putExtra("isComingCall", false));
            // videoCallBtn.setEnabled(false);
//            inputMenu.hideExtendMenuContainer();
        }
    }
    @OnClick(R.id.send_msg)
    public void sendMsg() {
        MFGT.gotoChatActivity(ContactActivity.this, user);
    }
}
