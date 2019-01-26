package com.renchaigao.zujuba.userserver.controller;

import com.renchaigao.zujuba.dao.User;
import com.renchaigao.zujuba.domain.response.RespCode;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import com.renchaigao.zujuba.mongoDB.info.user.UserInfo;
import com.renchaigao.zujuba.userserver.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping()
public class UserController {

    @Autowired
    UserServiceImpl userServiceImpl;

    @PostMapping(value = "/{firstStr}/{secondStr}/{thirdStr}/{fourthStr}", consumes = "application/json")
    @ResponseBody
    public ResponseEntity UserControllerFuns(@PathVariable("firstStr") String fistStr,
                                             @PathVariable("secondStr") String secondStr,
                                             @PathVariable("thirdStr") String thirdStr,
                                             @PathVariable("fourthStr") String fourthStr,
                                             @RequestBody String jsonObjectString) {
        switch (fistStr) {
            case "signin":
                //String phoneNumber, String parameter, String jsonObjectString
                return userServiceImpl.SignInUser(
                        secondStr,//传递手机号：telephone
                        thirdStr,//传递验证码：AuthCode
                        fourthStr,//传递用户请求类型：mode: 验证码请求 vercode， 注册请求：signin
                        jsonObjectString);//传递手机参数
            case "login":
                return userServiceImpl.LoginUser(
                        secondStr,//登录方式：auto；secret；
                        thirdStr,//传递手机号：telephone
                        fourthStr,//传递用户id：userId
                        jsonObjectString);//传递手机参数
//            case "quit":
//                return userServiceImpl.QuitUser(userId, secondStr, phone, jsonObjectString);
//            case "find":
//                return userServiceImpl.FindBackUser(userId, secondStr, phone, jsonObjectString);
            case "get":
                return userServiceImpl.GetUser(
                        secondStr,//传递用户id：userId
                        jsonObjectString);//传递userInfo
            case "update":
                return userServiceImpl.UpdateUser(
                        secondStr,//更新类型：updateStyle
                        thirdStr,//用户id：userId
                        jsonObjectString);
        }
        return new ResponseEntity(RespCode.WRONGIP, null);
    }
}
