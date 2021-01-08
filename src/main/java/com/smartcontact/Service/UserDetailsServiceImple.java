package com.smartcontact.Service;

import com.smartcontact.Configuration.CustomUserDetails;
import com.smartcontact.Entities.User;
import com.smartcontact.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImple implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

        User userByUserName = userRepository.getUserByUserName(s);

        if (userByUserName==null)
        {
            throw new UsernameNotFoundException("Could not found the user..!");
        }


        CustomUserDetails customUserDetails = new CustomUserDetails(userByUserName);

        return customUserDetails;
    }
}
