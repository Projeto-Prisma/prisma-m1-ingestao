package br.ufrpe.prisma.m1.domain.dto;

import br.ufrpe.prisma.m1.domain.enums.Emissor;

public record ManifestanteDTO(
    Emissor tipo,
    boolean sigilo,
    String nome,
    String profissao,
    String tipoDoc,
    String numeroDoc,
    String sexo,
    String genero,
    String celular,
    String email
) {}