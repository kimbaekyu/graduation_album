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

    @Size(max = 255)
    private String classGroupPhotoUrl;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] classGroupPhoto;

    @Size(max = 255)
    private String classVideoLetterUrl;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] classVideoLetter;

    @Size(max = 255)
    private String studentVideoLetterUrl;

    @Lob
    @Column(columnDefinition = "BLOB")
    private byte[] studentVideoLetter;

}


