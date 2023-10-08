import java.time.Duration;
import java.time.LocalDateTime;

public class TestApplication {

    public static void main(String[] args) {
        LocalDateTime start = LocalDateTime.of(2022, 5, 12, 1, 1);
        LocalDateTime end = LocalDateTime.of(2022, 5, 9, 1, 1);
        System.out.println("相差的天数: " + Duration.between(start, end).toDays() + "天");
        System.out.println("相差的小时数: " + Duration.between(start, end).toHours() + "小时");
        System.out.println("相差的分钟数: " + Duration.between(start, end).toMinutes() + "分钟");
        System.out.println("相差的毫秒数: " + Duration.between(start, end).toMillis() + "毫秒");
    }
}
