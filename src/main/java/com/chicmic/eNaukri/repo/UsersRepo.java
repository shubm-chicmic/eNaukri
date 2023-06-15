package com.chicmic.eNaukri.repo;

import com.chicmic.eNaukri.model.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsersRepo extends JpaRepository< Users,Long> {
    public Users findByEmail(String email);
    public Users findByUuid(String uuid);
    public Users findByUserId(long id);
//    @Query("Select u from Users u inner join Job j on ")
//    List<Users> getUsersByJobRequirements(Long jobId);

//    @Query("SELECT user FROM Users user LEFT JOIN user.roles role WHERE role.id = ?1")
//    List<Users> findUserByRole(int role);
}



//@Transactional
//@Modifying
//@Query(value=
//        "Update Users us set us.fullName = :fullName, us.phoneNumber=:phoneNumber, us.currentCompany=:currentCompany," +
//                "us.ppPath=:ppPath,us.cvPath=:cvPath where us.email=:email")
//void updateUsers(
//        @Param("fullName") String fullName, @Param("email") String email,@Param("phoneNumber") String phoneNumber,
//        @Param("currentCompany") String currentCompany, @Param("ppPath") String ppPath,@Param("cvPath") String cvPath);
//}
