package com.zsc.partnermatch;

import cn.hutool.core.date.StopWatch;
import com.zsc.partnermatch.entity.User;
import com.zsc.partnermatch.mapper.UserMapper;
import com.zsc.partnermatch.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class PartnerMatchBackendApplicationTests {
    @Autowired
    private UserMapper  userMapper;

    @Autowired
    private IUserService userService;

//    @Test
//    void contextLoads() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        final int insertCount = 600000;
//        List< User> users = new ArrayList<>();
//        for (int i = 0; i < insertCount; i++) {
//            User user = new User();
//            user.setUsername("zsc");
//            user.setUserAccount("zsc");
//            user.setAvatarUrl("https://picsum.photos/200/200?random=101");
//            user.setGender(0);
//            user.setUserPassword("123456");
//            user.setPhone("123");
//            user.setEmail("123@qq.com");
//            user.setUserStatus(0);
//            user.setRole(0);
//            user.setPlanetCode("11111");
//            user.setTags("[]");
//            users.add( user);
//
//
//        }
//        userService.saveBatch(users,50000);
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//    }

}
