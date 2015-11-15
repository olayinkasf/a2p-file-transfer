package android.olayinka.file.transfer;

import android.content.Context;
import android.util.Log;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

/**
 * Created by Olayinka on 11/9/2015.
 */
public class AppLogger {
    private static Logger logger = null;
    private static boolean mPaused = false;

    public static void wtf(Context context, String contextP, Throwable content) {
        wtf(context, contextP, content.getMessage());
    }

    public static void wtf(Context context1, String context, String content) {
        if (mPaused) return;
        if (!context1.getSharedPreferences(AppSettings.APP_SETTINGS, Context.MODE_PRIVATE).getBoolean(AppSettings.LOG_APP_ACTIVITY, false)) {
            return;
        }
        try {
            if (logger == null) {
                logger = configureLogBackDirectly(context1);
            }
            logger.error(context1.getClass() + "/" + context + ": " + content);
        } catch (Throwable throwable) {
            Log.wtf("logger", throwable);
            logger = null;
        }
    }

    private static Logger configureLogBackDirectly(Context context) {
        // reset the default context (which may already have been initialized)
        // since we want to reconfigure it
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        lc.reset();

        // setup FileAppender
        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
        encoder1.setContext(lc);
        encoder1.setPattern("%d{HH:mm:ss.SSS} %msg%n");
        encoder1.setImmediateFlush(true);
        encoder1.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(lc);
        fileAppender.setFile(context.getFileStreamPath(AppSettings.MAIN_LOG).getAbsolutePath());
        fileAppender.setEncoder(encoder1);
        fileAppender.start();

        // setup LogcatAppender
        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
        encoder2.setContext(lc);
        encoder2.setPattern("[%thread] %msg%n");
        encoder2.setImmediateFlush(true);
        encoder2.start();

        LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(lc);
        logcatAppender.setEncoder(encoder2);
        logcatAppender.start();

        // add the newly created appenders to the root logger;
        // qualify AppLogger to disambiguate from org.slf4j.AppLogger
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fileAppender);
        root.addAppender(logcatAppender);

        return root;
    }

    public static void pause() {
        mPaused = true;
    }

    public static void resume() {
        mPaused = false;
    }
}
