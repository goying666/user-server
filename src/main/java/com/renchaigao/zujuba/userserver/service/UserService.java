package com.renchaigao.zujuba.userserver.service;

import com.renchaigao.zujuba.dao.User;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import com.renchaigao.zujuba.mongoDB.info.user.UserInfo;

public interface UserService {

    ResponseEntity autoLoginUser(User user);
    ResponseEntity secretLoginUser(User user);
    ResponseEntity vercodeLoginUser(User user, String verCode);
    ResponseEntity addUser(User user, String verCode);
    ResponseEntity getUserInfo(User userApp);
    ResponseEntity updateInfo(UserInfo userInfo, String part);

//    ResponseEntity addUserLogin(UserLoin userLoin);
}
