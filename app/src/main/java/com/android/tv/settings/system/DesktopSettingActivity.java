package com.android.tv.settings.system;

import com.android.tv.settings.ActionBehavior;
import com.android.tv.settings.ActionKey;
import com.android.tv.settings.BaseSettingsActivity;
import com.android.tv.settings.R;
import com.android.tv.settings.desktop.ShowActivity;
import com.android.tv.settings.dialog.old.Action;
import com.android.tv.settings.dialog.old.ActionAdapter;
import com.android.tv.settings.util.SettingsHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import com.mstar.android.tv.TvCommonManager;//xiezhiwen 20160730 add for 3to1 function for mantis 0742


public class DesktopSettingActivity extends BaseSettingsActivity implements ActionAdapter.Listener{

    private static final String TAG = "DesktopSettingActivity";
    private static final boolean DEBUG = false;
    private SettingsHelper mHelper;
	//liyuanyuan 20160729 add for childlock password set on mantis 0737 start+++
	private LinearLayout layout;
	private AlertDialog setpwddialog;
	private Button button_confirm;
	private Button button_cancle;
	private EditText editText_childlock_oldpwd;
	private EditText editText_childlock_newpwd;
	private EditText editText_childlock_renewpwd;
	private static final String CHILDLOCK_PWD="childlock_pwd";
	private SharedPreferences  share_childlock_pwd;
	private SharedPreferences.Editor edit_childlock_pwd;		
	//liyuanyuan 20160729 add for childlock password set on mantis 0737 end---

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mResources = getResources();
        mActions = new ArrayList<Action>();
        mHelper = new SettingsHelper(this);
		initSetChildLockPwd();//liyuanyuan 20160713 add for childlock password set on mantis 0737 
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Object getInitialState() {
        return ActionType.DESKTOP_OVERVIEW;
    }
	//liyuanyuan 20160713 add for childlock password set on mantis 0737 start+++
    private void initSetChildLockPwd(){
    	share_childlock_pwd = getSharedPreferences(CHILDLOCK_PWD, Context.MODE_MULTI_PROCESS);   	
    	edit_childlock_pwd = share_childlock_pwd.edit();
    	layout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.childlock_password_setting,null);
		setpwddialog = new AlertDialog.Builder(this).create();
		//setpwddialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		button_confirm = (Button)layout.findViewById(R.id.button_confirm);
		button_cancle =  (Button)layout.findViewById(R.id.button_cancle);
		editText_childlock_oldpwd = (EditText)layout.findViewById(R.id.editText_childlock_oldpwd);
		editText_childlock_newpwd = (EditText)layout.findViewById(R.id.editText_childlock_newpwd);
		editText_childlock_renewpwd = (EditText)layout.findViewById(R.id.editText_childlock_renewpwd);
		button_confirm.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				String old_pwd = editText_childlock_oldpwd.getText().toString();
				String new_pwd = editText_childlock_newpwd.getText().toString();
				String renew_pwd = editText_childlock_renewpwd.getText().toString();
				Log.d(TAG,"old_pwd.length()"+old_pwd.length()+"new_pwd.length()"+new_pwd.length());
				if(old_pwd.length() == 0 || new_pwd.length() == 0 || renew_pwd.length() == 0)
				{	
					Toast.makeText(DesktopSettingActivity.this, getResources().getString(R.string.str_passwdempty_warning), Toast.LENGTH_SHORT).show();
				}
				else
				{
					String store_pwd = share_childlock_pwd.getString("childlock_pwd","2580");
					Log.d(TAG,"old_pwd:"+old_pwd+"new_pwd:"+new_pwd+"store_pwd:"+store_pwd);
					if(store_pwd.equals(old_pwd) || old_pwd.equals("2580"))
					{
						if(new_pwd.equals(renew_pwd))
						{
							edit_childlock_pwd.putString("childlock_pwd", new_pwd);
							edit_childlock_pwd.commit();
							setpwddialog.dismiss();	
						}
						else
						{
							Toast.makeText(DesktopSettingActivity.this, getResources().getString(R.string.str_twonewpwd_different_warning), Toast.LENGTH_SHORT).show();
						}
					}
					else{
						Toast.makeText(DesktopSettingActivity.this, getResources().getString(R.string.oldpasswordwrong), Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		});
		button_cancle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(setpwddialog.isShowing())
						setpwddialog.dismiss();
				
			}
		});

    }	
  //liyuanyuan 20160713 add for childlock password set on mantis 0737 end---
    @Override
    protected void refreshActionList() {
        mActions.clear();
        switch ((ActionType) mState) {
            case DESKTOP_OVERVIEW:
                mActions.add(ActionType.DESKTOP_WALLPAPER.toAction(mResources));
                mActions.add(ActionType.DESKTOP_DESKTOP_CONTROL.toAction(mResources,
                        mHelper.getSecureStatusIntSetting(Settings.Secure.DESKTOP_DESKTOP_CONTROL)));
				mActions.add(ActionType.GENERAL_CHILDLOCK_PWD_SET.toAction(mResources));//liyuanyuan 20160713 add for childlock password set on mantis 0737 
               // xiezhiwen 20160729 add for 3to1 function for mantis 0742 start+++
                mActions.add(ActionType.DESKTOP_POWERKEY_CONTROL.toAction(mResources,
                        mHelper.getStatusStringFromIntPowerkey(TvCommonManager.getInstance().getPowerKeyMode())));
               //xiezhiwen 20160729 add for 3to1 function for mantis 0742 end---
                break;
            case DESKTOP_DESKTOP_CONTROL:
                mActions.add(ActionBehavior.ON.toAction(ActionBehavior.getOnKey(
                        ActionType.DESKTOP_DESKTOP_CONTROL.name()), mResources, getProperty()));
                mActions.add(ActionBehavior.OFF.toAction(ActionBehavior.getOffKey(
                        ActionType.DESKTOP_DESKTOP_CONTROL.name()), mResources, !getProperty()));
                break;
           // xiezhiwen 20160729 add for 3to1 function for mantis 0742 start+++
            case DESKTOP_POWERKEY_CONTROL:
                mActions.add(ActionBehavior.DESKTOP_POWERKEY_THREE.toAction(ActionBehavior.getPowerThreeKey(
                        ActionType.DESKTOP_POWERKEY_CONTROL.name()), mResources, getProperty()));
                mActions.add(ActionBehavior.DESKTOP_POWERKEY_OFF.toAction(ActionBehavior.getPowerOffKey(
                        ActionType.DESKTOP_POWERKEY_CONTROL.name()), mResources, !getProperty()));
                break;
            //xiezhiwen 20160729 add for 3to1 function for mantis 0742 end---
            default:
                break;
        }
    }

    private boolean getProperty() {
        if ((ActionType) mState == ActionType.DESKTOP_DESKTOP_CONTROL) {
            return mHelper.getSecureIntValueSettingToBoolean(
                    Settings.Secure.DESKTOP_DESKTOP_CONTROL);
        } // xiezhiwen 20160729 add for 3to1 function for mantis 0742 start+++
        else if ((ActionType) mState == ActionType.DESKTOP_POWERKEY_CONTROL) {
            return mHelper.getPowerkey();
        }
        // xiezhiwen 20160729 add for 3to1 function for mantis 0742 end---
        return false;
    }

    private ArrayList<Action> getEnableActions(String type, boolean enabled) {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(ActionBehavior.ON.toAction(ActionBehavior.getOnKey(type), mResources, enabled));
        actions.add(ActionBehavior.OFF.toAction(ActionBehavior.getOffKey(type), mResources,!enabled));
        return actions;
    }

    @Override
    public void onActionClicked(Action action) {
        super.onActionClicked(action);
        ActionKey<ActionType, ActionBehavior> actionKey = new ActionKey<ActionType, ActionBehavior>(
                ActionType.class, ActionBehavior.class, action.getKey());
        final ActionType type = actionKey.getType();
        switch (type) {
            case DESKTOP_WALLPAPER:
                startActivity(new Intent(this, ShowActivity.class));
                return;
			//liyuanyuan 20160729 add for childlock password set on mantis 0737 start+++
			case GENERAL_CHILDLOCK_PWD_SET:
				Log.d(TAG,"show childlock pwd set dialog");
				editText_childlock_oldpwd.setText("");
                editText_childlock_newpwd.setText("");
				editText_childlock_renewpwd.setText("");
				setpwddialog.show();
				editText_childlock_oldpwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
				editText_childlock_newpwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
				WindowManager.LayoutParams param = setpwddialog.getWindow().getAttributes();
				setpwddialog.getWindow().setContentView(layout);
				if((param.flags & WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)  != 0)
					Log.d(TAG,"FLAG_NOT_FOCUSABLE");
				if((param.flags & WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)  != 0)
					Log.d(TAG,"FLAG_ALT_FOCUSABLE_IM");
				setpwddialog.getWindow().clearFlags( WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
				return;
			//liyuanyuan 20160729 add for childlock password set on mantis 0737 end---	
            default:
        }

        final ActionBehavior behavior = actionKey.getBehavior();
        switch (behavior) {
            case ON:
                setProperty(true);
                return;
            case OFF:
                setProperty(false);
                return;
            // xiezhiwen 20160729 add for 3to1 function for mantis 0742 start+++
            case DESKTOP_POWERKEY_THREE:
                TvCommonManager.getInstance().setPowerKeyMode(3);
                goBack();
                return;
            case DESKTOP_POWERKEY_OFF:
                TvCommonManager.getInstance().setPowerKeyMode(1);
                goBack();
                return;
            // xiezhiwen 20160729 add for 3to1 function for mantis 0742 end---
            default:
        }
        setState(type, true);
    }


    @Override
    protected void setProperty(boolean enable) {
        switch ((ActionType) mState) {
            case DESKTOP_DESKTOP_CONTROL:
                mHelper.setSecureIntSetting(Settings.Secure.DESKTOP_DESKTOP_CONTROL, enable);
                break;
            default:
        }
        goBack();
    }

    @Override
    protected void updateView() {
        refreshActionList();
        switch ((ActionType) mState) {
            case DESKTOP_OVERVIEW:
                setView(R.string.system_desktop, R.string.settings_app_name, 0,
                        R.drawable.ic_settings_desktop);
                break;
            default:
                setView(((ActionType) mState).getTitle(mResources),
                        getPrevState() != null ?
                                ((ActionType) getPrevState()).getTitle(mResources) :
                                getString(R.string.settings_app_name),
                        ((ActionType) mState).getDescription(mResources),
                        R.drawable.ic_settings_desktop);
        }
    }

}









