package com.renchaigao.zujuba.userserver;

import normal.dateUse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServerApplicationTests {

	@Test
	public void contextLoads() {
	}

	@Test
	public void dateUseTest(){
		String newDatStr = dateUse.DateToString(new Date());
		Date dateNow = dateUse.StringToDate(newDatStr);
		newDatStr = null;
	}
}
