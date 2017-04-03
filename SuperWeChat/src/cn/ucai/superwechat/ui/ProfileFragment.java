package cn.ucai.superwechat.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easemob.redpacketui.utils.RedPacketUtil;
import com.hyphenate.easeui.domain.User;
import com.hyphenate.easeui.utils.EaseUserUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.ucai.superwechat.Constant;
import cn.ucai.superwechat.R;
import cn.ucai.superwechat.SuperWeChatHelper;
import cn.ucai.superwechat.utils.MFGT;

/**
 * Created by Administrator on 2017/3/31 0031.
 */

public class ProfileFragment extends Fragment {
    @BindView(R.id.ivAvatar)
    ImageView mIvAvatar;
    @BindView(R.id.tvNick)
    TextView mTvNick;
    @BindView(R.id.tvUsername)
    TextView mTvUsername;
    User user;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.me_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initData() {
        user = SuperWeChatHelper.getInstance().getUserProfileManager().getCurrentAppUser();
        String username = user.getMUserName();
        Log.i("main", "ProfileFragment" + user.getMUserNick());
        mTvUsername.setText("微信号："+username);
        EaseUserUtils.setAppUserAvatar(getContext(),username,mIvAvatar);
        EaseUserUtils.setAppUserNick(username,mTvNick);
    }
    @OnClick(R.id.setting)
    public void showSetting(){
        MFGT.gotoSettingActivity(getActivity());
    }
    @OnClick(R.id.money)
    public void showMoney(){
        RedPacketUtil.startAppChangeActivity(getActivity());
    }
    @OnClick(R.id.avatar)
    public void showUserInfo(){
        MFGT.gotoUserProfileActivity(getActivity());
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(((MainActivity)getActivity()).isConflict){
            outState.putBoolean("isConflict", true);
        }else if(((MainActivity)getActivity()).getCurrentAccountRemoved()){
            outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
    }
}
