package ru.nspk.performance.theatre.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "purchase")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String event;

    private Long reserveId;

    private double sum;

    @Type(type = "jsonb")
    @Column(name = "seats", columnDefinition = "jsonb")
    private String seats;

    @Temporal(TemporalType.DATE)
    private Date eventDate;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
