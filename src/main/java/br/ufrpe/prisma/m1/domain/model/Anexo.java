package br.ufrpe.prisma.m1.domain.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "anexos")
@Getter @Setter
public class Anexo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String arquivoUrl;
    private String mimeType;

    @ManyToOne
    @JoinColumn(name = "denuncia_id")
    private Denuncia denuncia;
}
