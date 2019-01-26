package com.renchaigao.zujuba.userserver;

import com.renchaigao.zujuba.dao.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServerApplicationTests {

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	UserMapper userMapper;

	@Test
	public void contextLoads() {

//		User user = userMapper.selectByPrimaryKey("aaa");
//		user = null;
//		UserOpenInfo userOpenInfo = new UserOpenInfo();
//		 Update update = new Update();
//                mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is("0f03f89a241b482eab11da65ced64700")),
//                        update.set("userOpenInfo.id","999"), UserInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_INFO);
	}

//	@Test
//	public void dateUseTest(){
//		String newDatStr = dateUse.DateToString(new Date());
//		Date dateNow = dateUse.StringToDate(newDatStr);
//		newDatStr = null;
//	}
}
