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
        name = "classroom",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_classroom_class_num", columnNames = {"classNum"})
        }
)
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int classNum;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String homeroomTeacher;

    /** 🎥 학생 단체사진 파일 경로 */
    @Column(length = 255)
    private String classGroupPhoto;

    /** 🎥 교사  영상 파일 경로 */
    @Size(max = 255)
    @Column(length = 255)
    private String classVideoLetter;

    /** 🎥 학생들 영상 파일 경로 */
    @Size(max = 255)
    @Column(length = 255)
    private String studentVideoLetter;

}


