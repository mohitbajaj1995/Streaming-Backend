package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.interfaces.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    public final AdminRepository adminRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

//    @Transactional(readOnly = true)
//    public Admin getAdminWithOwner(Long id) {
//        Admin admin = adminRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + id));
//        Hibernate.initialize(admin.getOwners()); // This will trigger the lazy loading
//        return admin;
//    }
}
