package com.mediverse.controller;

import com.mediverse.entity.Department;
import com.mediverse.entity.Doctor;
import com.mediverse.entity.Branch;
import com.mediverse.repository.DepartmentRepository;
import com.mediverse.repository.DoctorRepository;
import com.mediverse.repository.BranchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private BranchRepository branchRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Get some statistics for the home page
        List<Department> departments = departmentRepository.findActiveDepartmentsOrderByName();
        List<Doctor> doctors = doctorRepository.findAvailableDoctors();
        List<Branch> branches = branchRepository.findActiveBranchesOrderByName();
        
        model.addAttribute("departmentCount", departments.size());
        model.addAttribute("doctorCount", doctors.size());
        model.addAttribute("branchCount", branches.size());
        model.addAttribute("featuredDepartments", departments.stream().limit(6).toList());
        model.addAttribute("featuredDoctors", doctors.stream().limit(8).toList());
        
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/services")
    public String services(Model model) {
        List<Department> departments = departmentRepository.findActiveDepartmentsOrderByName();
        model.addAttribute("departments", departments);
        return "services";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/doctors")
    public String doctors(Model model, @RequestParam(required = false) Long departmentId, 
                         @RequestParam(required = false) String search) {
        List<Doctor> doctors;
        List<Department> departments = departmentRepository.findActiveDepartmentsOrderByName();
        
        if (departmentId != null) {
            doctors = doctorRepository.findAvailableDoctorsByDepartment(departmentId);
        } else if (search != null && !search.trim().isEmpty()) {
            doctors = doctorRepository.findByNameContaining(search.trim());
        } else {
            doctors = doctorRepository.findAvailableDoctors();
        }
        
        model.addAttribute("doctors", doctors);
        model.addAttribute("departments", departments);
        model.addAttribute("selectedDepartmentId", departmentId);
        model.addAttribute("searchTerm", search);
        return "doctors";
    }

    @GetMapping("/departments")
    public String departments(Model model) {
        List<Department> departments = departmentRepository.findActiveDepartmentsOrderByName();
        
        // Add doctor count for each department
        for (Department department : departments) {
            List<Doctor> deptDoctors = doctorRepository.findAvailableDoctorsByDepartment(department.getId());
            department.setDoctorCount(deptDoctors.size()); // We'll need to add this field to Department entity
        }
        
        model.addAttribute("departments", departments);
        return "departments";
    }

    @GetMapping("/branches")
    public String branches(Model model) {
        List<Branch> branches = branchRepository.findActiveBranchesOrderByName();
        model.addAttribute("branches", branches);
        return "branches";
    }
}
