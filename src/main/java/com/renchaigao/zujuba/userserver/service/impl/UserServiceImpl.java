package com.renchaigao.zujuba.userserver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.renchaigao.zujuba.PropertiesConfig.MongoDBCollectionsName;
import com.renchaigao.zujuba.dao.User;
import com.renchaigao.zujuba.dao.mapper.*;
import com.renchaigao.zujuba.domain.response.RespCode;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import com.renchaigao.zujuba.mongoDB.info.AddressInfo;
import com.renchaigao.zujuba.mongoDB.info.VerificationCodeInfo;
import com.renchaigao.zujuba.mongoDB.info.user.UserInfo;
import com.renchaigao.zujuba.userserver.service.UserService;
import com.renchaigao.zujuba.userserver.uti.LoginUserFunctions;
import com.renchaigao.zujuba.userserver.uti.SignInUserFunctions;
import com.renchaigao.zujuba.userserver.uti.UpdateUserFunctions;
import normal.dateUse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static Logger logger = Logger.getLogger(UserServiceImpl.class);


    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    UserMapper userMapper;

    @Autowired
    UserLoginMapper userLoginMapper;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    UserRankMapper userRankMapper;

    @Autowired
    FriendMapper friendMapper;

    @Autowired
    AddressMapper addressMapper;

    @Autowired
    UserOpenInfoMapper userOpenInfoMapper;

    @Override
    public ResponseEntity SignInUser(String phoneNumber, String verCode,String mode, String jsonObjectString) {
        SignInUserFunctions signInUserFunctions = new SignInUserFunctions(userMapper, mongoTemplate, userRankMapper, userOpenInfoMapper);
        switch (mode){
            case "vercode"://请求获取验证码
                return signInUserFunctions.SignInUserGetVerCode(phoneNumber);
            case "signin"://通过验证码注册
                String verCodeFind;
                try{
                    verCodeFind = mongoTemplate.findById(phoneNumber, VerificationCodeInfo.class).getCode();
                }catch (Exception e){
                    return new ResponseEntity(RespCode.SIGNIN_WRONG,"Wrong verCode");
                }
                if (!verCodeFind.equals(verCode)) {
                    return new ResponseEntity(RespCode.SIGNIN_WRONG, "Wrong verCode");
                } else {
                    mongoTemplate.remove(Query.query(Criteria.where("_id").is(phoneNumber)),
                            VerificationCodeInfo.class, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_VERIFICATION_CODE);
//            创建user信息；
                    return signInUserFunctions.SignInUserCreateUserInfo(phoneNumber, jsonObjectString);
                }
        }
        return new ResponseEntity(RespCode.WRONGIP,"Wrong mode");
    }

    @Override
    public ResponseEntity LoginUser(
            String loginStyle, String phoneNumber, String userId, String jsonObjectString) {
        LoginUserFunctions loginUserFunctions = new LoginUserFunctions(userMapper,userLoginMapper);
//        通过loginStyle判断登录方式；
        switch (loginStyle) {
            case "auto":
//                检查用户是否存在；
                User user = userMapper.selectByPrimaryKey(userId);
                if (user == null) {
                    return new ResponseEntity(RespCode.LOGIN_AUTO_FAIL, "LOGIN_AUTO_FAIL");
                }
//          判断token的正误
                Date date = dateUse.StringToDate(user.getUpTime());
                if (!dateUse.tokenDateCompare(date)) {
                    return new ResponseEntity(RespCode.LOGIN_AUTO_WRONG, "LOGIN_AUTO_WRONG");
                }
                String userToken = JSONObject.parseObject(jsonObjectString).getString("userToken");
                if (!userToken.equals(user.getToken())) {
                    return new ResponseEntity(RespCode.LOGIN_AUTO_WRONG, "LOGIN_AUTO_WRONG");
                }
                return loginUserFunctions.checkUserInfoFUNC(user);
            case "secret":
//                检查用户账号唯一性
                List<User> userArrayList = userMapper.selectByTelephone(phoneNumber);
                if (userArrayList.size() == 0) {
                    return new ResponseEntity(RespCode.LOGIN_SECRET_WRONG, "LOGIN_SECRET_WRONG");
                }
                User usersql = userArrayList.get(0);
                String userPWD = JSONObject.parseObject(jsonObjectString).getString("userPWD");
                if (usersql.getUserPWD().equals(userPWD)) {
                    return loginUserFunctions.checkUserInfoFUNC(usersql);
                } else {
                    return new ResponseEntity(RespCode.LOGIN_SECRET_FAIL, "LOGIN_SECRET_FAIL");
                }
        }
        return null;

    }

    @Override
    public ResponseEntity GetUser(String userId, String jsonObjectString) {
        SignInUserFunctions signInUserFunctions = new SignInUserFunctions(userMapper, mongoTemplate, userRankMapper, userOpenInfoMapper);
        UserInfo userInfo = signInUserFunctions.AssembleAllInfo(userId);
        mongoTemplate.save(userInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_INFO);
        return new ResponseEntity(RespCode.SUCCESS, userInfo);
    }

    @Override
    public ResponseEntity UpdateUser(String updateStyle, String userId, String jsonObjectString) {
//        String userToken = JSONObject.parseObject(jsonObjectString).getString("userToken");
        UpdateUserFunctions updateUserFunctions = new UpdateUserFunctions(userMapper,userRankMapper,mongoTemplate);
        switch (updateStyle) {
            case "basicInfo":
                return updateUserFunctions.UpdateUserBasicPartFunction(userId, jsonObjectString);
            case "addressInfo":
                return updateUserFunctions.UpdateUserAddressInfoFunction(userId, jsonObjectString);
//            case "photoInfo":
//                userMongoDB.setUserOpenInfo(userOpenInfoMapper.selectByPrimaryKey(userMongoDB.getUserOpenInfo().getId()));
//                break;
        }
        return new ResponseEntity(RespCode.WARN, "wrong part");
    }

}
