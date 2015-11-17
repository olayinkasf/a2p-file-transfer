/*
 * Copyright 2015
 *
 *     Olayinka S. Folorunso <mail@olayinkasf.com>
 *     http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package desktop.olayinka.file.transfer.model;

import com.olayinka.file.transfer.Utils;
import com.olayinka.file.transfer.model.AppInfo;
import org.apache.commons.io.IOUtils;
import ripped.android.json.JSONArray;
import ripped.android.json.JSONException;
import ripped.android.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Olayinka on 9/23/2015.
 */
public class DerbyJDBCHelper {

    private EntityManager mManager;
    long mCurrentVersion = -1;
    public static final long DB_VERSION = 1;

    private static final File HOME_DIR;
    private static final File DB_FILE;
    private static final String DB_USER;
    private static final String DB_PWD;

    private static final File STORAGE_DIR;

    private static final File CONFIG_DIR;

    static {
        HOME_DIR = new File(System.getProperty("user.home"), ".a2p-file-transfer");
        STORAGE_DIR = new File(HOME_DIR, ".storage");
        CONFIG_DIR = new File(HOME_DIR, ".config");
        try {
            HOME_DIR.mkdirs();
            CONFIG_DIR.mkdirs();
            STORAGE_DIR.mkdirs();
        } catch (SecurityException e) {
            throw new RuntimeException("Well, we must have some permissions!");
        }
        if (!STORAGE_DIR.exists() || !CONFIG_DIR.exists()) {
            throw new RuntimeException("Well, we must have some permissions!");
        }
        DB_FILE = new File(STORAGE_DIR, "derby.jdbc");
        File credentialFile = new File(CONFIG_DIR, ".credentials");
        if (credentialFile.exists()) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(credentialFile))) {
                DB_USER = bufferedReader.readLine().trim();
                DB_PWD = bufferedReader.readLine().trim();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("This shouldn't happen", e);
            }
        } else {
            int sub = Utils.randomSize(0, 52);
            DB_USER = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(sub, sub + 1) + Utils.randomString(Utils.randomSize(64, 127));
            DB_PWD = Utils.randomString(Utils.randomSize(64, 128));
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(credentialFile))) {
                bufferedWriter.write(DB_USER);
                bufferedWriter.newLine();
                bufferedWriter.write(DB_PWD);
                bufferedWriter.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String DB_URL = "jdbc:derby:" + DB_FILE.getAbsolutePath();

    private static final DerbyJDBCHelper JDBC_HELPER = new DerbyJDBCHelper();

    private DerbyJDBCHelper() {
        try {
            System.out.println(DB_URL);

            //ensure database is created
            String dbUrl = "jdbc:derby:" + DB_FILE.getAbsolutePath() + ";create=true;user=" + DB_USER + ";password=" + DB_PWD;
            System.out.println(dbUrl);
            Connection mConnection;
            try {
                Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
                mConnection = DriverManager.getConnection(dbUrl);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
                return;
            }
            onStart(mConnection);
            onUpgrade(mConnection);
            mConnection.commit();
            mConnection.close();

            //instantiate persistence unit
            EntityManagerFactory managerFactory = null;
            Map<String, String> persistenceMap = new HashMap<String, String>();

            persistenceMap.put("javax.persistence.jdbc.url", DB_URL);
            persistenceMap.put("javax.persistence.jdbc.user", DB_USER);
            persistenceMap.put("javax.persistence.jdbc.password", DB_PWD);

            URL resource = ClassLoader.getSystemResource("");
            File file = new File(String.valueOf(resource));


            managerFactory = Persistence.createEntityManagerFactory("A2PPU", persistenceMap);
            mManager = managerFactory.createEntityManager();

            deviceProvider = new JDBCDeviceProvider(mManager);

        } catch (Exception except) {
            except.printStackTrace();
            System.exit(1);
        }
    }

    private JDBCDeviceProvider deviceProvider;

    public void shutdown() {
    }

    public static DerbyJDBCHelper instance() {
        return JDBC_HELPER;
    }

    public static void cleanUp(Statement statement, ResultSet resultSet) {
        if (resultSet != null) try {
            resultSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (statement != null) try {
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onStart(Connection mConnection) {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = mConnection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM  app_info");
            resultSet.next();
            mCurrentVersion = resultSet.getLong(1);
            cleanUp(statement, resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            cleanUp(statement, resultSet);
            try {
                statement = mConnection.createStatement();

                InputStream in = DerbyJDBCHelper.class.getResourceAsStream("/raw/db.sql");
                String[] queries = IOUtils.toString(in).split(";");
                for (String query : queries) {
                    query = query.trim();
                    if (!query.isEmpty()) {
                        statement.execute(query);
                    }
                }

                mConnection.commit();

                mCurrentVersion = 1;

                cleanUp(statement, null);
            } catch (SQLException | IOException e1) {
                e1.printStackTrace();
                cleanUp(statement, null);
                System.exit(1);
            }
        }

    }

    public void onUpgrade(Connection mConnection) {
        if (DB_VERSION > mCurrentVersion) {
        }
    }

    public static JSONArray convert(ResultSet rs) throws SQLException, JSONException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int numColumns = rsmd.getColumnCount();
        JSONArray jsonArray = new JSONArray();
        while (rs.next()) {
            JSONObject obj = new JSONObject();

            for (int i = 1; i < numColumns + 1; i++) {
                String columnName = rsmd.getColumnName(i);
                obj.put(columnName, rs.getObject(i));
            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }

    public JDBCDeviceProvider getDeviceProvider() {
        return deviceProvider;
    }

    public AppInfo getSystemProperties() {
        return mManager.find(AppInfo.class, 1l);
    }
}
