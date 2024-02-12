package com.slozic.dater.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "date_images")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class DateImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String imagePath;

    @NotNull
    private int imageSize;

    @NotNull
    @Column(name = "dateId")
    private UUID dateId;

    @NotNull
    @Builder.Default
    private boolean enabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dateId", nullable = false, insertable = false, updatable = false)
    private Date date;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        final DateImage dateImage = (DateImage) o;
        return Objects.equals(id, dateImage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
