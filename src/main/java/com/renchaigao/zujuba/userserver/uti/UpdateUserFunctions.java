package com.renchaigao.zujuba.userserver.uti;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.renchaigao.zujuba.PropertiesConfig.MongoDBCollectionsName;
import com.renchaigao.zujuba.dao.User;
import com.renchaigao.zujuba.dao.UserOpenInfo;
import com.renchaigao.zujuba.dao.UserRank;
import com.renchaigao.zujuba.dao.mapper.*;
import com.renchaigao.zujuba.domain.response.RespCode;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import com.renchaigao.zujuba.mongoDB.info.AddressInfo;
import com.renchaigao.zujuba.mongoDB.info.VerificationCodeInfo;
import com.renchaigao.zujuba.mongoDB.info.user.*;
import normal.TokenMaker;
import normal.UUIDUtil;
import normal.dateUse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UpdateUserFunctions {

    UserMapper userMapper;
    UserRankMapper userRankMapper;
    MongoTemplate mongoTemplate;

    public UpdateUserFunctions(UserMapper userMapper, UserRankMapper userRankMapper, MongoTemplate mongoTemplate) {
        this.userMapper = userMapper;
        this.userRankMapper = userRankMapper;
        this.mongoTemplate = mongoTemplate;
    }

    /*
     * 说明：更新用户个人的基础信息
     */
    public ResponseEntity UpdateUserBasicPartFunction(String userId, String jsonObjectString
    ) {
        User user = JSONObject.parseObject(jsonObjectString, User.class);
        userMapper.updateByPrimaryKey(user);
        UserInfo userInfo = AssembleAllInfo(userId, user);
        userInfo.setUpTime(dateUse.GetStringDateNow());
        mongoTemplate.save(userInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_INFO);
        return new ResponseEntity(RespCode.SUSER_UPDATE_SUCCESS, userInfo);
    }


    /*
     * 说明：更新用户个人的位置信息
     */
    public ResponseEntity UpdateUserAddressInfoFunction(String userId, String jsonObjectString) {
        AddressInfo addressInfo = JSONObject.parseObject(jsonObjectString, AddressInfo.class);
        addressInfo.setId(userId);
        addressInfo.setAddressClass("user");
        addressInfo.setUpTime(dateUse.GetStringDateNow());
        mongoTemplate.save(addressInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_ADDRESS_INFO);
        return new ResponseEntity(RespCode.SUCCESS, addressInfo);
    }

    /*
     * 说明：组装用户所有信息userInfo
     */
    private UserInfo AssembleAllInfo(String userId, User user) {
        UserInfo userInfo = JSONObject.parseObject(JSONObject.toJSONString(user), UserInfo.class);
        userInfo.setUserRank(userRankMapper.selectByPrimaryKey(userId));
        userInfo.setUserTeams(mongoTemplate.findById(userId, UserTeams.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_TEAMS));
        userInfo.setUserGames(mongoTemplate.findById(userId, UserGames.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_GAMES));
        userInfo.setUserPlaces(mongoTemplate.findById(userId, UserPlaces.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_PLACES));
        userInfo.setUserPhotosInfo(mongoTemplate.findById(userId, UserPhotosInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_PHOTOS));
        userInfo.setAddressInfo(mongoTemplate.findById(userId, AddressInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_ADDRESS_INFO));
        userInfo.setUserSpendInfo(mongoTemplate.findById(userId, UserSpendInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_SPEND));
        userInfo.setUserMessagesInfo(mongoTemplate.findById(userId, UserMessagesInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_MESSAGE));
        userInfo.setUserFriendInfo(mongoTemplate.findById(userId, UserFriendInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_FRIEND));
        userInfo.setUserIntegrationInfo(mongoTemplate.findById(userId, UserIntegrationInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_INTEGRATION));
        userInfo.setUserPermissionInfo(mongoTemplate.findById(userId, UserPermissionInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_PERMISSION));
        userInfo.setUserOpenInfo(mongoTemplate.findById(userId, UserOpenInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_OPENINFO));
        return userInfo;
    }


}
