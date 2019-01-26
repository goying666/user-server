package com.renchaigao.zujuba.userserver.uti;

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
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SignInUserFunctions {

    MongoTemplate mongoTemplate;
    UserMapper userMapper;
    UserRankMapper userRankMapper;
    UserOpenInfoMapper userOpenInfoMapper;
    public SignInUserFunctions(UserMapper userMapper, MongoTemplate mongoTemplate,UserRankMapper userRankMapper,UserOpenInfoMapper userOpenInfoMapper){
        this.userMapper = userMapper;
        this.mongoTemplate = mongoTemplate;
        this.userRankMapper = userRankMapper;
        this.userOpenInfoMapper = userOpenInfoMapper;
    };
    /*
     * 说明：发送验证码步骤
     */
    public ResponseEntity SignInUserGetVerCode(String phoneNumber) {
        //没有注册过
//            查重
        List<User> userList = userMapper.selectByTelephone(phoneNumber);
        if (userList.size() > 0) {
            return new ResponseEntity(RespCode.USERHAD, null);
        }
//            查询是否获取过验证码，验证码是否过期；
        VerificationCodeInfo verificationCodeInfo = mongoTemplate.findById(phoneNumber, VerificationCodeInfo.class,
                MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_VERIFICATION_CODE);
        if (null != verificationCodeInfo) {
//                判断是否过期
//                VerificationCodeInfo verCode = verificationCodeInfos.get(0);
//                verCode.getUpTime();
            String lastCTime = verificationCodeInfo.getCreateTime();
            Integer requestTimes = verificationCodeInfo.getRequestTimes();
            if (requestTimes > 5) {
                return new ResponseEntity(RespCode.VERCODE_REQUEST_TOO_MUCH, null);
            }
            if (dateUse.DateCompareByNow(lastCTime) < 30000 * requestTimes) {
                return new ResponseEntity(RespCode.VERCODE_BUSY, null);
            }
            verificationCodeInfo.setRequestTimes(requestTimes + 1);
        } else {
            verificationCodeInfo = new VerificationCodeInfo();
            verificationCodeInfo.setId(phoneNumber);
            verificationCodeInfo.setRequestTimes(1);
            verificationCodeInfo.setPhoneNumber(phoneNumber);
            verificationCodeInfo.setState("C");
        }
//            生成验证码 并 发送至手机
        String verCode = AuthenticationCodeMethod.CreateFourCode();
        if (!AuthenticationCodeMethod.GetAuthenticationCode(phoneNumber, verCode)) {
            return new ResponseEntity(RespCode.VERCODE_EXCEPTION, "verCode faile");
        }
        verificationCodeInfo.setCode(verCode);
        mongoTemplate.save(verificationCodeInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_VERIFICATION_CODE);
//            返回验证码，用以继续注册
        return new ResponseEntity(RespCode.VERCODE_SUCCESS, verCode);
    }

    /*
     * 说明：创建UserInfo信息
     */
    public ResponseEntity SignInUserCreateUserInfo(String phoneNumber, String jsonObjectString) {
        User userJson = JSONObject.parseObject(jsonObjectString,User.class);
        User user = new User();
//        基础部分：MySQL内的UserPart
        try {
            user = CreateNormalInfoPart(phoneNumber, userJson);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new ResponseEntity(RespCode.SIGNIN_FAIL,null);
        }
//          先将user内的信息付给userInfo
        UserInfo userInfo = JSONObject.parseObject(JSONObject.toJSONString(user), UserInfo.class);
        String userId = userInfo.getId();
        /*  创建各个object */
//          创建myRankInfo
        CreateMyRankInfo(userId);
//          创建myTeamsInfo
        CreatMyTeamsInfo(userId);
//          创建myGamesInfo
        CreatMyGamesInfo(userId);
//          创建myStoresInfo
        CreatMyPlacesInfo(userId);
//          创建myPhotoInfo
        CreatMyPhotoInfo(userId);
//          创建myAddress
        CreatMyAddress(userId);
//          创建mySpendInfo
        CreatMySpendInfo(userId);
//          创建myMessageInfo
        CreatMyMessageInfo(userId);
//          创建myFriendInfo
        CreatMyFriendInfo(userId);
//          创建myIntegrationInfo
        CreatMyIntegrationInfo(userId);
//          创建myPermissionInfo
        CreatMyPermissionInfo(userId);
//          创建user的openInfo
        CreatMyOpenInfo(user);
//          组装userInfo
        userInfo = AssembleAllInfo(userId);
        mongoTemplate.save(userInfo);
        return new ResponseEntity(RespCode.SIGNIN_SUCCESS, userInfo);
    }

    /*
     * 说明：创建用户的 基础信息
     */
    private User CreateNormalInfoPart(String phoneNumber, User user)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        //          验证通过，初始化用户信息；
        /*  为user 配置相应的属性； */
        //配置id属性;
        user.setId(UUIDUtil.getUUID());
//       配置密码
//        user.setUserPWD(user.getUserPWD());
        //配置age属性;
        user.setAge("0");
        //配置ageLevel属性;
        user.setAgeLevel("0");
        //配置realName属性;
        user.setRealName("0");
        //配置nickName属性;
        user.setNickName("0");
        //配置idCard属性;
        user.setIdCard("0");
        //配置gender属性;
        user.setGender("0");
        //配置job属性;
        user.setJob("0");
        //配置telephone属性;
//        user.setTelephone(phoneNumber);
        //配置marriage属性;
        user.setMarriage("0");
        //配置picPath属性;
        user.setPicPath("0");
        //配置deleteStyle属性;
        user.setDeleteStyle(false);
        //配置upTime属性;
        user.setUpTime(dateUse.GetStringDateNow());
        //配置token属性;
//            生成TOKEN
        String token;
        token = TokenMaker.EncoderByMd5(phoneNumber);
        user.setToken(token);
        userMapper.insert(user);
        return user;
    }

    /*
     * 说明：创建用户的 排名信息
     */
    private void CreateMyRankInfo(String userId) {
        UserRank userRank = new UserRank();
        userRank.setId(userId);
        userRank.setDeleteStyle(false);
        userRank.setUpTime(dateUse.DateToString(new Date()));
        userRank.setIntegral(0);
        userRank.setCityIntegralRank(0);
        userRank.setCountryIntegralRank(0);
        userRank.setProvinceIntegralRank(0);
        userRank.setUserId(userId);
        userRankMapper.updateByPrimaryKeySelective(userRank);
    }

    /*
     * 说明：创建用户的 组局信息
     */
    private void CreatMyTeamsInfo(String userId) {
        UserTeams userTeams = new UserTeams();
        userTeams.setId(userId);
        mongoTemplate.save(userTeams, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_TEAMS);
    }

    /*
     * 说明：创建用户的 游戏信息
     */
    private void CreatMyGamesInfo(String userId) {
        UserGames userGames = new UserGames();
        userGames.setId(userId);
        mongoTemplate.save(userGames, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_GAMES);
    }

    /*
     * 说明：创建用户的 场地信息
     */
    private void CreatMyPlacesInfo(String userId) {
        UserPlaces userPlaces = new UserPlaces();
        userPlaces.setId(userId);
        mongoTemplate.save(userPlaces, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_PLACES);
    }

    /*
     * 说明：创建用户的 图片信息
     */
    private void CreatMyPhotoInfo(String userId) {
        UserPhotosInfo userPhotosInfo = new UserPhotosInfo();
        userPhotosInfo.setId(userId);
        mongoTemplate.save(userPhotosInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_PHOTOS);
    }

    /*
     * 说明：创建用户的 位置信息
     */
    private void CreatMyAddress(String userId) {
        AddressInfo myAddressInfo = new AddressInfo();
        myAddressInfo.setId(userId);
        myAddressInfo.setAddressClass("user");
        mongoTemplate.save(myAddressInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_ADDRESS_INFO);
    }

    /*
     * 说明：创建用户的 消费信息
     */
    private void CreatMySpendInfo(String userId) {
        UserSpendInfo userSpendInfo = new UserSpendInfo();
        userSpendInfo.setId(userId);
        mongoTemplate.save(userSpendInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_SPEND);
    }

    /*
     * 说明：创建用户的 聊天信息
     */
    private void CreatMyMessageInfo(String userId) {
        UserMessagesInfo userMessagesInfo = new UserMessagesInfo();
        userMessagesInfo.setId(userId);
        mongoTemplate.save(userMessagesInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_MESSAGE);
    }

    /*
     * 说明：创建用户的 好友信息
     */
    private void CreatMyFriendInfo(String userId) {
        UserFriendInfo userFriendInfo = new UserFriendInfo();
        userFriendInfo.setId(userId);
        mongoTemplate.save(userFriendInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_FRIEND);
    }

    /*
     * 说明：创建用户的 积分信息
     */
    private void CreatMyIntegrationInfo(String userId) {
        UserIntegrationInfo userIntegrationInfo = new UserIntegrationInfo();
        userIntegrationInfo.setId(userId);
        mongoTemplate.save(userIntegrationInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_INTEGRATION);
    }

    /*
     * 说明：创建用户的 权限信息
     */
    private void CreatMyPermissionInfo(String userId) {
        UserPermissionInfo userPermissionInfo = new UserPermissionInfo();
        userPermissionInfo.setId(userId);
        mongoTemplate.save(userPermissionInfo, MongoDBCollectionsName.MONGO_DB_COLLECIONS_NAME_USER_PERMISSION);
    }


    /*
     * 说明：创建用户的 公开信息
     */
    private void CreatMyOpenInfo(User user) {
        UserOpenInfo userOpenInfo = JSONObject.parseObject(JSONObject.toJSONString(user), UserOpenInfo.class);
        userOpenInfo.setUserId(userOpenInfo.getId());
        userOpenInfo.setUpTime(dateUse.GetStringDateNow());
        userOpenInfo.setDeleteStyle(false);
        userOpenInfoMapper.insert(userOpenInfo);
    }


    /*
     * 说明：组装用户所有信息userInfo
     */
    public UserInfo AssembleAllInfo(String userId) {
        UserInfo userInfo = JSONObject.parseObject(JSONObject.toJSONString(userMapper.selectByPrimaryKey(userId)), UserInfo.class);
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
        userInfo.setUpTime(dateUse.GetStringDateNow());
        return userInfo;
    }


}
