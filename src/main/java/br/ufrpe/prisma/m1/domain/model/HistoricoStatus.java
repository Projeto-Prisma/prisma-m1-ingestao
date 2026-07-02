package br.ufrpe.prisma.m1.domain.model;
import br.ufrpe.prisma.m1.domain.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;        
import java.time.LocalDateTime; 

@Entity
@Table(name = "historico_status")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HistoricoStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    private LocalDateTime dataAlteracao;

    @ManyToOne
    @JoinColumn(name = "denuncia_id")
    private Denuncia denuncia;
}