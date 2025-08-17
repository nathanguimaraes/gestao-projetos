package com.planejao.gestao_projetos.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ReportDTO(Map<String, Long> quantidadePorStatus, Map<String, BigDecimal> totalOrcadoPorStatus,
                        double mediaDuracaoEncerrados, long totalMembrosUnicos) {}