package com.ivan.healthcare.healthcare_android.settings;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.andexert.library.RippleView;
import com.google.gson.Gson;
import com.ivan.healthcare.healthcare_android.AppContext;
import com.ivan.healthcare.healthcare_android.Configurations;
import com.ivan.healthcare.healthcare_android.R;
import com.ivan.healthcare.healthcare_android.local.Constellation;
import com.ivan.healthcare.healthcare_android.local.User;
import com.ivan.healthcare.healthcare_android.network.AbsBaseRequest;
import com.ivan.healthcare.healthcare_android.network.BaseStringRequest;
import com.ivan.healthcare.healthcare_android.network.OkHttpUtil;
import com.ivan.healthcare.healthcare_android.network.bean.UserInfoBean;
import com.ivan.healthcare.healthcare_android.settings.dialog.ChangePwdDialog;
import com.ivan.healthcare.healthcare_android.ui.BaseActivity;
import com.ivan.healthcare.healthcare_android.util.Compat;
import com.ivan.healthcare.healthcare_android.util.DialogBuilder;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * 个人资料页面
 * Created by Ivan on 16/4/7.
 */
public class PersonalInfoActivity extends BaseActivity implements View.OnClickListener, AppBarLayout.OnOffsetChangedListener, SwipeRefreshLayout.OnRefreshListener, RippleView.OnRippleCompleteListener {

    private static final int REQUEST_GALLERY_PICK = 0x31;
    private static final int REQUEST_IMAGE_CROP = 0x32;

    private View rootView;
    private AppBarLayout mAppbar;
    private CollapsingToolbarLayout mToolbarLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CircleImageView mAvatarImageView;
    private TextView mTodayTimesTextView;
    private TextView mTotalTimesTextView;
    private TextView mAssessTextView;
    private TextView mUidTextView;
    private EditText mNameEdit;
    private TextView mSexTextView;
    private TextView mBirthTextView;
    private TextView mAgeTextView;
    private TextView mConstellationTextView;
    private EditText mEmailEdit;
    private EditText mLocationEdit;
    private EditText mIntroEdit;
    private RippleView mLogoutView;
    private RippleView mChangePwdView;
    private View clickMask;
    private RelativeLayout mLogoutRel;

    private final static int UPLOAD_MENU_ITEM_ID = 0x01;

    private View currentFocusedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        refreshContents();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAppbar.addOnOffsetChangedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAppbar.removeOnOffsetChangedListener(this);
    }

    private void initView() {

        rootView = View.inflate(this, R.layout.activity_personalinfo, null);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.personal_PtrFrameLayout);

        Toolbar mToolbar = (Toolbar) mSwipeRefreshLayout.findViewById(R.id.personal_toolbar);
        mToolbarLayout = (CollapsingToolbarLayout) mSwipeRefreshLayout.findViewById(R.id.personal_collapsing_toolbar);
        mToolbarLayout.setTitle("Ivan");
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mAppbar = (AppBarLayout) mSwipeRefreshLayout.findViewById(R.id.personal_appbar);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.red,
                R.color.green,
                R.color.blue);
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(Compat.getColor(this, R.color.default_main_color));
        mSwipeRefreshLayout.setDistanceToTriggerSync(AppContext.dp2px(35));
        mSwipeRefreshLayout.setProgressViewEndTarget(true, AppContext.dp2px(100));
        mSwipeRefreshLayout.setOnRefreshListener(this);

        View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    PersonalInfoActivity.this.onClick(v);
                }
            }
        };

        mAvatarImageView = (CircleImageView) mSwipeRefreshLayout.findViewById(R.id.personal_info_avatar_imageview);
        mAvatarImageView.setOnClickListener(this);
        mTodayTimesTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_today_times);
        mTotalTimesTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_total_times);
        mAssessTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_health_assess);
        mUidTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_uid_edit_text);
        mUidTextView.setOnFocusChangeListener(onFocusChangeListener);
        mNameEdit = (EditText) mSwipeRefreshLayout.findViewById(R.id.personal_info_name_edit_text);
        mNameEdit.setOnClickListener(this);
        mNameEdit.setOnFocusChangeListener(onFocusChangeListener);
        mSexTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_sex_tv);
        mSexTextView.setOnClickListener(this);
        mBirthTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_birth_tv);
        mBirthTextView.setOnClickListener(this);
        mAgeTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_age_tv);
        mConstellationTextView = (TextView) mSwipeRefreshLayout.findViewById(R.id.personal_info_constellation_tv);
        mEmailEdit = (EditText) mSwipeRefreshLayout.findViewById(R.id.personal_email_edit_text);
        mEmailEdit.setOnClickListener(this);
        mEmailEdit.setOnFocusChangeListener(onFocusChangeListener);
        mLocationEdit = (EditText) mSwipeRefreshLayout.findViewById(R.id.personal_info_location_edit_text);
        mLocationEdit.setOnClickListener(this);
        mLocationEdit.setOnFocusChangeListener(onFocusChangeListener);
        mIntroEdit = (EditText) mSwipeRefreshLayout.findViewById(R.id.personal_info_intro_edit_text);
        mIntroEdit.setOnClickListener(this);
        mIntroEdit.setOnFocusChangeListener(onFocusChangeListener);
        mLogoutView = (RippleView) mSwipeRefreshLayout.findViewById(R.id.personal_logout_tv);
        mLogoutView.setOnRippleCompleteListener(this);
        mChangePwdView = (RippleView) mSwipeRefreshLayout.findViewById(R.id.personal_change_pwd_tv);
        mChangePwdView.setOnRippleCompleteListener(this);

        clickMask = mSwipeRefreshLayout.findViewById(R.id.personal_click_mask);
        clickMask.setOnClickListener(this);
        clickMask.setVisibility(View.INVISIBLE);

        mLogoutRel = (RelativeLayout) mSwipeRefreshLayout.findViewById(R.id.personal_info_logout_rel);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        setContentView(rootView);
    }

    private void refreshContents() {

        mToolbarLayout.setTitle(User.userName);

        String home = getFilesDir().getAbsolutePath();
        File avatarFile = new File(home + Configurations.AVATAR_FILE_PATH);
        if (avatarFile.exists()) {
            try {
                InputStream is = new FileInputStream(avatarFile);
                mAvatarImageView.setImageBitmap(BitmapFactory.decodeStream(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mAvatarImageView.setImageResource(R.drawable.default_avatar);
        }

        mTodayTimesTextView.setText(String.valueOf(User.todayMeasureTimes));
        mTotalTimesTextView.setText(String.valueOf(User.totalMeasureTimes));
        mAssessTextView.setText(String.valueOf(User.totalMeasureAssessment));
        mUidTextView.setText(String.valueOf(User.uid));
        mNameEdit.setText(User.userName);
        mBirthTextView.setText(User.birthday);
        mSexTextView.setTag(User.sex);
        if (User.sex == User.UserSex.USER_MALE) mSexTextView.setText(getResources().getString(R.string.personal_sex_male));
        else if (User.sex == User.UserSex.USER_FEMALE) mSexTextView.setText(getResources().getString(R.string.personal_sex_female));
        else if (User.sex == User.UserSex.USER_ALIEN) mSexTextView.setText(getResources().getString(R.string.personal_sex_alien));
        mConstellationTextView.setTag(User.constellation);
        mConstellationTextView.setText(Constellation.getConstellationString(User.constellation));
        if (User.age >= 0)  mAgeTextView.setText(String.valueOf(User.age));
        mEmailEdit.setText(User.email);
        mLocationEdit.setText(User.address);
        mIntroEdit.setText(User.introduction);

        if (User.uid == -1) {
            mLogoutRel.setVisibility(View.GONE);
        } else {
            mLogoutRel.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, UPLOAD_MENU_ITEM_ID, 0, R.string.personal_save).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(AppCompatActivity.RESULT_OK);
                finish();
                break;
            case UPLOAD_MENU_ITEM_ID:
                upload();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (currentFocusedView != null) {

            clickMask.setVisibility(View.INVISIBLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocusedView.getWindowToken(), 0);
            currentFocusedView = null;

        } else if (mAvatarImageView.equals(v)) {

            Intent intent = new Intent(Intent.ACTION_PICK ,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_GALLERY_PICK);

        } else if (mSexTextView.equals(v)) {

            final String[] array = getResources().getStringArray(R.array.sex_array);
            ListView listView = new ListView(this);
            listView.setAdapter(new ArrayAdapter<>(this, R.layout.layout_simple_list_item, array));
            final Dialog dialog = new DialogBuilder(this).create()
                                        .setCustomView(listView)
                                        .setPositive(null)
                                        .show();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSexTextView.setText(array[position]);
                    if (position == 0)          mSexTextView.setTag(User.UserSex.USER_MALE);
                    else if (position == 1)     mSexTextView.setTag(User.UserSex.USER_FEMALE);
                    else if (position == 2)     mSexTextView.setTag(User.UserSex.USER_ALIEN);
                    dialog.dismiss();
                }
            });

        } else if (mBirthTextView.equals(v)) {

            final Calendar cal = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    String date = year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
                    mBirthTextView.setText(date);
                    int age = cal.get(Calendar.YEAR) - year;
                    if (cal.get(Calendar.MONTH) < monthOfYear) {
                        age -= 1;
                    } else if (cal.get(Calendar.MONTH) == monthOfYear) {
                        if (cal.get(Calendar.DAY_OF_MONTH) < dayOfMonth) {
                            age -= 1;
                        }
                    }
                    mAgeTextView.setText(String.valueOf(age));
                    Constellation.ConstellationEnum constellation = Constellation.getConstellation(monthOfYear + 1, dayOfMonth);
                    mConstellationTextView.setTag(constellation);
                    mConstellationTextView.setText(Constellation.getConstellationString(constellation));

                }
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
            Compat.fixDialogStyle(datePickerDialog);

        } else if (v instanceof EditText) {

            clickMask.setVisibility(View.VISIBLE);
            currentFocusedView = v;
        }
    }

    private void upload() {
        final ProgressDialog dialog = new DialogBuilder(this)
                .createProgress(R.string.tips, getResources().getString(R.string.personal_upload_ing_message), false);
        dialog.show();

        int sex = 2;
        if (mSexTextView.getTag() == User.UserSex.USER_MALE) {
            sex = 0;
        } else if (mSexTextView.getTag() == User.UserSex.USER_FEMALE) {
            sex = 1;
        }

        BaseStringRequest.Builder builder = new BaseStringRequest.Builder();
        builder.url(Configurations.USER_URL)
                .add("action", "upload")
                .add("name", mNameEdit.getText().toString())
                .add("sex", sex)
                .add("age", mAgeTextView.getText().length() == 0 ? 0 : Integer.valueOf(mAgeTextView.getText().toString()))
                .add("birth", mBirthTextView.getText().toString())
                .add("constellation", Constellation.getConstellationInt((Constellation.ConstellationEnum) mConstellationTextView.getTag()))
                .add("email", mEmailEdit.getText().toString())
                .add("address", mLocationEdit.getText().toString())
                .add("introduction", mIntroEdit.getText().toString())
                .add("measure_today_times", mTodayTimesTextView.getText())
                .add("measure_total_times", mTotalTimesTextView.getText())
                .add("measure_total_assessment", mAssessTextView.getText());

        String home = getFilesDir().getAbsolutePath();
        File avatarFile = new File(home + Configurations.AVATAR_FILE_PATH);
        if (avatarFile.exists()) {
            builder.add("avatar", avatarFile, OkHttpUtil.MEDIA_TYPE_PNG);
        }

        builder.build()
                .post(new AbsBaseRequest.Callback() {
                    @Override
                    public void onResponse(String response) {
                        User.edit()
                                .setUserName(mNameEdit.getText().toString())
                                .setAge(mAgeTextView.getText().length() == 0 ? 0 : Integer.valueOf(mAgeTextView.getText().toString()))
                                .setBirthday(mBirthTextView.getText().toString())
                                .setSex((User.UserSex) mSexTextView.getTag())
                                .setConstellation((Constellation.ConstellationEnum) mConstellationTextView.getTag())
                                .setEmail(mEmailEdit.getText().toString())
                                .setAddress(mLocationEdit.getText().toString())
                                .setIntroduction(mIntroEdit.getText().toString())
                                .commit();
                        dialog.dismiss();
                        Snackbar.make(rootView, R.string.personal_upload_success_message, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int errorFlag, String error) {
                        dialog.dismiss();
                        Snackbar.make(rootView, error, Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void logout() {
        new DialogBuilder(this).create()
                .setTitle(R.string.tips)
                .setContent(R.string.personal_logout_tips)
                .setPositive(R.string.ok)
                .setNegative(R.string.cancel)
                .setOnPositiveClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        User.logout();
                        String home = getFilesDir().getAbsolutePath();
                        File avatarFile = new File(home + Configurations.AVATAR_FILE_PATH);
                        if (avatarFile.exists()) {
                            avatarFile.delete();
                        }
                        setResult(AppCompatActivity.RESULT_OK);
                        finish();
                    }
                })
                .show();
    }

    private void changePwd() {
        ChangePwdDialog dialog = new ChangePwdDialog(this);
        dialog.setOnChangeListener(new ChangePwdDialog.OnChangeListener() {

            @Override
            public void onSuccess() {
                new DialogBuilder(PersonalInfoActivity.this).create()
                        .setTitle(R.string.tips)
                        .setContent(R.string.change_pwd_dialog_success_message)
                        .setPositive(R.string.ok)
                        .show();
            }

            @Override
            public void onFail(int errorFlag, String error) {
                new DialogBuilder(PersonalInfoActivity.this).create()
                        .setTitle(R.string.tips)
                        .setContent(R.string.change_pwd_dialog_fail_message)
                        .setPositive(R.string.ok)
                        .show();
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_PICK) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Intent intent = new Intent();
                    intent.setClass(this, CropImageActivity.class);
                    intent.setData(data.getData());
                    startActivityForResult(intent, REQUEST_IMAGE_CROP);
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CROP) {
            if (resultCode == RESULT_OK) {
                Bitmap bm = data.getParcelableExtra(CropImageActivity.CROPPED_BITMAP);
                mAvatarImageView.setImageBitmap(bm);
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mSwipeRefreshLayout.setEnabled(verticalOffset == 0);
    }

    @Override
    public void onRefresh() {

        final BaseStringRequest request = new BaseStringRequest.Builder()
                                                    .url(Configurations.USER_URL)
                                                    .add("action", "info")
                                                    .build();

        request.post(new AbsBaseRequest.Callback() {
            @Override
            public void onResponse(String response) {
                mSwipeRefreshLayout.setRefreshing(false);
                Gson gson = new Gson();
                UserInfoBean bean = gson.fromJson(response, UserInfoBean.class);
                User.syncUserInfo(bean, PersonalInfoActivity.this);
                retrieveAvatar(bean.getAvatar());
                Snackbar.make(rootView, R.string.personal_refresh_success_message, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int errorFlag, String error) {
                mSwipeRefreshLayout.setRefreshing(false);
                Snackbar.make(rootView, R.string.personal_refresh_fail_message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onComplete(RippleView rippleView) {
        if (mLogoutView.equals(rippleView)) {
            logout();

        } else if (mChangePwdView.equals(rippleView)) {
            changePwd();
        }
    }

    private void retrieveAvatar(String url) {
        if (url == null || url.length() == 0) {
            return;
        }
        new BaseStringRequest.Builder()
                .url(url)
                .build()
                .input(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        PersonalInfoActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshContents();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        final InputStream is = response.body().byteStream();
                        String home = getFilesDir().getAbsolutePath();
                        File avatarFile = new File(home + Configurations.AVATAR_FILE_PATH);
                        if (!avatarFile.exists()) {
                            if (!avatarFile.createNewFile()) {
                                return;
                            }
                        }
                        Bitmap bm = BitmapFactory.decodeStream(is);
                        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(avatarFile));
                        bm.compress(Bitmap.CompressFormat.PNG, 100, os);  //图片存成png格式。
                        os.close();
                        is.close();
                        bm.recycle();
                        PersonalInfoActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshContents();
                            }
                        });
                    }
                });
    }
}
