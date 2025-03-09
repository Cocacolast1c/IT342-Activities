package com.G6Manabat.IT342.Controller;


import com.G6Manabat.IT342.Service.GoogleService;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class GoogleController {

    private final GoogleService googleService;

    public GoogleController(GoogleService googleService) {
        this.googleService = googleService;
    }

    @GetMapping
    public String getContacts(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                              @AuthenticationPrincipal OAuth2User oauth2User,
                              Model model) throws GeneralSecurityException, IOException {
        List<Person> contacts = googleService.getContacts(authorizedClient);
        model.addAttribute("contacts", contacts);
        model.addAttribute("user", oauth2User.getAttributes());
        return "contacts";
    }

    @PostMapping("/create")
    public String createContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                @RequestParam String givenName,
                                @RequestParam String familyName,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phoneNumber) throws GeneralSecurityException, IOException {
        Person newContact = new Person();

        Name name = new Name();// set each manually kai maguba
        name.setGivenName(givenName);
        name.setFamilyName(familyName);
        newContact.setNames(List.of(name));

        if (email != null && !email.isEmpty()) {
            EmailAddress emailAddress = new EmailAddress();
            emailAddress.setValue(email);
            newContact.setEmailAddresses(List.of(emailAddress));
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            PhoneNumber phone = new PhoneNumber();
            phone.setValue(phoneNumber);
            newContact.setPhoneNumbers(List.of(phone));
        }
        googleService.createContact(authorizedClient, newContact);
        return "redirect:/contacts";
    }

    @GetMapping("/edit")
    public String findContactByResourceName(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                            @RequestParam String resourceName,
                                            Model model) throws GeneralSecurityException, IOException {

        Person contact = googleService.getContactByResourceName(authorizedClient, resourceName);

        System.out.println("Fetched Contact: " + (contact != null ? contact.toPrettyString() : "No Contact Found"));
        System.out.println("Resource Name: " + resourceName);

        model.addAttribute("contact", contact);
        model.addAttribute("resourceName", resourceName);
        return "edit-contact";
    }

    @PostMapping("/edit/submit")
    public String submitContactEdit(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                    @RequestParam String resourceName,
                                    @RequestParam String givenName,
                                    @RequestParam String familyName,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String phoneNumber,
                                    Model model) throws GeneralSecurityException, IOException {


        googleService.updateContact(authorizedClient, resourceName, givenName, familyName, email, phoneNumber);

        return "redirect:/contacts";
    }



    @PostMapping("/delete")
    public String deleteContact(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient,
                                @RequestParam String resourceName) throws GeneralSecurityException, IOException {
        googleService.deleteContact(authorizedClient, resourceName);
        return "redirect:/contacts";
    }
}