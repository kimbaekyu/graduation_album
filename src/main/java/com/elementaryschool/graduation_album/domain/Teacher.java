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
        name = "teacher",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_teacher_classroom", columnNames = {"classroom_id"})
        }
)
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String name;

    @Size(max = 200)
    @Column(length = 200)
    private String classMotto;

    @Size(max = 500)
    @Column(length = 500)
    private String talkTo;

    @Size(max = 500)
    @Column(length = 500)
    private String promise;

    /** 📸 교사 사진 파일 경로 */
    @Size(max = 255)
    @Column(length = 255)
    private String teacherPhoto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private Gender gender = Gender.M;
}


