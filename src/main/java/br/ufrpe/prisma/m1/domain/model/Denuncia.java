package br.ufrpe.prisma.m1.domain.model;

import br.ufrpe.prisma.m1.domain.enums.Status;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "denuncias")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String protocolo;

    private String assunto;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    private String onde;

    private String protocoloAnterior;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime recebidaEm;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id")
    private Manifestante manifestante;

    @OneToMany(mappedBy = "denuncia", cascade = CascadeType.ALL)
    private List<Anexo> anexos;

    @PrePersist
    protected void onCreate() {
        this.status = Status.RECEBIDA;
    }
}