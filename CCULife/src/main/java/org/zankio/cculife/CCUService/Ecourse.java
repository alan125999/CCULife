package org.zankio.cculife.CCUService;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.zankio.cculife.CCUService.Parser.EcourseParser;
import org.zankio.cculife.CCUService.Source.EcourseLocalSource;
import org.zankio.cculife.CCUService.Source.EcourseRemoteSource;
import org.zankio.cculife.CCUService.Source.EcourseSource;
import org.zankio.cculife.CCUService.SourceSwitcher.AutoNetworkSourceSwitcher;
import org.zankio.cculife.CCUService.SourceSwitcher.ISwitcher;
import org.zankio.cculife.CCUService.SourceSwitcher.SingleSourceSwitcher;
import org.zankio.cculife.SessionManager;
import org.zankio.cculife.override.Exceptions;

import java.io.IOException;

public class Ecourse {
    private ISwitcher sourceSwitcher;

    public Course nowCourse = null;
    public int OFFLINE_MODE = 0;

    public Ecourse(Context context) throws Exception {
        EcourseRemoteSource ecourseRemoteSource;
        EcourseLocalSource ecourseLocalSource;
        SharedPreferences preferences;
        SessionManager sessionManager = SessionManager.getInstance(context);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        OFFLINE_MODE = sessionManager.isSave() &&
                preferences.getBoolean("offline_enable", true)
                ? Integer.valueOf(preferences.getString("offline_mode", "1")) : -1;

        ecourseRemoteSource = new EcourseRemoteSource(this, new EcourseParser());

        try {
            ecourseRemoteSource.Authentication(sessionManager);
            // if(OFFLINE_MODE == 0) syncAll();
        } catch (IOException e) {
            ecourseRemoteSource.setSessionManager(sessionManager);
        }

        if (OFFLINE_MODE < 0) {
            sourceSwitcher = new SingleSourceSwitcher(ecourseRemoteSource);
        } else {
            ecourseLocalSource = new EcourseLocalSource(this, context);
            ecourseRemoteSource.setLocalStorage(ecourseLocalSource);
            sourceSwitcher = new AutoNetworkSourceSwitcher(context, ecourseLocalSource, ecourseRemoteSource);
        }

    }

    public void openSource() {
        sourceSwitcher.openSource();
    }

    public void closeSource() {
        sourceSwitcher.closeSource();
    }

    public void switchCourse(Course course) {
        if(nowCourse == null || !course.getCourseid().equals(nowCourse.getCourseid())) {
            nowCourse = course;
            getSource().switchCourse(course);
        }
    }

    public void syncAll() {
        if (OFFLINE_MODE < 0) return;

        Course[] courses;
        Announce[] announces;
        try {
            courses = getCourses();
            for(Course course: courses) {
                announces = course.getAnnounces();
                for(Announce announce : announces) {
                    announce.getContent();
                }
                //course.getFiles();
                course.getScore();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public EcourseSource getSource() {
        return (EcourseSource) this.sourceSwitcher.getSource();
    }

    public Course[] getCourses() throws Exception {
        return getSource().getCourse();
    }

    //Todo impl debug mode
    public Course[] getCourses(int year, int term, Kiki kiki) throws Exception {
        return getSource().getCourse(year, term, kiki);
    }


    public class Course {
        private String courseid;
        private String id;
        private String name;
        private String teacher;
        private int notice;
        private int homework;
        private int exam;
        private boolean warning;
        private File[] files;
        private Announce[] announces;
        private Scores[] scores;
        private Ecourse ecourse;

        public Course(Ecourse content) {
            this.setEcourse(content);
        }

        public Course(String courseid, String id, String name, String teacher, int notice, int homework, int exam, String warning){
            this.setCourseid(courseid);
            this.setId(id);
            this.setName(name);
            this.setTeacher(teacher);
            this.setNotice(notice);
            this.setHomework(homework);
            this.setExam(exam);
            this.setWarning(!warning.equals("--"));
        }



        public Scores[] getScore() throws Exception {
            if (scores != null) return scores;

            EcourseSource ecourseSource;
            ecourseSource = getSource();

            getEcourse().switchCourse(this);
            try {
                this.scores = ecourseSource.getScore(this);
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }

            return this.scores;
        }

        public Classmate[] getClassmate() throws Exception {
            EcourseSource ecourseSource;
            ecourseSource = getSource();

            getEcourse().switchCourse(this);

            try {
                return ecourseSource.getClassmate(this);
            } catch (IOException e) {
                throw Exceptions.getNetworkException(e);
            }
        }

        public Announce[] getAnnounces() throws Exception {
            if (this.announces != null) return this.announces;
            EcourseSource ecourseSource;
            ecourseSource = getSource();

            getEcourse().switchCourse(this);

            try {
                this.announces = ecourseSource.getAnnounces(this);
                if(OFFLINE_MODE == 1) {

                    final Announce[] sync = this.announces;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            syncAnnounceContent(sync);
                        }
                    }).start();

                }
                return this.announces;
            } catch (IOException e) {
                throw Exceptions.getNetworkException();
            }
        }

        private void syncAnnounceContent(Announce[] announces) {
            if(!(sourceSwitcher instanceof AutoNetworkSourceSwitcher)) return;
            EcourseLocalSource ecourseLocalSource = (EcourseLocalSource) ((AutoNetworkSourceSwitcher)sourceSwitcher).getLocalSource();

            for (Announce announce : announces) {
                if(!ecourseLocalSource.hasAnnounceContent(announce))
                    announce.getContent();
            }
        }

        public File[] getFiles() throws Exception {
            if (this.files != null) return this.files;

            EcourseSource ecourseSource;
            ecourseSource = getSource();
            this.files = ecourseSource.getFiles(this);

            return this.files;
        }

        public String getCourseid() {
            return courseid;
        }

        public void setCourseid(String courseid) {
            this.courseid = courseid;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTeacher() {
            return teacher;
        }

        public void setTeacher(String teacher) {
            this.teacher = teacher;
        }

        public int getNotice() {
            return notice;
        }

        public void setNotice(int notice) {
            this.notice = notice;
        }

        public int getHomework() {
            return homework;
        }

        public void setHomework(int homework) {
            this.homework = homework;
        }

        public int getExam() {
            return exam;
        }

        public void setExam(int exam) {
            this.exam = exam;
        }

        public boolean isWarning() {
            return warning;
        }

        public void setWarning(boolean warning) {
            this.warning = warning;
        }

        public Ecourse getEcourse() {
            return ecourse;
        }

        public void setEcourse(Ecourse ecourse) {
            this.ecourse = ecourse;
        }

        public void setFiles(File[] files) {
            this.files = files;
        }

        public void setAnnounces(Announce[] announces) {
            this.announces = announces;
        }

        public Scores[] getScores() {
            return scores;
        }

    }

    public class File{
        public String Name;
        public String URL;
        public String Size;
    }

    public class Announce {
        public String url;
        public String Date;
        public String Title;
        public String Content = null;
        public String important;
        public int browseCount;
        public boolean isnew;
        protected Ecourse ecourse;
        protected Course course;

        public Announce(Ecourse ecourse, Course course) {this.ecourse = ecourse; this.course = course;}

        public String getCourseID() {
            return this.course.getCourseid();
        }

        public String getContent() {
            if (this.Content != null) return this.Content;
            EcourseSource ecourseSource;
            ecourseSource = getSource();
            ecourse.switchCourse(course);

            try {
                this.Content = ecourseSource.getAnnounceContent(this);

            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }

            return this.Content;
        }
    }

    public class Classmate {
        public String Name;
        public String StudentId;
        public String Department;
        public String Gender;
    }


    public class Scores {
        public String courseid;
        public Score[] scores;
        public String Name;
        public String Score;
        public String Rank;
    }

    public class Score {
        public String courseid;
        public String Name;
        public String Score;
        public String Rank;
        public String Percent;
    }
}
