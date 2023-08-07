package com.huamar.charge.pile.util;

//import ch.qos.logback.classic.Level;
//import ch.qos.logback.classic.LoggerContext;
//import ch.qos.logback.classic.filter.LevelFilter;
//import ch.qos.logback.classic.spi.ILoggingEvent;
//import ch.qos.logback.core.Appender;
//import ch.qos.logback.core.ConsoleAppender;
//import ch.qos.logback.core.FileAppender;
//import ch.qos.logback.core.filter.Filter;
//import ch.qos.logback.core.rolling.RollingFileAppender;
//import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
//import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
//import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
//import ch.qos.logback.core.spi.FilterReply;
//import ch.qos.logback.core.util.FileSize;
//import ch.qos.logback.core.util.OptionHelper;
//import org.slf4j.LoggerFactory;
//
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//
//import static org.apache.http.impl.ConnSupport.createEncoder;
//
///**
// * LogUtil
// * :date 2023/07
// *
// * @author TiAmo(TiAmolikecode@gmail.com)
// *
// */
//public class LogUtil {
//    private static String consoleAppenderName = "serve-console";
//    private static String maxFileSize = "50MB";
//    private static String totalSizeCap = "10GB";
//    private static int maxHistory = 30;
//    private static ConsoleAppender defaultConsoleAppender = null;
//
//    static {
//        Map<String, Appender<ILoggingEvent>> appenderMap = appender();
//        appenderMap.forEach((key, appender) -> {
//            // 如果logback配置文件中，已存在窗口输出的appender，则直接使用；不存在则重新生成
//            if (appender instanceof ConsoleAppender) {
//                defaultConsoleAppender = (ConsoleAppender) appender;
//                return;
//            }
//        });
//    }
//
//
//    /**
//     * description: 设置打印日志的级别
//     *
//     * @param level
//     * @return ch.qos.logback.core.filter.Filter
//     * @author Hlingoes 2020/6/10
//     */
//    private static Filter createLevelFilter(Level level) {
//        LevelFilter levelFilter = new LevelFilter();
//        levelFilter.setLevel(level);
//        levelFilter.setOnMatch(FilterReply.ACCEPT);
//        levelFilter.setOnMismatch(FilterReply.DENY);
//        levelFilter.start();
//        return levelFilter;
//    }
//
//    /**
//     * description: 创建日志文件的file appender
//     *
//     * @param name name
//     * @param level level
//     * @return ch.qos.logback.core.rolling.RollingFileAppender
//     * @author Hlingoes 2020/6/10
//     */
//    private static RollingFileAppender createAppender(String name, Level level, LoggerContext loggerContext) {
//        RollingFileAppender appender = new RollingFileAppender();
//        // 这里设置级别过滤器
//        appender.addFilter(createLevelFilter(level));
//        // 设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
//        // 但可以使用<scope="context">设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
//        appender.setContext(loggerContext);
//        // appender的name属性
//        appender.setName(name.toUpperCase() + "-" + level.levelStr.toUpperCase());
//        // 读取logback配置文件中的属性值，设置文件名
//        String logPath = OptionHelper.substVars("${logPath}-" + name + "-" + level.levelStr.toLowerCase() + ".log", loggerContext);
//        appender.setFile(logPath);
//        appender.setAppend(true);
//        appender.setPrudent(false);
//        // 加入下面两个节点
//        appender.setRollingPolicy(createRollingPolicy(name, level, loggerContext, appender));
//        appender.setEncoder(createEncoder(loggerContext));
//        appender.start();
//        return appender;
//    }
//
//
//    /**
//     * description: 设置日志的滚动策略
//     *
//     * @param name name
//     * @param level level
//     * @param context context
//     * @param appender appender
//     * @return ch.qos.logback.core.rolling.TimeBasedRollingPolicy
//     * @author Hlingoes 2020/6/10
//     */
//    private static SizeAndTimeBasedRollingPolicy<Object> createRollingPolicy(String name, Level level, LoggerContext context, FileAppender<Object> appender) {
//        // 读取logback配置文件中的属性值，设置文件名
//        String fp = OptionHelper.substVars("${logPath}/${LOG_NAME_PREFIX}-" + name + "-" + level.levelStr.toLowerCase() + "_%d{yyyy-MM-dd}_%i.log", context);
//        SizeAndTimeBasedRollingPolicy<Object> rollingPolicyBase = new SizeAndTimeBasedRollingPolicy<>();
//        // 设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
//        // 但可以使用<scope="context">设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
//        rollingPolicyBase.setContext(context);
//        // 设置父节点是appender
//        rollingPolicyBase.setParent(appender);
//        // 设置文件名模式
//        rollingPolicyBase.setFileNamePattern(fp);
//        SizeAndTimeBasedRollingPolicy<Object> sizeAndTimeBasedRollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
//        // 最大日志文件大小
//        sizeAndTimeBasedRollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
//        rollingPolicyBase.setTimeBasedFileNamingAndTriggeringPolicy(sizeAndTimeBasedRollingPolicy);
//        // 设置最大历史记录为30条
//        rollingPolicyBase.setMaxHistory(maxHistory);
//        // 总大小限制
//        rollingPolicyBase.setTotalSizeCap(FileSize.valueOf(totalSizeCap));
//        rollingPolicyBase.start();
//
//        return rollingPolicyBase;
//    }
//
//    /**
//     * description: 读取logback配置文件中的所有appender
//     */
//    private static Map<String, Appender<ILoggingEvent>> appender() {
//        Map<String, Appender<ILoggingEvent>> appenderMap = new HashMap<>();
//        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
//            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
//                Appender<ILoggingEvent> appender = index.next();
//                appenderMap.put(appender.getName(), appender);
//            }
//        }
//        return appenderMap;
//    }
//
//}
