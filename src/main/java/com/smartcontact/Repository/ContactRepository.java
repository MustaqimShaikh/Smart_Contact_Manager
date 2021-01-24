package com.smartcontact.Repository;

import com.smartcontact.Entities.Contact;
import com.smartcontact.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ContactRepository extends JpaRepository<Contact, Integer> {

   /* //Before Pagination
    @Query("select c from Contact as c where c.user.id =:userId")
    public List<Contact> findContactsByUser(@Param("userId") int userId);*/

    //pagination
    @Query("select c from Contact as c where c.user.id =:userId")
    public Page<Contact> findContactsByUser(@Param("userId") int userId, Pageable perPageable);

    /*Pageable has two details:
    Current Page -> we can say, very first page
    No. of Contacts in per Page -> let say 5 contacts   */


    //Search contacts
    public List<Contact> findByFirstNameContainingAndUser(String keywords, User user);
}
