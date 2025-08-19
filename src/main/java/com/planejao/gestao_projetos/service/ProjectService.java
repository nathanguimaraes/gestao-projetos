package com.planejao.gestao_projetos.service;

import com.planejao.gestao_projetos.domain.Project;
import com.planejao.gestao_projetos.domain.Member;
import com.planejao.gestao_projetos.domain.StatusProjeto;
import com.planejao.gestao_projetos.dto.*;
import com.planejao.gestao_projetos.exception.ProjectException;
import com.planejao.gestao_projetos.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository repository;
    private final MemberService memberService;

    private final List<StatusProjeto> sequence = Arrays.asList(
            StatusProjeto.EM_ANALISE, StatusProjeto.ANALISE_REALIZADA, StatusProjeto.ANALISE_APROVADA,
            StatusProjeto.INICIADO, StatusProjeto.PLANEJADO, StatusProjeto.EM_ANDAMENTO, StatusProjeto.ENCERRADO
    );

    public ProjectService(ProjectRepository repository, MemberService memberService) {
        this.repository = repository;
        this.memberService = memberService;
    }

    public Page<ProjectDTO> list(Pageable pageable, Optional<StatusProjeto> statusFilter) {
        if (statusFilter.isPresent()) {
            return repository.findByStatus(statusFilter.get(), pageable).map(this::toDTO);
        }
        return repository.findAll(pageable).map(this::toDTO);
    }

    public ProjectDTO getById(Long id) {
        return repository.findById(id).map(this::toDTO).orElseThrow(() -> new ProjectException("Projeto não encontrado"));
    }

    public ProjectDTO create(ProjectRequestDTO dto) {
        logger.info("Criando projeto: {}", dto.nome());
        logger.debug("Dados recebidos: {}", dto);

        if (dto.membros().isEmpty()) {
            logger.error("Tentativa de criar projeto sem membros");
            throw new ProjectException("Deve alocar pelo menos 1 membro");
        }

        logger.debug("Validando gerente com ID: {}", dto.gerenteId());
        Member gerente = convertAndValidateMember(dto.gerenteId(), false);
        logger.info("Gerente validado: {} (ID: {})", gerente.getNome(), gerente.getId());

        Project project = new Project();
        project.setNome(dto.nome());
        project.setDataInicio(dto.dataInicio());
        project.setPrevisaoTermino(dto.previsaoTermino());
        project.setOrcamentoTotal(dto.orcamentoTotal());
        project.setDescricao(dto.descricao());
        project.setGerente(gerente);
        project.setStatus(StatusProjeto.EM_ANALISE);
        project.setMembros(new ArrayList<>());

        logger.debug("Salvando projeto no banco...");
        Project saved = repository.save(project);
        logger.info("Projeto salvo com ID: {}", saved.getId());

        logger.debug("Adicionando {} membros ao projeto", dto.membros().size());
        for (Long memberId : dto.membros()) {
            logger.debug("Adicionando membro ID: {}", memberId);
            addMember(saved.getId(), memberId);
        }

        logger.info("Projeto criado com sucesso: {} (ID: {})", saved.getNome(), saved.getId());
        return toDTO(saved);
    }

    public ProjectDTO update(Long id, ProjectUpdateDTO dto) {
        logger.info("Atualizando projeto ID: {}", id);
        logger.debug("Dados recebidos: {}", dto);

        Project project = repository.findById(id).orElseThrow(() -> {
            logger.error("Projeto não encontrado com ID: {}", id);
            return new ProjectException("Projeto não encontrado");
        });

        logger.debug("Projeto encontrado: {} (ID: {})", project.getNome(), project.getId());

        if (dto.nome() != null) {
            logger.debug("Atualizando nome: {} -> {}", project.getNome(), dto.nome());
            project.setNome(dto.nome());
        }
        if (dto.dataInicio() != null) {
            logger.debug("Atualizando data início: {} -> {}", project.getDataInicio(), dto.dataInicio());
            project.setDataInicio(dto.dataInicio());
        }
        if (dto.previsaoTermino() != null) {
            logger.debug("Atualizando previsão término: {} -> {}", project.getPrevisaoTermino(), dto.previsaoTermino());
            project.setPrevisaoTermino(dto.previsaoTermino());
        }
        if (dto.dataRealTermino() != null) {
            logger.debug("Atualizando data real término: {} -> {}", project.getDataRealTermino(), dto.dataRealTermino());
            project.setDataRealTermino(dto.dataRealTermino());
        }
        if (dto.orcamentoTotal() != null) {
            logger.debug("Atualizando orçamento: {} -> {}", project.getOrcamentoTotal(), dto.orcamentoTotal());
            project.setOrcamentoTotal(dto.orcamentoTotal());
        }
        if (dto.descricao() != null) {
            logger.debug("Atualizando descrição");
            project.setDescricao(dto.descricao());
        }
        if (dto.gerenteId() != null) {
            logger.debug("Validando novo gerente com ID: {}", dto.gerenteId());
            Member gerente = convertAndValidateMember(dto.gerenteId(), false);
            logger.info("Novo gerente validado: {} (ID: {})", gerente.getNome(), gerente.getId());
            project.setGerente(gerente);
        }

        logger.debug("Salvando projeto atualizado...");
        Project saved = repository.save(project);
        logger.info("Projeto atualizado com sucesso: {} (ID: {})", saved.getNome(), saved.getId());

        return toDTO(saved);
    }

    public void delete(Long id) {
        Project project = repository.findById(id).orElseThrow(() -> new ProjectException("Projeto não encontrado"));
        if (List.of(StatusProjeto.INICIADO, StatusProjeto.EM_ANDAMENTO, StatusProjeto.ENCERRADO).contains(project.getStatus())) {
            throw new ProjectException("Não é permitido excluir projetos nos status iniciado, em andamento ou encerrado");
        }
        repository.delete(project);
    }

    public ProjectDTO changeStatus(Long id, StatusProjeto newStatus) {
        Project project = repository.findById(id).orElseThrow(() -> new ProjectException("Projeto não encontrado"));
        StatusProjeto current = project.getStatus();
        if (newStatus == StatusProjeto.CANCELADO) {
            project.setStatus(newStatus);
        } else {
            int currentIndex = sequence.indexOf(current);
            if (currentIndex == -1 || sequence.indexOf(newStatus) != currentIndex + 1) {
                throw new ProjectException("Transição de status inválida. Deve seguir a sequência ou cancelar.");
            }
            project.setStatus(newStatus);
            if (newStatus == StatusProjeto.ENCERRADO) {
                project.setDataRealTermino(LocalDate.now());
            }
        }
        return toDTO(repository.save(project));
    }

    public ProjectDTO addMember(Long id, Long memberId) {
        Project project = repository.findById(id).orElseThrow(() -> new ProjectException("Projeto não encontrado"));
        convertAndValidateMember(memberId, true);
        if (project.getMembros().contains(memberId)) {
            throw new ProjectException("Membro já alocado no projeto");
        }
        if (project.getMembros().size() >= 10) {
            throw new ProjectException("Projeto já tem o máximo de 10 membros");
        }
        long activeProjects = repository.findAll().stream()
                .filter(p -> p.getStatus().isActive() && p.getMembros().contains(memberId))
                .count();
        if (activeProjects >= 3) {
            throw new ProjectException("Membro já alocado em 3 projetos ativos");
        }
        project.getMembros().add(memberId);
        return toDTO(repository.save(project));
    }

    public ProjectDTO removeMember(Long id, Long memberId) {
        Project project = repository.findById(id).orElseThrow(() -> new ProjectException("Projeto não encontrado"));
        if (!project.getMembros().remove(memberId)) {
            throw new ProjectException("Membro não encontrado no projeto");
        }
        if (project.getMembros().isEmpty()) {
            throw new ProjectException("Projeto deve ter pelo menos 1 membro");
        }
        return toDTO(repository.save(project));
    }

    public ReportDTO getReport() {
        List<Project> all = repository.findAll();
        Map<String, Long> qtdPorStatus = all.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(), Collectors.counting()));
        Map<String, BigDecimal> totalOrcado = all.stream()
                .collect(Collectors.groupingBy(p -> p.getStatus().name(),
                        Collectors.reducing(BigDecimal.ZERO, Project::getOrcamentoTotal, BigDecimal::add)));
        List<Project> encerrados = all.stream()
                .filter(p -> p.getStatus() == StatusProjeto.ENCERRADO && p.getDataRealTermino() != null)
                .toList();
        double mediaDuracao = encerrados.isEmpty() ? 0 : encerrados.stream()
                .mapToLong(p -> ChronoUnit.DAYS.between(p.getDataInicio(), p.getDataRealTermino()))
                .average().orElse(0);
        Set<Long> unicos = all.stream().flatMap(p -> p.getMembros().stream()).collect(Collectors.toSet());
        return new ReportDTO(qtdPorStatus, totalOrcado, mediaDuracao, unicos.size());
    }

    private Member convertAndValidateMember(Long memberId, boolean isTeamMember) {
        logger.debug("Validando membro com ID: {} (isTeamMember: {})", memberId, isTeamMember);

        MemberDTO memberDTO = memberService.getMember(memberId);
        if (memberDTO == null) {
            logger.error("Membro não encontrado com ID: {}", memberId);
            throw new ProjectException("Membro não encontrado");
        }

        logger.debug("Membro encontrado: {} - {}", memberDTO.nome(), memberDTO.cargo());

        if (isTeamMember) {
            if (!"funcionario".equals(memberDTO.cargo())) {
                logger.error("Membro {} tem cargo '{}' mas deveria ser 'funcionario'", memberDTO.nome(), memberDTO.cargo());
                throw new ProjectException("Apenas membros com atribuição 'funcionário' podem ser associados");
            }
        } else {
            if (!"gerente".equals(memberDTO.cargo())) {
                logger.error("Membro {} tem cargo '{}' mas deveria ser 'gerente'", memberDTO.nome(), memberDTO.cargo());
                throw new ProjectException("Apenas membros com atribuição 'gerente' podem ser gerentes responsáveis");
            }
        }

        Member member = new Member();
        member.setId(memberDTO.id());
        member.setNome(memberDTO.nome());
        member.setCargo(memberDTO.cargo());

        logger.debug("Membro validado com sucesso: {} - {}", member.getNome(), member.getCargo());
        return member;
    }

    private ProjectDTO toDTO(Project p) {
        MemberDTO gerenteDTO = (p.getGerente() != null)
                ? new MemberDTO(p.getGerente().getId(), p.getGerente().getNome(), p.getGerente().getCargo())
                : null;
        return new ProjectDTO(p.getId(), p.getNome(), p.getDataInicio(), p.getPrevisaoTermino(), p.getDataRealTermino(),
                p.getOrcamentoTotal(), p.getDescricao(), gerenteDTO, p.getStatus(), p.getRisco(), p.getMembros());
    }
}