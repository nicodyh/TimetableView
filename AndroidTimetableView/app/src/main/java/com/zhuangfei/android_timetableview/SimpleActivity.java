package com.zhuangfei.android_timetableview;

import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.zhuangfei.android_timetableview.model.MySubject;
import com.zhuangfei.android_timetableview.model.SubjectRepertory;
import com.zhuangfei.timetable.TimetableView;
import com.zhuangfei.timetable.listener.ISchedule;
import com.zhuangfei.timetable.listener.IWeekView;
import com.zhuangfei.timetable.listener.OnItemBuildAdapter;
import com.zhuangfei.timetable.listener.TimeSlideAdapter;
import com.zhuangfei.timetable.model.Schedule;
import com.zhuangfei.timetable.model.ScheduleManager;
import com.zhuangfei.timetable.model.ScheduleSupport;
import com.zhuangfei.timetable.utils.ColorUtils;
import com.zhuangfei.timetable.utils.ScreenUtils;
import com.zhuangfei.timetable.view.WeekView;

import java.util.List;

/**
 * 简单示例,展示多项基础能力
 * 点击监听、节次设置、非本周设置、课程动态增删
 * <p>
 * 该界面的代码注释会比较详细，建议从此处开始看起
 */
public class SimpleActivity extends AppCompatActivity {

    private static final String TAG = "SimpleActivity";

    //控件
    TimetableView mTimetableView;
    WeekView weekView;

    //其他
    Button moreButton;
    TextView titleTextView;
    List<MySubject> mySubjects;

    //记录切换的周次，不一定是当前周
    int target = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_example);

        moreButton = findViewById(R.id.id_more);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopmenu();
            }
        });

        mySubjects = SubjectRepertory.loadDefaultSubjects();
        initTimetableView();
    }

    /**
     * 更新一下，防止因程序在后台时间过长（超过一天）而导致的日期或高亮不准确问题。
     * 该操作根据个人情况而定,推荐调用一下
     * <p>
     * #注意: 如果使用的TimetableView的版本小于等于v2.0.0,那么需要捕获该异常
     * // 第一次调用时会出异常
     * <p>
     * <pre>
     *      try{
     *          mTimetableView.getOnDateBuildListener().onHighLight();
     *      }catch (Exception e){
     *      }
     * </pre>
     * <p>
     * <p>
     * #注意: TimetableView-v2.0.1以及之后的版本就不存在这个问题了,
     * 直接使用即可,如下:
     * <pre>
     *     mTimetableView.getOnDateBuildListener().onHighLight();
     * </pre>
     */
    @Override
    protected void onStart() {
        super.onStart();
        mTimetableView.getOnDateBuildListener().onHighLight();
    }

    /**
     * 初始化课程控件
     */
    private void initTimetableView() {
        //获取控件
        mTimetableView = findViewById(R.id.id_timetableView);
        weekView = findViewById(R.id.id_weekview);
        titleTextView = findViewById(R.id.id_title);

        //设置周次选择属性
        //设置数据源的另外方式
        //weekView.setData(List<Schedule>)
        //设置数据源
        weekView.source(mySubjects)
                .curWeek(1)
                .setOnWeekItemClickedListener(new IWeekView.OnWeekItemClickedListener() {
                    @Override
                    public void onWeekClicked(int curWeek) {
                        //点击之后，切换周次
                        mTimetableView.changeWeekOnly(curWeek);
                    }
                })
                .setOnWeekLeftClickedListener(new IWeekView.OnWeekLeftClickedListener() {
                    @Override
                    public void onWeekLeftClicked() {
                        onWeekLeftLayoutClicked();
                    }
                })
                .isShow(false)//设置隐藏，默认显示
                .showView();

        //与构建有关的设置在ScheduleManager中
        final ScheduleManager manager = mTimetableView.getScheduleManager();
        manager.setOnItemClickListener(new ISchedule.OnItemClickListener() {
            @Override
            public void onItemClick(View v, List<Schedule> scheduleList) {
                display(scheduleList);
            }
        }).setOnItemBuildListener(new OnItemBuildAdapter() {
            @Override
            public void onItemUpdate(FrameLayout layout, TextView textView, TextView countTextView, Schedule schedule, GradientDrawable gd) {
                super.onItemUpdate(layout, textView, countTextView, schedule, gd);
                int color = manager.getColorPool().getUselessColor();
                if (ScheduleSupport.isThisWeek(schedule, mTimetableView.getCurWeek())) {
                    color = manager.getColorPool().getColorAuto(schedule.getColorRandom());
                }
                int newcolor = ColorUtils.alphaColor(color, 0.6f);
                gd.setColor(newcolor);
            }
        })
                .setItemHeight(ScreenUtils.dip2px(this, 40))
                .setMaxSlideItem(10);

        //与全局有关的设置在TimetableView中
        //设置数据源
        mTimetableView.setSource(mySubjects)
                .setCurWeek(1)
                .setCurTerm("大三下学期")
                .setOnWeekChangedListener(new ISchedule.OnWeekChangedListener() {
                    @Override
                    public void onWeekChanged(int curWeek) {
                        int size = mTimetableView.getDataSource().size();
                        titleTextView.setText("第" + curWeek + "周,共" + size + "门课");
                    }
                })
                .setSlideAlpha(0.6f)
                .showView();
    }

    /**
     * 周次选择布局的左侧被点击时回调
     */
    protected void onWeekLeftLayoutClicked() {
        final String items[] = new String[20];
        for (int i = 0; i < 20; i++) {
            items[i] = "第" + (i + 1) + "周";
        }
        target = -1;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置当前周");
        builder.setSingleChoiceItems(items, mTimetableView.getCurWeek() - 1,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        target = i;
                    }
                });
        builder.setPositiveButton("设置为当前周", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (target != -1) {
                    weekView.curWeek(target + 1).updateView();
                    mTimetableView.changeWeekForce(target + 1);
                }
            }
        });
        builder.setNegativeButton("取消", null);
        builder.create().show();
    }

    /**
     * 显示内容
     *
     * @param beans
     */
    protected void display(List<Schedule> beans) {
        String str = "";
        for (Schedule bean : beans) {
            str += bean.getName() + "、";
        }
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示弹出菜单
     */
    public void showPopmenu() {
        PopupMenu popup = new PopupMenu(this, moreButton);
        popup.getMenuInflater().inflate(R.menu.popmenu_simple, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.top1:
                        addSubject();
                        break;
                    case R.id.top2:
                        deleteSubject();
                        break;

                    case R.id.top4:
                        hideNonThisWeek();
                        break;
                    case R.id.top5:
                        showNonThisWeek();
                        break;
                    case R.id.top6:
                        setMaxItem(8);
                        break;
                    case R.id.top7:
                        setMaxItem(10);
                        break;
                    case R.id.top8:
                        setMaxItem(12);
                        break;
                    case R.id.top9:
                        showTime();
                        break;
                    case R.id.top10:
                        hideTime();
                        break;
                    case R.id.top11:
                        showWeekView();
                        break;
                    case R.id.top12:
                        hideWeekView();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        popup.show();
    }

    /**
     * 随机切换周次
     * <p>
     * 周次切换有两种模式：强制、非强制
     * 强制模式下，参数会被设置为当前周，非强制模式下仅仅切换周次
     *
     * @see TimetableView#changeWeekForce(int)
     * @see TimetableView#changeWeekOnly(int)
     * @see TimetableView#changeWeek(int, boolean)
     */
    protected void changeWeekByRandom() {
        int week = (int) (Math.random() * 20) + 1;
        while (week == mTimetableView.getCurWeek()) {
            week = (int) (Math.random() * 20) + 1;
        }
        mTimetableView.changeWeekOnly(week);
    }

    /**
     * 删除课程
     * 内部使用集合维护课程数据，操作集合的方法来操作它即可
     * 最后更新一下视图（全局更新）
     */
    protected void deleteSubject() {
        int size = mTimetableView.getDataSource().size();
        int pos = (int) (Math.random() * size);
        if (size > 0) {
            mTimetableView.getDataSource().remove(pos);
            mTimetableView.updateView();
        }
    }

    /**
     * 添加课程
     * 内部使用集合维护课程数据，操作集合的方法来操作它即可
     * 最后更新一下视图（全局更新）
     */
    protected void addSubject() {
        int size = mTimetableView.getDataSource().size();
        if (size > 0) {
            Schedule schedule = mTimetableView.getDataSource().get(0);
            mTimetableView.getDataSource().add(schedule);
            mTimetableView.updateView();
        }
    }

    /**
     * 隐藏非本周课程
     * 修改了内容的显示，所以必须更新全部（性能不高）
     * 建议：在初始化时设置该属性
     * <p>
     * updateView()被调用后，会重新构建课程，课程会回到当前周
     */
    protected void hideNonThisWeek() {
        mTimetableView.getScheduleManager().setShowNotCurWeek(false);
        mTimetableView.updateView();
    }

    /**
     * 显示非本周课程
     * 修改了内容的显示，所以必须更新全部（性能不高）
     * 建议：在初始化时设置该属性
     */
    protected void showNonThisWeek() {
        mTimetableView.getScheduleManager().setShowNotCurWeek(true);
        mTimetableView.updateView();
    }

    /**
     * 设置侧边栏最大节次，只影响侧边栏的绘制，对课程内容无影响
     * 最大节次取决于以下两个方法中的最小值，默认值都为12
     * 只修改了侧边栏的属性，所以只更新侧边栏即可（性能高），没有必要更新全部（性能低）
     *
     * @param num
     * @see com.zhuangfei.timetable.model.ScheduleManager#setMaxSlideItem(int)
     * @see ISchedule.OnSlideBuildListener#getSlideItemSize()
     */
    protected void setMaxItem(int num) {
        mTimetableView.getScheduleManager().setMaxSlideItem(num);
        mTimetableView.updateSlideView();
    }

    /**
     * 显示时间
     * 设置侧边栏构建监听，TimeSlideAdapter是控件实现的可显示时间的侧边栏
     * 只修改了侧边栏的属性，所以只更新侧边栏即可（性能高），没有必要更新全部（性能低）
     *
     * @see TimeSlideAdapter
     */
    protected void showTime() {
        String[] times = new String[]{
                "8:00", "9:00", "10:10", "11:00",
                "15:00", "16:00", "17:00", "18:00",
                "19:30", "20:30", "21:30", "22:30"
        };
        TimeSlideAdapter slideAdapter = new TimeSlideAdapter();
        slideAdapter.setTimes(times);
        mTimetableView.getScheduleManager().setOnSlideBuildListener(slideAdapter);
        mTimetableView.updateSlideView();
    }

    /**
     * 隐藏时间
     * 将侧边栏监听置Null后，会默认使用默认的构建方法，即不显示时间
     * 只修改了侧边栏的属性，所以只更新侧边栏即可（性能高），没有必要更新全部（性能低）
     */
    protected void hideTime() {
        mTimetableView.getScheduleManager().setOnSlideBuildListener(null);
        mTimetableView.updateSlideView();
    }

    /**
     * 显示WeekView
     */
    protected void showWeekView() {
        weekView.isShow(true);
    }

    /**
     * 隐藏WeekView
     */
    protected void hideWeekView() {
        weekView.isShow(false);
    }
}
