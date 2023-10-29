package ru.nspk.performance.theatre.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "purchase")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String event;

    private Long reserveId;

    private double sum;

    private String seats;

    @Temporal(TemporalType.DATE)
    private Date eventDate;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
