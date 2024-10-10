package apap.ti.appointment2206082266.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "doctor")
@SQLDelete(sql = "UPDATE doctor SET deleted_at = NOW() WHERE id=?")
@SQLRestriction("deleted_at IS NULL")
public class Doctor {

    @Id
    private String id = UUID.randomUUID().toString();


    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "specialist", nullable = false)
    private Integer specialist;

    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "gender", nullable = false)
    private Boolean gender;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @ElementCollection
    @CollectionTable(name = "doctor_schedules", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "schedule", nullable = false)
    private List<Integer> schedules;

    @Column(name = "fee", nullable = false)
    private Long fee;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Column(name = "deleted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date deletedAt;
}