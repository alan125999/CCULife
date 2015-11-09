package org.zankio.cculife.CCUService.base.helper;

import android.content.Context;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.zankio.cculife.CCUService.base.authentication.IAuth;
import org.zankio.cculife.override.Net;

import javax.net.ssl.SSLSocketFactory;

public class ConnectionHelper {
    private static Context context;
    IAuth<Connection> auth;
    private final static int CONNECT_TIMEOUT = Net.CONNECT_TIMEOUT;
    private static SSLSocketFactory sslSocketFoctory;

    public ConnectionHelper() { }

    public static void setContext(Context context) {
        ConnectionHelper.context = context;
    }

    public ConnectionHelper(IAuth<Connection> auth) {
        this.auth = auth;
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        if (sslSocketFoctory == null) {
            //Todo
            //if (context == null)
            sslSocketFoctory = Net.generateSSLSocketFactory(context);
        }
        return sslSocketFoctory;
    }

    public Connection create(String url) {
        Connection connection = Jsoup.connect(url);
        return init(connection);
    }

    public static void setSSLSocketFactory(SSLSocketFactory sslSocketFoctory) {
        ConnectionHelper.sslSocketFoctory = sslSocketFoctory;
    }

    public Connection init(Connection connection) {
        ConnectionHelper.initAuth(connection, auth);
        ConnectionHelper.initTimeout(connection);
        return connection;
    }

    public static Connection initTimeout(Connection connection) {
        connection.timeout(CONNECT_TIMEOUT);
        return connection;
    }

    public static Connection initAuth(Connection connection, IAuth<Connection> auth) {
        if(auth != null) auth.Auth(connection);
        return connection;
    }
}