package com.slozic.dater.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "dates")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class Date {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    private Double latitude;

    private Double longitude;

    @NotNull
    private boolean enabled;

    @NotNull
    private OffsetDateTime scheduledTime;

    @NotNull
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @NotNull
    @Column(name = "createdBy")
    private UUID createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createdBy", nullable = false, insertable = false, updatable = false)
    private User user;

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        final Date date = (Date) o;
        return Objects.equals(id, date.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Date{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
