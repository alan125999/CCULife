package org.zankio.cculife.ui.ecourse;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import org.zankio.cculife.CCUService.ecourse.Ecourse;
import org.zankio.cculife.CCUService.ecourse.model.Course;
import org.zankio.cculife.ui.base.IGetCourseData;

public class CourseDataFragment extends Fragment implements IGetCourseData, CourseListFragment.OnCourseSelectedListener {
    public static final String TAG_COURSE_DATA_FRAGMENT = "COURSE_DATA_FRAGMENT";
    private Ecourse ecourse;
    private Course course;

    public static CourseDataFragment getFragment(FragmentManager fragmentManager) {
        CourseDataFragment fragment = (CourseDataFragment) fragmentManager.findFragmentByTag(TAG_COURSE_DATA_FRAGMENT);
        if (fragment == null) {
            fragment = new CourseDataFragment();
            fragmentManager.beginTransaction().add(fragment, TAG_COURSE_DATA_FRAGMENT).commit();
        }
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public Ecourse getEcourse() {
        return this.ecourse;
    }

    public void setEcourse(Ecourse ecourse) {
        this.ecourse = ecourse;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public Course getCourse(String id) {
        return this.course;
    }

    @Override
    public void onCourseSelected(Ecourse ecourse, Course course) {
        this.ecourse = ecourse;
        this.course = course;
    }
}