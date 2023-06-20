package com.chicmic.eNaukri.controller;

import com.chicmic.eNaukri.Dto.ApiResponse;
import com.chicmic.eNaukri.model.Application;
import com.chicmic.eNaukri.model.Roles;
import com.chicmic.eNaukri.model.UserRole;
import com.chicmic.eNaukri.model.Users;
import com.chicmic.eNaukri.service.ApplicationService;
import com.chicmic.eNaukri.service.EmployerService;
import com.chicmic.eNaukri.service.JobService;
import com.chicmic.eNaukri.service.RolesService;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/employer")
@RequiredArgsConstructor
public class EmployerController {
    private final EmployerService employerService;
    private final JobService jobService;
    private final RolesService rolesService;

    private final ApplicationService applicationService;

    @PostMapping("/signup")
    public ApiResponse signup( String jsonString , @ModelAttribute MultipartFile userImg,
                              @ModelAttribute MultipartFile companyImg) throws IOException {
        System.out.println("sdjhsgdfgsd");
        ObjectMapper mapper = new ObjectMapper();
        Users users = mapper.readValue(jsonString, Users.class);
        users = employerService.saveEmployer(users, userImg, companyImg);
        users = employerService.saveEmployer(users, userImg, companyImg);
        Roles roles = rolesService.getRoleByRoleName("EMPLOYER");
        UserRole userRole = UserRole.builder()
                .userId(users)
                .roleId(roles)
                .build();
        rolesService.saveUserRole(userRole);
        return new ApiResponse( "User Register Successfully as Employer", "users.getEmployerProfile()", HttpStatus.CREATED );
    }
    @PutMapping("/{appId}/send-invites")
    public ApiResponse sendInvites(Principal principal,Long appId){
        Application app=employerService.inviteForJob(appId,principal);
        return new ApiResponse("User has been successfully invited",app,HttpStatus.OK);}

    @PostMapping("/addRole")
    public String addRole(@RequestParam String roleName){
        Roles roles = rolesService.getRoleByRoleName(roleName);
        if(roles == null){
            roles = Roles.builder()
                  .roleName(roleName.toUpperCase())
                  .build();
            rolesService.saveRoles(roles);
        }
        return "Successfully Added " + roleName;
    }
    @PutMapping("/set-job-status/{jobId}")
    public ResponseEntity<String> setJobStatus(@PathVariable Long jobId, Principal principal, boolean active){
        jobService.setStatus(jobId,active,principal);
        return ResponseEntity.ok("Status changed");
    }
    @PutMapping("/set-application-status/{appId}")
    public ApiResponse setApplicationStatus(@RequestParam Long statusId,@PathVariable Long appId,Principal principal){
        Application app=applicationService.changeApplicationStatus(appId,principal,statusId);
        return new ApiResponse("An email has been sent to applicant regarding application status",app,HttpStatus.ACCEPTED);
    }
    @GetMapping("/applicants/{jobId}")
    public List<Application> getAllApplicants(Principal principal,@PathVariable Long jobId){
        return employerService.getApplicantListForJob(principal,jobId);
    }
    @DeleteMapping("/delete/job/{jobId}")
    public String deleteJob(@PathVariable Long jobId){
        jobService.deleteJob(jobId);
        return "Successfully Deleted Job";
    }
}
