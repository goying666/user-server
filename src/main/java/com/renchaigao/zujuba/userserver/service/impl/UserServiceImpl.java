package com.renchaigao.zujuba.userserver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.renchaigao.zujuba.dao.*;
import com.renchaigao.zujuba.dao.mapper.*;
import com.renchaigao.zujuba.domain.response.RespCode;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import com.renchaigao.zujuba.mongoDB.info.*;
import com.renchaigao.zujuba.mongoDB.info.user.UserInfo;
import com.renchaigao.zujuba.mongoDB.info.user.myPlayGamesInfo;
import com.renchaigao.zujuba.mongoDB.info.user.myStoresInfo;
import com.renchaigao.zujuba.mongoDB.info.user.myTeamsInfo;
import com.renchaigao.zujuba.userserver.service.UserService;
import normal.TokenMaker;
import normal.UUIDUtil;
import normal.dateUse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
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
//    自动登陆
    public ResponseEntity autoLoginUser(User userApp) {
        String telephone = userApp.getTelephone();
        List<User> userList = userMapper.selectByTelephone(telephone);
        if (userList.size()==0){
            return new ResponseEntity(RespCode.NOUSER,userApp);
        }
        User userSql = userList.get(0);
        String strdate = userSql.getUpTime();
//          判断token的正误
        Date date = dateUse.StringToDate(userSql.getUpTime());
        if (!dateUse.tokenDateCompare(date)) {
            return new ResponseEntity(RespCode.TOKENOLD, userApp);
        }
        if (!userSql.getToken().equals(userApp.getToken())) {
            return new ResponseEntity(RespCode.TOKENWRONG, userApp);
        }
        return checkUserInfoFUNC(telephone, userSql);

    }

    @Override
//    密码登陆
    public ResponseEntity secretLoginUser(User userApp) {
        String telephone = userApp.getTelephone();
        List<User> userArrayList = userMapper.selectByTelephone(telephone);
        if (userArrayList.size() == 0) {
            return new ResponseEntity(RespCode.TELEPHONENOJOIN, userApp);
        }
        User userSql = userMapper.selectByTelephone(telephone).get(0);
        if (userSql.getUserPWD().equals("0")) {
            return new ResponseEntity(RespCode.PASSWORDMISSING, userApp);
        }
        if (userSql.getUserPWD().equals(userApp.getUserPWD())) {
            return checkUserInfoFUNC(telephone, userSql);
        } else {
            return new ResponseEntity(RespCode.WRONGPWD, userApp);
        }
    }

    @Override
//    验证码登陆
    public ResponseEntity vercodeLoginUser(User userApp, String verCode) {
        String telephone = userApp.getTelephone();
        User userSql = userMapper.selectByTelephone(telephone).get(0);
        if (checkVercode(verCode)) {
            return checkUserInfoFUNC(telephone, userSql);
        } else {
            return new ResponseEntity(RespCode.WRONGPWD, userApp);
        }
    }

    @Override
    //系统添加新用户信息
    public ResponseEntity addUser(User userApp, String verCode) {
        String telephone = userApp.getTelephone();
        String uniqueId = userApp.getUniqueId();
        User userRet = new User();
        String token;
        if (userMapper.selectByTelephone(telephone).size() != 0)
            return new ResponseEntity(RespCode.USERHAD, userApp);
        try {
            if (null != userApp.getTelephone()) {
//          验证码判断部分

//          验证码判断部分
                try {
                    token = TokenMaker.EncoderByMd5(telephone);
                } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return new ResponseEntity(RespCode.TOKENEXCEPTION, e);
                }
                /*  为user 配置相应的属性； */
                //配置id属性;
                userRet.setId(UUIDUtil.getUUID());

//              注册码注册，密码初始为123456
                userRet.setUserPWD("123456");
                //配置age属性;
                userRet.setAge("0");
                //配置ageLevel属性;
                userRet.setAgeLevel("0");
                //配置realName属性;
                userRet.setRealName("0");
                //配置nickName属性;
                userRet.setNickName("0");
                //配置idCard属性;
                userRet.setIdCard("0");
                //配置gender属性;
                userRet.setGender("0");
                //配置job属性;
                userRet.setJob("0");
                //配置telephone属性;
                userRet.setTelephone(telephone);
                //配置marriage属性;
                userRet.setMarriage("0");
                //配置userPWD属性;
                userRet.setUserPWD("0");
                //配置picPath属性;
                userRet.setPicPath("0");
                //配置deleteStyle属性;
                userRet.setDeleteStyle(false);
                //配置myOpenInfoId属性;
                userRet.setMyOpenInfoId(userRet.getId());
                //配置userInfoId属性;
//                userRet.setUserInfoId(UUIDUtil.getUUID());
                //配置uniqueId属性;
                userRet.setUniqueId(uniqueId);
                //配置myTeamsId属性;
                userRet.setMyTeamsId(userRet.getId());
                //配置myGamesId属性;
                userRet.setMyGamesId(userRet.getId());
                //配置myStoresId属性;
                userRet.setMyStoresId(userRet.getId());
                //配置 photoInfoId属性;
                userRet.setPhotoInfoId(userRet.getId());

                //配置 myAddressId属性;
                userRet.setMyAddressId(userRet.getId());

                //配置 myRankInfoId属性;
                userRet.setMyRankInfoId(userRet.getId());
                //配置 mySpendInfoId属性;
                userRet.setMySpendInfoId(userRet.getId());
                //配置 myMessageInfoId属性;
                userRet.setMyMessageInfoId(userRet.getId());
                //配置 myFriendInfoId属性;
                userRet.setMyFriendInfoId(userRet.getId());
                //配置 myIntegrationInfoId属性;
                userRet.setMyIntegrationInfoId(userRet.getId());
                //配置 peopleListId属性;
                userRet.setPeopleListId(userRet.getId());
                //配置 myPermissionInfoId属性;
                userRet.setMyPermissionInfoId(userRet.getId());
                //配置upTime属性;
                userRet.setUpTime(dateUse.GetStringDateNow());
                //配置token属性;
                userRet.setToken(token);

                if (userMapper.insert(userRet) == 1) {//sql 添加user数据成功；

                    redisTemplate.opsForValue().set(userRet.getId(),
                            JSONObject.toJSONString(userRet));
                    return new ResponseEntity(RespCode.SUCCESS, userRet);
                } else {
                    return new ResponseEntity(RespCode.USERADDFAIL, userRet);
                }
            } else {
                return new ResponseEntity(RespCode.USERNOTEL, userApp);
            }
        } catch (Exception e) {
            return new ResponseEntity(RespCode.EXCEPTION, e);
        }
    }

    @Override
    public ResponseEntity getUserInfo(User userApp) {
        long startTime = System.currentTimeMillis();    //获取开始时间
        UserInfo userInfo = mongoTemplate.findById(userApp.getId(), UserInfo.class);
        if (userInfo == null)//没有userInfo，创建一个在MongoDB里。
        {

            User userSQL = userMapper.selectByPrimaryKey(userApp.getId());
//            先将user内的信息付给userInfo
            userInfo = JSONObject.parseObject(JSONObject.toJSONString(userApp), UserInfo.class);
            /*  创建各个object */
//          创建myRankInfo
            UserRank userRank = new UserRank();
            userRank.setId(userApp.getMyRankInfoId());
            userRank.setDeleteStyle(false);
            userRank.setUpTime(dateUse.DateToString(new Date()));
            userRank.setIntegral(0);
            userRank.setCityIntegralRank(0);
            userRank.setCountryIntegralRank(0);
            userRank.setProvinceIntegralRank(0);
            userRank.setUserId(userApp.getId());
            userRankMapper.updateByPrimaryKeySelective(userRank);
            userInfo.setMyRankInfo(userRank);
            //          创建myTeamsInfo
            myTeamsInfo myTeamsInfo = new myTeamsInfo();
            myTeamsInfo.setId(userApp.getMyTeamsId());
            mongoTemplate.save(myTeamsInfo);
            userInfo.setMyTeamsInfo(myTeamsInfo);
//          创建myGamesInfo
            myPlayGamesInfo myPlayGamesInfo = new myPlayGamesInfo();
            myPlayGamesInfo.setId(userApp.getMyGamesId());
            mongoTemplate.save(myPlayGamesInfo);
            userInfo.setMyPlayGamesInfo(myPlayGamesInfo);
//          创建myStoresInfo
            myStoresInfo myStoresInfo = new myStoresInfo();
            myStoresInfo.setId(userApp.getMyStoresId());
            mongoTemplate.save(myStoresInfo);
            userInfo.setMyStoresInfo(myStoresInfo);
//          创建myPhotoInfo
            Photo photo = new Photo();
            photo.setId(userApp.getPhotoInfoId());
            mongoTemplate.save(photo);
            userInfo.setMyPhotoInfo(photo);
//          创建myAddress
            AddressInfo myAddressInfo = new AddressInfo();
            myAddressInfo.setId(userApp.getMyAddressId());
            mongoTemplate.save(myAddressInfo,"AddressInfo");
            userInfo.setMyAddressInfo(myAddressInfo);
//          创建mySpendInfo
            userSpendInfo userSpendInfo = new userSpendInfo();
            userSpendInfo.setId(userApp.getMySpendInfoId());
            mongoTemplate.save(userSpendInfo);
            userInfo.setMySpendInfo(userSpendInfo);
//          创建myMessageInfo

//          创建myFriendInfo
            userFriendInfo userFriendInfo = new userFriendInfo();
            userFriendInfo.setId(userApp.getMyFriendInfoId());
            mongoTemplate.save(userFriendInfo);
            userInfo.setMyFreiendInfo(userFriendInfo);
//          创建myIntegrationInfo

//          创建peopleList

//          创建myPermissionInfo
            userPermissionInfo userPermissionInfo = new userPermissionInfo();
            userPermissionInfo.setId(userApp.getMyPermissionInfoId());
            mongoTemplate.save(userPermissionInfo);
            userInfo.setMyPermissionInfo(userPermissionInfo);

//          创建user的openInfo
            UserOpenInfo userOpenInfo = new UserOpenInfo();
            userOpenInfo = JSONObject.parseObject(JSONObject.toJSONString(userSQL),UserOpenInfo.class);
            userOpenInfo.setUpTime(dateUse.GetStringDateNow());
            userOpenInfo.setDeleteStyle(false);
            mongoTemplate.save(userOpenInfo);

            mongoTemplate.save(userInfo);

            long endTime = System.currentTimeMillis();    //获取结束时间
            logger.debug("create userinfo has spend time is : " + (endTime - startTime));
            return new ResponseEntity(RespCode.USERINFOADD, userInfo);
        } else {
            userInfo.setMyTeamsInfo(mongoTemplate.findById(userApp.getMyTeamsId(), myTeamsInfo.class));
            userInfo.setMyPlayGamesInfo(mongoTemplate.findById(userApp.getMyGamesId(), myPlayGamesInfo.class));
            userInfo.setMyStoresInfo(mongoTemplate.findById(userApp.getMyStoresId(), myStoresInfo.class));
            userInfo.setMyPhotoInfo(mongoTemplate.findById(userApp.getPhotoInfoId(), Photo.class));
            userInfo.setMyAddressInfo(mongoTemplate.findById(userApp.getMyAddressId(),AddressInfo.class));
            userInfo.setMySpendInfo(mongoTemplate.findById(userApp.getMySpendInfoId(), userSpendInfo.class));
            userInfo.setMyRankInfo(mongoTemplate.findById(userApp.getMyRankInfoId(), UserRank.class));
//          创建myMessageInfo
            userInfo.setMyFreiendInfo(mongoTemplate.findById(userApp.getMyFriendInfoId(), userFriendInfo.class));
//          创建myIntegrationInfo
//          更新userOpenInfo信息
            userInfo.setUserOpenInfo(userOpenInfoMapper.selectByPrimaryKey(userApp.getId()));
//          创建peopleList
            userInfo.setMyPermissionInfo(mongoTemplate.findById(userApp.getMyPermissionInfoId(), userPermissionInfo.class));
            userInfo.setUpTime(dateUse.GetStringDateNow());
//          更新MongoDB内的userInfo
            mongoTemplate.save(userInfo);
//            Criteria criteria = Criteria.where("id").ne(userApp.getId());
//            Update update = Update.update("userInfo", userInfo);
//            mongoTemplate.updateFirst(Query.query(criteria), update, UserInfo.class);
//            logger.error(JSONObject.toJSONString(userInfo));
//        List<TeamInfo> teamInfoList = mongoTemplate.find(Query.query(criteria), TeamInfo.class);

            long endTime = System.currentTimeMillis();    //获取结束时间
            logger.debug("update userinfo has spend time is : " + (endTime - startTime));
            return new ResponseEntity(RespCode.SUCCESS, userInfo);
        }
    }


    public ResponseEntity getUpdateInfo(UserInfo userInfo) {
        UserInfo userInfoMongo = mongoTemplate.findById(userInfo.getId(), UserInfo.class);
        if (userInfoMongo != null)//没有userInfo，创建一个在MongoDB里。
        {
//            Criteria criteria = Criteria.where("id").is(userInfo.getId());
//            Update update = Update.update("id", userInfo.getId());
//            mongoTemplate.updateFirst(Query.query(criteria), update, UserInfo.class);
            mongoTemplate.save(userInfo);
            return new ResponseEntity(RespCode.SUCCESS, userInfo);
        } else {
            return new ResponseEntity(RespCode.USERINFONONE, userInfo);
        }
    }

    @Override
    public ResponseEntity updateInfo( UserInfo userInfo, String partName) {
        UserInfo userMongoDB = mongoTemplate.findById(userInfo.getId(), UserInfo.class);
        String dbString = JSONObject.toJSONString(userMongoDB);
        User user = new User();
        switch (partName) {
            case "address":
//                sql 增加address数据
                Address address = userInfo.getMyAddressInfo();
                if(addressMapper.selectByPrimaryKey(address.getId()) == null)
                {
                    addressMapper.insert(address);
                }else {
                    addressMapper.updateByPrimaryKey(address);
                }
//                userInfo增加addressInfo数据
                userMongoDB.setMyAddressId(userInfo.getMyAddressInfo().getId());
                userMongoDB.setMyAddressInfo(userInfo.getMyAddressInfo());
//                addreInfo增加数据
                mongoTemplate.save(userInfo.getMyAddressInfo());
                break;
            case "myAddressInfo":
                break;
//            case "id":
//                userMongoDB.setId(userInfo.getId());
//                break;
//
//            case "age":
//                break;
//
//            case "ageLevel":
//                break;
//
//            case "realName":
//                break;
//
//            case "nickName":
//                break;
//
//            case "idCard":
//                break;
//
//            case "gender":
//                break;
//
//            case "job":
//                break;
//
//            case "telephone":
//                break;
//
//            case "marriage":
//                break;
//
//            case "userPWD":
//                break;
//
//            case "token":
//                break;
//
//            case "picPath":
//                break;
//
//            case "deleteStyle":
//                break;
//
//            case "upTime":
//                break;
//
//            case "myOpenInfoId":
//                break;
//
//            case "userInfoId":
//                break;
//
//            case "uniqueId":
//                break;
//
//            case "myTeamsId":
//                break;
//
//            case "myGamesId":
//                break;
//
//            case "myStoresId":
//                break;
//
//            case "photoInfoId":
//                break;
//
//            case "myRankInfoId":
//                break;
//
//            case "mySpendInfoId":
//                break;
//
//            case "myMessageInfoId":
//                break;
//
//            case "myFriendInfoId":
//                break;
//
//            case "myIntegrationInfoId":
//                break;
//
//            case "peopleListId":
//                break;
//
//            case "myPermissionInfoId":
//                break;
//            case "userId":
//                break;
//            case "myTeamsInfo":
//                break;
//            case "myPlayGamesInfo":
//                break;
//            case "myStoresInfo":
//                break;
//            case "myPhotoInfo":
//                break;
//            case "mySpendInfo":
//                break;
//            case "myRankInfo":
//                break;
//            case "myFreiendInfo":
//                break;
//            case "myPermissionInfo":
//                break;
            case "UserOpenInfo":
                userMongoDB.setUserOpenInfo(userOpenInfoMapper.selectByPrimaryKey(userMongoDB.getUserOpenInfo().getId()));
                break;
        }
        mongoTemplate.save(userMongoDB, "userInfo");
        return new ResponseEntity(RespCode.SUCCESS, userMongoDB);
    }

    private boolean checkVercode(String verCode) {
        return true;
    }

    private ResponseEntity checkUserInfoFUNC(String telephone, User userSql) {
        String token;
        UserLogin userLogin = new UserLogin();
        String nowTime = dateUse.DateToString(new Date());
        try {
            token = TokenMaker.EncoderByMd5(telephone);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ResponseEntity(RespCode.TOKENEXCEPTION, e);
        }
        userSql.setToken(token);//通过判断后，更新token
        userSql.setUpTime(nowTime);//更新时间
        userLogin.setDeleteStyle(false);//通过判断后，新增login信息
        userLogin.setId(UUIDUtil.getUUID());
        userLogin.setLoginTime(nowTime);
        userLogin.setUserId(userSql.getId());
        try {
            userLoginMapper.insert(userLogin);
            userMapper.updateByPrimaryKey(userSql);//更新user信息；
            return new ResponseEntity(RespCode.SUCCESS, userSql);
        } catch (Exception e) {
            return new ResponseEntity(RespCode.EXCEPTION, e);
        } finally {
//            if (redisTemplate.opsForValue().get(userSql.getId()) == null)
//                redisTemplate.opsForValue().set(userSql.getId(),
//                        JSONObject.toJSONString(userSql));
//            else {
//                redisTemplate.opsForValue().
//            }
        }
    }

}
