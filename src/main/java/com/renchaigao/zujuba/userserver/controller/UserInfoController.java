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
@RequestMapping(value = "/info")
public class UserInfoController {

    @Autowired
    UserServiceImpl userServiceImpl;

    @PostMapping(value = "/get" , consumes = "application/json")
    @ResponseBody
    public ResponseEntity userGetInfo(
            @RequestBody User user){
        return userServiceImpl.getUserInfo(user);
    }

    @PostMapping(value = "/update/{part}" , consumes = "application/json")
    @ResponseBody
    public ResponseEntity userUpdateInfo(   @PathVariable("part") String part,
            @RequestBody UserInfo userInfo){
        return userServiceImpl.updateInfo(userInfo,part);
    }



}
