import org.junit.Test;

import java.util.Arrays;

public class MyTest {
    @Test
    public void t1() {
        String path = "/contextPath/servletName";
        String[] split = path.split("/");
        System.out.println(Arrays.toString(split));
    }
}
