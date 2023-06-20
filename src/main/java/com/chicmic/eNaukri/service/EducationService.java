package com.chicmic.eNaukri.service;

import com.chicmic.eNaukri.CustomExceptions.ApiException;
import com.chicmic.eNaukri.Dto.UserEducationDto;
import com.chicmic.eNaukri.model.Education;
import com.chicmic.eNaukri.model.UserEducation;
import com.chicmic.eNaukri.model.Users;
import com.chicmic.eNaukri.repo.EducationRepo;
import com.chicmic.eNaukri.repo.UserEducationRepo;
import com.chicmic.eNaukri.repo.UsersRepo;
import com.chicmic.eNaukri.util.CustomObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

@Service public class EducationService {
    @Autowired
    UsersRepo usersRepo;
    @Autowired
    EducationRepo educationRepo;
    @Autowired
    UserEducationRepo userEducationRepo;
    @Autowired
    UsersService usersService;
    public void addEducation(UserEducationDto dto,Long userId){
        Users user=usersRepo.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,"User doesn't exist"));
        boolean hasDuplicateDegree = usersService.getUserProfile(user).getEducationList().stream()
                .anyMatch(e -> e.getDegree().equalsIgnoreCase(dto.getDegree()));
        if (hasDuplicateDegree) {
            throw new ApiException(HttpStatus.CONFLICT,"A duplicate degree entry already exists for the user.");
        }
        if(dto.getStartFrom().isAfter(LocalDate.now())){
            throw new ApiException(HttpStatus.BAD_REQUEST,"Start date cannot be in the future.");
        }

        ObjectMapper mapper = CustomObjectMapper.createObjectMapper();
        Education education = mapper.convertValue(dto, Education.class);
        education.setEdId(Optional.of( educationRepo.findByDegree(dto.getDegree())).orElse(null).getEdId());
        if(education.getEndOn().isBefore(LocalDate.now())){
            education.setStudent(false);
        }
        else{
            education.setStudent(true);
        }
        education.setEdUser(usersService.getUserProfile(user));
        UserEducation userEducation=new UserEducation();
        userEducation.setEducation(education);
        userEducation.setUser(user);
        educationRepo.save(education);

    }
    public void deleteEducation(Long edId){
        educationRepo.deleteById(edId);
    }

}
