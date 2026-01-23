package com.elementaryschool.graduation_album.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "student",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_student_classroom_name", columnNames = {"classroom_id", "name"})
        }
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String name;

    @Size(max = 200)
    @Column(length = 200)
    private String motto;

    @Size(max = 500)
    @Column(length = 500)
    private String talk;

    @Size(max = 255)
    private String personalPhotoUrl;

    /** 📸 학생 사진 파일 경로 */
    @Size(max = 255)
    @Column(length = 255)
    private String personalPhoto;

    @Size(max = 255)
    private String handLetterPhotoUrl;


    /** 🎥 학생 손글씨 사진 파일 경로 */
    @Size(max = 255)
    @Column(length = 255)
    private String handLetterPhoto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Gender gender = Gender.M;
}


