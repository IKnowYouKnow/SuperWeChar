package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.hyphenate.easeui.domain.User;
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
        mMyNick.setText(user.getMUserNick());
        String nick = getIntent().getStringExtra(I.User.NICK);
        mFriendNick.setText("我是"+nick);
    }

    @OnClick(R.id.btn_send)
    public void onClick() {

    }
}
