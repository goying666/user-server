package com.renchaigao.zujuba.userserver.uti;

import com.renchaigao.zujuba.dao.User;
import com.renchaigao.zujuba.dao.UserLogin;
import com.renchaigao.zujuba.dao.mapper.UserLoginMapper;
import com.renchaigao.zujuba.dao.mapper.UserMapper;
import com.renchaigao.zujuba.domain.response.RespCode;
import com.renchaigao.zujuba.domain.response.ResponseEntity;
import normal.TokenMaker;
import normal.UUIDUtil;
import normal.dateUse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class LoginUserFunctions {

    UserMapper userMapper;
    UserLoginMapper userLoginMapper;
    public LoginUserFunctions(UserMapper userMapper , UserLoginMapper userLoginMapper){
        this.userMapper = userMapper;
        this.userLoginMapper = userLoginMapper;
    }
    public ResponseEntity checkUserInfoFUNC( User userSql ) {
        String token;
        UserLogin userLogin = new UserLogin();
        String nowTime = dateUse.DateToString(new Date());
        try {
            token = TokenMaker.EncoderByMd5(userSql.getTelephone());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ResponseEntity(RespCode.LOGIN_AUTO_EXCEPTION, e);
        }
        userSql.setToken(token);//通过判断后，更新token
        userSql.setUpTime(nowTime);//更新时间
        userLogin.setId(UUIDUtil.getUUID());
        userLogin.setDeleteStyle(false);//通过判断后，新增login信息
        userLogin.setLoginTime(nowTime);
        userLogin.setUserId(userSql.getId());
        try {
            userLoginMapper.insert(userLogin);
            userMapper.updateByPrimaryKey(userSql);//更新user信息；
            return new ResponseEntity(RespCode.LOGIN_AUTO_SUCCESS, userSql);
        } catch (Exception e) {
            return new ResponseEntity(RespCode.LOGIN_AUTO_EXCEPTION, e);
        }
    }

}
