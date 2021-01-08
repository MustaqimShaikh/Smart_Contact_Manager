package com.smartcontact.Controller;

import com.smartcontact.Entities.User;
import com.smartcontact.Helper.Message;
import com.smartcontact.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    //just for testing purpose
    @GetMapping("/test")
    @ResponseBody
    public String test(){

        User user = new User();
        user.setName("Aniket");
        user.setEmail("aniket@gmail.com");
        user.setImageUrl("default.png");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        System.out.println(user);
        return "it is working..!";
    }

    //for home page
    @GetMapping("home")
    public String home(Model model)
    {
        model.addAttribute("title", "Home - Smart Contact Manager");
        return "home";
    }

    //for about page
    @GetMapping("about")
    public String about(Model model)
    {
        model.addAttribute("title", "About - Smart Contact Manager");
        return "about";
    }

    //for signup page
    @GetMapping("signup")
    public String signup(Model model)
    {
        model.addAttribute("title", "Registration - Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }

    //for after registration
    @PostMapping("do_register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
                               Model model,
                               HttpSession session)
    {
        try {

            if (!agreement)
            {
                System.out.println("Please check the terms and Conditions..!");
                throw new Exception("You have not agreed terms and condition.");
            }

            if (bindingResult.hasErrors())
            {
                System.out.println("Errors :"+bindingResult.toString());
                model.addAttribute("user", user);
                return "signup";
            }

            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setImageUrl("default.png");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);

            System.out.println("Agreement " +agreement);
            System.out.println("User :" +user);

            User result = this.userRepository.save(user);

            model.addAttribute("user",new User());
            session.setAttribute("message",new Message("Successfully Registered..!","alert-success"));
            return "signup";


        }
        catch (Exception e){

            e.printStackTrace();
            model.addAttribute("user",user);

            session.setAttribute("message",new Message("Something Went Wrong..! "+e.getMessage(),"alert-danger"));
            return "signup";
        }
    }

    //for signin
    @GetMapping("signin")
    public String signIn(Model model)
    {
        model.addAttribute("title","Login Page..");
        return "login";
    }

    //for if login failed
    @GetMapping("login-fail")
    public String loginFail(Model model)
    {
        model.addAttribute("title","Login Failed");
        return "login-fail";
    }
}
