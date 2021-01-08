package com.smartcontact.Configuration;

import com.smartcontact.Service.UserDetailsServiceImple;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class MyConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public UserDetailsService getUserDetailsService()
    {
        return new UserDetailsServiceImple();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider()
    {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(this.getUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    //Configuration methods.
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {

        auth.authenticationProvider(daoAuthenticationProvider());
       // auth.inMemoryAuthentication(); // if in case we use inMemory Authentication..
    }

    //this method is define the route of urls., which url going to restricted or not
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .authorizeRequests()
                .antMatchers("/admin/**")
                .hasRole("ADMIN")
                .antMatchers("/user/**")
                .hasRole("USER")
                .antMatchers("/**").permitAll()
                .and()
                .formLogin().loginPage("/signin")
                .loginProcessingUrl("/dologin")
                .defaultSuccessUrl("/user/dashboard")
                .failureUrl("/login-fail")
                .and()
                .csrf().disable();


    }
}
