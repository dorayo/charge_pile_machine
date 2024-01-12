package com.huamar.charge.pile;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TimeSlotsExample {




    public static void main(String[] args) {
//        LocalTime startTime = LocalTime.of(0, 0); // 开始时间，即0点
//        int slotDurationMinutes = 30; // 每个时段的时长，30分钟
//
//        List<String> timeSlots = getTimeSlots(startTime, slotDurationMinutes);
//
//        // 打印每个时段的开始时间
//        for (int i = 0; i < timeSlots.size(); i++) {
//            System.out.println("时段 " + i + ": " + timeSlots.get(i));
//        }
//
//
//        List<MyObject> objects = new ArrayList<>();
//        objects.add(new MyObject(new BigDecimal("10.5")));
//        objects.add(new MyObject(new BigDecimal("5.2")));
//        objects.add(new MyObject(new BigDecimal("8.7")));
//
//        List<MyObject> sortedList = objects.stream()
//                .sorted((obj1, obj2) -> obj2.getValue().compareTo(obj1.getValue()))
//                .collect(Collectors.toList());
//
//        for (MyObject obj : sortedList) {
//            System.out.println(obj.getValue());
//        }


        String startTime = "00:00";
        String endTime = "00:00";

        String[] startSplit = startTime.split(":");
        int startHour = Integer.parseInt(startSplit[0]);
        int startMinute = Integer.parseInt(startSplit[1]);
        int startIndex = (startHour * 60 + startMinute) / 30;

        String[] endSplit = endTime.split(":");
        int endHour = Integer.parseInt(endSplit[0]);
        int endMinute = Integer.parseInt(endSplit[1]);
        int endIndex = (endHour * 60 + endMinute) / 30;

        List<Integer> indices = new ArrayList<>();
        for (int i = startIndex; i <= endIndex; i++) {
            indices.add(i);
        }
        System.out.printf(indices.toString());
    }

    public static List<String> getTimeSlots(LocalTime startTime, int slotDurationMinutes) {
        List<String> timeSlots = new ArrayList<>();

        LocalTime currentTime = startTime;
        for (int i = 0; i < 48; i++) {
            String timeSlot = currentTime.toString();
            timeSlots.add(timeSlot);
            currentTime = currentTime.plusMinutes(slotDurationMinutes);
        }

        return timeSlots;
    }

    public static class MyObject {
        private BigDecimal value;

        public MyObject(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getValue() {
            return value;
        }
    }


}
