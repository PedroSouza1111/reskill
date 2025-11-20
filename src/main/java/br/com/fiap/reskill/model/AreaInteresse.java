package br.com.fiap.reskill.model;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TB_RESKILL_AREA_INTERESSE")
public class AreaInteresse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome; // Ex: "TI", "Marketing", "Saúde"

    // --- ADICIONAR ESTES CONSTRUTORES ---
    public AreaInteresse() {
    }

    public AreaInteresse(String nome) {
        this.nome = nome;
    }

    // Getters e Setters (que você já tinha)
    // ...
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

    // --- ADICIONAR ESTES MÉTODOS (equals e hashCode) ---
    // Isto é crucial para o th:checked funcionar corretamente
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AreaInteresse that = (AreaInteresse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}