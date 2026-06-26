package br.ufrpe.prisma.m1.domain.model;
import br.ufrpe.prisma.m1.domain.enums.Emissor; 
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "manifestantes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Manifestante {
    @Id
    private UUID denunciaId; // PK e FK ligada à Denúncia

    @Enumerated(EnumType.STRING)
    private Emissor tipo;
    private boolean sigilo;
    private String nome;
    private String profissao;
    private String tipoDoc;
    private String numeroDoc;
    private String sexo;
    private String genero;
    private String celular;
    private String email;
}
