package br.com.fiap.reskill.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "TB_RESKILL_USUARIO")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    private String senha;

    @ManyToMany
    @JoinTable(
        name = "TB_USUARIO_AREA_INTERESSE",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "area_interesse_id")
    )
    private Set<AreaInteresse> areasInteresse = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "TB_USUARIO_CURSO_CONCLUIDO",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private Set<Curso> cursosConcluidos = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "TB_USUARIO_CURSO_RECOMENDADO",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "curso_id")
    )
    private Set<Curso> cursosRecomendados = new HashSet<>();

    // Construtor padrão (obrigatório pelo JPA)
    public Usuario() {}

    // Getters e Setters (omitidos para brevidade, mas você deve gerá-los)
    // ...

    // --- Métodos do UserDetails (Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por enquanto, todo usuário é "ROLE_USER"
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email; // Usaremos email como username
    }

    // Métodos para controle de conta (pode deixar true por padrão)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Getters e Setters para os campos da entidade ---
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Set<AreaInteresse> getAreasInteresse() {
        return areasInteresse;
    }

    public void setAreasInteresse(Set<AreaInteresse> areasInteresse) {
        this.areasInteresse = areasInteresse;
    }

    public Set<Curso> getCursosConcluidos() {
        return cursosConcluidos;
    }

    public void setCursosConcluidos(Set<Curso> cursosConcluidos) {
        this.cursosConcluidos = cursosConcluidos;
    }

    public Set<Curso> getCursosRecomendados() {
        return cursosRecomendados;
    }

    public void setCursosRecomendados(Set<Curso> cursosRecomendados) {
        this.cursosRecomendados = cursosRecomendados;
    }
}