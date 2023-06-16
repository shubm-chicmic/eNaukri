package com.chicmic.eNaukri.service;

import com.chicmic.eNaukri.CustomExceptions.ApiException;
import com.chicmic.eNaukri.Dto.UserEducationDto;
import com.chicmic.eNaukri.Dto.UserExperienceDto;
import com.chicmic.eNaukri.model.*;
import com.chicmic.eNaukri.repo.*;
import com.chicmic.eNaukri.util.CustomObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class ExperienceService {
    private final UsersRepo usersRepo;
    private final CompanyRepo companyRepo;
    private final ExperienceRepo experienceRepo;
    private final UserExperienceRepo userExperienceRepo;
    public void addExperience(Principal principal, UserExperienceDto dto){
        Users user=usersRepo.findByEmail(principal.getName());
        boolean hasDuplicateJoinedDate = user.getUserProfile().getExperienceList().stream()
                .anyMatch(e -> (e.getJoinedOn().isBefore(dto.getJoinedOn()) && e.getEndedOn().isAfter(dto.getJoinedOn()))
                    || (e.getJoinedOn().isBefore(dto.getEndedOn()) && e.getEndedOn().isAfter(dto.getEndedOn()))
                );

        if (hasDuplicateJoinedDate) {
            throw new ApiException(HttpStatus.CONFLICT,"A duplicate joinedOn date exists for the user's experience.");
        }

        // Check if the user already has a current company
        boolean hasCurrentCompany = user.getUserProfile().getExperienceList().stream()
                .anyMatch(Experience::isCurrentlyWorking);

        if (dto.isCurrentlyWorking() && hasCurrentCompany) {
            throw new ApiException(HttpStatus.CONFLICT,"User can have only one currently working company.");
        }
        ObjectMapper mapper = CustomObjectMapper.createObjectMapper();
        Experience experience = mapper.convertValue(dto, Experience.class);
        experience.setExCompany(companyRepo.getById(dto.getCompanyId()));
        experience.setExpUser(user.getUserProfile());
        UserExperience userExperience=new UserExperience();
        userExperience.setExperience(experience);
        userExperience.setUser(user);
        experienceRepo.save(experience);
        userExperienceRepo.save(userExperience);
    }
    public void deleteExperience(Long expId){
        experienceRepo.deleteById(expId);
    }
}
