package br.ufrpe.prisma.m1.domain.dto;
import br.ufrpe.prisma.m1.domain.enums.Status;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DenunciaResponseDTO(
    UUID id,
    String protocolo,
    String assunto,
    String descricao,
    String onde,
    String protocoloAnterior,
    Status status,
    LocalDateTime recebidaEm,
    LocalDateTime updatedAt,
    ManifestanteDTO manifestante,
    List<AnexoDTO> anexos,
    List<HistoricoStatusResponse> historico
) {
    public record HistoricoStatusResponse(
        Status status,
        LocalDateTime dataAlteracao
    ) {}
}
