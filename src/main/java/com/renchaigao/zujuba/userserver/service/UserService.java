package com.renchaigao.zujuba.userserver.service;

import com.renchaigao.zujuba.dao.User;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import com.renchaigao.zujuba.mongoDB.info.user.UserInfo;

public interface UserService {


    ResponseEntity SignInUser(String phoneNumber, String verCode,String mode, String jsonObjectString);

    ResponseEntity LoginUser(String loginStyle, String phoneNumber, String userId, String jsonObjectString);
//    ResponseEntity QuitUser(String userId, String parameter, String teamId, String jsonObjectString);
//    ResponseEntity FindBackUser(String userId, String parameter, String teamId, String jsonObjectString);
    ResponseEntity GetUser(String userId, String jsonObjectString);
    ResponseEntity UpdateUser(String updateStyle, String userId,  String jsonObjectString);
}
