package br.senac.tsi.gamecatalog;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Jogo extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "O título não pode ser vazio")
    @Size(min = 1, max = 200)
    public String titulo;

    @NotBlank(message = "A sinopse é obrigatória")
    @Size(max = 2000)
    public String sinopse;

    @Min(value = 1970, message = "Ano de lançamento inválido.")
    public int anoLancamento;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "10.0")
    public double notaCritica;

    // Relacionamento Muitos-para-Um
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "desenvolvedora_id")
    public Desenvolvedora desenvolvedora; // Simplificado

    // Relacionamento Muitos-para-Muitos (lado dono)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "jogo_genero",
            joinColumns = @JoinColumn(name = "jogo_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id")
    )

    // ... dentro da classe Jogo
    @Enumerated(EnumType.STRING) // Salva o nome do enum ("LIVRE") no banco, em vez do número (0)
    public ClassificacaoIndicativa classificacao;

    public Set<Genero> generos = new HashSet<>(); // Simplificado
}