package demo;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Objects;

@Slf4j
public class ByteBufferTest {


    @DisplayName("BufferTest")
    @Test
    public void test1(){
        //byte[] b = {0,1,2,3,4,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,0,1,2,3,4,5,6,7,8,9,0};
        //byte[] b = {1,2,3,4,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,0,1,2,3,4,5,6,7,8,9,0};
        byte[] b = {1, 2, 3, 4, -1, -1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, -1, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1};
        ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);
        ByteBuffer failBuffer = ByteBuffer.allocate(b.length);
        byteBuffer.put(b);
        byteBuffer.flip();

        while (true){
            byteBuffer.mark();
            byte item = byteBuffer.get();
            if(item != -1){
                failBuffer.put(item);
                continue;
            }

            byteBuffer.mark();
            byte next = byteBuffer.get();
            if(next == -1){
                failBuffer.put(next);
                byteBuffer.reset();
                break;
            }
            byteBuffer.reset();
            break;
        }

        failBuffer.flip();
        if(failBuffer.hasRemaining()){
            byte[] bytes = new byte[byteBuffer.limit() - byteBuffer.position()];
            byteBuffer.get(bytes);
            log.info("failBuffer haData:{}", "true");
            log.info("byteBuffer:{}", bytes);

            bytes = new byte[failBuffer.limit()];
            failBuffer.get(bytes);
            log.info("failBuffer:{}", bytes);
            return;
        }

        byteBuffer.rewind();

        log.info("byteBuffer:{}", byteBuffer.array());
        log.info("failBuffer:{}", failBuffer.array());
    }

    @DisplayName("BufferTest")
    @Test
    public void test2(){
        byte[] b = {0,1,2,3,4,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,0,1,2,3,4,5,6,7,8,9,0};
        ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);
        ByteBuffer failBuffer = ByteBuffer.allocate(b.length);
        byteBuffer.put(b);

        byteBuffer.flip();
        byteBuffer.mark();
        byte first = byteBuffer.get();
        byte next = byteBuffer.get();
        if(Objects.equals(first, 0) && !Objects.equals(next, 0)){
            return;
        }

        if(Objects.equals(first, 0) && Objects.equals(next, 0)){
            return;
        }

        log.info("byteBuffer:{}", byteBuffer.array());
        log.info("failBuffer:{}", failBuffer.array());
    }


}
