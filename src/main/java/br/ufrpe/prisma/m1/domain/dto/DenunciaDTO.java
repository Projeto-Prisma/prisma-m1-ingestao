package br.ufrpe.prisma.m1.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DenunciaDTO(
    @NotBlank(message = "O assunto é obrigatório") 
    String assunto,
    
    @NotBlank(message = "A descrição é obrigatória") 
    String descricao,
    
    String onde,
    
    String protocoloAnterior,

    @NotNull(message = "Os dados do manifestante são obrigatórios")
    @Valid 
    ManifestanteDTO manifestante,
    
    List<@Valid AnexoDTO> anexos
) {}