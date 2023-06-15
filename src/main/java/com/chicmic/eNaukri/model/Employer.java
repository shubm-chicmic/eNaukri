package com.chicmic.eNaukri.model;

import com.chicmic.eNaukri.TrimNullValidator.TrimAll;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
public class Employer {
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;

    private String ppPath;

    @Column(columnDefinition = "boolean default false")
    private Boolean isApproved;
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"handler", "hibernateLazyInitializer"})
    private Users users;
    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    @JsonIgnore
    private Company employerCompany;
    @OneToMany(mappedBy = "employer", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private Set<Job> jobList=new HashSet<>();



}
