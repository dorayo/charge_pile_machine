package com.huamar.charge.common.util;

import org.apache.commons.lang3.time.FastDateFormat;

@SuppressWarnings("unused")
public interface DatePattern {

     String NORM_DATE_PATTERN = "yyyy-MM-dd";
     FastDateFormat NORM_DATE_FORMAT = FastDateFormat.getInstance(NORM_DATE_PATTERN);

     String PURE_DATE_PATTERN = "yyyyMMdd";
     FastDateFormat PURE_DATE_FORMAT = FastDateFormat.getInstance(PURE_DATE_PATTERN);

     String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
     FastDateFormat NORM_DATETIME_FORMAT = FastDateFormat.getInstance(NORM_DATETIME_PATTERN);
     String NORM_DATETIME_MINUTE_PATTERN = "yyyy-MM-dd HH:mm";
     FastDateFormat NORM_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(NORM_DATETIME_MINUTE_PATTERN);

     String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";
     FastDateFormat PURE_DATETIME_FORMAT = FastDateFormat.getInstance(PURE_DATETIME_PATTERN);
     String PURE_DATETIME_MS_PATTERN = "yyyyMMddHHmmssSSS";
     FastDateFormat PURE_DATETIME_MS_FORMAT = FastDateFormat.getInstance(PURE_DATETIME_MS_PATTERN);

     String EN_DATE_PATTERN = "yyyy/MM/dd";
     FastDateFormat EN_DATE_FORMAT = FastDateFormat.getInstance(EN_DATE_PATTERN);
     String EN_DATE_MONTH_PATTERN = "yyyy/MM";
     FastDateFormat EN_DATE_MONTH_FORMAT = FastDateFormat.getInstance(EN_DATE_MONTH_PATTERN);
     String EN_DATETIME_MINUTE_PATTERN = "yyyy/MM/dd HH:mm";
     FastDateFormat EN_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(EN_DATETIME_MINUTE_PATTERN);
     String EN_DATETIME_PATTERN = "yyyy/MM/dd HH:mm:ss";
     FastDateFormat EN_DATETIME_FORMAT = FastDateFormat.getInstance(EN_DATETIME_PATTERN);

     String CN_DATE_PATTERN = "yyyy年MM月dd日";
     FastDateFormat CN_DATE_FORMAT = FastDateFormat.getInstance(CN_DATE_PATTERN);
     String CN_DATE_MONTH_PATTERN = "yyyy年MM月";
     FastDateFormat CN_DATE_MONTH_FORMAT = FastDateFormat.getInstance(CN_DATE_MONTH_PATTERN);
     String CN_DATETIME_MINUTE_PATTERN = "yyyy年MM月dd日 HH:mm";
     FastDateFormat CN_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(CN_DATETIME_MINUTE_PATTERN);
     String CN_DATETIME_PATTERN = "yyyy年MM月dd日 HH:mm:ss";
     FastDateFormat CN_DATETIME_FORMAT = FastDateFormat.getInstance(CN_DATETIME_PATTERN);

     String SIMPLE_DATE_MONTH_PATTERN = "yyyy-MM";
     FastDateFormat SIMPLE_DATE_MONTH_FORMAT = FastDateFormat.getInstance(SIMPLE_DATE_MONTH_PATTERN);
     String SIMPLE_DATE_DAY_PATTERN = "MM-dd";
     FastDateFormat SIMPLE_DATE_DAY_FORMAT = FastDateFormat.getInstance(SIMPLE_DATE_DAY_PATTERN);
     String SIMPLE_DATETIME_MINUTE_PATTERN = "MM-dd HH:mm";
     FastDateFormat SIMPLE_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(SIMPLE_DATETIME_MINUTE_PATTERN);
     String SIMPLE_DATETIME_SECOND_PATTERN = "MM-dd HH:mm:ss";
     FastDateFormat SIMPLE_DATETIME_SECOND_FORMAT = FastDateFormat.getInstance(SIMPLE_DATETIME_SECOND_PATTERN);
     String SIMPLE_TIME_MINUTE_PATTERN = "HH:mm";
     FastDateFormat SIMPLE_TIME_MINUTE_FORMAT = FastDateFormat.getInstance(SIMPLE_TIME_MINUTE_PATTERN);

     String NORM_TIME_PATTERN = "HH:mm:ss";
     FastDateFormat NORM_TIME_FORMAT = FastDateFormat.getInstance(NORM_TIME_PATTERN);
     String PURE_TIME_PATTERN = "HHmmss";
     FastDateFormat PURE_TIME_FORMAT = FastDateFormat.getInstance(PURE_TIME_PATTERN);

     String EN_SIMPLE_DATE_DAY_PATTERN = "MM/dd";
     FastDateFormat EN_SIMPLE_DATE_DAY_FORMAT = FastDateFormat.getInstance(EN_SIMPLE_DATE_DAY_PATTERN);
     String EN_SIMPLE_DATETIME_MINUTE_PATTERN = "MM/dd HH:mm";
     FastDateFormat EN_SIMPLE_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(EN_SIMPLE_DATETIME_MINUTE_PATTERN);
     String EN_SIMPLE_DATETIME_SECOND_PATTERN = "MM/dd HH:mm:ss";
     FastDateFormat EN_SIMPLE_DATETIME_SECOND_FORMAT = FastDateFormat.getInstance(EN_SIMPLE_DATETIME_SECOND_PATTERN);

     String CN_SIMPLE_DATE_DAY_PATTERN = "MM月dd日";
     FastDateFormat CN_SIMPLE_DATE_DAY_FORMAT = FastDateFormat.getInstance(CN_SIMPLE_DATE_DAY_PATTERN);
     String CN_SIMPLE_DATETIME_MINUTE_PATTERN = "MM月dd日 HH:mm";
     FastDateFormat CN_SIMPLE_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(CN_SIMPLE_DATETIME_MINUTE_PATTERN);
     String CN_SIMPLE_DATETIME_SECOND_PATTERN = "MM月dd日 HH:mm:ss";
     FastDateFormat CN_SIMPLE_DATETIME_SECOND_FORMAT = FastDateFormat.getInstance(CN_SIMPLE_DATETIME_SECOND_PATTERN);

     String SPECIAL_SIMPLE_DATE_PATTERN = "yyyy.MM.dd";
     FastDateFormat SPECIAL_SIMPLE_DATE_FORMAT = FastDateFormat.getInstance(SPECIAL_SIMPLE_DATE_PATTERN);
     String SPECIAL_SIMPLE_DATE_DAY_PATTERN = "MM.dd";
     FastDateFormat SPECIAL_SIMPLE_DATE_DAY_FORMAT = FastDateFormat.getInstance(SPECIAL_SIMPLE_DATE_DAY_PATTERN);

     String AD_DATETIME_PATTERN = "G y-MM-dd Z E HH:mm:ss:SSS a";
     FastDateFormat AD_DATETIME_FORMAT = FastDateFormat.getInstance(AD_DATETIME_PATTERN);

     String HTTP_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

     String JDK_DATETIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

     String UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

}
