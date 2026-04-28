package com.mediverse.controller;

import com.mediverse.entity.User;
import com.mediverse.entity.Patient;
import com.mediverse.entity.Role;
import com.mediverse.service.UserService;
import com.mediverse.repository.PatientRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "auth/register";
        }
        
        // Check if email already exists
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email address is already registered");
            return "auth/register";
        }
        
        try {
            // Set role as PATIENT for registration
            user.setRole(Role.PATIENT);
            user.setIsActive(true);
            
            // Save user
            User savedUser = userService.saveUser(user);
            
            // Create patient profile
            Patient patient = new Patient(savedUser);
            patientRepository.save(patient);
            
            redirectAttributes.addFlashAttribute("success", 
                "Registration successful! Please login with your credentials.");
            return "redirect:/login";
            
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again.");
            return "auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/?logout=true";
    }
}
