package com.planejao.gestao_projetos.service;

import com.planejao.gestao_projetos.domain.Project;
import com.planejao.gestao_projetos.domain.Member;
import com.planejao.gestao_projetos.domain.StatusProjeto;
import com.planejao.gestao_projetos.dto.*;
import com.planejao.gestao_projetos.exception.ProjectException;
import com.planejao.gestao_projetos.repository.ProjectRepository;
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
        if (dto.membros().isEmpty()) {
            throw new ProjectException("Deve alocar pelo menos 1 membro");
        }
        Member gerente = convertAndValidateMember(dto.gerenteId(), false);
        Project project = new Project();
        project.setNome(dto.nome());
        project.setDataInicio(dto.dataInicio());
        project.setPrevisaoTermino(dto.previsaoTermino());
        project.setOrcamentoTotal(dto.orcamentoTotal());
        project.setDescricao(dto.descricao());
        project.setGerente(gerente); // Usando setGerente com Member
        project.setStatus(StatusProjeto.EM_ANALISE);
        project.setMembros(new ArrayList<>());
        Project saved = repository.save(project);
        for (Long memberId : dto.membros()) {
            addMember(saved.getId(), memberId);
        }
        return toDTO(saved);
    }

    public ProjectDTO update(Long id, ProjectUpdateDTO dto) {
        Project project = repository.findById(id).orElseThrow(() -> new ProjectException("Projeto não encontrado"));
        if (dto.nome() != null) project.setNome(dto.nome());
        if (dto.dataInicio() != null) project.setDataInicio(dto.dataInicio());
        if (dto.previsaoTermino() != null) project.setPrevisaoTermino(dto.previsaoTermino());
        if (dto.dataRealTermino() != null) project.setDataRealTermino(dto.dataRealTermino());
        if (dto.orcamentoTotal() != null) project.setOrcamentoTotal(dto.orcamentoTotal());
        if (dto.descricao() != null) project.setDescricao(dto.descricao());
        if (dto.gerenteId() != null) {
            Member gerente = convertAndValidateMember(dto.gerenteId(), false);
            project.setGerente(gerente); // Usando setGerente com Member
        }
        return toDTO(repository.save(project));
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
        MemberDTO memberDTO = memberService.getMember(memberId);
        if (memberDTO == null) {
            throw new ProjectException("Membro não encontrado");
        }
        if (isTeamMember) {
            if (!"funcionario".equals(memberDTO.cargo())) {
                throw new ProjectException("Apenas membros com atribuição 'funcionário' podem ser associados");
            }
        } else {
            if (!"gerente".equals(memberDTO.cargo())) {
                throw new ProjectException("Apenas membros com atribuição 'gerente' podem ser gerentes responsáveis");
            }
        }
        Member member = new Member();
        member.setId(memberDTO.id());
        member.setNome(memberDTO.nome());
        member.setCargo(memberDTO.cargo());
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