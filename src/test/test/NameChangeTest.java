package test;

import com.enjoybt.util.UmFileUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NameChangeTest {

    private String origin = null;
    private String replace = null;

    @Before
    public void be() {
        origin = "r120_v070_erea_unis_h087_GDPS_SAMPLE.2018121600.gb2";
    }

    @Test
    public void te() {

        replace = UmFileUtil.changeFileNameVdrsStyleExample(origin);

        Assert.assertNotNull(replace);
        Assert.assertEquals(39, replace.length());
    }

    @After
    public void af() {
        System.out.println(replace);
    }
}
