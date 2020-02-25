package com.offcn.service;

import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    //通过set注入sellerService
    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<GrantedAuthority> grantedAuthorityList=new ArrayList<>();

        grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_SHOP"));

        TbSeller tbSeller = sellerService.findOne(username);//username==sellerId

        System.out.println("第一个："+username);

        if (null!=tbSeller){//防止空指针异常
            if (tbSeller.getStatus().equals("1")){//并且商家状态为可用
                System.out.println("第二个:"+username);
                return new User(username,tbSeller.getPassword(),grantedAuthorityList);
            }else {
                return null;
            }
        }else {
            return null;
        }


    }
}
