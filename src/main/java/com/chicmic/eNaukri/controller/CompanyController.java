package com.chicmic.eNaukri.controller;

import com.chicmic.eNaukri.Dto.ApiResponse;
import com.chicmic.eNaukri.Dto.JobDto;
import com.chicmic.eNaukri.Dto.SocialLinkDto;
import com.chicmic.eNaukri.model.*;
import com.chicmic.eNaukri.repo.CompanyRepo;
import com.chicmic.eNaukri.repo.JobRepo;
import com.chicmic.eNaukri.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/company/")
@RequiredArgsConstructor
public class CompanyController {
    private final UserServiceImpl userService;
    private final JobRepo jobRepo;
    private final CompanyRepo companyRepo;
    private final CompanyService companyService;
    private final ApplicationService applicationService;
    @Autowired JobService jobService;
    SocialLinkService linkService;

    @GetMapping("{id}")
    public Company companyPage(@PathVariable Long id){
        return companyRepo.findById(id).get();
    }
    @GetMapping("{id}/jobs/{jobId}")
    public ResponseEntity<?> getjobFromCompany(@PathVariable("id")Long id, @PathVariable("jobId") Long jobId){
            return ResponseEntity.ok(companyService.jobExistsForCompany(id, jobId));
    }
    @PostMapping("/postJob")
    public ApiResponse postJob(@RequestBody JobDto jobDto, Principal principal){
        Employer employer = userService.getUserByEmail(principal.getName()).getEmployerProfile();
        Job job = jobService.saveJob(jobDto, employer);
        return new ApiResponse("Job successfully posted", job, HttpStatus.CREATED);
    }
    @PostMapping("add-social-links/{id}")
    public ResponseEntity<String> addSocialLinks(@PathVariable Long id, SocialLinkDto dto){
        linkService.addSocialLinks(null, dto,id);
        return ResponseEntity.ok("Added social links");
    }
    @DeleteMapping("/delete-job/{jobId}")
    public void deleteJob(@PathVariable Long jobId){
        jobService.deletePostedJob(jobId);
    }
    @GetMapping("list-interested-applicants/{jobId}")
    public Collection<?> listInterestedApplicants(@PathVariable("jobId")Long jobId){
        return jobService.listInterestedApplicants(jobId);
    }
    @PostMapping("approve-employer/{empId}")
    public ApiResponse approve(@PathVariable Long empId){
        return new ApiResponse("Employer status changed",companyService.approveEmployer(empId),HttpStatus.CREATED);
    }
    @PostMapping("applicants-for-job/{jobId}")
    public ApiResponse getapplications(Principal principal, @PathVariable Long jobId){
        return new ApiResponse("Applications for the job",companyService.getApplicants(principal,jobId),HttpStatus.OK);
    }

}
