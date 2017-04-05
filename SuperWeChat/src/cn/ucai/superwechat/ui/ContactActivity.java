package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_info);
        ButterKnife.bind(this);
        user = (User) getIntent().getSerializableExtra(I.User.USER_NAME);
        Log.i("main", "ContactActivity,user=" + user);
        initView();
        mTitleBar.setLeftLayoutClickListener(listener);
    }
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MFGT.finish(ContactActivity.this);
        }
    };

    private void initView() {
        boolean isFriend = SuperWeChatHelper.getInstance().getAppContactList().containsKey(user.getMUserName());
        Log.i("main","ContactActivity,isFriend="+isFriend);
        if (isFriend) {
            SuperWeChatHelper.getInstance().saveAppContact(user);
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
    public void addFriend(){
        MFGT.gotoFriendProfile(ContactActivity.this,user.getMUserNick());
    }
}
