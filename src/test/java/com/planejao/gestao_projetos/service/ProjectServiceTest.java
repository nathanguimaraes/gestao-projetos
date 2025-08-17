package com.planejao.gestao_projetos.service;

import com.planejao.gestao_projetos.domain.Member;
import com.planejao.gestao_projetos.domain.Project;
import com.planejao.gestao_projetos.domain.StatusProjeto;
import com.planejao.gestao_projetos.dto.*;
import com.planejao.gestao_projetos.exception.ProjectException;
import com.planejao.gestao_projetos.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @MockBean
    private ProjectRepository projectRepository;

    @MockBean
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        // Resetar mocks antes de cada teste
        reset(projectRepository, memberService);

        // Mock dos membros
        when(memberService.getMember(1L)).thenReturn(new MemberDTO(1L, "Gerente", "gerente"));
        when(memberService.getMember(2L)).thenReturn(new MemberDTO(2L, "Func", "funcionario"));
        when(memberService.getMember(3L)).thenReturn(new MemberDTO(3L, "Funcionario 2", "funcionario"));
    }

    @Test
    void createProjectSuccess() {
        ProjectRequestDTO dto = new ProjectRequestDTO(
                "Proj",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000),
                "Desc",
                1L,
                List.of(2L)
        );
        Project project = new Project();
        project.setId(1L);
        project.setNome(dto.nome());
        project.setDataInicio(dto.dataInicio());
        project.setPrevisaoTermino(dto.previsaoTermino());
        project.setOrcamentoTotal(dto.orcamentoTotal());
        project.setDescricao(dto.descricao());
        project.setStatus(StatusProjeto.EM_ANALISE);
        project.setMembros(new ArrayList<>());
        project.setGerente(convertAndValidateMember(dto.gerenteId(), false));

        // Mock para save e findById
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectDTO result = projectService.create(dto);

        assertNotNull(result);
        assertEquals("Proj", result.nome());
        assertEquals(StatusProjeto.EM_ANALISE, result.status());
        assertEquals("Baixo", result.risco());
        assertTrue(result.membros().contains(2L));
        verify(projectRepository, times(2)).save(any(Project.class)); // Ajustado para 2 chamadas
        verify(projectRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void createProjectFailsNoMembers() {
        ProjectRequestDTO dto = new ProjectRequestDTO(
                "Proj",
                LocalDate.now(),
                LocalDate.now().plusMonths(1),
                BigDecimal.valueOf(50000),
                "Desc",
                1L,
                List.of()
        );

        assertThrows(ProjectException.class, () -> projectService.create(dto));
    }

    @Test
    void getByIdSuccess() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectDTO result = projectService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    void updateProjectSuccess() {
        Project project = new Project();
        project.setId(1L);
        project.setNome("Old Name");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(project);

        ProjectDTO result = projectService.update(1L, new ProjectUpdateDTO("New Name", null, null, null, null, null, null));

        assertEquals("New Name", result.nome());
    }

    @Test
    void createProjectMediumRisk() {
        ProjectRequestDTO dto = new ProjectRequestDTO(
                "Proj",
                LocalDate.now(),
                LocalDate.now().plusMonths(4),
                BigDecimal.valueOf(200000),
                "Desc",
                1L,
                List.of(2L)
        );
        Project project = new Project();
        project.setId(1L);
        project.setNome(dto.nome());
        project.setDataInicio(dto.dataInicio());
        project.setPrevisaoTermino(dto.previsaoTermino());
        project.setOrcamentoTotal(dto.orcamentoTotal());
        project.setDescricao(dto.descricao());
        project.setStatus(StatusProjeto.EM_ANALISE);
        project.setMembros(new ArrayList<>());
        project.setGerente(convertAndValidateMember(dto.gerenteId(), false));

        // Mock para save e findById
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ProjectDTO result = projectService.create(dto);

        assertEquals("Medio", result.risco());
        assertTrue(result.membros().contains(2L));
        verify(projectRepository, times(2)).save(any(Project.class)); // Ajustado para 2 chamadas
        verify(projectRepository, atLeastOnce()).findById(1L);
    }

    @Test
    void changeStatusSequentialSuccess() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(project);

        ProjectDTO result = projectService.changeStatus(1L, StatusProjeto.ANALISE_REALIZADA);

        assertNotNull(result);
        assertEquals(StatusProjeto.ANALISE_REALIZADA, result.status());
    }

    @Test
    void changeStatusSkipFails() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ProjectException.class, () -> projectService.changeStatus(1L, StatusProjeto.INICIADO));
    }

    @Test
    void changeStatusToCanceladoSuccess() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any())).thenReturn(project);

        ProjectDTO result = projectService.changeStatus(1L, StatusProjeto.CANCELADO);

        assertNotNull(result);
        assertEquals(StatusProjeto.CANCELADO, result.status());
    }

    @Test
    void addMemberSuccess() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANALISE);
        project.setMembros(new ArrayList<>());
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.findAll()).thenReturn(Collections.singletonList(project));
        when(projectRepository.save(any())).thenReturn(project);

        ProjectDTO result = projectService.addMember(1L, 2L);

        assertTrue(project.getMembros().contains(2L));
        assertNotNull(result);
    }

    @Test
    void addMemberFailsNotFuncionario() {
        Project project = new Project();
        project.setId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(memberService.getMember(2L)).thenReturn(new MemberDTO(2L, "Gerente", "gerente"));

        assertThrows(ProjectException.class, () -> projectService.addMember(1L, 2L));
    }

    @Test
    void addMemberFailsMax10() {
        Project project = new Project();
        project.setId(1L);
        project.setMembros(new ArrayList<>(Collections.nCopies(10, 3L)));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(memberService.getMember(2L)).thenReturn(new MemberDTO(2L, "Func", "funcionario"));

        assertThrows(ProjectException.class, () -> projectService.addMember(1L, 2L));
    }

    @Test
    void addMemberFailsMax3Active() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANALISE);
        project.setMembros(new ArrayList<>());
        Project p2 = new Project(); p2.setMembros(List.of(2L)); p2.setStatus(StatusProjeto.EM_ANALISE);
        Project p3 = new Project(); p3.setMembros(List.of(2L)); p3.setStatus(StatusProjeto.EM_ANALISE);
        Project p4 = new Project(); p4.setMembros(List.of(2L)); p4.setStatus(StatusProjeto.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.findAll()).thenReturn(List.of(project, p2, p3, p4));
        when(memberService.getMember(2L)).thenReturn(new MemberDTO(2L, "Func", "funcionario"));

        assertThrows(ProjectException.class, () -> projectService.addMember(1L, 2L));
    }

    @Test
    void removeMemberFailsMin1() {
        Project project = new Project();
        project.setId(1L);
        project.setMembros(new ArrayList<>(List.of(2L)));
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ProjectException.class, () -> projectService.removeMember(1L, 2L));
    }

    @Test
    void deleteSuccess() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANALISE);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertDoesNotThrow(() -> projectService.delete(1L));
        verify(projectRepository).delete(project);
    }

    @Test
    void deleteFailsProtectedStatus() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(StatusProjeto.EM_ANDAMENTO);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        assertThrows(ProjectException.class, () -> projectService.delete(1L));
    }

    @Test
    void getReport() {
        Project p1 = new Project();
        p1.setStatus(StatusProjeto.ENCERRADO);
        p1.setOrcamentoTotal(BigDecimal.valueOf(100000));
        p1.setDataInicio(LocalDate.now().minusDays(10));
        p1.setDataRealTermino(LocalDate.now());
        p1.setMembros(List.of(1L, 2L));

        Project p2 = new Project();
        p2.setStatus(StatusProjeto.EM_ANALISE);
        p2.setOrcamentoTotal(BigDecimal.valueOf(200000));
        p2.setMembros(List.of(2L, 3L));

        when(projectRepository.findAll()).thenReturn(List.of(p1, p2));

        ReportDTO report = projectService.getReport();

        assertEquals(1, report.quantidadePorStatus().get("ENCERRADO"));
        assertEquals(BigDecimal.valueOf(100000), report.totalOrcadoPorStatus().get("ENCERRADO"));
        assertEquals(10.0, report.mediaDuracaoEncerrados());
        assertEquals(3, report.totalMembrosUnicos());
    }

    // Método auxiliar para simular convertAndValidateMember do ProjectService
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
}