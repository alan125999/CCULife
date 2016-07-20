package org.zankio.ccudata.kiki;


import android.content.Context;

import org.zankio.ccudata.base.Repository;
import org.zankio.ccudata.base.model.User;
import org.zankio.ccudata.base.source.BaseSource;
import org.zankio.ccudata.kiki.source.local.DatabaseTimeTableSource;
import org.zankio.ccudata.kiki.source.remote.Authenticate;
import org.zankio.ccudata.kiki.source.remote.CourseListSource;
import org.zankio.ccudata.kiki.source.remote.TimetableSource;

public class Kiki extends Repository {
    private OfflineMode offline_mode = OfflineMode.ALL;
    private User user = new User(this);

    public Kiki(Context context) {
        super(context);
    }

    @Override
    protected <TData, TArgument> RequestTransformer<TData, TArgument> filterSource() {
        // no disabled
        if (offline_mode.compareTo(OfflineMode.DISABLED) != 0)
            return requestObservable -> requestObservable;

        // disabled offline
        return requestObservable ->
                requestObservable.filter(request -> !request.source().isOffline());
    }

    @Override
    protected BaseSource[] getSources() {
        return new BaseSource[]{
                new CourseListSource(),
                new TimetableSource(),
                new Authenticate(),

                new DatabaseTimeTableSource(this),
        };
    }

    public User user(){
        return user;
    }

    public OfflineMode getOfflineMode() {
        return offline_mode;
    }

    public enum OfflineMode {
        ALL,
        CLASS,
        BROWSERED,
        DISABLED
    }

}
