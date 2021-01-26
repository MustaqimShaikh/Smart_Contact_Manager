package com.smartcontact.Controller;

import com.smartcontact.Entities.Contact;
import com.smartcontact.Entities.User;
import com.smartcontact.Helper.Message;
import com.smartcontact.Repository.ContactRepository;
import com.smartcontact.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    //method to adding common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        //get the user using UserName..

        String userName = principal.getName();
        System.out.println("USERNAME :" + userName);

        User userByUserName = this.userRepository.getUserByUserName(userName);
        System.out.println("USER DETAILS :" + userByUserName);

        model.addAttribute("user", userByUserName);
        model.addAttribute("title", "User DashBoard");

    }


    //dashboard home
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title", "User DashBoard");
        return "normal/user_dashboard";
    }

    //adding contact form
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";

    }

    //processing contacts
    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("ProfileImage") MultipartFile imageFile,
                                 Principal principal,
                                 HttpSession session) {
        try {
            //getting the current logged user
            String name = principal.getName();
            User userByUserName = this.userRepository.getUserByUserName(name);

            //intentionally throwing error
           /* if(2>3){
                throw new Exception();
            }*/

            //processing and uploading file

            if (imageFile.isEmpty()) {

                //if imageFile is empty try this block
                System.out.println("ImageFile is empty..");

                //default image in case, no Profile uploaded
                contact.setImage("default_user1.png");

            } else {

                /*if imageFile is not empty try this block
                upload the name to contact*/

                //setting the imageFile in Database
                contact.setImage(imageFile.getOriginalFilename());

                //target path where the image is being uploaded
                File saveImageFile = new ClassPathResource("static/img").getFile();
                Path TargetPath = Paths.get(saveImageFile.getAbsolutePath() + File.separator + imageFile.getOriginalFilename());

                Files.copy(imageFile.getInputStream(), TargetPath, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is Uploaded..");
            }

            contact.setUser(userByUserName);
            userByUserName.getContactList().add(contact);

            this.userRepository.save(userByUserName);

            /*System.out.println("DATA :" + contact);*/

            System.out.println("Data is added..");


            //success message after adding contact into database
            session.setAttribute("message",
                    new Message("Your contact added successfully..! Add more", "success"));


        } catch (Exception e) {

            System.out.println("ERROR :" + e.getMessage());
            e.printStackTrace();

            //error message if anythings goes wrong
            session.setAttribute("message",
                    new Message("Something went wrong! Try again", "danger"));

        }

        return "normal/add_contact_form";
    }

    /*Before the pagination*/
    /*//for show contacts
    @GetMapping("/show-contacts")
    public String showContacts(Model model,
                               Principal principal)
    {
        model.addAttribute("title","Contacts Booklet");

        //getting current login user
        String userName = principal.getName();
        User userByUserName = this.userRepository.getUserByUserName(userName);

        //getting all contact list by current login user's id--> "user_id"
        List<Contact> contactsByUser = this.contactRepository.findContactsByUser(userByUserName.getId());

        //sending contacts list to the web page.
        model.addAttribute("contacts", contactsByUser);


        return "normal/show_contacts";
    }*/


    //for show contacts
    //per page -> 5[n]
    //current page -> 0[page]
    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") int currentPage,
                               Model model,
                               Principal principal) {
        model.addAttribute("title", "Contacts Booklet");

        //getting current login user
        String userName = principal.getName();
        User userByUserName = this.userRepository.getUserByUserName(userName);

        /*Pageable has two details:
         Current Page -> we can say, very first page
        No. of Contacts in per Page -> let say 5 contacts   */
        Pageable pageable = PageRequest.of(currentPage, 5);

        //getting contact page by current login user's id and currentPage & it's size--> "user_id" , pageable
        Page<Contact> contactsByUserId = this.contactRepository.findContactsByUser(userByUserName.getId(), pageable);

        //sending contacts list to the web page.
        model.addAttribute("contacts", contactsByUserId);

        //sending current page which have the 5 contact
        model.addAttribute("currentPage", currentPage);

        //sending total pages of contact
        model.addAttribute("totalPages", contactsByUserId.getTotalPages());


        return "normal/show_contacts";
    }

    //for showing particular contact details
    @GetMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") int cId,
                                    Model model,
                                    Principal principal) {
        System.out.println("CID: " + cId);
        model.addAttribute("title", "Contact Detail");

        Optional<Contact> ContactById = this.contactRepository.findById(cId);
        Contact contactDetail = ContactById.get();



        /*For the security Bug..
        First we had to get current login user using 'principal'*/
        String loginUserName = principal.getName();

        //Now getting user from database using UserName
        User loginUserByUserName = this.userRepository.getUserByUserName(loginUserName);

        //Checking the condition for particular User
        if (loginUserByUserName.getId() == contactDetail.getUser().getId()) {
            //Sending the contact detail to Login User
            model.addAttribute("contact", contactDetail);

            //In the title bar showing contact's Fist Name
            model.addAttribute("title", "Contact: " + contactDetail.getFirstName());

        }

        return "normal/contact_detail";
    }

    //for delete contact
    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable("cId") int cId,
                                Model model,
                                Principal principal,
                                HttpSession session) {
        /*For the security Bug..
        First we had to get current login user using 'principal'*/
        String loginUserName = principal.getName();

        Optional<Contact> ContactById = this.contactRepository.findById(cId);
        Contact contactDetail = ContactById.get();

        User loginUserByUserName = this.userRepository.getUserByUserName(loginUserName);

        //System.out.println("Contact Detail : "+contactDetail);


        //Checking the condition for particular User
        if (loginUserByUserName.getId() == contactDetail.getUser().getId()) {
            // System.out.println("Contact Id : "+contactDetail.getcId()+" is deleted");

            contactDetail.setUser(null);

            this.contactRepository.delete(contactDetail);

            System.out.println("Contact Deleted..!");

            session.setAttribute("message", new Message("Contact Deleted Successfully..!", "success"));

        }

        return "redirect:/user/show-contacts/1";

    }

    //for updating contact
    @PostMapping("/update-contact/{cId}")
    public String updateContact(@PathVariable("cId") int cId,
                                Model model) {
        model.addAttribute("title", "Update Form");

        Optional<Contact> contactById = this.contactRepository.findById(cId);
        Contact contact = contactById.get();

        model.addAttribute("contact", contact);

        return "normal/update_form";
    }

    //for to handle update process
    @PostMapping("/process-update")
    public String updateProcess(@ModelAttribute Contact contact,
                                @RequestParam("ProfileImage") MultipartFile imageFile,
                                Principal principal,
                                HttpSession session,
                                Model model) {
        try {

            Optional<Contact> oldContactId = this.contactRepository.findById(contact.getcId());
            Contact oldContactDetail = oldContactId.get();

            //
            if (imageFile.isEmpty()) {
                contact.setImage(oldContactDetail.getImage());

            } else {

                //DELETE OLD FILE//
                File deleteFile = new ClassPathResource("static/img").getFile();
                File dFile = new File(deleteFile, oldContactDetail.getImage());
                dFile.delete();

                //UPLOAD NEW PHOTO//
                //target path where the image is being uploaded
                File saveImageFile = new ClassPathResource("static/img").getFile();
                Path TargetPath = Paths.get(saveImageFile.getAbsolutePath() + File.separator + imageFile.getOriginalFilename());

                Files.copy(imageFile.getInputStream(), TargetPath, StandardCopyOption.REPLACE_EXISTING);

                //image name is stored in DB with it's OriginalFileName
                contact.setImage(imageFile.getOriginalFilename());

            }
            String userName = principal.getName();
            User userByUserName = this.userRepository.getUserByUserName(userName);
            contact.setUser(userByUserName);
            this.contactRepository.save(contact);
            //After clicking on update button, Showing success message
            session.setAttribute("message", new Message("Your Contact is Updated..!", "success"));

        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
        }
        System.out.println("Contact Name: " + contact.getFirstName());
        System.out.println("Contact ID: " + contact.getcId());

        return "redirect:/user/" + contact.getcId() + "/contact";

    }

    @GetMapping("/user-profile")
    public String userProfile(){

        return "normal/profile";
    }

    @GetMapping("/settings")
    public String openSettings(){

        return "normal/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal,
                                 HttpSession httpSession){
        System.out.println("OLD PASSWORD :"+oldPassword);
        System.out.println("NEW PASSWORD :"+newPassword);
        String user = principal.getName();
        User currentLoggedUser = this.userRepository.getUserByUserName(user);

        String encodedPassword = currentLoggedUser.getPassword();

        System.out.println("ENCODED PASSWORD FROM DATABASE : "+encodedPassword);

        if (this.bCryptPasswordEncoder.matches(oldPassword,encodedPassword)){
            //change password
            currentLoggedUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currentLoggedUser);
            httpSession.setAttribute("message", new Message("Your Password Changed Successfully..!", "success"));
            System.out.println("PASSWORD CHANGED SUCCESSFULLY..");
        }else {
            //error
            httpSession.setAttribute("message", new Message("Please Enter Your Correct Old Password..!", "danger"));

            System.out.println("OLD PASSWORD IS WRONG.. TRY AGAIN");
            return "redirect:/user/settings";
        }



        return "redirect:/user/dashboard";
    }

}
