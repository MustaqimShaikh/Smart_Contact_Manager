package com.smartcontact.Controller;

import com.smartcontact.Entities.Contact;
import com.smartcontact.Entities.User;
import com.smartcontact.Repository.ContactRepository;
import com.smartcontact.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class SearchController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){

        System.out.println(query);

        User user = this.userRepository.getUserByUserName(principal.getName());
       // List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);

        List<Contact> contacts = this.contactRepository.findByFirstNameContainingAndUser(query,user);

        return ResponseEntity.ok(contacts);
    }
}
