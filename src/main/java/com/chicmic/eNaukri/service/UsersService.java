package com.chicmic.eNaukri.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.chicmic.eNaukri.Dto.UserProfileDto;
import com.chicmic.eNaukri.Dto.UsersDto;
import com.chicmic.eNaukri.model.*;
import com.chicmic.eNaukri.repo.PreferenceRepo;
import com.chicmic.eNaukri.repo.SkillsRepo;
import com.chicmic.eNaukri.repo.UserProfileRepo;
import com.chicmic.eNaukri.repo.UsersRepo;
import com.chicmic.eNaukri.util.CustomObjectMapper;
import com.chicmic.eNaukri.util.FileUploadUtil;
import com.chicmic.eNaukri.util.JwtUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static com.chicmic.eNaukri.ENaukriApplication.passwordEncoder;
import static com.chicmic.eNaukri.config.SecurityConstants.EXPIRATION_TIME;
import static com.chicmic.eNaukri.config.SecurityConstants.SECRET;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsersService {
    private final UsersRepo usersRepo;
    private final JavaMailSender javaMailSender;
    private final FileUploadUtil fileUploadUtil;
    private final UserProfileRepo userProfileRepo;
    private final PreferenceRepo preferenceRepo;
    private final SkillsRepo skillsRepo;

    public Users getUserByEmail(String email) {
        return usersRepo.findByEmail(email);
    }
    public Users getUserByUuid(String uuid) { return usersRepo.findByUuid(uuid); }

    public Users getUserById(Long userId){return usersRepo.findByUserId(userId);}



    public Users register(Users newUser) {
        String uuid= UUID.randomUUID().toString();
        newUser.setUuid(uuid);
        newUser.setPassword(passwordEncoder().encode(newUser.getPassword()));
        usersRepo.save(newUser);
        String token = JwtUtils.createJwtToken(newUser.getUuid());
        String link = "http://localhost:8081/eNaukri/verify/"+token+"/"+newUser.getUuid();
        String to= newUser.getEmail();
        String subject="eNaukri job portal - Verify your account";
        String body="Click the link to verify your account" +link;
        sendEmail(to,subject,body);
        return newUser;
    }
    public void verifyUserAccount(String token, String uuid){
        String decodedToken = JwtUtils.verifyJwtToken(token);
        Users user= usersRepo.findByUuid(uuid);
        if(decodedToken==uuid){
            user.setVerified(true);
        }
    }

    @Async public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("harmanjyot.singh@chicmic.co.in");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }

    public void updateUser(@Valid UsersDto user, @RequestParam MultipartFile imgFile,
                           @RequestParam MultipartFile resumeFile) throws IOException {
        Users existingUser=usersRepo.findByEmail(user.getEmail());
        ObjectMapper mapper = CustomObjectMapper.createObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.updateValue(existingUser,user);
        existingUser.getUserProfile().setPpPath(FileUploadUtil.imageUpload(imgFile));
        existingUser.getUserProfile().setCvPath(FileUploadUtil.resumeUpload(resumeFile));
        existingUser.setUpdatedAt(LocalDateTime.now());
        usersRepo.save(existingUser);
    }
    public UserProfile createProfile(UserProfileDto dto, Principal principal)//, MultipartFile imgFile)
            {
        Users user = usersRepo.findByEmail(principal.getName());
        UserProfile userProfile= CustomObjectMapper.convertDtoToObject(dto, UserProfile.class);
        userProfile.setUsers(user);
//        userProfile.setPpPath(FileUploadUtil.imageUpload(imgFile));
        List<UserSkills> userSkillsList=new ArrayList<>();
        for (Long skillId : dto.getSkillsList()) {
            Skills skill = skillsRepo.findById(skillId).orElse(null);
            UserSkills userSkills = UserSkills.builder().skills(skill).userProfile(userProfile).build();
            if (skill != null) {
                userSkillsList.add(userSkills);
            }
        }
        for(Education ed:userProfile.getEducationList()){
            ed.setEdUser(userProfile);
        }
        for(Experience ex:userProfile.getExperienceList()){
            ex.setExpUser(userProfile);
        }
//        userProfile.getEduca;
        userProfile.setUserSkillsList(userSkillsList);
        userProfileRepo.save(userProfile);
        user.setUserProfile(userProfile);
        return user.getUserProfile();
    }
    public Preference createPreferences(Principal principal, Preference preference){
        Users user=usersRepo.findByEmail(principal.getName());
        Preference preference1=preferenceRepo.save(preference);
        preference1.setUserPreferences(user.getUserProfile());
        return preference1;
    }
}
